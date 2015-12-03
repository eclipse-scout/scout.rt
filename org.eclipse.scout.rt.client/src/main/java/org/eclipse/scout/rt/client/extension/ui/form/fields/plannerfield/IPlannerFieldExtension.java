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
package org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldLoadResourcesChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.plannerfield.PlannerFieldChains.PlannerFieldPopulateResourcesChain;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.AbstractPlannerField;

public interface IPlannerFieldExtension<P extends IPlanner<RI, AI>, RI, AI, OWNER extends AbstractPlannerField<P, RI, AI>> extends IFormFieldExtension<OWNER> {

  List<Resource<RI>> execLoadResources(PlannerFieldLoadResourcesChain<? extends IPlanner<RI, AI>, RI, AI> chain);

  void execPopulateResources(PlannerFieldPopulateResourcesChain<? extends IPlanner<RI, AI>, RI, AI> chain);
}
