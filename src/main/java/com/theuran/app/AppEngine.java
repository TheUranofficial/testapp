package com.theuran.app;

import com.theuran.app.bridge.BridgeMenu;
import com.theuran.app.ui.UIMainMenu;
import com.theuran.app.ui.UIScreen;
import mchorse.bbs.BBS;
import mchorse.bbs.BBSData;
import mchorse.bbs.BBSSettings;
import mchorse.bbs.bridge.IBridge;
import mchorse.bbs.bridge.IBridgeMenu;
import mchorse.bbs.core.Engine;
import mchorse.bbs.core.input.MouseInput;
import mchorse.bbs.data.DataToString;
import mchorse.bbs.events.L10nReloadEvent;
import mchorse.bbs.events.UpdateEvent;
import mchorse.bbs.events.register.RegisterL10nEvent;
import mchorse.bbs.graphics.GLStates;
import mchorse.bbs.graphics.shaders.ShaderRepository;
import mchorse.bbs.graphics.window.IFileDropListener;
import mchorse.bbs.graphics.window.Window;
import mchorse.bbs.l10n.L10n;
import mchorse.bbs.l10n.L10nUtils;
import mchorse.bbs.resources.packs.DataSourcePack;
import mchorse.bbs.resources.packs.InternalAssetsSourcePack;
import mchorse.bbs.settings.values.ValueLanguage;
import mchorse.bbs.utils.IOUtils;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class AppEngine extends Engine implements IBridge, IFileDropListener {
    public AppRenderer renderer;
    public UIScreen screen;

    private Map<Class<?>, Object> apis = new HashMap<>();

    public AppEngine(App app) {
        this.apis.put(IBridgeMenu.class, new BridgeMenu(this));

        BBS.events.register(this);

        BBS.registerCore(this, app.gameDirectory);
        BBS.registerFactories();
        BBS.registerFoundation();

        this.renderer = new AppRenderer(this);
        this.screen = new UIScreen(this);

        this.registerMiscellaneous();

        BBS.getShaders().setReloadCallback(this.renderer::reloadShaders);
    }

    @Subscribe
    public void registerL10n(RegisterL10nEvent event) {
        this.reloadSupportedLanguages();

        event.l10n.registerOne(lang -> App.link("lang/" + lang + ".json"));
    }

    @Subscribe
    public void reloadL10n(L10nReloadEvent event) {
        File export = BBS.getAssetsPath("lang_editor/" + BBSSettings.language.get());
        File[] files = export.listFiles();

        if (files == null)
            return;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                this.overwriteLanguage(event.l10n, file);
            }
        }
    }

    private void overwriteLanguage(L10n l10n, File file) {
        try {
            l10n.overwrite(DataToString.mapFromString(IOUtils.readText(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws Exception {
        super.init();

        App.PROFILER.endBegin("init_bbs");
        BBS.initialize();
        App.PROFILER.endBegin("init_bbs_data");
        BBSData.load(BBS.getDataFolder(), this);

        App.PROFILER.endBegin("init_renderer");
        this.renderer.init();
        this.screen.init();
        this.resize(Window.width, Window.height);

        Window.focus();
        Window.toggleMousePointer(false);

        App.PROFILER.endBegin("init_callbacks");
        this.registerSettingsCallbacks();
    }

    private void registerSettingsCallbacks() {
        BBSSettings.language.postCallback(value -> {
           this.reloadSupportedLanguages();
           BBS.getL10n().reload(((ValueLanguage) value).get(), BBS.getProvider());
        });

        BBSSettings.userIntefaceScale.postCallback(value -> BBS.getEngine().needsResize());
    }

    @Override
    public void delete() {
        super.delete();

        this.screen.delete();

        BBSData.delete();
        BBS.terminate();
    }

    @Override
    public void resize(int width, int height) {
        GLStates.resetViewport();

        this.renderer.resize(width, height);
        this.screen.resize(width, height);
    }

    @Override
    public boolean handleGamepad(int i, int i1) {
        return false;
    }

    @Override
    public boolean handleKey(int key, int scancode, int action, int mods) {
        return this.keys.keybinds.handleKey(key, scancode, action, mods)
                || this.screen.handleKey(key, scancode, action, mods);
    }

    @Override
    public void handleTextInput(int key) {
        this.screen.handleTextInput(key);
    }

    @Override
    public void handleMouse(int button, int action, int mode) {
        this.screen.handleMouse(button, action, mode);
    }

    @Override
    public void handleScroll(double x, double y) {
        this.screen.handleScroll(x, y);
    }

    @Override
    public void render(float transition) {
        super.render(transition);

        float worldTransition = this.screen.isPaused() ? 0 : transition;

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        this.renderer.render(worldTransition);
        this.screen.render(transition);
    }

    @Override
    public void update() {
        super.update();

        this.renderer.update();
        this.screen.update();

        BBS.events.post(new UpdateEvent());
    }

    private void registerMiscellaneous() {
        BBS.getProvider().register(new InternalAssetsSourcePack("app", AppEngine.class));

        File file = BBS.getGamePath("assets.dat");

        if (file.isFile()) {
            try {
                BBS.getProvider().register(new DataSourcePack(file.toURI().toURL()));
                System.out.println("Loaded packed assets from assets.dat!");
            } catch (MalformedURLException e) {
                System.err.println("Failed to load packed assets.dat");
                e.printStackTrace();
            }
        }

        Window.registerFileDropListener(this);
    }

    private void reloadSupportedLanguages() {
        BBS.getL10n().reloadSupportedLanguages(L10nUtils.readAdditionalLanguages(BBS.getAssetsPath("lang_editor/languages.json")));
    }

    @Override
    public void acceptFilePaths(String[] paths) {
        this.screen.acceptFilePaths(paths);
    }

    @Override
    public Engine getEngine() {
        return this;
    }

    @Override
    public <T> T get(Class<T> aClass) {
        return aClass.cast(this.apis.get(aClass));
    }
}
