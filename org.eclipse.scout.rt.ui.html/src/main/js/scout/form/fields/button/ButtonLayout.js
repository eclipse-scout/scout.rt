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
scout.ButtonLayout = function(button) {
  scout.ButtonLayout.parent.call(this, button);
  this.button = button;
};
scout.inherits(scout.ButtonLayout, scout.FormFieldLayout);

scout.ButtonLayout.prototype.layout = function($container) {
  scout.ButtonLayout.parent.prototype.layout.call(this, $container);

  var $icon = this.button.get$Icon(),
    $submenuIcon = this.button.$submenuIcon,
    $label = this.button.$buttonLabel,
    $fieldContainer = this.button.$fieldContainer;

  // Set max width to make it possible to set text-overflow: ellipsis using CSS
  var submenuIconWidth = $submenuIcon ? scout.graphics.size($submenuIcon, {
    includeMargin: true,
    exact: true
  }).width : 0;
  var iconWidth = $icon.length ? scout.graphics.size($icon, {
    includeMargin: true,
    exact: true
  }).width : 0;
  var labelMaxWidth = $fieldContainer.width() - (submenuIconWidth + iconWidth);
  $label.css('max-width', labelMaxWidth);
};

scout.ButtonLayout.prototype.preferredLayoutSize = function($container, options) {
  var $label = this.button.$buttonLabel;

  // Reset max width before calculating pref size
  var maxWidth = $label.css('max-width');
  $label.css('max-width', '');

  var prefSize = scout.ButtonLayout.parent.prototype.preferredLayoutSize.call(this, $container, options);
  $label.css('max-width', maxWidth);

  return prefSize;
};
