package org.eclipse.scout.rt.ui.svg.calendar.comp;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.ui.svg.calendar.CalendarSvgUtility;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.Element;

public abstract class AbstractComponentElementFactory implements IComponentElementFactory {

  private final AtomicInteger m_componentCounter = new AtomicInteger();
  private CalendarComponent m_selectedComponent;

  @Override
  public final void setSelectedComponent(CalendarComponent c) {
    m_selectedComponent = c;
  }

  protected final CalendarComponent getSelectedComponent() {
    return m_selectedComponent;
  }

  protected static class SvgRect {
    protected float x, y, width, height;
  }

  protected static void setElementDimensions(Element e, SvgRect dimensions) {
    e.setAttribute(SVGConstants.SVG_X_ATTRIBUTE, "" + dimensions.x);
    e.setAttribute(SVGConstants.SVG_Y_ATTRIBUTE, "" + dimensions.y);
    e.setAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE, "" + dimensions.width);
    e.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, "" + dimensions.height);
  }

  protected static SvgRect getCopyWithPadding(SvgRect rect, float padding) {
    SvgRect ret = new SvgRect();
    ret.x = rect.x + padding;
    ret.y = rect.y + padding;
    ret.width = rect.width - (2 * padding);
    ret.height = rect.height - (2 * padding);
    return ret;
  }

  protected Element createNewComponentElement(Element template, CalendarComponent c, Date day) {
    Element newEl = (Element) template.cloneNode(false);

    // Tooltip
    Element title = template.getOwnerDocument().createElementNS(SVGUtility.SVG_NS, SVGConstants.SVG_TITLE_TAG);
    SVGUtility.setTextContent(title, c.getTooltip(day));
    newEl.appendChild(title);

    // ID
    newEl.setAttribute(SVGConstants.SVG_ID_ATTRIBUTE, "comp" + m_componentCounter.incrementAndGet());

    // background and border
    CalendarSvgUtility.removeBorder(newEl);
    CalendarSvgUtility.setBackgroundColor(newEl, c.getItem().getColor(), getSelectedComponent() == c);

    return newEl;
  }

  protected static SvgRect getElementDimensions(Element e) {
    SvgRect ret = new SvgRect();

    ret.x = Float.parseFloat(e.getAttribute(SVGConstants.SVG_X_ATTRIBUTE));
    ret.y = Float.parseFloat(e.getAttribute(SVGConstants.SVG_Y_ATTRIBUTE));
    ret.width = Float.parseFloat(e.getAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE));
    ret.height = Float.parseFloat(e.getAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE));

    return ret;
  }
}
