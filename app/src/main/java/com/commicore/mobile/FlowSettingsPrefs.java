package com.commicore.mobile;

import android.content.Context;
import android.content.SharedPreferences;

public final class FlowSettingsPrefs {
    private static final String PREF = "flow_settings";
    private FlowSettingsPrefs() {}

    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static String get(Context c, String key, String def) { return p(c).getString(key, def); }
    public static boolean getBool(Context c, String key, boolean def) { return p(c).getBoolean(key, def); }
    public static int getInt(Context c, String key, int def) { return p(c).getInt(key, def); }

    public static void save(Context c,
                            String videoType,
                            String imageModel,
                            String imageAspect,
                            String imageScale,
                            String videoModel,
                            String videoAspect,
                            String videoScale,
                            String duration,
                            String quality,
                            int sceneCount,
                            boolean createNewProject,
                            boolean autoAnimate,
                            boolean reuseProduct,
                            boolean usePresenterReference) {
        p(c).edit()
            .putString("videoType", videoType)
            .putString("imageModel", imageModel)
            .putString("imageAspect", imageAspect)
            .putString("imageScale", imageScale)
            .putString("videoModel", videoModel)
            .putString("videoAspect", videoAspect)
            .putString("videoScale", videoScale)
            .putString("duration", duration)
            .putString("quality", quality)
            .putInt("sceneCount", sceneCount)
            .putBoolean("createNewProject", createNewProject)
            .putBoolean("autoAnimate", autoAnimate)
            .putBoolean("reuseProduct", reuseProduct)
            .putBoolean("usePresenterReference", usePresenterReference)
            .putBoolean("configured", true)
            .apply();
    }

    public static boolean configured(Context c) { return p(c).getBoolean("configured", false); }

    public static String summary(Context c) {
        return "Mode: " + get(c,"videoType","Frames") +
            "\nImage: " + get(c,"imageModel","Nano Banana 2") + " • " + get(c,"imageAspect","9:16") + " • " + get(c,"imageScale","1x") +
            "\nVideo: " + get(c,"videoModel","Veo 3.1 Lite") + " • " + get(c,"videoAspect","9:16") + " • " + get(c,"videoScale","1x") + " • " + get(c,"duration","8s") +
            "\nDownload: " + get(c,"quality","1080p Upscaled") +
            "\nScenes: " + getInt(c,"sceneCount",1);
    }
}
