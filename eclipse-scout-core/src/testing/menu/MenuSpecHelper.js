/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import $ from 'jquery';
import {scout} from '../../index';

export default class MenuSpecHelper {
  constructor(session) {
    this.session = session;
  }

  createModel(text, iconId, menuTypes) {
    let model = createSimpleModel('Menu', this.session);
    $.extend(model, {
      text: text,
      iconId: iconId,
      menuTypes: menuTypes,
      visible: true
    });
    return model;
  }

  createMenu(model) {
    model = model || {};
    model.objectType = model.objectType || 'Menu';
    model.session = this.session;
    model.parent = this.session.desktop;
    return scout.create('Menu', model);
  }
}

