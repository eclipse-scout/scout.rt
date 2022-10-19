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
import {FormFieldModel, TabItem, TabItemModel} from '../../../index';
import {TabAreaStyle} from './TabArea';
import {RefModel} from '../../../types';

export default interface TabBoxModel extends FormFieldModel {
  /**
   * The tab, that should be selected initially.
   * If a string is provided, the tab will be resolved automatically.
   *
   * By default, the first tab will be selected.
   */
  selectedTab?: TabItem | string;
  tabItems?: (TabItem | RefModel<TabItemModel>)[];
  tabAreaStyle?: TabAreaStyle;
}
