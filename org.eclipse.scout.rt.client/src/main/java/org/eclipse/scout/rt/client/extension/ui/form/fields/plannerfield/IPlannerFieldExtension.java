package org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldLoadResourcesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldPopulateResourcesChain;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.AbstractPlannerField;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.Resource;

public interface IPlannerFieldExtension<P extends IPlanner<RI, AI>, RI, AI, OWNER extends AbstractPlannerField<P, RI, AI>> extends IFormFieldExtension<OWNER> {

  List<Resource> execLoadResources(PlannerFieldLoadResourcesChain<? extends IPlanner<RI, AI>, RI, AI> chain) throws ProcessingException;

  void execPopulateResources(PlannerFieldPopulateResourcesChain<? extends IPlanner<RI, AI>, RI, AI> chain) throws ProcessingException;
}
