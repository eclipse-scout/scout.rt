package org.eclipse.scout.rt.client.extension.ui.basic.planner;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.Resource;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PlannerChains {

  private PlannerChains() {
  }

  protected abstract static class AbstractPlannerChain<RI, AI> extends AbstractExtensionChain<IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> {

    public AbstractPlannerChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions, IPlannerExtension.class);
    }
  }

  public static class PlannerActivityCellSelectedChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerActivityCellSelectedChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execActivityCellSelected(final Activity<RI, AI> cell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) throws ProcessingException {
          next.execActivityCellSelected(PlannerActivityCellSelectedChain.this, cell);
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

  public static class PlannerDecorateActivityCellChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerDecorateActivityCellChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execDecorateActivityCell(final Activity<RI, AI> cell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) throws ProcessingException {
          next.execDecorateActivityCell(PlannerDecorateActivityCellChain.this, cell);
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

  public static class PlannerCellActionChain<RI, AI> extends AbstractPlannerChain<RI, AI> {

    public PlannerCellActionChain(List<? extends IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>>> extensions) {
      super(extensions);
    }

    public void execCellAction(final Resource<RI> resource, final Activity<RI, AI> activityCell) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerExtension<RI, AI, ? extends AbstractPlanner<RI, AI>> next) throws ProcessingException {
          next.execCellAction(PlannerCellActionChain.this, resource, activityCell);
        }
      };
      callChain(methodInvocation, resource, activityCell);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
    }
  }
}
