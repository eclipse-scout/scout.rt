package org.eclipse.scout.rt.ui.svg.calendar.comp;

import java.util.Date;

import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.ui.svg.calendar.CalendarSvgUtility;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.Element;

/**
 * Time line factory which additionally shows text inside the created element
 */
public class TimeLineTextComponentElementFactory extends TimeLineComponentElementFactory {

  /**
   * @param startHour
   * @param endHour
   */
  public TimeLineTextComponentElementFactory(int startHour, int endHour) {
    super(startHour, endHour);
  }

  private final static float TEXT_Y_OFFSET = 4.0f; // small offset so that it looks better

  @Override
  protected Element createTextElement(CalendarComponent c, Element parent, SvgRect parentDimension, Date day) {
    Element txt = parent.getOwnerDocument().createElementNS(SVGUtility.SVG_NS, SVGConstants.SVG_TEXT_TAG);

    // align text in the middle of the component
    float x = parentDimension.x + (parentDimension.width / 2.0f);
    float y = parentDimension.y + (parentDimension.height / 2.0f) + TEXT_Y_OFFSET;

    SVGUtility.setTextContent(txt, c.getLabel(day));
    CalendarSvgUtility.setTextPos(txt, x, y);
    CalendarSvgUtility.setTextAlignCenter(txt);

    return txt;
  }
}
