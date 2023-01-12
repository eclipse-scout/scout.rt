/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.planner;

import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.platform.util.Range;

public interface IPlannerUIFacade<RI, AI> {

  void setDisplayModeFromUI(int displayMode);

  void setViewRangeFromUI(Range<Date> viewRange);

  void setSelectedActivityFromUI(Activity<RI, AI> activity);

  void setSelectedResourcesFromUI(List<? extends Resource<RI>> resources);

  void setSelectionRangeFromUI(Range<Date> selectionRange);
}
