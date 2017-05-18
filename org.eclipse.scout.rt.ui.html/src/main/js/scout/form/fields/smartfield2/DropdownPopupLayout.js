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
scout.DropdownPopupLayout = function(popup, childWidget) {
  scout.DropdownPopupLayout.parent.call(this, popup, childWidget);
  this.childWidget = childWidget;
};
scout.inherits(scout.DropdownPopupLayout, scout.SinglePopupLayout);

scout.DropdownPopupLayout.prototype.layout = function($container) {
  scout.DropdownPopupLayout.parent.prototype.layout.call(this, $container);

  // The first time it gets layouted, add shown class to be able to animate
  if (!this.popup.htmlComp.layouted) {
    this.popup.htmlComp.$comp.addClassForAnimation('animate-open');
  }
};

scout.DropdownPopupLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize = scout.DropdownPopupLayout.parent.prototype.preferredLayoutSize.call(this, $container);

  // Popup is as width as the anchor
  prefSize.width = this.popup.$anchor.outerWidth();
  // table has a top margin of -1 which is not taken into account by pref size
  // -> make popup 1px smaller to prevent 1px white gap at the bottom
  prefSize.height = prefSize.height - 1;
  return prefSize;
};
