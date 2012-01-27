package org.eclipse.scout.rt.ui.svg.calendar.comp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.w3c.dom.Element;

/**
 * Factory that creates elements and spreads them equally inside the container.
 * Each element uses the full width.
 */
public class LinearFillComponentElementFactory extends AbstractComponentElementFactory {

  private final static float PADDING = 1.0f;

  @Override
  public Map<CalendarComponent, Element> create(Element container, Date day, CalendarComponent[] components) {
    HashMap<CalendarComponent, Element> ret = new HashMap<CalendarComponent, Element>(components.length);
    SvgRect containerDimension = getElementDimensions(container);

    // create new component rectangles for each component in the given parent (day)
    for (int i = 0; i < components.length; i++) {
      Element e = createComponentRectangle(container, containerDimension, components[i], i, components.length, day);
      ret.put(components[i], e);
    }
    return ret;
  }

  private Element createComponentRectangle(Element container, SvgRect containerDimension, CalendarComponent c, int index, int numComponents, Date day) {
    Element newEl = createNewComponentElement(container, c, day);

    SvgRect elDimension = new SvgRect();
    elDimension.height = containerDimension.height / numComponents;
    elDimension.width = containerDimension.width;
    elDimension.y = containerDimension.y + (elDimension.height * index);
    elDimension.x = containerDimension.x;

    setElementDimensions(newEl, getCopyWithPadding(elDimension, PADDING));

    return newEl;
  }
}
