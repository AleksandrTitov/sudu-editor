package org.sudu.experiments;

public class FontLoader {
  public interface JetBrainsMono {
    String typeface = "JetBrains Mono";
    String Bold = "JetBrainsMono-Bold.ttf";
    String BoldItalic = "JetBrainsMono-BoldItalic.ttf";
    String Italic = "JetBrainsMono-Italic.ttf";
    String Medium = "JetBrainsMono-Medium.ttf";
    String MediumItalic = "JetBrainsMono-MediumItalic.ttf";
    String Regular = "JetBrainsMono-Regular.ttf";
    String SemiBold = "JetBrainsMono-SemiBold.ttf";
    String SemiBoldItalic = "JetBrainsMono-SemiBoldItalic.ttf";

    static FontConfig regular() {
      return new FontConfig(FontLoader::load, JetBrainsMono.Regular);
    }

    static FontConfig all() {
      return new FontConfig(FontLoader::load,
          JetBrainsMono.Regular,    JetBrainsMono.Italic,
          JetBrainsMono.Medium,     JetBrainsMono.MediumItalic,
          JetBrainsMono.SemiBold,   JetBrainsMono.SemiBoldItalic,
          JetBrainsMono.Bold,       JetBrainsMono.BoldItalic);
    }
  }

  public static byte[] load(String name) {
    return ResourceLoader.load("fonts/".concat(name), FontLoader.class);
  }
}