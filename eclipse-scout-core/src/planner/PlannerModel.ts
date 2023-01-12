/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateRange, JsonDateRange, Menu, ObjectOrChildModel, PlannerActivity, PlannerDisplayMode, PlannerDisplayModeOptions, PlannerResource, PlannerSelectionMode, WidgetModel} from '../index';

export interface PlannerModel extends WidgetModel {
  resources?: PlannerResource[];
  displayMode?: PlannerDisplayMode;
  availableDisplayModes?: PlannerDisplayMode[];
  viewRange?: DateRange | JsonDateRange;
  /**
   * Selected resources or id's of selected resources.
   */
  selectedResources?: PlannerResource[] | string[];
  selectionRange?: DateRange | JsonDateRange;
  /**
   * Selected activity or id of selected activity.
   */
  selectedActivity?: PlannerActivity | string;
  displayModeOptions?: Partial<Record<PlannerDisplayMode, PlannerDisplayModeOptions>>;
  activitySelectable?: boolean;
  headerVisible?: boolean;
  label?: string;
  selectionMode?: PlannerSelectionMode;
  menus?: ObjectOrChildModel<Menu>[];
}
