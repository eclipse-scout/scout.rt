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
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerActivitySelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDecorateActivityChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDisposePlannerChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerInitPlannerChain;
import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractPlannerExtension<RI, AI, OWNER extends AbstractPlanner<RI, AI>> extends AbstractExtension<OWNER> implements IPlannerExtension<RI, AI, OWNER> {

  public AbstractPlannerExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execActivitySelected(PlannerActivitySelectedChain<RI, AI> chain, Activity<RI, AI> cell) throws ProcessingException {
    chain.execActivitySelected(cell);
  }

  @Override
  public void execDisposePlanner(PlannerDisposePlannerChain<RI, AI> chain) throws ProcessingException {
    chain.execDisposePlanner();
  }

  @Override
  public void execDecorateActivityCell(PlannerDecorateActivityChain<RI, AI> chain, Activity<RI, AI> cell) throws ProcessingException {
    chain.execDecorateActivity(cell);
  }

  @Override
  public void execInitPlanner(PlannerInitPlannerChain<RI, AI> chain) throws ProcessingException {
    chain.execInitPlanner();
  }

}
