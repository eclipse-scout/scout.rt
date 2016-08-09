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
  this._form = form;
};
scout.inherits(scout.FormLayout, scout.AbstractLayout);

scout.FormLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlRootGb = this._htmlRootGroupBox(),
    rootGbSize;

  rootGbSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets())
    .subtract(htmlRootGb.getMargins());

  if (this._form.isDialog()) {
    rootGbSize.height -= this._titleHeight();
  }

  $.log.trace('(FormLayout#layout) rootGbSize=' + rootGbSize);
  htmlRootGb.setSize(rootGbSize);
};

scout.FormLayout.prototype.preferredLayoutSize = function($container, options) {
  options = options || {};
  var htmlContainer = this._form.htmlComp,
    containerInsets = htmlContainer.getInsets(),
    prefSize = new scout.Dimension();

  var titleHeight = this._titleHeight();
  prefSize.height += titleHeight;

  var htmlInnerComp = this._htmlRootGroupBox();
  if (htmlInnerComp && htmlInnerComp.isVisible()) {
    var innerMargins = htmlInnerComp.getMargins();
    var innerOptions = {};
    if (options.widthHint) {
      innerOptions.widthHint = options.widthHint - containerInsets.horizontal() - innerMargins.horizontal();
    }
    if (options.heightHint) {
      innerOptions.heightHint = options.heightHint - titleHeight - containerInsets.vertical() - innerMargins.vertical();
    }

    var innerPrefSize = htmlInnerComp.getPreferredSize(innerOptions);
    prefSize.width += innerPrefSize.width + innerMargins.horizontal();
    prefSize.height += innerPrefSize.height + innerMargins.vertical();
  }

  return prefSize.add(containerInsets);

//  var htmlContainer = scout.HtmlComponent.get($container),
//    htmlRootGb = this._htmlRootGroupBox(),
//    prefSize;
//
//  prefSize = htmlRootGb.getPreferredSize()
//    .add(htmlContainer.getInsets())
//    .add(htmlRootGb.getMargins());
//  prefSize.height += this._titleHeight();
//
//  return prefSize;
};

scout.FormLayout.prototype._htmlRootGroupBox = function() {
  var $rootGroupBox = this._form.$container.children('.root-group-box');
  return scout.HtmlComponent.get($rootGroupBox);
};

scout.FormLayout.prototype._titleHeight = function() {
  var $titleBox = this._form.$container.children('.title-box');
  return scout.graphics.prefSize($titleBox, {
    includeMargin: true
  }).height;
};
