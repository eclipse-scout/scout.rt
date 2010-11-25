/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;

/**
 * Default implementation of a calendar service delivering holiday items <br>
 * Produces data using XML resource file XML records can be recurring (with year
 * 0000) and/or single entries (with specific year)
 * 
 * <pre>
 * @code <holidays>
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
@Priority(-1)
public class HolidayCalendarItemParser {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(HolidayCalendarItemParser.class);
  private static final String HOLIDAYS = "holidays";
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
  private SimpleXmlElement m_xml;

  public HolidayCalendarItemParser(URL xmlResource) throws ProcessingException {
    m_xml = new SimpleXmlElement();
    if (xmlResource != null) {
      try {
        m_xml.parseStream(xmlResource.openStream());
      }
      catch (Throwable t) {
        throw new ProcessingException("loading " + xmlResource, t);
      }
    }
  }

  public HolidayCalendarItemParser(InputStream xmlResource, String displayFileName) throws ProcessingException {
    m_xml = new SimpleXmlElement();
    if (xmlResource != null) {
      try {
        m_xml.parseStream(xmlResource);
      }
      catch (Throwable t) {
        throw new ProcessingException("loading " + displayFileName, t);
      }
    }
  }

  public ICalendarItem[] getItems(Locale loc, Date minDate, Date maxDate) throws ProcessingException {
    ArrayList<HolidayItem> itemList = new ArrayList<HolidayItem>();
    int startYear, endYear;
    Calendar cal = Calendar.getInstance();
    cal.setTime(minDate);
    startYear = cal.get(Calendar.YEAR);
    cal.setTime(maxDate);
    endYear = cal.get(Calendar.YEAR);
    for (int year = startYear; year <= endYear; year++) {
      addHolidays(loc, year, itemList);
    }
    return itemList.toArray(new HolidayItem[itemList.size()]);
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
    for (Iterator holidayIt = m_xml.getChildren(HOLIDAY).iterator(); holidayIt.hasNext();) {
      SimpleXmlElement holidayElem = (SimpleXmlElement) holidayIt.next();
      try {
        Date d = evaluateHolidayDate(holidayElem, holidayMap, year);
        if (d != null) {
          // find correct text
          String text = getAttributeByLocale(holidayElem, locPatterns, TEXT);
          String tooltip = getAttributeByLocale(holidayElem, locPatterns, TOOLTIP);
          //
          String itemId = holidayElem.getStringAttribute(ID, null);
          if (itemId == null) {
            itemId = "" + index;
          }
          HolidayItem item = new HolidayItem();
          item.setStart(d);
          item.setSubject(text);
          item.setBody(tooltip);
          item.setColor(holidayElem.getStringAttribute(COLOR));
          index++;
          holidayMap.put(itemId, item);
        }
      }
      catch (Exception e) {
        LOG.warn("item " + holidayElem.toString(), e);
      }
    }
    newList.addAll(holidayMap.values());
  }

  private Date evaluateHolidayDate(SimpleXmlElement holidayElem, Map<String, HolidayItem> holidayMap, int year) throws ParseException {
    String datePattern = holidayElem.getStringAttribute(DATE);
    String weekdayPattern = holidayElem.getStringAttribute(WEEKDAY);
    String instancePattern = holidayElem.getStringAttribute(INSTANCE);
    String relativeToId = holidayElem.getStringAttribute(RELATIVE_TO, null);
    //
    int weekday = -1;
    if (weekdayPattern != null) {
      if (weekdayPattern.equalsIgnoreCase("MONDAY")) weekday = Calendar.MONDAY;
      else if (weekdayPattern.equalsIgnoreCase("TUESDAY")) weekday = Calendar.TUESDAY;
      else if (weekdayPattern.equalsIgnoreCase("WEDNESDAY")) weekday = Calendar.WEDNESDAY;
      else if (weekdayPattern.equalsIgnoreCase("THURSDAY")) weekday = Calendar.THURSDAY;
      else if (weekdayPattern.equalsIgnoreCase("FRIDAY")) weekday = Calendar.FRIDAY;
      else if (weekdayPattern.equalsIgnoreCase("SATURDAY")) weekday = Calendar.SATURDAY;
      else if (weekdayPattern.equalsIgnoreCase("SUNDAY")) weekday = Calendar.SUNDAY;
    }
    int instance = 0;
    if (instancePattern != null) {
      if (instancePattern.equalsIgnoreCase("FIRST")) instance = 1;
      else if (instancePattern.equalsIgnoreCase("LAST")) instance = -1;
      else instance = Integer.parseInt(instancePattern);
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
            if (n == 0) break;
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
            if (n == 0) break;
          }
          cal.add(Calendar.DATE, -1);
        }
      }
    }
    startDate = cal.getTime();
    return startDate;
  }

  private String getAttributeByLocale(SimpleXmlElement e, String[] locPatterns, String attributeNamePrefix) {
    String s = null;
    for (int i = 0; i < locPatterns.length; i++) {
      s = e.getStringAttribute(attributeNamePrefix + "_" + locPatterns[i], null);
      if (s != null) break;
    }
    return s;
  }

}
