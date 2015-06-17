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
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractPlannerExtension<RI, AI, OWNER extends AbstractPlanner<RI, AI>> extends AbstractExtension<OWNER> implements IPlannerExtension<RI, AI, OWNER> {

  public AbstractPlannerExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execActivityCellSelected(PlannerActivityCellSelectedChain<RI, AI> chain, Activity<RI, AI> cell) throws ProcessingException {
    chain.execActivityCellSelected(cell);
  }

  @Override
  public void execDisposePlanner(PlannerDisposePlannerChain<RI, AI> chain) throws ProcessingException {
    chain.execDisposePlanner();
  }

  @Override
  public void execDecorateActivityCell(PlannerDecorateActivityCellChain<RI, AI> chain, Activity<RI, AI> cell) throws ProcessingException {
    chain.execDecorateActivityCell(cell);
  }

  @Override
  public void execInitPlanner(PlannerInitPlannerChain<RI, AI> chain) throws ProcessingException {
    chain.execInitPlanner();
  }

  @Override
  public void execCellAction(PlannerCellActionChain<RI, AI> chain, Resource<RI> resource, Activity<RI, AI> activityCell) throws ProcessingException {
    chain.execCellAction(resource, activityCell);
  }

}
