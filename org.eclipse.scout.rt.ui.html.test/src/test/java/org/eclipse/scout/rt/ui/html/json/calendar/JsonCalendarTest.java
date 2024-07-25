/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.calendar;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.calendar.CalendarResourceDo;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonCalendarTest {

  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  @Test
  public void testResourcesToJson() {
    P_Calendar calendar = createCalendar();

    JsonCalendar<ICalendar> jsonCalendar;
    JSONObject json;

    // ------------------

    jsonCalendar = UiSessionTestUtility.newJsonAdapter(m_uiSession, calendar);
    json = jsonCalendar.toJson();
    assertNotNull(json);
    assertFalse(json.has("resources"));
    jsonCalendar.dispose();

    // ------------------

    calendar.setResources(new ArrayList<>());
    jsonCalendar = UiSessionTestUtility.newJsonAdapter(m_uiSession, calendar);
    json = jsonCalendar.toJson();
    assertNotNull(json);
    assertEquals(new JSONArray(), json.getJSONArray("resources"));
    jsonCalendar.dispose();

    // ------------------

    calendar.setResources(List.of(BEANS.get(CalendarResourceDo.class).withName("test")));
    jsonCalendar = UiSessionTestUtility.newJsonAdapter(m_uiSession, calendar);
    json = jsonCalendar.toJson();
    assertNotNull(json);
    JSONArray array = json.getJSONArray("resources");
    assertEquals(1, array.length());
    assertEquals("scout.CalendarResource", ((JSONObject) array.get(0)).getString("_type"));
    assertEquals("test", ((JSONObject) array.get(0)).getString("name"));
    jsonCalendar.dispose();
  }

  /**
   * Creates an empty calendar.
   */
  private P_Calendar createCalendar() {
    P_Calendar calendar = new P_Calendar();
    calendar.init();
    return calendar;
  }

  public static class P_Calendar extends AbstractCalendar {
  }
}
