package com.theuran.app;

import mchorse.bbs.BBS;
import mchorse.bbs.core.IComponent;
import mchorse.bbs.graphics.Framebuffer;
import mchorse.bbs.graphics.RenderingContext;
import mchorse.bbs.graphics.text.FontRenderer;
import mchorse.bbs.graphics.texture.Texture;
import mchorse.bbs.graphics.ubo.ProjectionViewUBO;
import mchorse.bbs.resources.Link;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

public class AppRenderer implements IComponent {
    public AppEngine engine;

    public RenderingContext context;

    public ProjectionViewUBO ubo;

    public Framebuffer finalFramebuffer;
    public Framebuffer tmpFramebuffer;

    private int ticks;

    public AppRenderer(AppEngine engine) {
        this.engine = engine;
    }

    public void init() {
        this.context = BBS.getRender();

        this.ubo = new ProjectionViewUBO(0);
        this.ubo.init();
        this.ubo.bindUnit();

        this.context.getLights().init();
        this.context.getLights().bindUnit();

        this.context.setup(BBS.getFonts().getRenderer(Link.assets("fonts/bbs_round.json")), BBS.getVAOs(), BBS.getTextures());
        this.context.setUBO(this.ubo);

        this.setupShaderPipeline();
    }

    private void setupShaderPipeline() {
        this.finalFramebuffer = BBS.getFramebuffers().getFramebuffer(Link.bbs("final"), (framebuffer) -> {
            Texture texture = new Texture();

            texture.setFilter(GL11.GL_LINEAR);
            texture.setWrap(GL13.GL_CLAMP_TO_EDGE);

            framebuffer.deleteTextures().attach(texture, GL30.GL_COLOR_ATTACHMENT0);
            framebuffer.unbind();
        });

        this.tmpFramebuffer = BBS.getFramebuffers().getFramebuffer(Link.bbs("tmp"), (framebuffer) -> {
            Texture texture = new Texture();

            texture.setFilter(GL11.GL_LINEAR);
            texture.setWrap(GL13.GL_CLAMP_TO_EDGE);

            framebuffer.deleteTextures().attach(texture, GL30.GL_COLOR_ATTACHMENT0);
            framebuffer.unbind();
        });
    }

    public void render(float transition) {
        for (FontRenderer value : BBS.getFonts().fontRenderers.values()) {
            if (value != null)
                value.setTime(this.ticks + transition);
        }

        this.context.setTransition(transition);

        if (this.engine.screen.canRefresh()) {
        }

        this.context.runRunnables();
    }

    public void resize(int width, int height) {

    }

    public void reloadShaders(Boolean bool) {
    }

    @Override
    public void delete() {
        this.ubo.delete();
    }

    @Override
    public void update() {
        this.ticks++;
    }
}
