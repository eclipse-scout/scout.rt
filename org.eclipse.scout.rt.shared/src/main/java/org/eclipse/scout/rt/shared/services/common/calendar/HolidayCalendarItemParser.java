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
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Default implementation of a calendar service delivering holiday items <br>
 * Produces data using XML resource file XML records can be recurring (with year 0000) and/or single entries (with
 * specific year)
 *
 * <pre>
 * &#64;code <holidays>
 *   <!--1st april every year-->
 *   <holiday date="01.04.0000" color="RRGGBB" text_de="..." tooltip_de="..." text_en="..." tooltip_en="..."/>
 *   <!--1st april only in 2005-->
 *   <holiday date="01.04.2006" color="RRGGBB" text_de="..." tooltip_de="..." text_en="..." tooltip_en="..."/>
 *   <!--every 3rd monday in april-->
 *   <holiday date="01.04.0000" weekday="Monday" instance="3" color="RRGGBB" text_de="..." tooltip_de="..." text_en="..." tooltip_en="..."/>
 *   <!--every last monday in april-->
 *   <holiday date="01.04.0000" weekday="Monday" instance="last" color="RRGGBB" text_de="..." tooltip_de="..." text_en="..." tooltip_en="..."/>
 *   <!--second sunday after easter-->
 *   <holiday id="easter" date="01.04.0000" weekday="Sunday" instance="2" color="RRGGBB" text_de="Easter" tooltip_de="..." text_en="..." tooltip_en="..."/>
 *   <holiday id="pfingsten" relativeTo="easter" weekday="Sunday" instance="2" color="RRGGBB" text_de="..." tooltip_de="..." text_en="..." tooltip_en="..."/>
 * </holidays>
 * }
 * </pre>
 */
public class HolidayCalendarItemParser {
  private static final Logger LOG = LoggerFactory.getLogger(HolidayCalendarItemParser.class);

  private static final String HOLIDAY = "holiday";
  private static final String DATE = "date";
  private static final String WEEKDAY = "weekday";
  private static final String INSTANCE = "instance";
  private static final String COLOR = "color";
  private static final String TEXT = "text";
  private static final String TOOLTIP = "tooltip";
  private static final String ID = "id";
  private static final String RELATIVE_TO = "relativeTo";

  private SimpleDateFormat m_dateFormat = new SimpleDateFormat("dd.MM.yyyy");
  private final Element m_xml;

  public HolidayCalendarItemParser(URL xmlResource) {
    m_xml = XmlUtility.getXmlDocument(xmlResource).getDocumentElement();
  }

  public HolidayCalendarItemParser(InputStream xmlResource, String displayFileName) {
    m_xml = XmlUtility.getXmlDocument(xmlResource).getDocumentElement();
  }

  public Set<? extends ICalendarItem> getItems(Locale loc, Date minDate, Date maxDate) {
    Set<HolidayItem> itemList = new HashSet<HolidayItem>();
    int startYear, endYear;
    Calendar cal = Calendar.getInstance();
    cal.setTime(minDate);
    startYear = cal.get(Calendar.YEAR);
    cal.setTime(maxDate);
    endYear = cal.get(Calendar.YEAR);
    // load all holidays of the given years
    for (int year = startYear; year <= endYear; year++) {
      addHolidays(loc, year, itemList);
    }
    // remove all the holidays lying before minDate or after maxDate
    Iterator<HolidayItem> iter = itemList.iterator();
    while (iter.hasNext()) {
      HolidayItem item = iter.next();
      if (minDate.after(item.getStart()) || maxDate.before(item.getStart())) {
        iter.remove();
      }
    }
    return itemList;
  }

  private void addHolidays(Locale loc, int year, Collection<HolidayItem> newList) {
    HashMap<String/* id */, HolidayItem> holidayMap = new HashMap<String, HolidayItem>();
    // prepare locale patterns
    String[] locPatterns = new String[]{
        loc.getCountry() + "_" + loc.getLanguage() + "_" + loc.getVariant(),
        loc.getCountry() + "_" + loc.getLanguage(),
        loc.getLanguage(),
    };
    long index = 1;

    for (Element holidayElem : XmlUtility.getChildElements(m_xml, HOLIDAY)) {
      try {
        Date d = evaluateHolidayDate(holidayElem, holidayMap, year);
        if (d != null) {
          // find correct text
          String text = getAttributeByLocale(holidayElem, locPatterns, TEXT);
          String tooltip = getAttributeByLocale(holidayElem, locPatterns, TOOLTIP);
          String itemId = null;
          if (holidayElem.hasAttribute(ID)) {
            itemId = holidayElem.getAttribute(ID);
          }
          if (itemId == null) {
            itemId = "" + index;
          }
          HolidayItem item = new HolidayItem();
          item.setStart(d);
          item.setSubject(text);
          item.setBody(tooltip);
          if (holidayElem.hasAttribute(COLOR)) {
            item.setColor(holidayElem.getAttribute(COLOR));
          }
          index++;
          holidayMap.put(itemId, item);
        }
      }
      catch (Exception e) {
        LOG.warn("Could not parse item '{}'", holidayElem, e);
      }
    }
    newList.addAll(holidayMap.values());
  }

  private Date evaluateHolidayDate(Element holidayElem, Map<String, HolidayItem> holidayMap, int year) throws ParseException {
    String datePattern = null;
    if (holidayElem.hasAttribute(DATE)) {
      datePattern = holidayElem.getAttribute(DATE);
    }

    String weekdayPattern = null;
    if (holidayElem.hasAttribute(WEEKDAY)) {
      weekdayPattern = holidayElem.getAttribute(WEEKDAY);
    }

    String instancePattern = null;
    if (holidayElem.hasAttribute(INSTANCE)) {
      instancePattern = holidayElem.getAttribute(INSTANCE);
    }
    String relativeToId = null;
    if (holidayElem.hasAttribute(RELATIVE_TO)) {
      relativeToId = holidayElem.getAttribute(RELATIVE_TO);
    }
    //
    int weekday = -1;
    if (weekdayPattern != null) {
      if ("MONDAY".equalsIgnoreCase(weekdayPattern)) {
        weekday = Calendar.MONDAY;
      }
      else if ("TUESDAY".equalsIgnoreCase(weekdayPattern)) {
        weekday = Calendar.TUESDAY;
      }
      else if ("WEDNESDAY".equalsIgnoreCase(weekdayPattern)) {
        weekday = Calendar.WEDNESDAY;
      }
      else if ("THURSDAY".equalsIgnoreCase(weekdayPattern)) {
        weekday = Calendar.THURSDAY;
      }
      else if ("FRIDAY".equalsIgnoreCase(weekdayPattern)) {
        weekday = Calendar.FRIDAY;
      }
      else if ("SATURDAY".equalsIgnoreCase(weekdayPattern)) {
        weekday = Calendar.SATURDAY;
      }
      else if ("SUNDAY".equalsIgnoreCase(weekdayPattern)) {
        weekday = Calendar.SUNDAY;
      }
    }
    int instance = 0;
    if (instancePattern != null) {
      if ("FIRST".equalsIgnoreCase(instancePattern)) {
        instance = 1;
      }
      else if ("LAST".equalsIgnoreCase(instancePattern)) {
        instance = -1;
      }
      else if (StringUtility.hasText(instancePattern)) {
        // tag might be empty instead of null
        instance = Integer.parseInt(instancePattern);
      }
    }
    Date startDate = null;
    if (relativeToId != null) {
      HolidayItem relItem = holidayMap.get(relativeToId);
      if (relItem != null) {
        startDate = relItem.getStart();
      }
    }
    if (startDate == null) {
      if (datePattern == null) {
        // not existing
        return null;
      }
      else if (datePattern.endsWith("0000")) {
        // wildcard ok
        startDate = m_dateFormat.parse(datePattern.substring(0, datePattern.length() - 4) + year);
      }
      else if (datePattern.endsWith("" + year)) {
        // correct year
        startDate = m_dateFormat.parse(datePattern);
      }
      else {
        // not matching year
        return null;
      }
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(startDate);
    //
    if (weekday >= 0 && instance != 0) {
      if (instance > 0) {
        // 1st, second, ...
        int n = instance;
        while (n > 0) {
          if (cal.get(Calendar.DAY_OF_WEEK) == weekday) {
            n--;
            if (n == 0) {
              break;
            }
          }
          cal.add(Calendar.DATE, 1);
        }
      }
      else {
        // last, secondlast, ...
        int n = -instance;
        while (n > 0) {
          if (cal.get(Calendar.DAY_OF_WEEK) == weekday) {
            n--;
            if (n == 0) {
              break;
            }
          }
          cal.add(Calendar.DATE, -1);
        }
      }
    }
    startDate = cal.getTime();
    return startDate;
  }

  private String getAttributeByLocale(Element e, String[] locPatterns, String attributeNamePrefix) {
    for (int i = 0; i < locPatterns.length; i++) {
      String attribName = attributeNamePrefix + "_" + locPatterns[i];
      if (e.hasAttribute(attribName)) {
        String s = e.getAttribute(attribName);
        if (s != null) {
          return s;
        }
      }
    }
    return null;
  }

}
