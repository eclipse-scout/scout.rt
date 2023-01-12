/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, FormMenuPopupStyle, MenuModel, ObjectOrChildModel} from '../index';

export interface FormMenuModel extends MenuModel {
  form?: ObjectOrChildModel<Form>;
  popupStyle?: FormMenuPopupStyle;
  popupClosable?: boolean;
  popupMovable?: boolean;
  popupResizable?: boolean;
}
