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
import {Action, ActionModel, BenchColumnLayoutData, Menu, MenuModel, Outline, OutlineModel, ViewButton, ViewButtonModel, Widget, WidgetModel} from '../index';
import {DesktopDisplayStyle, NativeNotificationDefaults} from './Desktop';
import {RefModel} from '../types';

export default interface DesktopModel extends WidgetModel {
  nativeNotificationDefaults?: NativeNotificationDefaults;
  displayStyle?: DesktopDisplayStyle;
  title?: string;
  selectViewTabsKeyStrokesEnabled?: boolean;
  selectViewTabsKeyStrokeModifier?: string;
  cacheSplitterPosition?: boolean;
  logoId?: string;
  navigationVisible?: boolean;
  navigationHandleVisible?: boolean;
  logoActionEnabled?: boolean;
  benchVisible?: boolean;
  headerVisible?: boolean;
  splitterVisible?: boolean;
  benchLayoutData?: BenchColumnLayoutData;
  menus?: Menu[] | RefModel<MenuModel>[];
  addOns?: Widget[] | RefModel<WidgetModel>[];
  keyStrokes?: Action[] | RefModel<ActionModel>[];
  viewButtons?: ViewButton[] | RefModel<ViewButtonModel>[];
  outline?: Outline | RefModel<OutlineModel>;
  inBackground?: boolean;
  theme?: string;
  dense?: boolean;
}
