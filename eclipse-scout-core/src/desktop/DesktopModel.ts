/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, BenchColumnLayoutData, DesktopDisplayStyle, DesktopNotification, FileChooser, Form, Menu, MessageBox, NativeNotificationDefaults, ObjectOrChildModel, Outline, ViewButton, Widget, WidgetModel} from '../index';

export interface DesktopModel extends WidgetModel {
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
  menus?: ObjectOrChildModel<Menu>[];
  notifications?: ObjectOrChildModel<DesktopNotification>[];
  addOns?: ObjectOrChildModel<Widget>[];
  dialogs?: ObjectOrChildModel<Form>[];
  views?: ObjectOrChildModel<Form>[];
  messageBoxes?: ObjectOrChildModel<MessageBox>[];
  fileChoosers?: ObjectOrChildModel<FileChooser>[];
  keyStrokes?: ObjectOrChildModel<Action>[];
  viewButtons?: ObjectOrChildModel<ViewButton>[];
  outline?: ObjectOrChildModel<Outline> | string;
  inBackground?: boolean;
  theme?: string;
  dense?: boolean;
}
