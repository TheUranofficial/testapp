package com.theuran.app;

import mchorse.bbs.bridge.IBridgeWorld;
import mchorse.bbs.data.types.MapType;
import mchorse.bbs.graphics.window.Window;
import mchorse.bbs.resources.Link;
import mchorse.bbs.utils.CrashReport;
import mchorse.bbs.utils.Pair;
import mchorse.bbs.utils.Profiler;
import mchorse.bbs.utils.TimePrintStream;
import mchorse.bbs.utils.cli.ArgumentParser;
import mchorse.bbs.utils.cli.ArgumentType;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

public class App {
    public static final Profiler PROFILER = new Profiler();

    public File gameDirectory;
    public int windowWidth = 1280;
    public int windowHeight = 720;
    public boolean openGLDebug;

    public static Link link(String path) {
        return new Link("app", path);
    }

    private static boolean canLock(File file) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            FileLock fileLock = randomAccessFile.getChannel().tryLock();

            if (fileLock != null) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        fileLock.release();
                        randomAccessFile.close();
                        file.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void main(String[] args) {
        PROFILER.begin("bootstrap");

        System.out.println("TESTAPP: 1488, LWJGL: " + Version.getVersion() + ", GLFW: " + GLFW.glfwGetVersionString());

        System.setOut(new TimePrintStream(System.out));
        System.setErr(new TimePrintStream(System.err));

        ArgumentParser parser = new ArgumentParser();

        parser.register("gameDirectory", ArgumentType.PATH)
                .register("glDebug", "gld", ArgumentType.NUMBER)
                .register("width", "ww", ArgumentType.NUMBER)
                .register("height", "wh", ArgumentType.NUMBER);

        App app = new App();

        app.setup(parser.parse(args));

        File lockFile = new File(app.gameDirectory, "instance.lock");

        if (canLock(lockFile)) {
            app.launch();
        } else {
            System.err.println("An instance of TestApp is already running! Please shut it down.");
            System.err.println("If you're absolutely sure that it's not running, then remove " + lockFile.getAbsolutePath() + " file, and try launching again!");
            System.err.println("If you can't remove that file... then it's still running in the background!");

            JOptionPane.showMessageDialog(null, "TestApp instance is already running!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setup(MapType data) {
        if (data.has("gameDirectory")) {
            this.gameDirectory = new File(data.getString("gameDirectory"));
        }

        this.windowHeight = data.getInt("height", this.windowHeight);
        this.windowWidth = data.getInt("width", this.windowWidth);
        this.openGLDebug = data.getBool("glDebug", this.openGLDebug);
    }

    public void launch() {
        PROFILER.endBegin("launch");

        if (this.gameDirectory == null || !this.gameDirectory.isDirectory()) {
            throw new IllegalStateException("Given game directory '" + this.gameDirectory + "' doesn't not exist or not a directory...");
        }

        AppEngine engine = new AppEngine(this);
        long id = -1;

        try {
            PROFILER.endBegin("setup_window");

            Window.initialize("Icons", this.windowWidth, this.windowHeight, true);
            Window.setupStates();

            id = Window.getWindow();

            PROFILER.endBegin("init_engine");
            engine.init();
            PROFILER.end();
            PROFILER.print();
            engine.start(id);
        } catch (Exception e) {
            File crashes = new File(this.gameDirectory, "crashes");
            Pair<File, String> crash = CrashReport.writeCrashReport(crashes, e, "BBS has crashed! Here is a crash stacktrace:");

            e.printStackTrace();

            CrashReport.showDialogue(crash, "BBS has crashed! The crash log " + crash.a.getName() + " was generated in \"crashes\" folder, which you should send to debt hunt's developer. IMPORTANT: don't screenshot this window!");
        }

        engine.delete();

        Callbacks.glfwFreeCallbacks(id);
        GLFW.glfwDestroyWindow(id);

        GLFW.glfwSetErrorCallback(null).free();
        GLFW.glfwTerminate();
    }
}