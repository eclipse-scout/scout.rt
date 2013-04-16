/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.calendar;

import java.util.Date;

import org.junit.Assert;
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
    Assert.assertEquals(task.getId(), 0L);
    Assert.assertNull(task.getResponsibleId());
    Assert.assertNull(task.getStart());
    Assert.assertNull(task.getDue());
    Assert.assertNull(task.getComplete());
    Assert.assertNull(task.getSubject());
    Assert.assertNull(task.getBody());
    Assert.assertNull(task.getColor());
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
    Assert.assertEquals("task id", 0L, task.getId());
    Assert.assertNull(task.getResponsibleId());
    Assert.assertNotNull(task.getStart());
    Assert.assertEquals("task start", DATE_1, task.getStart());
    Assert.assertNotNull(task.getDue());
    Assert.assertEquals("task due", DATE_2, task.getDue());
    Assert.assertNotNull(task.getComplete());
    Assert.assertEquals("task complete", DATE_3, task.getComplete());
    Assert.assertNotNull(task.getSubject());
    Assert.assertEquals("task subject", "1", task.getSubject());
    Assert.assertNotNull(task.getBody());
    Assert.assertEquals("task body", "2", task.getBody());
    Assert.assertNotNull(task.getColor());
    Assert.assertEquals("task color", "3", task.getColor());
  }

  @Test
  public void testCalendarAppointmentConstruction1() {
    ICalendarAppointment app = new CalendarAppointment();
    Assert.assertEquals("appointment id", 0L, app.getId());
    Assert.assertNull(app.getPersonId());
    Assert.assertNull(app.getStart());
    Assert.assertNull(app.getEnd());
    Assert.assertNull(app.getLocation());
    Assert.assertFalse(app.isFullDay());
    Assert.assertNull(app.getSubject());
    Assert.assertNull(app.getBody());
    Assert.assertNull(app.getColor());
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
    Assert.assertEquals("appointment id", app.getId(), 0L);
    Assert.assertNull(app.getPersonId());
    Assert.assertNotNull(app.getStart());
    Assert.assertEquals("appointment start", DATE_1, app.getStart());
    Assert.assertNotNull(app.getEnd());
    Assert.assertEquals("appointment end", DATE_2, app.getEnd());
    Assert.assertNull(app.getLocation());
    Assert.assertTrue(app.isFullDay());
    Assert.assertNotNull(app.getSubject());
    Assert.assertEquals("appointment subject", "1", app.getSubject());
    Assert.assertNotNull(app.getBody());
    Assert.assertEquals("appointment body", "2", app.getBody());
    Assert.assertNotNull(app.getColor());
    Assert.assertEquals("appointment color", "3", app.getColor());
  }

  @Test
  public void testItemIdInstanceCreation() {
    ICalendarItem item = new CalendarTask();
    Assert.assertEquals("Item Id", 0L, item.getItemId());
  }

  @Test
  public void testItemIdInstanceCreation_2() {
    ICalendarItem item = new CalendarAppointment();
    Assert.assertEquals("Item Id", 0L, item.getItemId());
  }

  @Test
  public void testItemIdGetSet_1() {
    ICalendarItem item = new CalendarTask();
    item.setId(1L);
    Assert.assertEquals("Item Id", 1L, item.getItemId());
  }

  @Test
  public void testItemIdGetSet_2() {
    ICalendarItem item = new CalendarTask();
    item.setItemId(1L);
    Assert.assertEquals("Item Id", 1L, item.getId());
  }

  @Test
  public void testItemIdGetSet_3() {
    ICalendarItem item = new CalendarTask();
    item.setItemId(new String("1"));
    Assert.assertEquals("Item Id", "1", item.getItemId());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testItemIdGetSet_4() {
    ICalendarItem item = new CalendarTask();
    item.setItemId(new String("1"));
    item.getId();
  }

  @Test
  public void testResponsibleInstanceCreation() {
    ICalendarTask task = new CalendarTask();
    Assert.assertNull("ResponsibleId", task.getResponsibleId());
  }

  @Test
  public void testResponsibleGetSet_1() {
    ICalendarTask task = new CalendarTask();
    task.setResponsibleId(1L);
    Assert.assertEquals("Responsible", 1L, task.getResponsible());
  }

  @Test
  public void testResponsibleGetSet_2() {
    ICalendarTask task = new CalendarTask();
    task.setResponsible(1L);
    Assert.assertEquals("Responsible Id", Long.valueOf(1L), task.getResponsibleId());
  }

  @Test
  public void testResponsibleGetSet_3() {
    ICalendarTask task = new CalendarTask();
    task.setResponsible(new String("1"));
    Assert.assertEquals("Responsible", "1", task.getResponsible());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testResponsibleGetSet_4() {
    ICalendarTask task = new CalendarTask();
    task.setResponsible(new String("1"));
    task.getResponsibleId();
  }

  @Test
  public void testPersonInstanceCreation() {
    ICalendarAppointment app = new CalendarAppointment();
    Assert.assertNull("PersonId", app.getPersonId());
  }

  @Test
  public void testPersonGetSet_1() {
    ICalendarAppointment app = new CalendarAppointment();
    app.setPersonId(1L);
    Assert.assertEquals("Person", 1L, app.getPerson());
  }

  @Test
  public void testPersonGetSet_2() {
    ICalendarAppointment app = new CalendarAppointment();
    app.setPerson(1L);
    Assert.assertEquals("PersonId", Long.valueOf(1L), app.getPersonId());
  }

  @Test
  public void testPersonGetSet_3() {
    ICalendarAppointment app = new CalendarAppointment();
    app.setPerson(new String("1"));
    Assert.assertEquals("Person", "1", app.getPerson());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPersonGetSet_4() {
    ICalendarAppointment app = new CalendarAppointment();
    app.setPerson(new String("1"));
    app.getPersonId();
  }
}
