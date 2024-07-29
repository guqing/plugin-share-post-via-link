package io.github.guqing.share;

import io.github.guqing.share.model.PostVo;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;

public interface PostService {
    Mono<PostVo> convertToVo(Post post, String snapshotName);
}
