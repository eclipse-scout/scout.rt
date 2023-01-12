/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ObjectOrChildModel, PopupModel, ResizableMode, Widget} from '../index';

export interface WidgetPopupModel<TContent extends Widget = Widget> extends PopupModel {
  /**
   * Default is false.
   */
  closable?: boolean;
  /**
   * Default is false.
   */
  movable?: boolean;
  /**
   * Default is false.
   */
  resizable?: boolean;
  /**
   * Default none.
   */
  resizeModes?: ResizableMode[];
  /**
   * The content of the WidgetPopup
   */
  content?: ObjectOrChildModel<TContent>;
}
