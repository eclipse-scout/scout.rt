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
scout.TagChooserPopupLayout = function(popup) {
  scout.TagChooserPopupLayout.parent.call(this, popup);
  this.doubleCalcPrefSize = false;
};
scout.inherits(scout.TagChooserPopupLayout, scout.PopupLayout);

scout.TagChooserPopupLayout.prototype.layout = function($container) {
  scout.TagChooserPopupLayout.parent.prototype.layout.call(this, $container);

  // layout table
  var htmlComp = this.popup.htmlComp;
  var size = htmlComp.size().subtract(htmlComp.insets());
  this.popup.table.htmlComp.setSize(size);

  this.popup.position();
};

/**
 * @override AbstractLayout.js
 */
scout.TagChooserPopupLayout.prototype.preferredLayoutSize = function($container) {
  var tableHandler = scout.create('TableLayoutResetter', this.popup.table);
  tableHandler.modifyDom();
  var prefSize = scout.TagChooserPopupLayout.parent.prototype.preferredLayoutSize.call(this, $container);
  tableHandler.restoreDom();
  return prefSize;
};
