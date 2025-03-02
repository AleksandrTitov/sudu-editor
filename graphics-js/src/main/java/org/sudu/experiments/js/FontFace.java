package org.sudu.experiments.js;

import org.sudu.experiments.fonts.FontConfigJs;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSArrayReader;
import org.teavm.jso.core.JSString;
import org.teavm.jso.dom.html.HTMLDocument;

public abstract class FontFace implements JSObject {
  @JSBody(
      params = {"family", "source"},
      script = "return new FontFace(family, source);"
  )
  public static native FontFace create(JSString family, JSString source);

  @JSBody(
      params = {"family", "source", "descriptors"},
      script = "return new FontFace(family, source, descriptors);"
  )
  public static native FontFace create(String family, String source, JSObject descriptors);

  @JSBody(
      params = {"style", "weight"},
      script = "return {style: style, weight: weight};"
  )
  public static native JSObject createDescriptor(String style, int weight);

  public static FontFace createWithStyleWeight(
      String family, String source, String style, int weight
  ) {
    return create(family, source, createDescriptor(style, weight));
  }

  public native Promise<FontFace> load();

  @JSProperty
  public native int getWeight();

  @JSProperty
  public native JSString getStyle();

  @JSBody(
      params = {"doc", "font"},
      script = "doc.fonts.add(font);"
  )
  public static native void addToDocument(HTMLDocument doc, FontFace font);

  public void addToDocument() {
    addToDocument(HTMLDocument.current(), this);
  }

  public static Promise<JSArrayReader<JSObject>> loadFonts(FontConfigJs[] fonts) {
    JSArray<Promise<?>> array = JSArray.create();
    for (FontConfigJs font : fonts) {
      var ff = FontFace.createWithStyleWeight(
          font.family, "url(" + font.file + ')',
          font.style, font.weight
      );
      array.push(ff.load());
    }
    return Promise.all(array);
  }
}

