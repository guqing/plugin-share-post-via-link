package io.github.guqing.share;

import io.github.guqing.share.model.PostVo;
import reactor.core.publisher.Mono;

public interface PostShareService {
    Mono<PostVo> getByName(String name);
}
