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
import {Form, MenuModel} from '../index';
import {FormMenuPopupStyle} from './FormMenu';

export default interface FormMenuModel extends MenuModel {
  form?: Form;
  popupStyle?: FormMenuPopupStyle;
  popupClosable?: boolean;
  popupMovable?: boolean;
  popupResizable?: boolean;
}
