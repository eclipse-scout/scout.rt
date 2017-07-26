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
scout.FormLayout = function(form) {
  scout.FormLayout.parent.call(this);
  this.form = form;
};
scout.inherits(scout.FormLayout, scout.AbstractLayout);

scout.FormLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlRootGb = this._htmlRootGroupBox(),
    rootGbSize;

  this.form.validateLogicalGrid();

  rootGbSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets())
    .subtract(htmlRootGb.margins());

  if (this.form.isDialog()) {
    rootGbSize.height -= this._titleHeight();
  }

  $.log.trace('(FormLayout#layout) rootGbSize=' + rootGbSize);
  htmlRootGb.setSize(rootGbSize);
};

scout.FormLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlRootGb = this._htmlRootGroupBox(),
    prefSize;

  this.form.validateLogicalGrid();

  prefSize = htmlRootGb.prefSize()
    .add(htmlContainer.insets())
    .add(htmlRootGb.margins());
  prefSize.height += this._titleHeight();

  return prefSize;
};

scout.FormLayout.prototype._htmlRootGroupBox = function() {
  var $rootGroupBox = this.form.$container.children('.root-group-box');
  return scout.HtmlComponent.get($rootGroupBox);
};

scout.FormLayout.prototype._titleHeight = function() {
  var $titleBox = this.form.$container.children('.header');
  return scout.graphics.prefSize($titleBox, true).height;
};
