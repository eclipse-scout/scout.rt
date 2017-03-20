/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.MenuSpecHelper = function(session) {
  this.session = session;
};

scout.MenuSpecHelper.prototype.createModel = function(text, iconId, menuTypes) {
  var model = createSimpleModel('Menu', this.session);
  $.extend(model, {
    text: text,
    iconId: iconId,
    menuTypes: menuTypes,
    visible: true
  });
  return model;
};

scout.MenuSpecHelper.prototype.createMenu = function(model) {
  model.objectType = model.objectType || 'Menu';
  model.session = model.session || this.session;
  model.parent = model.parent || this.session.desktop;
  return scout.create('Menu', model);
};
