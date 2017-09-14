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
scout.PageTilesSelectKeyStroke = function(pageTiles) {
  scout.PageTilesSelectKeyStroke.parent.call(this);
  this.field = pageTiles;

  // range [1..9]
  this.registerRange(
    scout.keys['1'], // range from
    function() {
      return scout.keys[Math.min(this._tiles().length, 9)]; // range to
    }.bind(this)
  );

  // rendering hints
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var index = event.which - scout.keys['1'];
    var tiles = this._tiles();
    if (index < tiles.length && tiles[index].widget instanceof scout.TileButton) {
      return tiles[index].widget.$fieldContainer;
    }
    return null;
  }.bind(this);
};
scout.inherits(scout.PageTilesSelectKeyStroke, scout.RangeKeyStroke);

/**
 * @override
 */
scout.PageTilesSelectKeyStroke.prototype._accept = function(event) {
  var accepted = scout.PageTilesSelectKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var index = scout.codesToKeys[event.which] - 1;
  var tiles = this._tiles();

  if (index < tiles.length && tiles[index].widget instanceof scout.TileButton) {
    event._$element = tiles[index].$container;
    if (event._$element) {
      return true;
    }
  }
  return false;
};

/**
 * @override
 */
scout.PageTilesSelectKeyStroke.prototype.handle = function(event) {
  var tile = event._$element.data('widget');
  tile.widget.doAction();
};

scout.PageTilesSelectKeyStroke.prototype._tiles = function() {
  return this.field.tiles;
};
