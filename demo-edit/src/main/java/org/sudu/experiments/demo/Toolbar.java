package org.sudu.experiments.demo;

import org.sudu.experiments.*;
import org.sudu.experiments.math.Numbers;
import org.sudu.experiments.math.V2i;
import org.sudu.experiments.math.V4f;

import java.util.ArrayList;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class Toolbar {
  static int vPad = 0;
  private FontDesk font;
  private final DemoRect rect = new DemoRect();
  private final V2i textureSize = new V2i();
  private int border, textPadding;
  private Button hoverItem = null;
  private final ArrayList<Button> buttons = new ArrayList<>();
  private GL.Texture texture;
  boolean textureDirty;

  static boolean useTopMode = false;

  public void setBgColor(V4f bgColor) {
    rect.color.set(bgColor);
  }

  public void setFont(FontDesk font) {
    this.font = font;
    disposeTexture();
  }

  public void dispose() {
    disposeTexture();
  }

  private void disposeTexture() {
    texture = Disposable.assign(texture, null);
  }

  public Disposable addButton(String text, ButtonColors colors, Runnable r) {
    Button b = new Button(r, text, colors);
    buttons.add(b);
    return () -> deleteButton(b);
  }

  private void deleteButton(Button b) {
    if (hoverItem == b) hoverItem = null;
    buttons.remove(b);
  }

  public V2i measure(Canvas mCanvas, double devicePR) {
    mCanvas.setFont(font);
    int textHeight = font.lineHeight() + vPad + vPad;
    border = Numbers.iRnd(2 * devicePR);
    textPadding = Numbers.iRnd(font.WWidth);
    int tw = 0;
    for (int i = 0; i < buttons.size(); i++) {
      Button button = buttons.get(i);
      int m = (int)(mCanvas.measureText(button.text) + 7.f / 8);
      int w = textPadding + m + textPadding;
      button.tRect.pos.x = tw;
      button.tRect.pos.y = 0;
      button.tRect.size.x = w;
      button.tRect.size.y = textHeight;
      button.tRect.textureRegion.set(tw, 0, w, textHeight);
      tw += w;
    }
    textureSize.x = tw;
    textureSize.y = textHeight;
    rect.size.y = textHeight + border * 2;
    rect.size.x = tw + border * buttons.size() + border;
    return rect.size;
  }

  public void setPos(int x, int y) {
    rect.pos.set(x, y);
    x += border;
    for (int i = 0; i < buttons.size(); i++) {
      TextRect tRect = buttons.get(i).tRect;
      tRect.pos.x = x;
      tRect.pos.y = y + border;
      x += tRect.size.x + border;
    }
  }

  public V2i size() {
    return rect.size;
  }

  private void renderTexture(WglGraphics g) {
    Canvas canvas = g.createCanvas(textureSize.x, textureSize.y);
    canvas.setFont(font);
    canvas.setTopMode(useTopMode);
    float baseline = vPad + (useTopMode ? 0 : font.fAscent);

    for (int i = 0; i < buttons.size(); i++) {
      Button button = buttons.get(i);
      canvas.drawText(button.text, button.tRect.textureRegion.x + textPadding, baseline);
    }
    texture = Disposable.assign(texture, g.createTexture());
    texture.setContent(canvas);
    canvas.dispose();
    textureDirty = false;
  }

  public void invalidateTexture() {
    textureDirty = true;
  }


  public void render(WglGraphics g) {
    if (texture == null || textureDirty) renderTexture(g);
    rect.draw(g, 0, 0);
    for (int i = 0; i < buttons.size(); i++) {
      buttons.get(i).tRect.drawText(g, texture, 0, 0, 0);
    }
  }

  public boolean onMouseMove(V2i pos) {
    Button h = find(pos);
    if (hoverItem != h) {
      if (hoverItem != null) hoverItem.setHover(false);
      if (h != null ) h.setHover(true);
      hoverItem = h;
    }
    return rect.isInside(pos);
  }

  public boolean onMouseClick(V2i pos, boolean press) {
    if (press) {
      Button b = find(pos);
      if (b != null) {
        b.action.run();
        return true;
      }
    }
    return false;
  }

  private Button find(V2i pos) {
    for (int i = 0; i < buttons.size(); i++) {
      Button button = buttons.get(i);
      if (button.tRect.isInside(pos)) {
        return button;
      }
    }
    return null;
  }

  static class Button {
    final TextRect tRect = new TextRect();
    final Runnable action;
    final ButtonColors colors;
    String text;

    public Button(Runnable r, String text, ButtonColors colors) {
      this.text = text;
      this.colors = colors;
      action = r;
      tRect.color.set(colors.color);
      tRect.bgColor.set(colors.bgColor);
    }

    public void setHover(boolean b) {
      tRect.bgColor.set(b ? colors.bgHighlight : colors.bgColor);
    }
  }

  public static final class ButtonColors {
    final V4f color;
    final V4f bgColor;
    final V4f bgHighlight;

    public ButtonColors(V4f color, V4f bgColor, V4f bgHighlight) {
      this.color = color;
      this.bgColor = bgColor;
      this.bgHighlight = bgHighlight;
    }
  }
}
