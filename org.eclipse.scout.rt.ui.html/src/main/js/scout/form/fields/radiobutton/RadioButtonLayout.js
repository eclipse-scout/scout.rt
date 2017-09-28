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
scout.RadioButtonLayout = function(radioButton) {
  scout.RadioButtonLayout.parent.call(this, radioButton);
  this.radioButton = radioButton;
};
scout.inherits(scout.RadioButtonLayout, scout.FormFieldLayout);

scout.RadioButtonLayout.prototype.layout = function($container) {
  scout.RadioButtonLayout.parent.prototype.layout.call(this, $container);

  var $icon = this.radioButton.get$Icon(),
    $circle = this.radioButton.$radioButton,
    $label = this.radioButton.$buttonLabel,
    $fieldContainer = this.radioButton.$fieldContainer;

  var labelMaxWidth = $fieldContainer.width() - ($circle.outerWidth(true) + ($icon.length ? $icon.outerWidth(true) : 0));
  $label.css('max-width', labelMaxWidth);
};
