/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.ResetMenu = function() {
  scout.ResetMenu.parent.call(this);

  this.actionStyle = scout.Action.ActionStyle.BUTTON;
  this.systemType = scout.Button.SystemType.RESET;
};
scout.inherits(scout.ResetMenu, scout.Menu);

scout.ResetMenu.prototype._init = function(model) {
  scout.ResetMenu.parent.prototype._init.call(this, model);
  this.text = scout.nvl(this.text, this.session.text('ResetButton'));
};

scout.ResetMenu.prototype.getForm = function() {
  return scout.Form.findForm(this);
};

scout.ResetMenu.prototype._doAction = function() {
  var event = new scout.Event();
  this.trigger('action', event);
  if (!event.defaultPrevented) {
    this.getForm().reset();
  }
};
