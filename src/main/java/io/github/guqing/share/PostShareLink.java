package io.github.guqing.share;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.lang.NonNull;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

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
        private String postName;
        private Instant expirationAt;
        private String owner;
        private ShareType shareType;
    }

    @Data
    @Schema(name = "PostShareLinkStatus")
    public static class Status {
        private String permalink;
        private Long observedVersion;
    }
}
