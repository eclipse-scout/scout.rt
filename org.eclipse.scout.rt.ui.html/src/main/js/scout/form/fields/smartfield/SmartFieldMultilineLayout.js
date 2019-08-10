/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
/**
 * This layout only layouts the INPUT and DIV part of the multi-line smart-field, not the entire form-field.
 */
scout.SmartFieldMultilineLayout = function(smartField) {
  scout.SmartFieldMultilineLayout.parent.call(this);
  this.smartField = smartField;

  this._initDefaults();

  this.htmlPropertyChangeHandler = this._onHtmlEnvironmenPropertyChange.bind(this);
  scout.HtmlEnvironment.on('propertyChange', this.htmlPropertyChangeHandler);
  this.smartField.one('remove', function() {
    scout.HtmlEnvironment.off('propertyChange', this.htmlPropertyChangeHandler);
  }.bind(this));
};
scout.inherits(scout.SmartFieldMultilineLayout, scout.AbstractLayout);

scout.SmartFieldMultilineLayout.prototype._initDefaults = function() {
  this.rowHeight = scout.HtmlEnvironment.formRowHeight;
};

scout.SmartFieldMultilineLayout.prototype._onHtmlEnvironmenPropertyChange = function() {
  this._initDefaults();
  this.smartField.invalidateLayoutTree();
};

scout.SmartFieldMultilineLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    $input = $container.children('.multiline-input'),
    $lines = $container.children('.multiline-lines'),
    innerSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets());

  $input.cssHeight(this.rowHeight);
  $lines.cssHeight(innerSize.height - this.rowHeight);
};
