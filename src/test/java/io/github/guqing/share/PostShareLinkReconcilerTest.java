package io.github.guqing.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.ExternalLinkProcessor;

/**
 * Tests for {@link PostShareLinkReconciler}.
 *
 * @author guqing
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostShareLinkReconcilerTest {
    @Mock
    private ExtensionClient client;

    @Mock
    private ExternalLinkProcessor externalLinkProcessor;

    @InjectMocks
    private PostShareLinkReconciler postShareLinkReconciler;

    @Test
    void reconcileForExpiration() {
        var now = Instant.now();
        var link = new PostShareLink();
        link.setMetadata(new Metadata());
        link.setSpec(new PostShareLink.Spec()
            .setPostName("fake-post")
            .setExpirationAt(now.minusSeconds(30))
        );
        when(client.fetch(PostShareLink.class, "test"))
            .thenReturn(Optional.of(link));
        var result = postShareLinkReconciler.reconcile(new Reconciler.Request("test"));
        assertThat(result.reEnqueue()).isFalse();
        assertThat(result.retryAfter()).isNull();
    }

    @Test
    void reconcileForNotExpired() {
        var now = Instant.now();
        var link = new PostShareLink();
        link.setMetadata(new Metadata());
        link.setSpec(new PostShareLink.Spec()
            .setPostName("fake-post")
            .setExpirationAt(now.plusSeconds(30))
        );
        when(client.fetch(PostShareLink.class, "test"))
            .thenReturn(Optional.of(link));
        var result = postShareLinkReconciler.reconcile(new Reconciler.Request("test"));
        assertThat(result.reEnqueue()).isTrue();
        // > 0 and <= 30
        assertThat(result.retryAfter()).isLessThanOrEqualTo(Duration.ofSeconds(30));
        assertThat(result.retryAfter()).isPositive();
    }
}
