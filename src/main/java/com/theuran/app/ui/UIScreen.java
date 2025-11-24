package com.theuran.app.ui;

import com.theuran.app.AppEngine;
import mchorse.bbs.BBSSettings;
import mchorse.bbs.core.IEngine;
import mchorse.bbs.core.input.MouseInput;
import mchorse.bbs.graphics.RenderingContext;
import mchorse.bbs.graphics.shaders.ShaderRepository;
import mchorse.bbs.graphics.window.IFileDropListener;
import mchorse.bbs.graphics.window.Window;
import mchorse.bbs.ui.framework.UIBaseMenu;
import mchorse.bbs.ui.framework.UIRenderingContext;
import mchorse.bbs.utils.joml.Matrices;
import org.lwjgl.glfw.GLFW;

public class UIScreen implements IEngine, IFileDropListener {
    public AppEngine engine;

    public UIRenderingContext context;
    public UIShaders shaders;

    public UIBaseMenu menu;

    private boolean refresh;

    public UIScreen(AppEngine engine) {
        this.engine = engine;
    }

    public void reload() {
        this.showMenu(this.getMenu());
    }

    public boolean hasMenu() {
        return this.menu != null;
    }

    public boolean isPaused() {
        return this.hasMenu() && this.menu.canPause();
    }

    public boolean canRefresh() {
        if (this.refresh) {
            this.refresh = false;

            return true;
        }

        return !this.hasMenu() || this.menu.canRefresh();
    }

    public void pause() {
        this.showMenu(this.getMenu());
    }

    public void showMenu(UIBaseMenu menu) {
        UIBaseMenu old = this.menu;

        if (this.menu != null) {
            this.menu.onClose(menu);
        }

        this.menu = menu;

        if (this.menu != null) {
            int scale = BBSSettings.getScale();

            this.menu.context.setup(this.context);
            this.menu.onOpen(old);
            this.menu.resize(Window.width / scale, Window.height / scale);
        }

        Window.toggleMousePointer(this.menu == null);
        this.engine.keys.keybinds.resetKeybinds();
    }

    public UIBaseMenu getMenu() {
        if (this.menu == null) {
            this.menu = new UIMainMenu(this.engine);
        }

        return this.menu;
    }

    @Override
    public void init() {
        this.shaders = new UIShaders();
        this.context = new UIRenderingContext(this.engine.renderer.context, this.shaders.ortho);

        this.context.setUBO(this.shaders.ubo);

        ShaderRepository mainShaders = this.context.getMainShaders();
        ShaderRepository pickingShaders = this.context.getPickingShaders();

        mainShaders.register(this.shaders.vertexRGBA2D);
        mainShaders.register(this.shaders.vertexUVRGBA2D);

        pickingShaders.register(this.shaders.pickingPreview);

        this.reload();
    }

    @Override
    public void delete() {
        this.shaders.ubo.delete();
    }

    @Override
    public void resize(int width, int height) {
        this.refresh = true;

        this.shaders.resize(width, height);

        if (this.menu != null) {
            int scale = BBSSettings.getScale();

            this.menu.resize(width / scale, height / scale);
        }
    }

    @Override
    public void render(float transition) {
        this.context.setTransition(transition);
        this.context.getUBO().update(this.shaders.ortho, Matrices.EMPTY_4F);

        if (this.menu != null) {
            MouseInput mouse = this.engine.mouse;

            this.menu.renderMenu(this.context, BBSSettings.transform(mouse.x), BBSSettings.transform(mouse.y));
        }

        this.context.runRunnables();
    }

    @Override
    public void update() {
        if (this.menu != null)
            this.menu.update();
    }

    @Override
    public boolean handleKey(int key, int scancode, int action, int mods) {
        if (this.menu != null)
            return this.menu.handleKey(key, scancode, action, mods);

        return false;
    }

    @Override
    public void handleTextInput(int key) {
        if (this.menu != null)
            this.menu.handleTextInput(key);
    }

    @Override
    public void handleMouse(int button, int action, int mode) {
        if (this.menu == null)
            return;

        MouseInput mouse = this.engine.mouse;

        if (action == GLFW.GLFW_PRESS) {
            this.menu.mouseClicked(BBSSettings.transform(mouse.x), BBSSettings.transform(mouse.y), button);
        } else if (action == GLFW.GLFW_RELEASE) {
            this.menu.mouseReleased(BBSSettings.transform(mouse.x), BBSSettings.transform(mouse.y), button);
        }
    }

    @Override
    public void handleScroll(double x, double y) {
        if (this.menu == null)
            return;

        MouseInput mouse = this.engine.mouse;
        int mouseWheel = (int) Math.round(y);

        if (mouseWheel != 0) {
            this.menu.mouseScrolled(BBSSettings.transform(mouse.x), BBSSettings.transform(mouse.y), mouseWheel);
        }
    }

    @Override
    public void acceptFilePaths(String[] paths) {
        if (this.menu != null)
            for (IFileDropListener listener : this.menu.getRoot().getChildren(IFileDropListener.class))
                listener.acceptFilePaths(paths);
    }
}
