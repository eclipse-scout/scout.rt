package org.eclipse.scout.rt.client.extension.ui.basic.planner;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PlannerChains {

  private PlannerChains() {
  }

  protected abstract static class AbstractPlannerChain<RI, AI> extends AbstractExtensionChain<IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> {

    public AbstractPlannerChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions, IPlannerExtension.class);
    }
  }

  public static class PlannerActivitySelectedChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerActivitySelectedChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execActivitySelected(final Activity<RI, AI> cell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) throws ProcessingException {
          next.execActivitySelected(PlannerActivitySelectedChain.this, cell);
        }
      };
      callChain(methodInvocation, cell);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
    }
  }

  public static class PlannerDisposePlannerChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerDisposePlannerChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDisposePlanner() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) throws ProcessingException {
          next.execDisposePlanner(PlannerDisposePlannerChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
    }
  }

  public static class PlannerDecorateActivityChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerDecorateActivityChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDecorateActivity(final Activity<RI, AI> cell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) throws ProcessingException {
          next.execDecorateActivityCell(PlannerDecorateActivityChain.this, cell);
        }
      };
      callChain(methodInvocation, cell);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
    }
  }

  public static class PlannerInitPlannerChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerInitPlannerChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execInitPlanner() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) throws ProcessingException {
          next.execInitPlanner(PlannerInitPlannerChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
    }
  }

}
