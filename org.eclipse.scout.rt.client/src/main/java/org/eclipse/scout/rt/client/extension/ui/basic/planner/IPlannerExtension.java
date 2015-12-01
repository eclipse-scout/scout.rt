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

import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerActivitySelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDecorateActivityChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDisplayModeChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerDisposePlannerChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerInitPlannerChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerResourcesSelectedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerSelectionRangeChangedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.planner.PlannerChains.PlannerViewRangeChangedChain;
import org.eclipse.scout.rt.client.ui.basic.planner.AbstractPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IPlannerExtension<RI, AI, OWNER extends AbstractPlanner<RI, AI>> extends IExtension<OWNER> {

  void execActivitySelected(PlannerActivitySelectedChain<RI, AI> chain, Activity<RI, AI> cell);

  void execDisposePlanner(PlannerDisposePlannerChain<RI, AI> chain);

  void execDecorateActivityCell(PlannerDecorateActivityChain<RI, AI> chain, Activity<RI, AI> cell);

  void execInitPlanner(PlannerInitPlannerChain<RI, AI> chain);

  void execResourcesSelected(PlannerResourcesSelectedChain<RI, AI> chain, List<Resource<RI>> resources);

  void execSelectionRangeChanged(PlannerSelectionRangeChangedChain<RI, AI> chain, Range<Date> selectionRange);

  void execViewRangeChanged(PlannerViewRangeChangedChain<RI, AI> chain, Range<Date> viewRange);

  void execDisplayModeChanged(PlannerDisplayModeChangedChain<RI, AI> chain, int displayMode);

}
