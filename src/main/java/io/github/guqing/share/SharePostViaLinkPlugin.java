package io.github.guqing.share;

import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

import io.github.guqing.share.model.PostVo;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;
import run.halo.app.theme.TemplateNameResolver;
import run.halo.app.theme.dialect.CommentWidget;
import run.halo.app.theme.router.ModelConst;

@Component
public class SharePostViaLinkPlugin extends BasePlugin {
    static final Scheme POST_SCHEME = Scheme.buildFromType(Post.class);

    private final SchemeManager schemeManager;
    private final PostShareService postShareService;
    private final TemplateNameResolver templateNameResolver;

    public SharePostViaLinkPlugin(PluginContext pluginContext, SchemeManager schemeManager,
        PostShareService postShareService,
        TemplateNameResolver templateNameResolver) {
        super(pluginContext);
        this.schemeManager = schemeManager;
        this.postShareService = postShareService;
        this.templateNameResolver = templateNameResolver;
    }

    static Map<String, Object> postModel(PostVo postVo) {
        Map<String, Object> model = new HashMap<>();
        model.put("name", postVo.getMetadata().getName());
        model.put(ModelConst.TEMPLATE_ID, "post");
        model.put("groupVersionKind", POST_SCHEME.groupVersionKind());
        model.put("plural", POST_SCHEME.plural());
        model.put("post", postVo);
        model.put(CommentWidget.ENABLE_COMMENT_ATTRIBUTE, postVo.getSpec().getAllowComment());
        return model;
    }

    @Override
    public void start() {
        schemeManager.register(PostShareLink.class, indexSpecs -> {
            indexSpecs.add(new IndexSpec()
                .setName(PostShareLink.SYNC_ON_STARTUP_INDEX)
                .setIndexFunc(simpleAttribute(PostShareLink.class, link -> {
                    var version = link.getMetadata().getVersion();
                    var observedVersion = link.getStatus().getObservedVersion();
                    if (observedVersion == null || observedVersion < version) {
                        return BooleanUtils.TRUE;
                    }
                    // do not care about the false case so return null to avoid indexing
                    return null;
                })));
        });
    }

    @Override
    public void stop() {
        schemeManager.unregister(Scheme.buildFromType(PostShareLink.class));
    }

    @Bean
    RouterFunction<ServerResponse> sharePostViaLinkRouterFunction() {
        return RouterFunctions.route()
            .GET("/preview/shared-posts/{name}", request -> {
                var name = request.pathVariable("name");
                return postShareService.getByName(name)
                    .flatMap(postVo -> {
                        String template = postVo.getSpec().getTemplate();
                        Map<String, Object> model = postModel(postVo);
                        return templateNameResolver.resolveTemplateNameOrDefault(request.exchange(),
                                template, "post")
                            .flatMap(
                                templateName -> ServerResponse.ok().render(templateName, model));
                    });
            })
            .build();
    }
}
