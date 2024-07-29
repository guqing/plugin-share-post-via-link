package io.github.guqing.share;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.ExternalLinkProcessor;

@Component
@RequiredArgsConstructor
public class PostShareLinkReconciler implements Reconciler<Reconciler.Request> {
    private final ExtensionClient client;
    private final ExternalLinkProcessor externalLinkProcessor;

    @Override
    public Result reconcile(Request request) {
        client.fetch(PostShareLink.class, request.name())
            .ifPresent(postShareLink -> {
                postShareLink.getStatus()
                    .setPermalink(buildPermalink(postShareLink.getMetadata().getName()));
                client.fetch(Post.class, postShareLink.getSpec().getPostName())
                    .ifPresent(post -> postShareLink.getStatus()
                        .setTitle(post.getSpec().getTitle())
                    );
                client.update(postShareLink);
            });
        return Result.doNotRetry();
    }

    private String buildPermalink(String shareName) {
        return externalLinkProcessor.processLink("/preview/shared-posts/" + shareName);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new PostShareLink())
            .syncAllListOptions(ListOptions.builder()
                .fieldQuery(equal(PostShareLink.SYNC_ON_STARTUP_INDEX, BooleanUtils.TRUE))
                .build()
            )
            .build();
    }
}
