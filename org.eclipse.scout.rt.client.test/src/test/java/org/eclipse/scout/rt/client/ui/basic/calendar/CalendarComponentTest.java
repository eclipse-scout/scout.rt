/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.shared.services.common.calendar.CalendarAppointment;
import org.eclipse.scout.rt.shared.services.common.calendar.HolidayItem;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarAppointment;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link CalendarComponent}
 *
 * @author Adrian Moser
 */
public class CalendarComponentTest {
  private static final Date DATE_1 = DateUtility.parse("01.07.2012", "dd.MM.yyyy");
  private static final Date DATE_2 = DateUtility.parse("02.07.2012", "dd.MM.yyyy");

  @Test
  public void testOrder1() throws Exception {
    CalendarComponent comp1 = createComponent(createItem("A"));
    CalendarComponent comp2 = createComponent(createItem("B"));
    Set<CalendarComponent> set = createSet(comp1, comp2);

    Assert.assertEquals("Order", comp1, toArray(set)[0]);
    Assert.assertEquals("Order", comp2, toArray(set)[1]);
  }

  private CalendarComponent[] toArray(Set<CalendarComponent> set) {
    return set.toArray(new CalendarComponent[set.size()]);
  }

  @Test
  public void testOrder2() throws Exception {
    CalendarComponent comp1 = createComponent(createItem("B"));
    CalendarComponent comp2 = createComponent(createItem("A"));
    Set<CalendarComponent> set = createSet(comp1, comp2);

    Assert.assertEquals("Order", comp2, toArray(set)[0]);
    Assert.assertEquals("Order", comp1, toArray(set)[1]);
  }

  @Test
  public void testOrder3() throws Exception {
    CalendarComponent comp1 = createComponent(createItem("C"));
    CalendarComponent comp2 = createComponent(createItem("C"));
    Set<CalendarComponent> set = createSet(comp1, comp2);

    Assert.assertEquals("Count", 2, set.size());
  }

  @Test
  public void testDateOrder1() throws Exception {
    CalendarComponent comp1 = createComponent(createItem(DATE_1, "B"));
    CalendarComponent comp2 = createComponent(createItem("A"));
    Set<CalendarComponent> set = createSet(comp1, comp2);

    Assert.assertEquals("Order", comp1, toArray(set)[0]);
    Assert.assertEquals("Order", comp2, toArray(set)[1]);
  }

  @Test
  public void testDateOrder2() throws Exception {
    CalendarComponent comp1 = createComponent(createItem(DATE_2, "B"));
    CalendarComponent comp2 = createComponent(createItem(DATE_1, "A"));
    Set<CalendarComponent> set = createSet(comp1, comp2);

    Assert.assertEquals("Order", comp2, toArray(set)[0]);
    Assert.assertEquals("Order", comp1, toArray(set)[1]);
  }

  @Test
  public void testDateOrder3() throws Exception {
    CalendarComponent comp1 = createComponent(createItem(DATE_2, null));
    CalendarComponent comp2 = createComponent(createItem(DATE_2, null));
    Set<CalendarComponent> set = createSet(comp1, comp2);

    Assert.assertEquals("Count", 2, set.size());
  }

  @Test
  public void testCoveredDayRange1() throws Exception {
    ICalendarAppointment appointment = new CalendarAppointment(UUID.randomUUID(), null, DATE_1, DATE_2, false, "Subject", "Body", null);
    CalendarComponent component = createComponent(appointment);
    Range<Date> coveredDayRange = component.getCoveredDaysRange();
    assertNotNull(coveredDayRange);
    assertEquals(DateUtility.truncDate(DATE_1), coveredDayRange.getFrom());
    assertEquals(DateUtility.truncDate(DATE_2), coveredDayRange.getTo());
  }

  @Test
  public void testCoveredDayRange2() throws Exception {
    ICalendarAppointment appointment = new CalendarAppointment(UUID.randomUUID(), null, DATE_1, null, false, "Subject", "Body", null);
    CalendarComponent component = createComponent(appointment);
    Range<Date> coveredDayRange = component.getCoveredDaysRange();
    assertNotNull(coveredDayRange);
    assertEquals(DateUtility.truncDate(DATE_1), coveredDayRange.getFrom());
    assertEquals(DateUtility.truncDate(DATE_1), coveredDayRange.getTo());
  }

  @Test
  public void testCoveredDayRange3() throws Exception {
    ICalendarAppointment appointment = new CalendarAppointment(UUID.randomUUID(), null, null, DATE_2, false, "Subject", "Body", null);
    CalendarComponent component = createComponent(appointment);
    Range<Date> coveredDayRange = component.getCoveredDaysRange();
    assertNotNull(coveredDayRange);
    assertEquals(DateUtility.truncDate(DATE_2), coveredDayRange.getFrom());
    assertEquals(DateUtility.truncDate(DATE_2), coveredDayRange.getTo());
  }

  private CalendarComponent createComponent(ICalendarItem item) {
    return new CalendarComponent(null, null, item);
  }

  private ICalendarItem createItem(Date date, String subject) {
    HolidayItem item = new HolidayItem();
    item.setSubject(subject);
    item.setItemId(UUID.randomUUID());
    item.setStart(date);
    return item;
  }

  private ICalendarItem createItem(String subject) {
    return createItem(DATE_2, subject);
  }

  private Set<CalendarComponent> createSet(CalendarComponent... calendarComponents) {
    Set<CalendarComponent> set = new TreeSet<CalendarComponent>();
    for (CalendarComponent component : calendarComponents) {
      set.add(component);
    }
    return set;
  }

}
