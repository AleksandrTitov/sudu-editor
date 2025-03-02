package org.sudu.experiments.tests;

import org.sudu.experiments.Disposable;
import org.sudu.experiments.Scene0;
import org.sudu.experiments.SceneApi;
import org.sudu.experiments.WglGraphics;
import org.sudu.experiments.input.InputListener;
import org.sudu.experiments.input.KeyCode;
import org.sudu.experiments.input.KeyEvent;
import org.sudu.experiments.math.Color;
import org.sudu.experiments.math.V2i;
import org.sudu.experiments.math.V4f;
import org.sudu.experiments.math.XorShiftRandom;

public class WindowSizeTestScene extends Scene0 implements InputListener {

  Disposable disposable;
  V4f mainColor = Color.Cvt.fromHSV(r(), 1, 1);

  public WindowSizeTestScene(SceneApi api) {
    super(api);
    disposable = api.input.addListener(this);
  }

  @Override
  public void dispose() {
    disposable = Disposable.dispose(disposable);
    super.dispose();
  }

  @Override
  public boolean onKey(KeyEvent event) {
    if (event.isPressed && event.keyCode == KeyCode.F11) {
      api.window.addChild("child", WindowSizeTestScene::new);
    }
    return InputListener.super.onKey(event);
  }

  @Override
  public void paint() {
    super.paint();
    V2i horSize = new V2i(0, 1);
    V2i verSize = new V2i(1, 0);
    int left = -2, right = size.x - 1;
    int top = 0, bottom = size.y - 1;
    WglGraphics g = api.graphics;
    for (int i = 0; i < 2000; i++) {
      if (left < right && top <= bottom) {
        horSize.x = right - left + 1;
        g.drawRect(left, top, horSize, mainColor);
        left += 2;
      } else break;

      if (top < bottom && left <= right) {
        verSize.y = bottom - top + 1;
        g.drawRect(right, top, verSize, mainColor);
        top += 2;
      } else break;

      if (left < right && top <= bottom) {
        horSize.x = right - left + 1;
        g.drawRect(left, bottom, horSize, mainColor);
        right -= 2;
      } else break;

      if (top < bottom && left <= right) {
        verSize.y = bottom - top + 1;
        g.drawRect(left, top, verSize, mainColor);
        bottom -= 2;
      } else break;
    }
  }
  static double r() { return new XorShiftRandom().nextDouble(); }
}
