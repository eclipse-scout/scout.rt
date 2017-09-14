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
/**
 * A tile containing a widget. The widget will be rendered and its $container used as $container for the tile.
 * If the widget has gridDataHints, they will be used as gridDataHints for the tile.
 */
scout.WidgetTile = function() {
  scout.WidgetTile.parent.call(this);
  this.widget = null;
  this._addWidgetProperties(['widget']);
  this._widgetPropertyChangeHandler = this._onWidgetPropertyChange.bind(this);
};
scout.inherits(scout.WidgetTile, scout.Tile);

scout.WidgetTile.prototype._init = function(model) {
  scout.WidgetTile.parent.prototype._init.call(this, model);
  scout.assertProperty(this, 'widget', scout.Widget);
  if (this.widget.gridDataHints) {
    this._setGridDataHints(this.widget.gridDataHints);
  }
  this.widget.on('propertyChange', this._widgetPropertyChangeHandler);
};

scout.WidgetTile.prototype._destroy = function() {
  this.widget.off('propertyChange', this._widgetPropertyChangeHandler);
  scout.WidgetTile.parent.prototype._destroy.call(this);
};

scout.WidgetTile.prototype._render = function() {
  this.widget.render(this.$parent);
  this.widget.$container.addClass('tile');
  this.widget.$container.attr('data-tileadapter', this.id);
  this.$container = this.widget.$container;
  this.htmlComp = this.widget.htmlComp;
};

scout.WidgetTile.prototype._onWidgetPropertyChange = function(event) {
  if (event.propertyName === 'gridDataHints') {
    this.setGridDataHints(event.newValue);
  }
};
