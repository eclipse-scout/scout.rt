/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, objects, Table} from '../index';

export default class TableUpdateBuffer {

  constructor(table) {
    this._rowMap = {};
    this.promises = [];
    this.table = table;
  }

  /**
   * The buffer is active if it contains at least one promise. When all promises resolve the buffer will be processed.
   */
  pushPromise(promise) {
    this.promises.push(promise);

    // Also make sure viewport is not rendered as long as update events are buffered
    // Otherwise the other cells might already be visible during buffering
    this.table._renderViewportBlocked = true;
    this.table.setLoading(true);

    let handler = function() {
      arrays.remove(this.promises, promise);

      // process immediately when all promises have resolved
      if (this.promises.length === 0) {
        this.process();
      }
    }.bind(this);
    // Use then instead of always to ensure it is always executed asynchronous, even for null values
    promise.then(handler, handler);
  }

  isBuffering() {
    return this.promises.length > 0;
  }

  buffer(rows) {
    rows = arrays.ensure(rows);

    // Don't buffer duplicate rows
    rows.forEach(function(row) {
      this._rowMap[row.id] = row;
    }, this);
  }

  /**
   * Calls {@link Table.prototype.updateRows} with the buffered rows and renders the viewport if the rendering was blocked.
   */
  process() {
    if (this.table.destroyed) {
      return;
    }

    let rows = objects.values(this._rowMap);
    this.table.updateRows(rows);
    this._rowMap = {};

    // Update the viewport as well if rendering was blocked
    this.table.setLoading(false);
    this.table._renderViewportBlocked = false;
    if (this.table._isDataRendered()) {
      this.table._renderViewport();
    }
  }
}
