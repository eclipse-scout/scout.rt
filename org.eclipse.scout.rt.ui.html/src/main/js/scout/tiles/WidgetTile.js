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
  // The referenced widget which will be rendered (it is not possible to just call it 'widget' due to the naming conflict with the widget function)
  this.refWidget = null;
  this._addWidgetProperties(['refWidget']);
  this._widgetPropertyChangeHandler = this._onWidgetPropertyChange.bind(this);
};
scout.inherits(scout.WidgetTile, scout.Tile);

scout.WidgetTile.prototype._init = function(model) {
  scout.WidgetTile.parent.prototype._init.call(this, model);
  scout.assertProperty(this, 'refWidget', scout.Widget);
  // FIXME CGU tiles which gridData is the master?
//  if (this.refWidget.gridDataHints) {
//    this._setGridDataHints(this.refWidget.gridDataHints);
//  }
//  this.refWidget.on('propertyChange', this._widgetPropertyChangeHandler);
};

scout.WidgetTile.prototype._destroy = function() {
  this.refWidget.off('propertyChange', this._widgetPropertyChangeHandler);
  scout.WidgetTile.parent.prototype._destroy.call(this);
};

scout.WidgetTile.prototype._render = function() {
  this.refWidget.render(this.$parent);
  this.refWidget.$container.addClass('tile');
  this.refWidget.$container.attr('data-tileadapter', this.id);
  this.$container = this.refWidget.$container;
  this.htmlComp = this.refWidget.htmlComp;
};

scout.WidgetTile.prototype._onWidgetPropertyChange = function(event) {
  if (event.propertyName === 'gridDataHints') {
    this.setGridDataHints(event.newValue);
  }
};
