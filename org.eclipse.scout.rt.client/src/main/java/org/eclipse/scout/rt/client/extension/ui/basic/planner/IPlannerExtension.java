/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.basic.planner;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerActivityCellSelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerCellActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDecorateActivityCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDisposePlannerChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerInitPlannerChain;
import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.form.fields.plannerfield.Resource;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IPlannerExtension<RI, AI, OWNER extends AbstractPlanner<RI, AI>> extends IExtension<OWNER> {

  void execActivityCellSelected(PlannerActivityCellSelectedChain<RI, AI> chain, Activity<RI, AI> cell) throws ProcessingException;

  void execDisposePlanner(PlannerDisposePlannerChain<RI, AI> chain) throws ProcessingException;

  void execDecorateActivityCell(PlannerDecorateActivityCellChain<RI, AI> chain, Activity<RI, AI> cell) throws ProcessingException;

  void execInitPlanner(PlannerInitPlannerChain<RI, AI> chain) throws ProcessingException;

  void execCellAction(PlannerCellActionChain<RI, AI> chain, Resource resource, Activity<RI, AI> activityCell) throws ProcessingException;

}
