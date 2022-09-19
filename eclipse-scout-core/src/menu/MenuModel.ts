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
import {ActionModel} from '../index';
import {PopupAlignment} from '../popup/Popup';
import {MenuFilter, MenuStyle, SubMenuVisibility} from './Menu';

export default interface MenuModel extends ActionModel {
  childActions?: MenuModel[];
  menuTypes?: string[];
  /**
   * Default is {@link Menu.MenuStyle.NONE}.
   */
  menuStyle?: MenuStyle;
  popupHorizontalAlignment?: PopupAlignment;
  popupVerticalAlignment?: PopupAlignment;
  /**
   * Default is true
   */
  stackable?: boolean;
  /**
   * Default is false.
   */
  ellipsis?: boolean;
  /**
   * Default is false.
   */
  rightAligned?: boolean;
  /**
   * Default is false
   */
  separator?: boolean;
  /**
   * Default is false
   */
  shrinkable?: boolean;
  /**
   * Default is {@link Menu.SubMenuVisibility.DEFAULT}.
   */
  subMenuVisibility?: SubMenuVisibility;
  menuFilter?: MenuFilter;
}
