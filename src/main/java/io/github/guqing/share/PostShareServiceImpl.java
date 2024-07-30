package io.github.guqing.share;

import io.github.guqing.share.PostShareLink.ShareType;
import io.github.guqing.share.model.PostVo;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ReactiveExtensionClient;

@Component
@RequiredArgsConstructor
public class PostShareServiceImpl implements PostShareService {
    private final ReactiveExtensionClient client;
    private final PostService postService;

    @Override
    public Mono<PostVo> getByName(String name) {
        return client.get(PostShareLink.class, name)
            .flatMap(postShare -> {
                var postName = postShare.getSpec().getPostName();
                var type = postShare.getSpec().getShareType();
                return client.fetch(Post.class, postName)
                    .switchIfEmpty(Mono.error(new NotFoundException("Post not found")))
                    .flatMap(post -> {
                        var snapshotName =
                            ShareType.LATEST.equals(type) ? post.getSpec().getReleaseSnapshot()
                                : post.getSpec().getHeadSnapshot();
                        return convertToPostVo(post, snapshotName);
                    });
            });
    }

    private Mono<PostVo> convertToPostVo(Post post, String snapshotName) {
        return postService.convertToVo(post, snapshotName)
            .doOnNext(postVo -> {
                // fake some attributes only for preview when they are not published
                Post.PostSpec spec = postVo.getSpec();
                if (spec.getPublishTime() == null) {
                    spec.setPublishTime(Instant.now());
                }
                if (spec.getPublish() == null) {
                    spec.setPublish(false);
                }
                Post.PostStatus status = postVo.getStatus();
                if (status == null) {
                    status = new Post.PostStatus();
                    postVo.setStatus(status);
                }
                if (status.getLastModifyTime() == null) {
                    status.setLastModifyTime(Instant.now());
                }
            });
    }
}
