/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DateRange, Menu, WidgetModel} from '../index';
import {PlannerActivity, PlannerDisplayMode, PlannerDisplayModeOptions, PlannerResource, PlannerSelectionMode} from './Planner';
import {JsonDateRange} from '../util/dates';
import {ObjectOrChildModel} from '../scout';

export default interface PlannerModel extends WidgetModel {
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
