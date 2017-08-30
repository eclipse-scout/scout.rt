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
scout.TableUpdateBuffer = function(table) {
  this._rowMap = {};
  this.promises = [];
  this.table = table;
};

/**
 * The buffer is active if it contains at least one promise. When all promises resolve the buffer will be processed.
 */
scout.TableUpdateBuffer.prototype.pushPromise = function(promise) {
  this.promises.push(promise);

  // Also make sure viewport is not rendered as long as update events are buffered
  // Otherwise the other cells might already be visible during buffering
  this.table._renderViewportBlocked = true;
  this.table.setLoading(true);

  promise.always(function() {
    scout.arrays.remove(this.promises, promise);

    // process immediately when all promises have resolved
    if (this.promises.length === 0) {
      this.process();
    }
  }.bind(this));
};

scout.TableUpdateBuffer.prototype.isBuffering = function() {
  return this.promises.length > 0;
};

scout.TableUpdateBuffer.prototype.buffer = function(rows) {
  rows = scout.arrays.ensure(rows);

  // Don't buffer duplicate rows
  rows.forEach(function(row) {
    this._rowMap[row.id] = row;
  }, this);
};

/**
 * Calls {@link scout.Table.prototype.updateRows} with the buffered rows and renders the viewport if the rendering was blocked.
 */
scout.TableUpdateBuffer.prototype.process = function() {
  var rows = scout.objects.values(this._rowMap);
  this.table.updateRows(rows);
  this._rowMap = {};

  // Update the viewport as well if rendering was blocked
  this.table.setLoading(false);
  this.table._renderViewportBlocked = false;
  if (this.table.rendered) {
    this.table._renderViewport();
  }
};
