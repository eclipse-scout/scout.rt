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
import {Menu, MenuModel, scout, Session} from '../../index';
import {FullModelOf} from '../../scout';

export default class MenuSpecHelper {
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

