/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.basic.planner;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerAdapter;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerEvent;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.json.JSONException;
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
  public void testDeleteResources() throws JSONException {
    P_Planner planner = createPlanner();
    Resource<Integer> resource = createResource(1);
    Activity<Resource<Integer>, Integer> activity = createActivity(resource, 2);
    resource.addActivity(activity);
    planner.addResource(resource);
    JsonPlanner<IPlanner> jsonPlanner = UiSessionTestUtility.newJsonAdapter(m_uiSession, planner, null);
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

  /**
   * Creates an empty planner.
   */
  private P_Planner createPlanner() {
    P_Planner planner = new P_Planner();
    planner.initPlanner();
    return planner;
  }

  private Resource<Integer> createResource(int id) {
    return new Resource<Integer>(id, "resource" + id);
  }

  private Activity<Resource<Integer>, Integer> createActivity(Resource<Integer> resource, int id) {
    return new Activity<Resource<Integer>, Integer>(resource, id);
  }

  class CapturingPlannerAdapter extends PlannerAdapter {
    private List<PlannerEvent> m_events = new ArrayList<>();

    protected List<PlannerEvent> getEvents() {
      return m_events;
    }

    @Override
    public void plannerChanged(PlannerEvent e) {
      m_events.add(e);
    }
  }

  public static class P_Planner extends AbstractPlanner<Integer, Integer> {
  }
}
