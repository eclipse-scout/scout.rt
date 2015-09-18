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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.Range;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.AbstractPlannerExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerActivitySelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDecorateActivityChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDisplayModeChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDisposePlannerChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerInitPlannerChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerResourcesSelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerSelectionRangeChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerViewRangeChangedChain;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
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

  @Test
  public void testInterceptorExecActivitySelected() throws ProcessingException {
    callInterceptorAndAssertExtensionMethodCalled(ExtensionMethod.ActivitySelected, new P_InterceptorCaller() {
      @Override
      public void run(P_Planner p) throws ProcessingException {
        p.interceptActivitySelected(null);
      }
    });
  }

  @Test
  public void testInterceptorExecDecorateActivityCell() throws ProcessingException {
    callInterceptorAndAssertExtensionMethodCalled(ExtensionMethod.DecorateActivityCell, new P_InterceptorCaller() {
      @Override
      public void run(P_Planner p) throws ProcessingException {
        p.interceptDecorateActivity(null);
      }
    });
  }

  @Test
  public void testInterceptorExecDisplayModeChanged() throws ProcessingException {
    callInterceptorAndAssertExtensionMethodCalled(ExtensionMethod.DisplayModeChanged, new P_InterceptorCaller() {
      @Override
      public void run(P_Planner p) throws ProcessingException {
        p.interceptDisplayModeChanged(0);
      }
    });
  }

  @Test
  public void testInterceptorExecDisposePlanner() throws ProcessingException {
    callInterceptorAndAssertExtensionMethodCalled(ExtensionMethod.DisposePlanner, new P_InterceptorCaller() {
      @Override
      public void run(P_Planner p) throws ProcessingException {
        p.interceptDisposePlanner();
      }
    });
  }

  @Test
  public void testInterceptorExecInitPlanner() throws ProcessingException {
    callInterceptorAndAssertExtensionMethodCalled(ExtensionMethod.InitPlanner, new P_InterceptorCaller() {
      @Override
      public void run(P_Planner p) throws ProcessingException {
        p.interceptInitPlanner();
      }
    });
  }

  @Test
  public void testInterceptorExecResourcesSelected() throws ProcessingException {
    callInterceptorAndAssertExtensionMethodCalled(ExtensionMethod.ResourcesSelected, new P_InterceptorCaller() {
      @Override
      public void run(P_Planner p) throws ProcessingException {
        p.interceptResourcesSelected(null);
      }
    });
  }

  @Test
  public void testInterceptorExecSelectionRangeChanged() throws ProcessingException {
    callInterceptorAndAssertExtensionMethodCalled(ExtensionMethod.SelectionRangeChanged, new P_InterceptorCaller() {
      @Override
      public void run(P_Planner p) throws ProcessingException {
        p.interceptSelectionRangeChanged(null);
      }
    });
  }

  @Test
  public void testInterceptorExecViewRangeChanged() throws ProcessingException {
    callInterceptorAndAssertExtensionMethodCalled(ExtensionMethod.ViewRangeChanged, new P_InterceptorCaller() {
      @Override
      public void run(P_Planner p) throws ProcessingException {
        p.interceptViewRangeChanged(null);
      }
    });
  }

  private void callInterceptorAndAssertExtensionMethodCalled(ExtensionMethod expectedExtensionMethodCalled, P_InterceptorCaller callPlannerInterceptor) throws ProcessingException {
    P_PlannerExtension.reset();
    try {
      BEANS.get(IExtensionRegistry.class).register(P_PlannerExtension.class);
      P_Planner planner = new P_Planner();
      callPlannerInterceptor.run(planner);
      assertEquals(1, P_PlannerExtension.getCalledInterceptors().size());
      assertTrue(P_PlannerExtension.getCalledInterceptors().contains(expectedExtensionMethodCalled));
    }
    finally {
      BEANS.get(IExtensionRegistry.class).deregister(P_PlannerExtension.class);
    }
  }

  private abstract class P_InterceptorCaller {
    abstract public void run(P_Planner p) throws ProcessingException;
  }

  public enum ExtensionMethod {
    ActivitySelected,
    DecorateActivityCell,
    DisplayModeChanged,
    DisposePlanner,
    InitPlanner,
    ResourcesSelected,
    SelectionRangeChanged,
    ViewRangeChanged
  }

  public static class P_PlannerExtension extends AbstractPlannerExtension<Integer, Integer, P_Planner> {

    public P_PlannerExtension(P_Planner owner) {
      super(owner);
    }

    private static Set<ExtensionMethod> calledInterceptors = new HashSet<ExtensionMethod>();

    public static Set<ExtensionMethod> getCalledInterceptors() {
      return Collections.unmodifiableSet(calledInterceptors);
    }

    @Override
    public void execActivitySelected(PlannerActivitySelectedChain<Integer, Integer> chain, Activity<Integer, Integer> cell) throws ProcessingException {
      calledInterceptors.add(ExtensionMethod.ActivitySelected);
    }

    @Override
    public void execDisposePlanner(PlannerDisposePlannerChain<Integer, Integer> chain) throws ProcessingException {
      calledInterceptors.add(ExtensionMethod.DisposePlanner);
    }

    @Override
    public void execDecorateActivityCell(PlannerDecorateActivityChain<Integer, Integer> chain, Activity<Integer, Integer> cell) throws ProcessingException {
      calledInterceptors.add(ExtensionMethod.DecorateActivityCell);
    }

    @Override
    public void execInitPlanner(PlannerInitPlannerChain<Integer, Integer> chain) throws ProcessingException {
      calledInterceptors.add(ExtensionMethod.InitPlanner);
    }

    @Override
    public void execResourcesSelected(PlannerResourcesSelectedChain<Integer, Integer> chain, List<Resource<Integer>> resources) throws ProcessingException {
      calledInterceptors.add(ExtensionMethod.ResourcesSelected);
    }

    @Override
    public void execSelectionRangeChanged(PlannerSelectionRangeChangedChain<Integer, Integer> chain, Range<Date> selectionRange) throws ProcessingException {
      calledInterceptors.add(ExtensionMethod.SelectionRangeChanged);
    }

    @Override
    public void execViewRangeChanged(PlannerViewRangeChangedChain<Integer, Integer> chain, Range<Date> viewRange) throws ProcessingException {
      calledInterceptors.add(ExtensionMethod.ViewRangeChanged);
    }

    @Override
    public void execDisplayModeChanged(PlannerDisplayModeChangedChain<Integer, Integer> chain, int displayMode) throws ProcessingException {
      calledInterceptors.add(ExtensionMethod.DisplayModeChanged);
    }

    public static void reset() {
      calledInterceptors.clear();
    }

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
