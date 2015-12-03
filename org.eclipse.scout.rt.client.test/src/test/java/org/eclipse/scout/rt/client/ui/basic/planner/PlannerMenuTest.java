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
package org.eclipse.scout.rt.client.ui.basic.planner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.PlannerMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.fixture.OwnerValueCapturingMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IPlannerContextMenu;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractPlannerMenu}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PlannerMenuTest {

  private OwnerValueCapturingMenu m_resourceMenu;
  private OwnerValueCapturingMenu m_emptySpaceMenu;
  private OwnerValueCapturingMenu m_activityMenu;
  private OwnerValueCapturingMenu m_rangeMenu;
  private OwnerValueCapturingMenu m_all;

  @Before
  public void before() {
    m_resourceMenu = new OwnerValueCapturingMenu(PlannerMenuType.Resource);
    m_emptySpaceMenu = new OwnerValueCapturingMenu(PlannerMenuType.EmptySpace);
    m_activityMenu = new OwnerValueCapturingMenu(PlannerMenuType.Activity);
    m_rangeMenu = new OwnerValueCapturingMenu(PlannerMenuType.Range);
    m_all = new OwnerValueCapturingMenu(PlannerMenuType.values());
  }

  /**
   * Tests that {@link AbstractMenu#execOwnerValueChanged} is called for resource menu
   */
  @Test
  public void testOwnerValueOnResourceSelection() {
    final ContextMenuPlanner planner = createContextMenuPlanner();
    addTestMenus(planner);
    planner.selectResource(planner.getResources().get(0));

    assertOwnerValueChange(m_resourceMenu, 1);
    assertOwnerValueChange(m_all, 1);
    assertNoOwnerValueChange(m_emptySpaceMenu);
    assertNoOwnerValueChange(m_activityMenu);
    assertNoOwnerValueChange(m_rangeMenu);
  }

  /**
   * Tests that {@link AbstractMenu#execOwnerValueChanged} is called for range menu
   */
  @Test
  public void testOwnerValueOnRangeSelection() {
    final ContextMenuPlanner planner = createContextMenuPlanner();
    addTestMenus(planner);
    planner.selectResource(planner.getResources().get(0));
    planner.setSelectionRange(new Range<Date>(new Date(), new Date()));

    assertOwnerValueChange(m_resourceMenu, 2);
    assertOwnerValueChange(m_rangeMenu, 1);
    assertOwnerValueChange(m_all, 2);
    assertNoOwnerValueChange(m_emptySpaceMenu);
    assertNoOwnerValueChange(m_activityMenu);
  }

  /**
   * Tests that {@link AbstractMenu#execOwnerValueChanged} is called for empty space menus
   */
  @Test
  public void testOwnerValueOnEmptySpace() {
    final ContextMenuPlanner planner = createContextMenuPlanner();
    planner.selectResource(planner.getResources().get(0));
    addTestMenus(planner);
    planner.deselectAllResources();

    assertOwnerValueChange(m_emptySpaceMenu, 1);
    assertOwnerValueChange(m_all, 1);
    assertNoOwnerValueChange(m_resourceMenu);
    assertNoOwnerValueChange(m_activityMenu);
    assertNoOwnerValueChange(m_rangeMenu);
  }

  /// HELPERS

  private void addTestMenus(IPlanner planner) {
    planner.addMenu(m_emptySpaceMenu);
    planner.addMenu(m_resourceMenu);
    planner.addMenu(m_activityMenu);
    planner.addMenu(m_all);
    planner.addMenu(m_rangeMenu);
  }

  private ContextMenuPlanner createContextMenuPlanner() {
    final ContextMenuPlanner planner = new ContextMenuPlanner();
    Resource<Integer> resource = createResource(1);
    Activity<Resource<Integer>, Integer> activity = createActivity(resource, 10);
    resource.addActivity(activity);
    planner.addResource(resource);
    return planner;
  }

  private void assertOwnerValueChange(OwnerValueCapturingMenu menu, int count) {
    assertEquals(count, menu.getCount());
    assertTrue("Owner should be a CompositeObject, containing resource, activity, range" + menu.getLastOwnerValue().getClass(), menu.getLastOwnerValue() instanceof CompositeObject);
  }

  private void assertNoOwnerValueChange(OwnerValueCapturingMenu menu) {
    assertEquals(0, menu.getCount());
  }

  /// FIXTURES

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

  private static class ContextMenuPlanner extends AbstractPlanner<Integer, Integer> {
    @Override
    public void setContextMenu(IPlannerContextMenu contextMenu) {
      super.setContextMenu(contextMenu);
    }
  }

}
