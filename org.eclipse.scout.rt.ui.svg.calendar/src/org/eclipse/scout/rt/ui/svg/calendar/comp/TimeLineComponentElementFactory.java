package org.eclipse.scout.rt.ui.svg.calendar.comp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.batik.util.SVGConstants;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.ui.svg.calendar.builder.AbstractCalendarDocumentBuilder;
import org.eclipse.scout.svg.client.SVGUtility;
import org.w3c.dom.Element;

/**
 * Timeline capable element factory. Creates elements and positions them according to the start and end time vertically
 * in the container.
 * parallel components are displayed by splitting the horizontal space as needed.
 */
public class TimeLineComponentElementFactory extends AbstractComponentElementFactory {
  private static int m_startHour = 7;
  private static int m_endHour = 19;
  private static final float PADDING = 1.5f;

  // defines the minimal height of an calendar element to ensure it can be clicked even if the linear duration scale would be smaller
  private static final float MIN_ELEMENT_HEIGHT = 10.0f;

  // Start value must be higher than the end value to ensure that end events always come first.
  // this is important when calendar component ends at 12:00 and the next starts at 12:00
  // if the start event would be first in the event list, then an overlap of the events would be detected.
  // ensuring that the end event always comes first, allows us to display elements in one column if they happen serialized.
  private static final Integer EVENT_START = new Integer(2);
  private static final Integer EVENT_END = new Integer(1);

  public TimeLineComponentElementFactory(int startHour, int endHour) {
    super();
    m_startHour = startHour;
    m_endHour = endHour;
  }

  /**
   * helper class to store meta data to a calendar component.
   */
  private static class CalendarComponentComposite {
    private Integer index; // defines in which column index the element will be positioned.
    private CalendarComponent comp;
    private Calendar start, end;

    private CalendarComponentComposite(CalendarComponent c, Calendar s, Calendar e) {
      comp = c;
      start = s;
      end = e;
    }
  }

  @Override
  public Map<CalendarComponent, Element> create(Element container, Date day, CalendarComponent[] components) {
    // create list of all events. an event is a start or end date of a calendar component.
    // the list is sorted ascending by the date of the event.
    TreeMap<CompositeObject, CalendarComponentComposite> events = new TreeMap<CompositeObject, CalendarComponentComposite>();
    HashSet<CalendarComponentComposite> list = new HashSet<CalendarComponentComposite>(components.length);
    int numFullDay = 0;
    for (int i = 0; i < components.length; i++) {
      Calendar start = AbstractCalendarDocumentBuilder.createCalendar(truncateToSingleDay(components[i].getFromDate(), day));
      Calendar end = AbstractCalendarDocumentBuilder.createCalendar(truncateToSingleDay(components[i].getToDate(), day));

      CalendarComponentComposite e = new CalendarComponentComposite(components[i], start, end);
      list.add(e);
      if (components[i].isFullDay()) {
        e.index = numFullDay++;
      }
      else {
        events.put(new CompositeObject(start.getTime(), EVENT_START, i), e);
        events.put(new CompositeObject(end.getTime(), EVENT_END, i), e);
      }
    }

    // calculate indices for all (non-full-day) components
    int maxIndex = calculateIndices(events);

    // get dimension of the container grid box
    SvgRect containerDimension = getElementDimensions(container);

    // create elements for all (non-full-day) calendar components
    HashMap<CalendarComponent, Element> ret = new HashMap<CalendarComponent, Element>(list.size());
    int fullDayIndex = 0;
    for (CalendarComponentComposite c : list) {
      Element e = createComponentElement(container, containerDimension, c, maxIndex + 1, numFullDay, list, day);
      if (c.comp.isFullDay()) {
        fullDayIndex++;
      }
      if (e != null) {
        ret.put(c.comp, e);
      }
    }

    return ret;
  }

  private static int calculateIndices(Map<CompositeObject, CalendarComponentComposite> events) {
    int maxIndex = 0; // stores the highest column index that was used
    HashSet<Integer> usedIndices = new HashSet<Integer>(); // stores all indices currently used
    for (Entry<CompositeObject, CalendarComponentComposite> el : events.entrySet()) { // go through all events and give column indices to the calendar components
      Integer eventType = (Integer) el.getKey().getComponent(1);
      if (eventType.equals(EVENT_START)) {
        // start of new element
        int index = getNextFreeIndex(usedIndices);
        el.getValue().index = index;
        if (index > maxIndex) {
          maxIndex = index;
        }
      }
      else {
        // end of an element
        releaseIndex(usedIndices, (Integer) el.getValue().index);
      }
    }
    return maxIndex;
  }

  private static void releaseIndex(HashSet<Integer> collector, Integer i) {
    collector.remove(i);
  }

  private static Integer getNextFreeIndex(HashSet<Integer> collector) {
    int i = 0;
    while (collector.contains(i)) {
      i++;
    }
    collector.add(i);
    return i;
  }

  private Element createComponentElement(Element container, SvgRect containerDimension, CalendarComponentComposite c,
      int numColumns, int numFullDay, HashSet<CalendarComponentComposite> list, Date day) {

    Element newEl = createNewComponentElement(container, c.comp, day);
    Element composite = container.getOwnerDocument().createElementNS(SVGUtility.SVG_NS, SVGConstants.SVG_G_TAG);

    SvgRect elementDimension = getCopyWithPadding(getTimeLineElementDimension(c, list, containerDimension, numColumns, numFullDay), PADDING);
    setElementDimensions(newEl, elementDimension);

    composite.appendChild(newEl);

    Element txt = createTextElement(c.comp, newEl, elementDimension, day);
    if (txt != null) {
      composite.appendChild(txt);
    }
    return composite;
  }

  private SvgRect getTimeLineElementDimension(CalendarComponentComposite c, HashSet<CalendarComponentComposite> list, SvgRect containerDimension, int numColumns, int numFullDay) {
    float elementWidth, columnWidth;
    if (c.comp.isFullDay()) {
      columnWidth = containerDimension.width / numFullDay;
      elementWidth = columnWidth;
    }
    else {
      columnWidth = containerDimension.width / numColumns;

      // calculate how many columns the current component can span (use available width)
      int extend = getNextBlockIndex(list, c.comp, c.index + 1);
      if (extend < 0) {
        // no more blocks -> use full available width
        extend = numColumns - c.index;
      }
      else {
        extend -= c.index;
      }
      elementWidth = columnWidth * extend;
    }

    float xOffset = c.index * columnWidth;

    // start and end y-offset relative to container position
    float yOffsetStart = getYOffset(c, containerDimension, false);
    float yOffsetEnd = getYOffset(c, containerDimension, true);
    if (yOffsetEnd - MIN_ELEMENT_HEIGHT <= yOffsetStart) {
      yOffsetEnd = yOffsetStart + MIN_ELEMENT_HEIGHT;
    }

    SvgRect elDimension = new SvgRect();
    elDimension.x = containerDimension.x + xOffset;
    elDimension.y = containerDimension.y + yOffsetStart;
    elDimension.width = elementWidth;
    elDimension.height = yOffsetEnd - yOffsetStart;
    return elDimension;
  }

  protected Element createTextElement(CalendarComponent c, Element parent, SvgRect parentDimension, Date day) {
    return null;
  }

  private int getNextBlockIndex(HashSet<CalendarComponentComposite> list, CalendarComponent c, int startIndex) {
    int nextBlockIndex = -1;
    for (CalendarComponentComposite ccc : list) {
      if (!ccc.comp.isFullDay() && ccc.index >= startIndex && DateUtility.intersects(ccc.comp.getFromDate(), ccc.comp.getToDate(), c.getFromDate(), c.getToDate())) {
        if (nextBlockIndex < 0 || nextBlockIndex > ccc.index) {
          nextBlockIndex = ccc.index;
        }
      }
    }
    return nextBlockIndex;
  }

  private static float getYOffset(CalendarComponentComposite d, SvgRect container, boolean isEnd) {

    final int NUM_ELEMENTS = m_endHour - m_startHour + 2;
    final float ELEMENT_HEIGHT = container.height / NUM_ELEMENTS;

    // get time of current element
    int hour, minute;
    if (isEnd) {
      hour = d.end.get(Calendar.HOUR_OF_DAY);
      minute = d.end.get(Calendar.MINUTE);
    }
    else {
      hour = d.start.get(Calendar.HOUR_OF_DAY);
      minute = d.start.get(Calendar.MINUTE);
    }

    if (d.comp.isFullDay()) {
      // FULL DAY
      if (isEnd) {
        return ELEMENT_HEIGHT;
      }
      else {
        return 0;
      }
    }
    else if (hour < m_startHour) {
      // EARLIER
      if (isEnd) {
        return ELEMENT_HEIGHT * 2;
      }
      else {
        return ELEMENT_HEIGHT;
      }
    }
    else if (hour >= m_endHour) {
      // LATER
      if (isEnd) {
        return container.height;
      }
      else {
        return container.height - ELEMENT_HEIGHT;
      }
    }
    else {
      // LINEAR TIME LINE
      final int minutes = ((hour - m_startHour) * 60) + minute;
      final int totalMinutes = (m_endHour - m_startHour) * 60;
      final float timeHeight = container.height - (2 * ELEMENT_HEIGHT);
      return ELEMENT_HEIGHT + (minutes * timeHeight / totalMinutes);
    }
  }

  private static Date truncateToSingleDay(Date d, Date day) {
    day = DateUtility.truncDate(day);
    if (DateUtility.isSameDay(d, day)) {
      return d;
    }
    else if (d.compareTo(day) < 0) {
      return day;
    }
    else {
      return new Date(day.getTime() + 24 * 3600 * 1000 - 1);
    }
  }
}
