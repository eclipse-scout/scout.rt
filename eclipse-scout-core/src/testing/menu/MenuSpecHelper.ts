/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FullModelOf, Menu, MenuModel, scout, Session} from '../../index';

export class MenuSpecHelper {
  session: Session;

  constructor(session: Session) {
    this.session = session;
  }

  createModel(text?: string, iconId?: string, menuTypes?: string[]): FullModelOf<Menu> {
    let model = createSimpleModel('Menu', this.session) as FullModelOf<Menu>;
    $.extend(model, {
      text: text,
      iconId: iconId,
      menuTypes: menuTypes,
      visible: true
    });
    return model;
  }

  createMenu(model?: MenuModel): Menu {
    model = model || {};
    model.objectType = model.objectType || Menu;
    model.session = this.session;
    model.parent = this.session.desktop;
    return scout.create(model as FullModelOf<Menu>);
  }
}

