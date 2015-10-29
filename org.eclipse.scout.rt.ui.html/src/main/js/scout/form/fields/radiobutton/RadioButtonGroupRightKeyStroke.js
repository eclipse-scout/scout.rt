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
scout.RadioButtonGroupRightKeyStroke = function(radioButtonGroup) {
  scout.RadioButtonGroupRightKeyStroke.parent.call(this);
  this.field = radioButtonGroup;
  this.which = [scout.keys.RIGHT];
  this.renderingHints.render = false;
};
scout.inherits(scout.RadioButtonGroupRightKeyStroke, scout.KeyStroke);

scout.RadioButtonGroupRightKeyStroke.prototype.handle = function(event) {
  var fieldBefore,
    selectedKey = $(event.target).attr('value');
  for (var key in this.field._radioButtonMap) {
    var radioButton = this.field._radioButtonMap[key];
    if (fieldBefore && radioButton.enabled && radioButton.visible) {
      radioButton._renderTabbable(true);
      radioButton._toggleChecked();
      fieldBefore._renderTabbable(false);
      this.field.session.focusManager.requestFocus(radioButton.$field);
      break;
    }
    if (key === selectedKey && radioButton.enabled && radioButton.visible) {
      fieldBefore = this.field._radioButtonMap[key];
    }
  }
};
