/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormFieldModel, ObjectOrChildModel, TabAreaStyle, TabItem} from '../../../index';

export interface TabBoxModel extends FormFieldModel {
  /**
   * The tab, that should be selected initially.
   * If a string is provided, the tab will be resolved automatically.
   *
   * By default, the first tab will be selected.
   */
  selectedTab?: TabItem | string;
  tabItems?: ObjectOrChildModel<TabItem>[];
  tabAreaStyle?: TabAreaStyle;
}
