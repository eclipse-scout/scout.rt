/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.platform.util.Range;

public interface IPlannerUIFacade<RI, AI> {

  void setDisplayModeFromUI(int displayMode);

  void setViewRangeFromUI(Range<Date> viewRange);

  void setSelectedActivityCellFromUI(Activity<RI, AI> cell);

  void setSelectionFromUI(List<? extends Resource<RI>> resources, Range<Date> selectionRange);

  /**
   * Action on a empty cell or activity cell
   *
   * @param activityCell
   *          may be null
   */
//  void fireCellActionFromUI(Resource resource, Activity<RI, AI> activityCell);
}
