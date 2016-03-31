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
  var fieldBefore;
  this.field.radioButtons.some(function(radioButton) {
    if (fieldBefore && radioButton.enabled && radioButton.visible) {
      radioButton.select();
      this.field.session.focusManager.requestFocus(radioButton.$field);
      return true;
    }
    if (radioButton === this.field.selectedButton && radioButton.enabled && radioButton.visible) {
      fieldBefore = radioButton;
    }
  }, this);
};
