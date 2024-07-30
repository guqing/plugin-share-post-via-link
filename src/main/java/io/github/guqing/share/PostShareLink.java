package io.github.guqing.share;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.lang.NonNull;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;

/**
 * Link expiration time is set by {@link Metadata#getDeletionTimestamp}, which is filled with the
 * time when the link is created.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "postshare.guqing.io", version = "v1alpha1", kind = "PostShareLink",
    plural = "postsharelinks", singular = "postsharelink")
public class PostShareLink extends AbstractExtension {
    public static final String SYNC_ON_STARTUP_INDEX = "sync-on-startup";

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Schema(requiredMode = NOT_REQUIRED)
    @Getter(onMethod_ = @NonNull)
    private Status status = new Status();

    public void setStatus(Status status) {
        this.status = (status == null ? new Status() : status);
    }

    public enum ShareType {
        PUBLISHED,
        LATEST
    }

    @Data
    @Schema(name = "PostShareLinkSpec")
    public static class Spec {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String postName;

        private String owner;

        private ShareType shareType;
    }

    @Data
    @Schema(name = "PostShareLinkStatus")
    public static class Status {
        private String permalink;
        private String title;
        private Long observedVersion;
    }
}
