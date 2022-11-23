package org.sudu.experiments;

import org.sudu.experiments.math.Numbers;
import org.sudu.experiments.math.V4f;

public class FontDesk {
  public static final int WEIGHT_REGULAR = 400;

  public static final int STYLE_NORMAL  = 0;
  public static final int STYLE_OBLIQUE = 1;
  public static final int STYLE_ITALIC  = 2;

  public final String name;
  public final String sStyle;
  public final float size;
  public final int iSize;
  public final int weight;
  public final int style;

  public final int   iAscent, iDescent;
  public final float fAscent, fDescent;

  public final float spaceWidth, WWidth;
  public final boolean monospace;

  public final Object platformFont;

  public FontDesk(String name, float size, int weight, int style, Canvas measuringCanvas) {
    this.name = name;
    this.size = size;
    this.weight = weight;
    this.style = style;

    iSize = iSize(size);

    measuringCanvas.setFont(name, size, weight, style);

    V4f fontMetrics = measuringCanvas.getFontMetrics();
    fAscent = fontMetrics.x;
    fDescent = fontMetrics.y;
    WWidth = fontMetrics.z;
    spaceWidth = fontMetrics.w;
    float dotWidth = measuringCanvas.measureText(".");
    iAscent = Numbers.iRnd(fAscent);
    iDescent = Numbers.iRnd(fDescent);
    monospace = monospace(spaceWidth, WWidth, dotWidth);
    platformFont = measuringCanvas.platformFont(name, iSize);
    sStyle = stringStyle(style);

    if (1>0) debug(dotWidth);
  }

  public FontDesk(
      String name, float size, int weight, int style,
      float ascent, float descent,
      float spaceWidth, float WWidth, float dotWidth,
      Object platformFont
  ) {
    this.name = name;
    this.size = size;
    this.iSize = iSize(size);
    this.weight = weight;
    this.style = style;
    this.fAscent = ascent;
    this.fDescent = descent;
    this.spaceWidth = spaceWidth;
    this.WWidth = WWidth;
    this.platformFont = platformFont;

    this.iAscent = Numbers.iRnd(fAscent);
    this.iDescent = Numbers.iRnd(fDescent);
    monospace = monospace(spaceWidth, WWidth, dotWidth);
    sStyle = stringStyle(style);
    debug(dotWidth);
  }

  private void debug(float dotWidth) {
    Debug.consoleInfo("new FontDesk: font " + name
        + ", size " + size + ", style = " + sStyle + ", weight " + weight);
    Debug.consoleInfo("  font ascent = ", fAscent);
    Debug.consoleInfo("  font descent = ", fDescent);

    int dotSize = (int) (dotWidth * 32);
    int spaceSize = (int) (spaceWidth * 32);
    int WSize = (int) (WWidth * 32);
    Debug.consoleInfo("  '.' size * 32 = ", dotSize);
    Debug.consoleInfo("  'W' size * 32 = ", WSize);
    Debug.consoleInfo("  ' ' size * 32 = ", spaceSize);
    Debug.consoleInfo("  monospace = " + monospace);
    Debug.consoleInfo("  platformFont = ", platformFont);
  }

  // usually this is very near to (size * 1.2)
  public int realFontSize() {
    return iAscent + iDescent;
  }

  public int caretHeight(int lineHeight) {
    int caretOffer = (iAscent + iDescent + iSize) / 2;
    return lineHeight - (lineHeight / 2 - caretOffer / 2) * 2;
  }

  static int iSize(float size) {
    int iSize = (int)size;
    if (iSize != size) {
      Debug.consoleInfo("FontDesk::FontDesk iSize != size: " + size);
    }
    return iSize;
  }

  static boolean monospace(float spaceWidth, float WWidth, float dotWidth) {
    int dotSize = (int) (dotWidth * 32);
    int spaceSize = (int) (spaceWidth * 32);
    int WSize = (int) (WWidth * 32);

    return spaceSize == dotSize && spaceSize == WSize;
  }

  static String stringStyle(int style) {
    return switch (style) {
      case 1 -> "oblique";
      case 2 -> "italic";
      default -> "normal";
    };
  }
}
