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
  this.tileWidget = null;
  this._addWidgetProperties(['tileWidget']);
};
scout.inherits(scout.WidgetTile, scout.Tile);

scout.WidgetTile.prototype._init = function(model) {
  scout.WidgetTile.parent.prototype._init.call(this, model);
  scout.assertProperty(this, 'tileWidget', scout.Widget);
};

scout.WidgetTile.prototype._render = function() {
  this.tileWidget.render(this.$parent);
  this.tileWidget.$container.addClass('tile');
  this.tileWidget.$container.attr('data-tileadapter', this.id);
  this.$container = this.tileWidget.$container;
  this.htmlComp = this.tileWidget.htmlComp;
};
