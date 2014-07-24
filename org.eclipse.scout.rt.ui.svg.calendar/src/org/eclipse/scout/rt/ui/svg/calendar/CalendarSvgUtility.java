package org.eclipse.scout.rt.ui.svg.calendar;

import java.awt.Color;
import java.util.ArrayList;

import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.commons.StringUtility;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGStylable;

public final class CalendarSvgUtility {
  public static final String COLOR_PREFIX = "#";

  private static String ensureColorPrefix(String color) {
    if (!color.startsWith(COLOR_PREFIX)) {
      color = COLOR_PREFIX + color;
    }
    return color;
  }

  public static void clearChildNodes(Element e) {
    Node n = null;
    while ((n = e.getFirstChild()) != null) {
      e.removeChild(n);
    }
  }

  public static void setCalendarDisplayModeXPos(Element e, float x) {
    // y pos (465.908) as defined in the svg file
    setTextPos(e, x, 465.908f);
  }

  public static void setTextPos(Element e, float x, float y) {
    e.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "matrix(1 0 0 1 " + x + " " + y + ")");
  }

  public static void setBorderMiter(Element e, int miter) {
    SVGStylable css = (SVGStylable) e;
    css.getStyle().setProperty(SVGConstants.CSS_STROKE_MITERLIMIT_PROPERTY, "" + miter, "");
  }

  public static void setFont(Element element, String font) {
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.CSS_FONT_FAMILY_PROPERTY, font, "");
  }

  public static void removeBorder(Element element) {
    SVGStylable css = (SVGStylable) element;
    css.getStyle().removeProperty(SVGConstants.SVG_STROKE_ATTRIBUTE);
    css.getStyle().removeProperty(SVGConstants.CSS_STROKE_MITERLIMIT_PROPERTY);
  }

  public static void setFontWeightBold(Element element) {
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.CSS_FONT_WEIGHT_PROPERTY, SVGConstants.SVG_BOLDER_VALUE, "");
  }

  public static void setFontWeightNormal(Element element) {
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.CSS_FONT_WEIGHT_PROPERTY, SVGConstants.SVG_NORMAL_VALUE, "");
  }

  public static void setFontSize(Element element, float size) {
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.CSS_FONT_SIZE_PROPERTY, "" + size, "");
  }

  public static void setFontColor(Element element, String color, boolean darken) {
    if (darken) {
      color = getDarkerCopy(color);
    }
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.CSS_FILL_PROPERTY, ensureColorPrefix(color), "");
  }

  public static void setFontColor(Element element, String color) {
    setFontColor(element, color, false);
  }

  public static void setTextAlignCenter(Element element) {
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_MIDDLE_VALUE, "");
  }

  public static void setTextAlignRight(Element element) {
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_END_VALUE, "");
  }

  public static void setBorderColor(Element element, String color) {
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.SVG_STROKE_ATTRIBUTE, ensureColorPrefix(color), "");
  }

  public static void clearBackgroundColor(Element element) {
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.CSS_FILL_PROPERTY, SVGConstants.SVG_NONE_VALUE, "");
  }

  public static void setBackgroundColor(Element element, String color) {
    setBackgroundColor(element, color, false);
  }

  public static void setBackgroundColor(Element element, String color, boolean darken) {
    if (darken) {
      color = getDarkerCopy(color);
    }
    SVGStylable css = (SVGStylable) element;
    css.getStyle().setProperty(SVGConstants.CSS_FILL_PROPERTY, ensureColorPrefix(color), "");
  }

  public static Element[] getAllChildElements(Element root, String tagName) {
    ArrayList<Element> collector = new ArrayList<Element>();
    getAllChildElementsRec(root, tagName, collector);
    return collector.toArray(new Element[collector.size()]);
  }

  private static String getDarkerCopy(String color) {
    Color orig = Color.decode(ensureColorPrefix(color)).darker();
    return COLOR_PREFIX + colorToHexString(orig);
  }

  private static void getAllChildElementsRec(Element root, String tagName, ArrayList<Element> collector) {
    for (int i = 0; i < root.getChildNodes().getLength(); i++) {
      Node childNode = root.getChildNodes().item(i);
      if (childNode instanceof Element) {
        Element child = (Element) childNode;
        if (child.getNodeName().equals(tagName)) {
          collector.add(child);
        }
        getAllChildElementsRec(child, tagName, collector);
      }
    }
  }

  private static String colorToHexString(Color c) {
    if (c == null) {
      return null;
    }
    int rgb = c.getRGB() & 0xffffff;
    return StringUtility.lpad(Integer.toHexString(rgb), "0", 6);
  }
}
