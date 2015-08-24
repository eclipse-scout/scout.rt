package org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfieldold.AbstractPlannerFieldOld;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PlannerFieldOldChains {

  private PlannerFieldOldChains() {
  }

  protected abstract static class AbstractPlannerFieldOldChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI>
      extends AbstractExtensionChain<IPlannerFieldOldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerFieldOld<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>>> {

    public AbstractPlannerFieldOldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IPlannerFieldOldExtension.class);
    }
  }

  public static class PlannerFieldOldPopulateActivitiesChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractPlannerFieldOldChain<T, P, RI, AI> {

    public PlannerFieldOldPopulateActivitiesChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPopulateActivities(final List<RI> resourceIds, final List<ITableRow> resourceRows) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerFieldOldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerFieldOld<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>> next)
            throws ProcessingException {
          next.execPopulateActivities(PlannerFieldOldPopulateActivitiesChain.this, resourceIds, resourceRows);
        }
      };
      callChain(methodInvocation, resourceIds, resourceRows);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PlannerFieldOldLoadResourceTableDataChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractPlannerFieldOldChain<T, P, RI, AI> {

    public PlannerFieldOldLoadResourceTableDataChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public Object[][] execLoadResourceTableData() throws ProcessingException {
      MethodInvocation<Object[][]> methodInvocation = new MethodInvocation<Object[][]>() {
        @Override
        protected void callMethod(IPlannerFieldOldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerFieldOld<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>> next)
            throws ProcessingException {
          setReturnValue(next.execLoadResourceTableData(PlannerFieldOldLoadResourceTableDataChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class PlannerFieldOldPopulateResourceTableChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractPlannerFieldOldChain<T, P, RI, AI> {

    public PlannerFieldOldPopulateResourceTableChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPopulateResourceTable() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerFieldOldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerFieldOld<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>> next)
            throws ProcessingException {
          next.execPopulateResourceTable(PlannerFieldOldPopulateResourceTableChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PlannerFieldOldLoadActivityMapDataChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractPlannerFieldOldChain<T, P, RI, AI> {

    public PlannerFieldOldLoadActivityMapDataChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public Object[][] execLoadActivityMapData(final List<? extends RI> resourceIds, final List<? extends ITableRow> resourceRows) throws ProcessingException {
      MethodInvocation<Object[][]> methodInvocation = new MethodInvocation<Object[][]>() {
        @Override
        protected void callMethod(IPlannerFieldOldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerFieldOld<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>> next)
            throws ProcessingException {
          setReturnValue(next.execLoadActivityMapData(PlannerFieldOldLoadActivityMapDataChain.this, resourceIds, resourceRows));
        }
      };
      callChain(methodInvocation, resourceIds, resourceRows);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }
}
