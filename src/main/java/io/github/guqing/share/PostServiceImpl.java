package io.github.guqing.share;

import io.github.guqing.share.model.CategoryVo;
import io.github.guqing.share.model.ContentVo;
import io.github.guqing.share.model.ContributorVo;
import io.github.guqing.share.model.ListedPostVo;
import io.github.guqing.share.model.PostVo;
import io.github.guqing.share.model.StatsVo;
import io.github.guqing.share.model.TagVo;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentWrapper;
import run.halo.app.content.PostContentService;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.GVK;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.ReactivePostContentHandler;

@Component
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    public static final String GHOST_USER_NAME = "ghost";

    private final ReactiveExtensionClient client;

    private final PostContentService postContentService;

    private final ExtensionGetter extensionGetter;

    static String meterNameOf(String group, String plural, String name) {
        if (StringUtils.isBlank(group)) {
            return String.join("/", plural, name);
        }
        return String.join(".", plural, group) + "/" + name;
    }

    private Mono<ListedPostVo> convertToListedVo(@NonNull Post post) {
        Assert.notNull(post, "Post must not be null");
        ListedPostVo postVo = ListedPostVo.from(post);
        postVo.setCategories(List.of());
        postVo.setTags(List.of());
        postVo.setContributors(List.of());
        var steps = new ArrayList<Publisher<?>>();
        steps.add(populateStats(postVo).doOnNext(postVo::setStats));
        steps.add(getContributor(post.getSpec().getOwner())
            .doOnNext(postVo::setOwner));
        steps.add(findTags(postVo.getSpec().getTags())
            .collectList()
            .doOnNext(postVo::setTags));

        steps.add(findCategories(post.getSpec().getCategories())
            .collectList()
            .doOnNext(postVo::setCategories));

        var contributorNames = post.getStatus().getContributors();
        if (CollectionUtils.isEmpty(contributorNames)) {
            var contributorsMono = Flux.fromIterable(contributorNames)
                .flatMap(this::getContributor)
                .collectList()
                .doOnNext(postVo::setContributors);
            steps.add(contributorsMono);
        }
        return Mono.when(steps).thenReturn(postVo);
    }

    private Flux<CategoryVo> findCategories(List<String> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Flux.empty();
        }
        return Flux.fromIterable(categories)
            .flatMap(name -> client.fetch(Category.class, name))
            .map(CategoryVo::from);
    }

    private Flux<TagVo> findTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return Flux.empty();
        }
        return Flux.fromIterable(tags)
            .flatMap(tagName -> client.fetch(Tag.class, tagName))
            .map(TagVo::from);
    }

    private Mono<ContributorVo> getContributor(String name) {
        return client.fetch(User.class, name)
            .switchIfEmpty(Mono.defer(() -> client.get(User.class, GHOST_USER_NAME)))
            .map(ContributorVo::from);
    }

    @Override
    public Mono<PostVo> convertToVo(Post post, String snapshotName) {
        final String postName = post.getMetadata().getName();
        return convertToListedVo(post)
            .map(PostVo::from)
            .flatMap(postVo -> getContent(postName, snapshotName)
                .doOnNext(postVo::setContent)
                .thenReturn(postVo)
            );
    }

    Mono<ContentVo> getContent(String postName, String snapshotName) {
        return client.get(Post.class, postName)
            .flatMap(post -> postContentService.getSpecifiedContent(post.getMetadata().getName(),
                    snapshotName)
                .flatMap(wrapper -> extendPostContent(post, wrapper))
            );
    }

    protected Mono<ContentVo> extendPostContent(Post post,
        ContentWrapper wrapper) {
        Assert.notNull(post, "Post name must not be null");
        Assert.notNull(wrapper, "Post content must not be null");
        return extensionGetter.getEnabledExtensions(ReactivePostContentHandler.class)
            .reduce(Mono.fromSupplier(() -> ReactivePostContentHandler.PostContentContext.builder()
                    .post(post)
                    .content(wrapper.getContent())
                    .raw(wrapper.getRaw())
                    .rawType(wrapper.getRawType())
                    .build()
                ),
                (contentMono, handler) -> contentMono.flatMap(handler::handle)
            )
            .flatMap(Function.identity())
            .map(postContent -> ContentVo.builder()
                .content(postContent.getContent())
                .raw(postContent.getRaw())
                .build()
            );
    }

    private <T extends ListedPostVo> Mono<StatsVo> populateStats(T postVo) {
        GVK annotation = Post.class.getAnnotation(GVK.class);
        return client.fetch(Counter.class, meterNameOf(annotation.group(), annotation.plural(),
                postVo.getMetadata().getName())
            )
            .map(counter -> StatsVo.builder()
                .visit(counter.getVisit())
                .upvote(counter.getUpvote())
                .comment(counter.getApprovedComment())
                .build()
            )
            .defaultIfEmpty(StatsVo.empty());
    }
}
