package com.theuran.app.ui;

import mchorse.bbs.BBSSettings;
import mchorse.bbs.graphics.shaders.CommonShaderAccess;
import mchorse.bbs.graphics.shaders.Shader;
import mchorse.bbs.graphics.ubo.ProjectionViewUBO;
import mchorse.bbs.graphics.vao.VBOAttributes;
import mchorse.bbs.resources.Link;
import org.joml.Matrix4f;

public class UIShaders {
    public ProjectionViewUBO ubo;
    public Matrix4f ortho = new Matrix4f();
    public Shader vertexRGBA2D;
    public Shader vertexUVRGBA2D;
    public Shader pickingPreview;

    public UIShaders() {
        this.ubo = new ProjectionViewUBO(1);
        this.ubo.init();
        this.ubo.bindUnit();

        this.vertexRGBA2D = new Shader(Link.assets("shaders/ui/vertex_rgba_2d.glsl"), VBOAttributes.VERTEX_RGBA_2D);
        this.vertexUVRGBA2D = new Shader(Link.assets("shaders/ui/vertex_uv_rgba_2d.glsl"), VBOAttributes.VERTEX_UV_RGBA_2D);

        this.vertexRGBA2D.onInitialize(CommonShaderAccess::initializeTexture).attachUBO(this.ubo, "u_matrices");
        this.vertexUVRGBA2D.onInitialize(CommonShaderAccess::initializeTexture).attachUBO(this.ubo, "u_matrices");

        this.pickingPreview = new Shader(Link.assets("shaders/picking/vertex_uv_rgba_2d-preview.glsl"), VBOAttributes.VERTEX_UV_RGBA_2D);

        this.pickingPreview.onInitialize(CommonShaderAccess::initializeTexture).attachUBO(this.ubo, "u_matrices");
    }

    public void resize(int width, int height) {
        int scale = BBSSettings.getScale();

        this.ortho.setOrtho(0, (float) width / scale, (float) height / scale, 0, -100, 100);
    }
}