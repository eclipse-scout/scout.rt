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
scout.OkMenu = function() {
  scout.OkMenu.parent.call(this);

  this.actionStyle = scout.Action.ActionStyle.BUTTON;
  this.keyStroke = 'enter';
  this.systemType = scout.Button.SystemType.OK;
};
scout.inherits(scout.OkMenu, scout.Menu);

scout.OkMenu.prototype.init = function(model) {
  scout.OkMenu.parent.prototype.init.call(this, model);
  this.text = this.session.text('OkButton');
};

scout.OkMenu.prototype.getForm = function() {
  return scout.Form.findForm(this);
};

scout.OkMenu.prototype._doAction = function() {
  var event = new scout.Event();
  this.trigger('click', event);
  if (!event.defaultPrevented) {
    this.getForm().ok();
  }
};
