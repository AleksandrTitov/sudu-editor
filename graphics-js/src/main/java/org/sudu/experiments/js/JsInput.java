package org.sudu.experiments.js;

import org.sudu.experiments.Debug;
import org.sudu.experiments.Disposable;
import org.sudu.experiments.input.InputListeners;
import org.sudu.experiments.input.KeyEvent;
import org.sudu.experiments.input.MimeTypes;
import org.sudu.experiments.input.MouseEvent;
import org.sudu.experiments.math.Numbers;
import org.sudu.experiments.math.V2i;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.browser.Window;
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.events.*;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Element;

import java.util.function.Consumer;

public class JsInput {
  static final boolean debug = 1 < 0;

  private final JsHelper.HTMLElement element;
  private final InputListeners listeners;
  private Disposable disposer;
  private V2i clientRect = null;

  public JsInput(HTMLElement element, InputListeners listeners) {
    this.element = element.cast();
    this.listeners = listeners;
    Window window = Window.current();
    this.disposer = Disposable.composite(
        addListener(element, "keydown", this::onKeyDown),
        addListener(element, "keyup", this::onKeyUp),
        addListener(element, "mousemove", this::onMouseMove),
        addListener(element, "mousedown", this::onMouseDown),
        addListener(element, "mouseup", this::onMouseUp),
        // todo add onMouseWheelOnWindow
        addListener(element, "wheel", this::onMouseWheelOnElement),
        addListener(element, "dblclick", this::onDoubleClick),
        addListener(element, "contextmenu", this::onContextMenu),
        addListener(element, "focus", this::onFocus),
        addListener(element, "blur", this::onBlur),
        addListener(element, "drop", this::onDrop),
        addListener(window, "paste", this::onPaste, true),
        addListener(window, "copy", this::onCopy),
        addListener(window, "cut", this::onCut),
        addListener(window, "blur", e -> {
          } // JsHelper.consoleInfo("Window.blur event ", e); }
        ),
        addListener(window, "focus", e -> {
          } // JsHelper.consoleInfo("Window.focus event ", e)
        )
    );
    initPointerCapture(element);
  }

  public void setClientRect(int w, int h) {
    clientRect = new V2i(w, h);
  }

  public void dispose() {
    if (disposer != null) {
      disposer.dispose();
      disposer = null;
    }
  }

  private <T extends Event> Disposable addListener(EventTarget element, String type, EventListener<T> listener) {
    element.addEventListener(type, listener);
    return remover(element, type, listener);
  }

  private <T extends Event> Disposable addListener(
      EventTarget element, String type,
      EventListener<T> listener, boolean useCapture
  ) {
    element.addEventListener(type, listener, useCapture);
    return remover(element, type, listener);
  }

  private <T extends Event> Disposable remover(EventTarget element, String type, EventListener<T> listener) {
    return () -> element.removeEventListener(type, listener);
  }


  static void debug(String string) {
    if (debug) Debug.consoleInfo(string);
  }

  static void debug(String string, int btn) {
    if (debug) Debug.consoleInfo(string + btn);
  }

  private void onMouseMove(org.teavm.jso.dom.events.MouseEvent event) {
    if (clientRect == null) return;
    MouseEvent mouseEvent = mouseEvent(event);
    listeners.sendMouseMove(mouseEvent);
    stopEvent(event);
  }

  // mouse down events are not stopped to propagate in order to
  // allow browser focus management to function properly
  private void onMouseDown(org.teavm.jso.dom.events.MouseEvent event) {
    debug("onMouseDown bt #", event.getButton());
    if (clientRect == null) return;
    MouseEvent mouseEvent = mouseEvent(event);
    listeners.sendMouseButton(mouseEvent, event.getButton(), true, 1);
  }

  private void onMouseUp(org.teavm.jso.dom.events.MouseEvent event) {
    debug("onMouseUp bt #", event.getButton());
    if (clientRect == null) return;
    MouseEvent mouseEvent = mouseEvent(event);
    if (listeners.sendMouseButton(mouseEvent, event.getButton(), false, 1)) {
      stopEvent(event);
    }
  }

  private void onDoubleClick(org.teavm.jso.dom.events.MouseEvent event) {
    debug("onDoubleClick");
    if (clientRect == null) return;
    MouseEvent mouseEvent = mouseEvent(event);
    if (listeners.sendMouseButton(mouseEvent, event.getButton(), true, 2)) {
      stopEvent(event);
    }
  }

  private void onContextMenu(org.teavm.jso.dom.events.MouseEvent event) {
    debug("onContextMenu");
    if (clientRect == null) return;
    MouseEvent mouseEvent = mouseEvent(event);
    if (listeners.sendContextMenu(mouseEvent)) {
      stopEvent(event);
    }
  }

  private void onMouseWheelOnElement(WheelEvent event) {
    if (clientRect == null) return;
    debug("onMouseWheelElement");
    float scale = switch (event.getDeltaMode()) {
      case WheelEvent.DOM_DELTA_PIXEL -> 1;
      case WheelEvent.DOM_DELTA_LINE -> 25;
      case WheelEvent.DOM_DELTA_PAGE -> 250;
      default -> 0;
    };
    listeners.sendMouseWheel(mouseEvent(event),
        scale * event.getDeltaX(), scale * event.getDeltaY());
    stopEvent(event);
  }

  private void onKeyDown(KeyboardEvent event) {
    if (debug) JsHelper.consoleInfo(
        "onKeyDown id=" + getId() + ", event.key = " + event.getKey());

    if (listeners.sendKeyEvent(keyEvent(event, true))) {
      stopEvent(event);
    }
  }

  private String getId() {
    return element.getParentNode().<HTMLElement>cast().getId();
  }

  private void onKeyUp(KeyboardEvent event) {
    if (listeners.sendKeyEvent(keyEvent(event, false))) {
      stopEvent(event);
    }
  }

  private void onBlur(Event event) {
    if (debug) JsHelper.consoleInfo("onBlur event("
            + getId() + "), target == element " +
        (event.getTarget() == element));
    listeners.sendBlurEvent();
  }

  private void onFocus(Event event) {
    if (debug) JsHelper.consoleInfo("onFocus event("
        + getId() + "), target == element " +
        (event.getTarget() == element));
    listeners.sendFocusEvent();
  }

  private void onDrop(DragEvent event) {
    debug("onDrop");
    if (clientRect == null) return;
    MouseEvent mouseEvent = mouseEvent(event);
  }

  private void onPaste(ClipboardEvent event) {
    JSArrayReader<DataTransfer.Item> items = event.getClipboardData().getItems();
    for (int i = 0, n = items.getLength(); i < n; i++) {
      DataTransfer.Item item = items.get(i);
      if (item.isString() && item.isTextPlain()) {
        Consumer<String> pasteHandler = listeners.onPastePlainText();
        if (pasteHandler != null) {
          item.getAsString(sendAndRepaint(pasteHandler, listeners.repaint));
          stopEvent(event);
        }
      } else {
        Debug.consoleInfo(
            "onPaste: item.type = " + item.getType()
                + ", item.kind = " + item.getKind());
      }
    }
  }

  static StringConsumer sendAndRepaint(Consumer<String> receiver, Runnable repaint) {
    return s -> {
      JsHelper.consoleInfo("paste plain string ", s);
      receiver.accept(s.stringValue()); repaint.run();
    };
  }

  private void onCopy(ClipboardEvent e) {
    if (listeners.sendCopy(wrapCopyCutEvent(e), false)) {
      stopEvent(e);
//      DataTransfer.Item item = e.getClipboardData().getItems().get(0);
//      item.getAsString(s -> JsHelper.consoleInfo("item.getAsString: " + s));
    }
  }

  private void onCut(ClipboardEvent e) {
    if (listeners.sendCopy(wrapCopyCutEvent(e), true)) {
      stopEvent(e);
    }
  }

  private Consumer<String> wrapCopyCutEvent(ClipboardEvent e) {
    return text -> e.getClipboardData().setData(MimeTypes.textPlain, text);
  }

  private MouseEvent mouseEvent(org.teavm.jso.dom.events.MouseEvent event) {
    double devicePixelRatio = Window.current().getDevicePixelRatio();
    DOMRect rect = element.getBoundingClientRectD();

    V2i position = new V2i(
        Numbers.iRnd((event.getClientX() - rect.getLeft()) * devicePixelRatio),
        Numbers.iRnd((event.getClientY() - rect.getTop()) * devicePixelRatio));

    V2i size = new V2i(clientRect);
    return new MouseEvent(
        position, size,
        event.getCtrlKey(),
        event.getAltKey(),
        event.getShiftKey(),
        event.getMetaKey()
    );
  }

  private KeyEvent keyEvent(KeyboardEvent jsEvent, boolean pressed) {
    return new KeyEvent(
        jsEvent.getKey(), jsEvent.getKeyCode(), pressed, jsEvent.isRepeat(),
        jsEvent.isCtrlKey(), jsEvent.isAltKey(), jsEvent.isShiftKey(), jsEvent.isMetaKey()
    );
  }

  private void stopEvent(Event e) {
    e.stopPropagation();
    e.preventDefault();
  }

  interface DragEvent extends org.teavm.jso.dom.events.MouseEvent {
    @JSProperty DataTransfer getDataTransfer();
  }

  interface ClipboardEvent extends Event {
    @JSProperty DataTransfer getClipboardData();
  }

  @JSFunctor
  public interface StringConsumer extends JSObject {
    void accept(JSString value);
  }

  interface DataTransfer extends JSObject {
    @JSProperty JSArrayReader<Item> getItems();
    void setData(String format, String content);

    abstract class Item implements JSObject {
      @JSProperty native String getKind();

      @JSProperty native String getType();

      @JSBody(script = "return this.kind == 'string';")
      native boolean isString();

      @JSBody(script = "return this.type == 'text/plain';")
      native boolean isTextPlain();

      @JSBody(script = "this.kind == 'file';")
      native boolean isFile();

      native void getAsString(StringConsumer receiver);
    }
  }

  @JSFunctor
  public interface PointerEventFunction extends JSObject {
    void f(PointerEvent e);
  }

  @JSBody(params = {"element", "pointer"},
    script = "element.setPointerCapture(pointer.pointerId)")
  static native void setPointerCapture(Element element, PointerEvent pointer);

  @JSBody(params = {"element", "pointer"},
    script = "element.releasePointerCapture(pointer.pointerId)")
  static native void releasePointerCapture(Element element, PointerEvent pointer);

  @JSBody(params = {"element", "f"},
    script = "element.onpointerdown = f")
  static native void setOnPointerDown(Element element, PointerEventFunction f);

  @JSBody(params = {"element", "f"},
    script = "element.onpointerup = f")
  static native void setOnPointerUp(Element element, PointerEventFunction f);

  public void initPointerCapture(Element element) {
    setOnPointerDown(element, pointer -> setPointerCapture(element, pointer));
    setOnPointerUp(element, pointer -> releasePointerCapture(element, pointer));
  }

}