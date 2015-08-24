package org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold.PlannerFieldOldChains.PlannerFieldOldLoadActivityMapDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold.PlannerFieldOldChains.PlannerFieldOldLoadResourceTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold.PlannerFieldOldChains.PlannerFieldOldPopulateActivitiesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold.PlannerFieldOldChains.PlannerFieldOldPopulateResourceTableChain;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfieldold.AbstractPlannerFieldOld;

public abstract class AbstractPlannerFieldOldExtension<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI, OWNER extends AbstractPlannerFieldOld<T, P, RI, AI>> extends AbstractFormFieldExtension<OWNER>
    implements IPlannerFieldOldExtension<T, P, RI, AI, OWNER> {

  public AbstractPlannerFieldOldExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execPopulateActivities(PlannerFieldOldPopulateActivitiesChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain, List<RI> resourceIds, List<ITableRow> resourceRows) throws ProcessingException {
    chain.execPopulateActivities(resourceIds, resourceRows);
  }

  @Override
  public Object[][] execLoadResourceTableData(PlannerFieldOldLoadResourceTableDataChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain) throws ProcessingException {
    return chain.execLoadResourceTableData();
  }

  @Override
  public void execPopulateResourceTable(PlannerFieldOldPopulateResourceTableChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain) throws ProcessingException {
    chain.execPopulateResourceTable();
  }

  @Override
  public Object[][] execLoadActivityMapData(PlannerFieldOldLoadActivityMapDataChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain, List<? extends RI> resourceIds, List<? extends ITableRow> resourceRows)
      throws ProcessingException {
    return chain.execLoadActivityMapData(resourceIds, resourceRows);
  }
}
