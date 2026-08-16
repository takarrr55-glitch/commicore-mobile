package com.commicore.mobile;

public final class PromptBuilder {
    private PromptBuilder(){}

    public static String caption(DbHelper.Product p) {
        String t = clean(p.title);
        String c = t + " น่าใช้ ใช้ง่าย ดูคุ้ม ✨ #ของดีบอกต่อ #รีวิวของใช้";
        if (c.length() > 150) c = t.substring(0, Math.min(70,t.length())) +
            " น่าใช้ ✨ #ของดีบอกต่อ #รีวิว";
        return c;
    }

    public static String imagePrompt(DbHelper.Product p) {
        return "Create a premium vertical 9:16 ecommerce lifestyle image using the exact product from the reference: " +
            clean(p.title) + ". Preserve the real product identity, shape, proportions, color, packaging, logos and labels. " +
            "Realistic Thai lifestyle setting, clean natural daylight, premium commercial product photography. " +
            "No extra product variants, no invented text, no watermark, no on-screen captions.";
    }

    public static String videoPrompt(DbHelper.Product p) {
        return "Create a vertical 9:16 short ecommerce product video using the exact product from the reference: " +
            clean(p.title) + ". Keep product shape, color, packaging and branding consistent with the source/reference. " +
            "8 seconds, realistic commercial lighting, simple useful demonstration, gentle camera motion, product clearly visible. " +
            "No morphing, no replacement product, no unwanted extra objects, no on-screen text, no watermark.";
    }

    private static String clean(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+"," ").trim();
    }
}
