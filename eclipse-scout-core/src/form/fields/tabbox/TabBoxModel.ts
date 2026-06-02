/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormFieldModel, ObjectOrChildModel, TabAreaStyle, TabBoxMarkStrategy, TabItem} from '../../../index';

export interface TabBoxModel extends FormFieldModel {
  /**
   * Defines the strategy to update the {@link TabItem.marked} property.
   *
   * If set to null, the {@link TabItem.marked} property won't be updated an can be set manually if needed.
   *
   * Default is {@link TabBox.MarkStrategy.EMPTY}.
   */
  markStrategy?: TabBoxMarkStrategy;
  /**
   * The tab, that should be selected initially.
   * If a string is provided, the tab will be resolved automatically.
   *
   * By default, the first tab will be selected.
   */
  selectedTab?: TabItem | string;
  /**
   * Defines the {@link TabItem}s to be displayed.
   */
  tabItems?: ObjectOrChildModel<TabItem>[];
  /**
   * Defines the {@link TabAreaStyle} of the {@link TabArea}.
   *
   * Default is {@link TabArea.DisplayStyle.DEFAULT}
   */
  tabAreaStyle?: TabAreaStyle;
}
