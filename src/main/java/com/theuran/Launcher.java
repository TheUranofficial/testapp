package com.theuran;

import mchorse.bbs.data.types.MapType;
import mchorse.bbs.utils.JavaLauncher;

import java.io.File;
import java.util.List;

public class Launcher {
    public static void main(String[] strings) {
        JavaLauncher launcher = new JavaLauncher();
        MapType defaultSettings = new MapType();

        defaultSettings.putInt("game.width", 1280);
        defaultSettings.putInt("game.height", 720);
        defaultSettings.putString("game.directory", "game");

        List<String> args = launcher.getArguments("com.theuran.app.App");
        MapType settings = launcher.readSettings(new File("launcher.json"), defaultSettings);

        String gameDirectory = settings.getString("game.directory");

        args.add("--gameDirectory");
        args.add(gameDirectory);

        if (settings.has("game.width")) {
            args.add("-ww");
            args.add(String.valueOf(settings.getInt("game.width")));
        }

        if (settings.has("game.height")) {
            args.add("-wh");
            args.add(String.valueOf(settings.getInt("game.height")));
        }

        try {
            File logFile = launcher.getLogFile(gameDirectory);

            launcher.launch(args, logFile);

            System.out.println(String.join(" ", args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}