/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.planner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractPlanner}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PlannerTest {

  /**
   * Tests that update event is fired if activities are added / removed.
   */
  @Test
  public void testResourceUpdateEvent_CellChanged() throws ProcessingException {
    P_Planner planner = createTestPlanner();
    Resource<Integer> resource = createTestResource(0);
    planner.addResource(resource);
    final CapturingPlannerAdapter ta = new CapturingPlannerAdapter();
    planner.addPlannerListener(ta);

    assertEquals(0, ta.getEvents().size());
    resource.getCell().setText("newText");
    assertEquals(1, ta.getEvents().size());
    assertEquals(PlannerEvent.TYPE_RESOURCES_UPDATED, ta.getEvents().get(0).getType());
  }

  /**
   * Tests that update event is fired if activities are added / removed.
   */
  @Test
  public void testResourceUpdateEvent_ActivityListChanged() throws ProcessingException {
    P_Planner planner = createTestPlanner();
    Resource<Integer> resource = createTestResource(0);
    planner.addResource(resource);
    final CapturingPlannerAdapter ta = new CapturingPlannerAdapter();
    planner.addPlannerListener(ta);

    assertEquals(0, ta.getEvents().size());
    resource.addActivity(createTestActivity(resource, 0));
    assertEquals(1, ta.getEvents().size());
    assertEquals(PlannerEvent.TYPE_RESOURCES_UPDATED, ta.getEvents().get(0).getType());
  }

  @Test
  public void testSelectResources() throws ProcessingException {
    P_Planner planner = createTestPlanner();
    Resource<Integer> resource = createTestResource(0);
    planner.addResource(resource);

    assertTrue(planner.getSelectedResources().isEmpty());
    planner.selectResource(resource);
    assertFalse(planner.getSelectedResources().isEmpty());
    assertEquals(resource, planner.getSelectedResource());
  }

  /**
   * Tests that selected resources are still selected if resources are replaced
   */
  @Test
  public void testRestoreSelection() throws ProcessingException {
    P_Planner planner = createTestPlanner();
    Resource<Integer> resource = createTestResource(1);
    planner.addResource(resource);
    planner.selectResource(resource);
    assertEquals(resource, planner.getSelectedResource());

    List<Resource<Integer>> newResources = new ArrayList<Resource<Integer>>();
    newResources.add(createTestResource(0));
    newResources.add(createTestResource(1));
    planner.replaceResources(newResources);

    assertEquals(1, planner.getSelectedResources().size());
    assertNotEquals(resource, planner.getSelectedResource());
    assertEquals(newResources.get(1), planner.getSelectedResource());
  }

  /**
   * Tests that selection is cleared after replace if the previous selected resource isn't there anymore
   */
  @Test
  public void testRestoreSelection_NoMatch() throws ProcessingException {
    P_Planner planner = createTestPlanner();
    Resource<Integer> resource = createTestResource(1);
    planner.addResource(resource);
    planner.selectResource(resource);
    assertEquals(resource, planner.getSelectedResource());

    List<Resource<Integer>> newResources = new ArrayList<Resource<Integer>>();
    newResources.add(createTestResource(0));
    planner.replaceResources(newResources);

    assertTrue(planner.getSelectedResources().isEmpty());
  }

  /**
   * Creates an empty planner.
   */
  private P_Planner createTestPlanner() throws ProcessingException {
    P_Planner planner = new P_Planner();
    planner.initPlanner();
    return planner;
  }

  private Resource<Integer> createTestResource(int id) {
    return new Resource<Integer>(id, "resource" + id);
  }

  private Activity<Resource<Integer>, Integer> createTestActivity(Resource<Integer> resource, int id) {
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
