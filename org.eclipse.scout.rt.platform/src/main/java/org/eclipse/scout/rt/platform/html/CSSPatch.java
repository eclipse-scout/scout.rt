/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.html;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Hashtable;

import javax.swing.text.StyleContext;
import javax.swing.text.html.CSS;
import javax.swing.text.html.CSS.Attribute;

import org.eclipse.scout.rt.platform.reflect.ReflectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Patch for CSS class. The patch is applied as soon as this class is accessed.
 *
 * @deprecated This legacy utility will be removed in Scout 7. Use {@link org.eclipse.scout.rt.platform.html.HTML} and
 *             {@link HtmlHelper} instead.
 */
@Deprecated
public final class CSSPatch {
  private static final Logger LOG = LoggerFactory.getLogger(CSSPatch.class);

  private CSSPatch() {
  }

  public static final Attribute AZIMUTH = createAttribute("azimuth", null, false);
  public static final Attribute BORDER_BOTTOM_COLOR = createAttribute("border-bottom-color", null, false);
  public static final Attribute BORDER_BOTTOM_STYLE = createAttribute("border-bottom-style", null, false);
  public static final Attribute BORDER_COLLAPSE = createAttribute("border-collapse", null, false);
  public static final Attribute BORDER_LEFT_COLOR = createAttribute("border-left-color", null, false);
  public static final Attribute BORDER_LEFT_STYLE = createAttribute("border-left-style", null, false);
  public static final Attribute BORDER_RIGHT_COLOR = createAttribute("border-right-color", null, false);
  public static final Attribute BORDER_RIGHT_STYLE = createAttribute("border-right-style", null, false);
  public static final Attribute BORDER_SPACING = createAttribute("border-spacing", null, false);
  public static final Attribute BORDER_TOP_COLOR = createAttribute("border-top-color", null, false);
  public static final Attribute BORDER_TOP_STYLE = createAttribute("border-top-style", null, false);
  public static final Attribute BOTTOM = createAttribute("bottom", null, false);
  public static final Attribute CAPTION_SIDE = createAttribute("caption-side", null, false);
  public static final Attribute CLIP = createAttribute("clip", null, false);
  public static final Attribute CONTENT = createAttribute("content", null, false);
  public static final Attribute COUNTER_INCREMENT = createAttribute("counter-increment", null, false);
  public static final Attribute COUNTER_RESET = createAttribute("counter-reset", null, false);
  public static final Attribute CUE = createAttribute("cue", null, false);
  public static final Attribute CUE_AFTER = createAttribute("cue-after", null, false);
  public static final Attribute CUE_BEFORE = createAttribute("cue-before", null, false);
  public static final Attribute CURSOR = createAttribute("cursor", null, false);
  public static final Attribute DIRECTION = createAttribute("direction", null, false);
  public static final Attribute ELEVATION = createAttribute("elevation", null, false);
  public static final Attribute EMPTY_CELLS = createAttribute("empty-cells", null, false);
  public static final Attribute LEFT = createAttribute("left", null, false);
  public static final Attribute MAX_HEIGHT = createAttribute("max-height", null, false);
  public static final Attribute MAX_WIDTH = createAttribute("max-width", null, false);
  public static final Attribute MIN_HEIGHT = createAttribute("min-height", null, false);
  public static final Attribute MIN_WIDTH = createAttribute("min-width", null, false);
  public static final Attribute ORPHANS = createAttribute("orphans", null, false);
  public static final Attribute OUTLINE = createAttribute("outline", null, false);
  public static final Attribute OUTLINE_COLOR = createAttribute("outline-color", null, false);
  public static final Attribute OUTLINE_STYLE = createAttribute("outline-style", null, false);
  public static final Attribute OUTLINE_WIDTH = createAttribute("outline-width", null, false);
  public static final Attribute OVERFLOW = createAttribute("overflow", null, false);
  public static final Attribute PAGE_BREAK_AFTER = createAttribute("page-break-after", null, false);
  public static final Attribute PAGE_BREAK_BEFORE = createAttribute("page-break-before", null, false);
  public static final Attribute PAGE_BREAK_INSIDE = createAttribute("page-break-inside", null, false);
  public static final Attribute PAUSE = createAttribute("pause", null, false);
  public static final Attribute PAUSE_AFTER = createAttribute("pause-after", null, false);
  public static final Attribute PAUSE_BEFORE = createAttribute("pause-before", null, false);
  public static final Attribute PITCH = createAttribute("pitch", null, false);
  public static final Attribute PITCH_RANGE = createAttribute("pitch-range", null, false);
  public static final Attribute PLAY_DURING = createAttribute("play-during", null, false);
  public static final Attribute POSITION = createAttribute("position", null, false);
  public static final Attribute QUOTES = createAttribute("quotes", null, false);
  public static final Attribute RICHNESS = createAttribute("richness", null, false);
  public static final Attribute RIGHT = createAttribute("right", null, false);
  public static final Attribute SPEAK = createAttribute("speak", null, false);
  public static final Attribute SPEAK_HEADER = createAttribute("speak-header", null, false);
  public static final Attribute SPEAK_NUMERAL = createAttribute("speak-numeral", null, false);
  public static final Attribute SPEAK_PUNCTUATION = createAttribute("speak-punctuation", null, false);
  public static final Attribute SPEECH_RATE = createAttribute("speech-rate", null, false);
  public static final Attribute STRESS = createAttribute("stress", null, false);
  public static final Attribute TABLE_LAYOUT = createAttribute("table-layout", null, false);
  public static final Attribute TOP = createAttribute("top", null, false);
  public static final Attribute UNICODE_BIDI = createAttribute("unicode-bidi", null, false);
  public static final Attribute VISIBILITY = createAttribute("visibility", null, false);
  public static final Attribute VOICE_FAMILY = createAttribute("voice-family", null, false);
  public static final Attribute VOLUME = createAttribute("volume", null, false);
  public static final Attribute WIDOWS = createAttribute("widows", null, false);
  public static final Attribute Z_INDEX = createAttribute("z-index", null, false);

  public static void apply() {
    //dummy method to kick class initialization
  }

  static {
    try {
      patchCSSClass();
    }
    catch (Throwable t) {
      LOG.error("Failed patching CSS class", t);
    }
  }

  @SuppressWarnings("unchecked")
  private static void patchCSSClass() throws Throwable {
    Attribute[] additionalAttributes = new Attribute[]{
        AZIMUTH,
        BORDER_BOTTOM_COLOR,
        BORDER_BOTTOM_STYLE,
        BORDER_COLLAPSE,
        BORDER_LEFT_COLOR,
        BORDER_LEFT_STYLE,
        BORDER_RIGHT_COLOR,
        BORDER_RIGHT_STYLE,
        BORDER_SPACING,
        BORDER_TOP_COLOR,
        BORDER_TOP_STYLE,
        BOTTOM,
        CAPTION_SIDE,
        CLIP,
        CONTENT,
        COUNTER_INCREMENT,
        COUNTER_RESET,
        CUE,
        CUE_AFTER,
        CUE_BEFORE,
        CURSOR,
        DIRECTION,
        ELEVATION,
        EMPTY_CELLS,
        LEFT,
        MAX_HEIGHT,
        MAX_WIDTH,
        MIN_HEIGHT,
        MIN_WIDTH,
        ORPHANS,
        OUTLINE,
        OUTLINE_COLOR,
        OUTLINE_STYLE,
        OUTLINE_WIDTH,
        OVERFLOW,
        PAGE_BREAK_AFTER,
        PAGE_BREAK_BEFORE,
        PAGE_BREAK_INSIDE,
        PAUSE,
        PAUSE_AFTER,
        PAUSE_BEFORE,
        PITCH,
        PITCH_RANGE,
        PLAY_DURING,
        POSITION,
        QUOTES,
        RICHNESS,
        RIGHT,
        SPEAK,
        SPEAK_HEADER,
        SPEAK_NUMERAL,
        SPEAK_PUNCTUATION,
        SPEECH_RATE,
        STRESS,
        TABLE_LAYOUT,
        TOP,
        UNICODE_BIDI,
        VISIBILITY,
        VOICE_FAMILY,
        VOLUME,
        WIDOWS,
        Z_INDEX,
    };
    for (Attribute key : additionalAttributes) {
      if (key == null) {
        throw new Exception("Attribute is null");
      }
    }
    //extends CSS.Attribute.allAttributes
    Field f = Attribute.class.getDeclaredField("allAttributes");
    ReflectionUtility.removeFinalFlagOnField(f);
    f.setAccessible(true);
    Attribute[] allAttributes = (Attribute[]) f.get(null);
    Attribute[] newArray = new Attribute[allAttributes.length + additionalAttributes.length];
    System.arraycopy(allAttributes, 0, newArray, 0, allAttributes.length);
    System.arraycopy(additionalAttributes, 0, newArray, allAttributes.length, additionalAttributes.length);
    f.set(null, newArray);
    //extends CSS.attributeMap
    f = CSS.class.getDeclaredField("attributeMap");
    f.setAccessible(true);
    Hashtable attributeMap = (Hashtable) f.get(null);
    for (Attribute key : additionalAttributes) {
      attributeMap.put(key.toString(), key);
    }
    //extends StyleContext
    for (Attribute key : additionalAttributes) {
      try {
        StyleContext.registerStaticAttributeKey(key);
      }
      catch (Throwable t) {
        LOG.error("Failed registering CSS.Attribute '{}' on StyleContext", key, t);
      }
    }
  }

  private static Attribute createAttribute(String s1, String s2, boolean b) {
    try {
      Constructor<Attribute> c = Attribute.class.getDeclaredConstructor(String.class, String.class, boolean.class);
      c.setAccessible(true);
      return c.newInstance(s1, s2, b);
    }
    catch (Throwable t) {
      LOG.error("Failed patching CSS by adding key '{}'", s1, t);
    }
    return null;
  }
}
