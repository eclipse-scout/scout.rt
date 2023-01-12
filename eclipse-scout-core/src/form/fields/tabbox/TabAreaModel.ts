/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Tab, TabAreaStyle, TabBox, WidgetModel} from '../../../index';

export interface TabAreaModel extends WidgetModel {
  tabBox?: TabBox;
  tabs?: Tab[];
  displayStyle?: TabAreaStyle;
  hasSubLabel?: boolean;
  selectedTab?: Tab;
}
