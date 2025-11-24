package com.theuran.app;

import mchorse.bbs.BBS;
import mchorse.bbs.core.IComponent;
import mchorse.bbs.graphics.GLStates;
import mchorse.bbs.graphics.RenderingContext;
import mchorse.bbs.graphics.shaders.Shader;
import mchorse.bbs.graphics.shaders.pipeline.ShaderPipeline;
import mchorse.bbs.graphics.text.FontRenderer;
import mchorse.bbs.graphics.ubo.ProjectionViewUBO;
import mchorse.bbs.resources.Link;

public class AppRenderer implements IComponent {
    public AppEngine engine;

    public RenderingContext context;

    public ProjectionViewUBO ubo;

    public Shader finalShader;

    private ShaderPipeline pipeline = new ShaderPipeline();
    private AppShaders shaders = new AppShaders(this.pipeline);
    private AppShaders targetShaders = new AppShaders(this.pipeline);

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
        this.shaders.reload();
        this.targetShaders.reload();

        for (AppShaders.Stage stage : this.shaders.stages) {
            stage.shader.attachUBO(this.context.getLights(), "u_lights_block");
        }

        for (AppShaders.Stage stage : this.targetShaders.stages) {
            stage.shader.attachUBO(this.context.getLights(), "u_lights_block");
        }
    }

    public void render(float transition) {
        for (FontRenderer value : BBS.getFonts().fontRenderers.values()) {
            if (value != null)
                value.setTime(this.ticks + transition);
        }

        this.context.setTransition(transition);

        if (this.engine.screen.canRefresh()) {
            context.stack.reset();

            GLStates.activeTexture(0);
            GLStates.resetViewport();
        }

        this.context.runRunnables();
    }

    public void resize(int width, int height) {
        this.shaders.resize(width, height);
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
