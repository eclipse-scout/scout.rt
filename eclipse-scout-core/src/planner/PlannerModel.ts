/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateRange, JsonDateRange, Menu, ObjectOrChildModel, PlannerActivityModel, PlannerDisplayMode, PlannerDisplayModeOptions, PlannerResourceModel, WidgetModel} from '../index';

export interface PlannerModel extends WidgetModel {
  resources?: PlannerResourceModel[];
  displayMode?: PlannerDisplayMode;
  availableDisplayModes?: PlannerDisplayMode[];
  viewRange?: DateRange | JsonDateRange;
  /**
   * Selected resources or id's of selected resources.
   */
  selectedResources?: PlannerResourceModel[] | string[];
  selectionRange?: DateRange | JsonDateRange;
  /**
   * Selected activity or id of selected activity.
   */
  selectedActivity?: PlannerActivityModel | string;
  displayModeOptions?: Partial<Record<PlannerDisplayMode, PlannerDisplayModeOptions>>;
  activitySelectable?: boolean;
  headerVisible?: boolean;
  multiSelect?: boolean;
  label?: string;
  rangeSelectable?: boolean;
  menus?: ObjectOrChildModel<Menu>[];
  defaultMenuTypes?: string[];
}
