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
package org.eclipse.scout.rt.client.extension.ui.basic.planner;

import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PlannerChains {

  private PlannerChains() {
  }

  protected abstract static class AbstractPlannerChain<RI, AI> extends AbstractExtensionChain<IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> {

    public AbstractPlannerChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions, IPlannerExtension.class);
    }
  }

  public static class PlannerResourcesSelectedChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerResourcesSelectedChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execResourcesSelected(final List<Resource<RI>> resources) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) {
          next.execResourcesSelected(PlannerResourcesSelectedChain.this, resources);
        }
      };
      callChain(methodInvocation, resources);
    }
  }

  public static class PlannerSelectionRangeChangedChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerSelectionRangeChangedChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execSelectionRangeChanged(final Range<Date> selectionRange) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) {
          next.execSelectionRangeChanged(PlannerSelectionRangeChangedChain.this, selectionRange);
        }
      };
      callChain(methodInvocation, selectionRange);
    }
  }

  public static class PlannerViewRangeChangedChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerViewRangeChangedChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execViewRangeChanged(final Range<Date> viewRange) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) {
          next.execViewRangeChanged(PlannerViewRangeChangedChain.this, viewRange);
        }
      };
      callChain(methodInvocation, viewRange);
    }
  }

  public static class PlannerDisplayModeChangedChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerDisplayModeChangedChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDisplayModeChanged(final int displayMode) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) {
          next.execDisplayModeChanged(PlannerDisplayModeChangedChain.this, displayMode);
        }
      };
      callChain(methodInvocation, displayMode);
    }
  }

  public static class PlannerActivitySelectedChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerActivitySelectedChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execActivitySelected(final Activity<RI, AI> cell) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) {
          next.execActivitySelected(PlannerActivitySelectedChain.this, cell);
        }
      };
      callChain(methodInvocation, cell);
    }
  }

  public static class PlannerDisposePlannerChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerDisposePlannerChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDisposePlanner() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) {
          next.execDisposePlanner(PlannerDisposePlannerChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PlannerDecorateActivityChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerDecorateActivityChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDecorateActivity(final Activity<RI, AI> cell) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) {
          next.execDecorateActivityCell(PlannerDecorateActivityChain.this, cell);
        }
      };
      callChain(methodInvocation, cell);
    }
  }

  public static class PlannerInitPlannerChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerInitPlannerChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execInitPlanner() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) {
          next.execInitPlanner(PlannerInitPlannerChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

}
