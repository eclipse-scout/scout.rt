/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.basic.planner;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.JsonDateRange;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonPlannerTest {
  private UiSessionMock m_uiSession;

  @Before
  public void setUp() {
    m_uiSession = new UiSessionMock();
  }

  /**
   * Tests whether the id maps are correctly cleaned up
   */
  @Test
  public void testDeleteResources() {
    P_Planner planner = createPlanner();
    Resource<Integer> resource = createResource(1);
    Activity<Resource<Integer>, Integer> activity = createActivity(resource, 2);
    resource.addActivity(activity);
    planner.addResource(resource);
    JsonPlanner<IPlanner<?, ?>> jsonPlanner = UiSessionTestUtility.newJsonAdapter(m_uiSession, planner);
    jsonPlanner.toJson();
    String resourceId = jsonPlanner.getResourceId(resource);
    String activityId = jsonPlanner.getActivityId(activity);
    Assert.assertNotNull(resourceId);
    Assert.assertNotNull(activityId);
    Assert.assertEquals(resource, jsonPlanner.getResource(resourceId));
    Assert.assertEquals(activity, jsonPlanner.getActivity(activityId));

    planner.deleteResource(resource);
    JsonTestUtility.processBufferedEvents(m_uiSession);
    Assert.assertNull(jsonPlanner.getResourceId(resource));
    Assert.assertNull(jsonPlanner.getActivityId(activity));
    Assert.assertNull(jsonPlanner.getResource(resourceId));
    Assert.assertNull(jsonPlanner.getActivity(activityId));

    planner.addResource(resource);
    JsonTestUtility.processBufferedEvents(m_uiSession);
    resourceId = jsonPlanner.getResourceId(resource);
    activityId = jsonPlanner.getActivityId(activity);
    Assert.assertNotNull(resourceId);
    Assert.assertNotNull(activityId);
    Assert.assertEquals(resource, jsonPlanner.getResource(resourceId));
    Assert.assertEquals(activity, jsonPlanner.getActivity(activityId));

    planner.deleteAllResources();
    JsonTestUtility.processBufferedEvents(m_uiSession);
    Assert.assertNull(jsonPlanner.getResourceId(resource));
    Assert.assertNull(jsonPlanner.getActivityId(activity));
    Assert.assertNull(jsonPlanner.getResource(resourceId));
    Assert.assertNull(jsonPlanner.getActivity(activityId));
  }

  @Test
  public void testUpdateSelectionRangeWhileChanging() {
    Date from = new Date();
    Date to = DateUtility.addDays(from, 2);
    Range<Date> originalRange = new Range<>(from, to);

    to = DateUtility.addDays(from, 3);
    Range<Date> adjustedRange = new Range<>(from, to);

    P_Planner planner = new P_Planner() {
      @Override
      protected void execSelectionRangeChanged(Range<Date> selectionRange) {
        if (selectionRange.equals(originalRange)) {
          setSelectionRange(adjustedRange);
        }
      }
    };
    JsonPlanner<IPlanner<?, ?>> jsonPlanner = UiSessionTestUtility.newJsonAdapter(m_uiSession, planner);
    jsonPlanner.toJson();

    JsonEvent event = createSelectionChangeEvent(jsonPlanner.getId(), originalRange);
    jsonPlanner.handleUiEvent(event);
    assertEquals(adjustedRange, planner.getSelectionRange());

    Object value = JsonTestUtility.extractProperty(m_uiSession.currentJsonResponse(), jsonPlanner.getId(), IPlanner.PROP_SELECTION_RANGE);
    assertEquals(adjustedRange, new Range<>(jsonPlanner.toJavaDate((JSONObject) value, "from"), jsonPlanner.toJavaDate((JSONObject) value, "to")));
  }

  /**
   * Selection must not be cleared if resourceIds cannot be resolved.
   */
  @Test
  public void testIgnorableSelectionEventInconsistentState() throws JSONException {
    P_Planner planner = createPlanner();
    Resource<Integer> resource0 = createResource(0);
    Resource<Integer> resource1 = createResource(1);
    Resource<Integer> resource2 = createResource(2);
    planner.addResource(resource0);
    planner.addResource(resource1);
    planner.addResource(resource2);
    planner.selectResource(resource0);

    JsonPlanner<IPlanner<?, ?>> jsonPlanner = UiSessionTestUtility.newJsonAdapter(m_uiSession, planner);
    jsonPlanner.toJson();

    assertTrue(planner.getSelectedResources().contains(resource0));
    assertFalse(planner.getSelectedResources().contains(resource1));

    // ----------

    // Model selection MUST NOT be cleared when an invalid selection is sent from the UI

    JsonEvent event = createJsonSelectedEvent("not-existing-id");
    jsonPlanner.handleUiEvent(event);
    jsonPlanner.cleanUpEventFilters();

    assertTrue(planner.getSelectedResources().contains(resource0));
    assertFalse(planner.getSelectedResources().contains(resource1));

    // No reply (we assume that the UI state is correct and only the event was wrong, e.g. due to caching)
    List<JsonEvent> responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonPlanner.EVENT_RESOURCES_SELECTED);
    assertEquals(0, responseEvents.size());
    JsonTestUtility.endRequest(m_uiSession);

    // ----------

    // Model selection MUST be cleared when an empty selection is sent from the UI

    event = createJsonSelectedEvent(null);
    jsonPlanner.handleUiEvent(event);
    jsonPlanner.cleanUpEventFilters();

    assertFalse(planner.getSelectedResources().contains(resource0));
    assertFalse(planner.getSelectedResources().contains(resource1));

    // No reply (states should be equal)
    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonPlanner.EVENT_RESOURCES_SELECTED);
    assertEquals(0, responseEvents.size());
    JsonTestUtility.endRequest(m_uiSession);

    // ----------

    // Model selection MUST be updated when a partially invalid selection is sent from the UI

    event = createJsonSelectedEvent("not-existing-id");
    event.getData().getJSONArray(JsonPlanner.PROP_RESOURCE_IDS).put(jsonPlanner.getResourceId(resource1));
    jsonPlanner.handleUiEvent(event);
    jsonPlanner.cleanUpEventFilters();

    assertFalse(planner.getSelectedResources().contains(resource0));
    assertTrue(planner.getSelectedResources().contains(resource1));

    // Inform the UI about the change
    responseEvents = JsonTestUtility.extractEventsFromResponse(
        m_uiSession.currentJsonResponse(), JsonPlanner.EVENT_RESOURCES_SELECTED);
    assertEquals(1, responseEvents.size());
    List<Resource<?>> resources = jsonPlanner.extractResources(responseEvents.get(0).getData());
    assertEquals(resource1, resources.get(0));
    JsonTestUtility.endRequest(m_uiSession);
  }

  public static JsonEvent createJsonSelectedEvent(String resourceId) throws JSONException {
    String tableId = "x"; // never used
    JSONObject data = new JSONObject();
    JSONArray resourceIds = new JSONArray();
    if (resourceId != null) {
      resourceIds.put(resourceId);
    }
    data.put(JsonPlanner.PROP_RESOURCE_IDS, resourceIds);
    return new JsonEvent(tableId, JsonPlanner.EVENT_RESOURCES_SELECTED, data);
  }

  public static JsonEvent createSelectionChangeEvent(String adapterId, Range<Date> range) throws JSONException {
    JSONObject data = new JSONObject();
    data.put("selectionRange", new JsonDateRange(range).toJson());
    return new JsonEvent(adapterId, "property", data);
  }

  /**
   * Creates an empty planner.
   */
  private P_Planner createPlanner() {
    P_Planner planner = new P_Planner();
    planner.init();
    return planner;
  }

  private Resource<Integer> createResource(int id) {
    return new Resource<>(id, "resource" + id);
  }

  private Activity<Resource<Integer>, Integer> createActivity(Resource<Integer> resource, int id) {
    return new Activity<>(resource, id);
  }

  @ClassId("6e444198-b06e-4197-834c-6271fe2fb545")
  public static class P_Planner extends AbstractPlanner<Integer, Integer> {
  }
}
