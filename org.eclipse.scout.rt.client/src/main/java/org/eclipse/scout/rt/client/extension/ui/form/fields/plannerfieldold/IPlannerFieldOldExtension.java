package org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold.PlannerFieldOldChains.PlannerFieldOldLoadActivityMapDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold.PlannerFieldOldChains.PlannerFieldOldLoadResourceTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold.PlannerFieldOldChains.PlannerFieldOldPopulateActivitiesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfieldold.PlannerFieldOldChains.PlannerFieldOldPopulateResourceTableChain;
import org.eclipse.scout.rt.client.ui.basic.activitymap.IActivityMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfieldold.AbstractPlannerFieldOld;

public interface IPlannerFieldOldExtension<T extends ITable, P extends IActivityMap<RI, AI>, RI, AI, OWNER extends AbstractPlannerFieldOld<T, P, RI, AI>> extends IFormFieldExtension<OWNER> {

  void execPopulateActivities(PlannerFieldOldPopulateActivitiesChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain, List<RI> resourceIds, List<ITableRow> resourceRows) throws ProcessingException;

  Object[][] execLoadResourceTableData(PlannerFieldOldLoadResourceTableDataChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain) throws ProcessingException;

  void execPopulateResourceTable(PlannerFieldOldPopulateResourceTableChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain) throws ProcessingException;

  Object[][] execLoadActivityMapData(PlannerFieldOldLoadActivityMapDataChain<? extends ITable, ? extends IActivityMap<RI, AI>, RI, AI> chain, List<? extends RI> resourceIds, List<? extends ITableRow> resourceRows)
      throws ProcessingException;
}
