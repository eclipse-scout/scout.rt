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
scout.Composite = function() {
  scout.Composite.parent.call(this);

  this.widgets = [];
  this._addWidgetProperties(['widgets']);
};
scout.inherits(scout.Composite, scout.Widget);

scout.Composite.prototype._render = function() {
  this.$container = this.$parent.appendDiv();
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

scout.Composite.prototype._renderProperties = function() {
  scout.Composite.parent.prototype._renderProperties.call(this);
  this._renderWidgets();
};

scout.Composite.prototype.setWidgets = function(widgets) {
  this.setProperty('widgets', widgets);
};

scout.Composite.prototype._renderWidgets = function() {
  this.widgets.forEach(function(widget) {
    widget.render();
  }, this);
  this.invalidateLayoutTree();
};
