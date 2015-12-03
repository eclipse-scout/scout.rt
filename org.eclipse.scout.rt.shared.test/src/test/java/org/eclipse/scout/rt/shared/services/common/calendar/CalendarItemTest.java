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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

/**
 * Test for {@link ICalendarItem}.
 *
 * @since 3.8.3
 */
public class CalendarItemTest {

  private static final Date DATE_1 = new Date();
  private static final Date DATE_2 = new Date(DATE_1.getTime() + 1);
  private static final Date DATE_3 = new Date(DATE_1.getTime() + 2);

  @Test
  public void testCalendarTaskConstruction1() {
    ICalendarTask task = new CalendarTask();
    assertEquals(task.getItemId(), 0L);
    assertNull(task.getResponsible());
    assertNull(task.getStart());
    assertNull(task.getDue());
    assertNull(task.getComplete());
    assertNull(task.getSubject());
    assertNull(task.getBody());
    assertNull(task.getCssClass());
  }

  @Test
  public void testCalendarTaskConstruction2() {
    ICalendarTask task = new CalendarTask(0L, null, DATE_1, DATE_2, DATE_3, "1", "2", "3");
    verifyCalendarTask(task);
  }

  @Test
  public void testCalendarTaskConstruction3() {
    ICalendarTask task = new CalendarTask(new Object[]{0L, null, DATE_1, DATE_2, DATE_3, "1", "2", "3"});
    verifyCalendarTask(task);
  }

  private void verifyCalendarTask(ICalendarTask task) {
    assertEquals("task id", 0L, task.getItemId());
    assertNull(task.getResponsible());
    assertNotNull(task.getStart());
    assertEquals("task start", DATE_1, task.getStart());
    assertNotNull(task.getDue());
    assertEquals("task due", DATE_2, task.getDue());
    assertNotNull(task.getComplete());
    assertEquals("task complete", DATE_3, task.getComplete());
    assertNotNull(task.getSubject());
    assertEquals("task subject", "1", task.getSubject());
    assertNotNull(task.getBody());
    assertEquals("task body", "2", task.getBody());
    assertNotNull(task.getCssClass());
    assertEquals("task color", "3", task.getCssClass());
  }

  @Test
  public void testCalendarAppointmentConstruction1() {
    ICalendarAppointment app = new CalendarAppointment();
    assertEquals("appointment id", 0L, app.getItemId());
    assertNull(app.getPerson());
    assertNull(app.getStart());
    assertNull(app.getEnd());
    assertNull(app.getLocation());
    assertFalse(app.isFullDay());
    assertNull(app.getSubject());
    assertNull(app.getBody());
    assertNull(app.getCssClass());
  }

  @Test
  public void testCalendarAppointmentConstruction2() {
    ICalendarAppointment app = new CalendarAppointment(0L, null, DATE_1, DATE_2, true, "1", "2", "3");
    verifyCalendarAppointment(app);
  }

  @Test
  public void testCalendarAppointmentConstruction3() {
    ICalendarAppointment task = new CalendarAppointment(new Object[]{0L, null, DATE_1, DATE_2, true, "1", "2", "3"});
    verifyCalendarAppointment(task);
  }

  private void verifyCalendarAppointment(ICalendarAppointment app) {
    assertEquals("appointment id", app.getItemId(), 0L);
    assertNull(app.getPerson());
    assertNotNull(app.getStart());
    assertEquals("appointment start", DATE_1, app.getStart());
    assertNotNull(app.getEnd());
    assertEquals("appointment end", DATE_2, app.getEnd());
    assertNull(app.getLocation());
    assertTrue(app.isFullDay());
    assertNotNull(app.getSubject());
    assertEquals("appointment subject", "1", app.getSubject());
    assertNotNull(app.getBody());
    assertEquals("appointment body", "2", app.getBody());
    assertNotNull(app.getCssClass());
    assertEquals("appointment color", "3", app.getCssClass());
  }

  @Test
  public void testItemIdInstanceCreation() {
    ICalendarItem item = new CalendarTask();
    assertEquals("Item Id", 0L, item.getItemId());
  }

  @Test
  public void testItemIdGetSet_1() {
    ICalendarItem item = new CalendarTask();
    item.setItemId(1L);
    assertEquals("Item Id", 1L, item.getItemId());
  }

  @Test
  public void testItemIdGetSet_2() {
    ICalendarItem item = new CalendarTask();
    item.setItemId(new String("1"));
    assertEquals("Item Id", "1", item.getItemId());
  }

  @Test
  public void testResponsibleInstanceCreation() {
    ICalendarTask task = new CalendarTask();
    assertNull("ResponsibleId", task.getResponsible());
  }

  @Test
  public void testResponsibleGetSet_1() {
    ICalendarTask task = new CalendarTask();
    task.setResponsible(1L);
    assertEquals("Responsible", 1L, task.getResponsible());
  }

  @Test
  public void testResponsibleGetSet_2() {
    ICalendarTask task = new CalendarTask();
    task.setResponsible(1L);
    assertEquals("Responsible Id", Long.valueOf(1L), task.getResponsible());
  }

  @Test
  public void testResponsibleGetSet_3() {
    ICalendarTask task = new CalendarTask();
    task.setResponsible(new String("1"));
    assertEquals("Responsible", "1", task.getResponsible());
  }

  @Test
  public void testPersonInstanceCreation() {
    ICalendarAppointment app = new CalendarAppointment();
    assertNull("PersonId", app.getPerson());
  }

  @Test
  public void testPersonGetSet_1() {
    ICalendarAppointment app = new CalendarAppointment();
    app.setPerson(1L);
    assertEquals("Person", 1L, app.getPerson());
  }

  @Test
  public void testPersonGetSet_2() {
    ICalendarAppointment app = new CalendarAppointment();
    app.setPerson(1L);
    assertEquals("PersonId", Long.valueOf(1L), app.getPerson());
  }

  @Test
  public void testPersonGetSet_3() {
    ICalendarAppointment app = new CalendarAppointment();
    app.setPerson(new String("1"));
    assertEquals("Person", "1", app.getPerson());
  }

  @Test
  public void testExternalKey() {
    CalendarAppointment app = new CalendarAppointment();
    app.setExternalKey("1");
    assertEquals("ExternalKey", "1", app.getExternalKey());
    assertNull("copy() should not copy external key", app.copy().getExternalKey());
  }

  @Test
  public void testGetDescription() {
    ICalendarTask task = new CalendarTask();
    task.setBody("foo");
    assertEquals("foo", task.getDescription());

    ICalendarAppointment app = new CalendarAppointment();
    app.setLocation("foo");
    app.setBody("bar");
    assertEquals("foo\nbar", app.getDescription());

    app.setLocation(null);
    assertEquals("bar", app.getDescription());
  }
}
