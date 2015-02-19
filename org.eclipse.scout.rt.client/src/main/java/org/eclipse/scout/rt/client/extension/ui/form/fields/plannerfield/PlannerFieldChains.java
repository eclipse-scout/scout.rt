package org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.AbstractPlannerField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PlannerFieldChains {

  private PlannerFieldChains() {
  }

  protected abstract static class AbstractPlannerFieldChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractExtensionChain<IPlannerFieldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerField<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>>> {

    public AbstractPlannerFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IPlannerFieldExtension.class);
    }
  }

  public static class PlannerFieldPopulateActivitiesChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractPlannerFieldChain<T, P, RI, AI> {

    public PlannerFieldPopulateActivitiesChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPopulateActivities(final List<RI> resourceIds, final List<ITableRow> resourceRows) throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerFieldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerField<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>> next) throws ProcessingException {
          next.execPopulateActivities(PlannerFieldPopulateActivitiesChain.this, resourceIds, resourceRows);
        }
      };
      callChain(methodInvocation, resourceIds, resourceRows);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PlannerFieldLoadResourceTableDataChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractPlannerFieldChain<T, P, RI, AI> {

    public PlannerFieldLoadResourceTableDataChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public Object[][] execLoadResourceTableData() throws ProcessingException {
      MethodInvocation<Object[][]> methodInvocation = new MethodInvocation<Object[][]>() {
        @Override
        protected void callMethod(IPlannerFieldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerField<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>> next) throws ProcessingException {
          setReturnValue(next.execLoadResourceTableData(PlannerFieldLoadResourceTableDataChain.this));
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }
      return methodInvocation.getReturnValue();
    }
  }

  public static class PlannerFieldPopulateResourceTableChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractPlannerFieldChain<T, P, RI, AI> {

    public PlannerFieldPopulateResourceTableChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execPopulateResourceTable() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPlannerFieldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerField<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>> next) throws ProcessingException {
          next.execPopulateResourceTable(PlannerFieldPopulateResourceTableChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class PlannerFieldLoadActivityMapDataChain<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI> extends AbstractPlannerFieldChain<T, P, RI, AI> {

    public PlannerFieldLoadActivityMapDataChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public Object[][] execLoadActivityMapData(final List<? extends RI> resourceIds, final List<? extends ITableRow> resourceRows) throws ProcessingException {
      MethodInvocation<Object[][]> methodInvocation = new MethodInvocation<Object[][]>() {
        @Override
        protected void callMethod(IPlannerFieldExtension<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI, ? extends AbstractPlannerField<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI>> next) throws ProcessingException {
          setReturnValue(next.execLoadActivityMapData(PlannerFieldLoadActivityMapDataChain.this, resourceIds, resourceRows));
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
