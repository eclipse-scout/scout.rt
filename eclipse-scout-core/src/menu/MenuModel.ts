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
import {ActionModel} from '../action/ActionModel';
import {Menu} from '../index';
import {RefWidgetModel} from '../widget/WidgetModel';

export default interface MenuModel extends ActionModel {
  objectType?: string | { new(): Menu }; // Overridden to only allow Menu references
  childActions?: Menu[] | RefWidgetModel<MenuModel>[];
  defaultMenu?: boolean;
  menuTypes?: string[];
}
