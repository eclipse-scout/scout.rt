/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, arrays, Event, EventEmitter, EventMap, ObjectOrModel, objects, Table, TableRow} from '../index';

export class TableUpdateBuffer extends EventEmitter {
  declare self: TableUpdateBuffer;
  declare eventMap: UpdateBufferEventMap;

  promises: JQuery.Promise<any>[];
  table: Table;
  protected _rowMap: Record<string, ObjectOrModel<TableRow>>;

  constructor(table: Table) {
    super();
    this._rowMap = {};
    this.promises = [];
    this.table = table;
  }

  /**
   * The buffer is active if it contains at least one promise. When all promises resolve the buffer will be processed.
   */
  pushPromise(promise: JQuery.Promise<any>) {
    this.promises.push(promise);

    // Also make sure viewport is not rendered as long as update events are buffered
    // Otherwise the other cells might already be visible during buffering
    this.table._renderViewportBlocked = true;
    this.table.setLoading(true);

    let handler = () => {
      arrays.remove(this.promises, promise);

      // process immediately when all promises have resolved
      if (this.promises.length === 0) {
        this.process();
      }
    };
    // Use then instead of always to ensure it is always executed asynchronous, even for null values
    promise.then(handler, handler);
  }

  isBuffering(): boolean {
    return this.promises.length > 0;
  }

  buffer(rows: ObjectOrModel<TableRow> | ObjectOrModel<TableRow>[]) {
    rows = arrays.ensure(rows);

    // Don't buffer duplicate rows
    rows.forEach(row => {
      this._rowMap[row.id] = row;
    });
  }

  /**
   * Calls {@link Table.updateRows} with the buffered rows and renders the viewport if the rendering was blocked.
   */
  process() {
    if (this.table.destroyed) {
      return;
    }

    try {
      let rows = objects.values(this._rowMap)
        .filter(row => this.table.hasRow(row)); // ignore old buffered rows that have been removed in the meantime
      this.table.updateRows(rows);
    } catch (err: any) {
      App.get().errorHandler.handle(err); // otherwise error is ignored
      throw err; // let the promise fail
    } finally {
      this._rowMap = {};
      this.table.setLoading(false);
      this.table._renderViewportBlocked = false;
    }

    // Update the viewport and selection as well if rendering was blocked
    if (this.table._isDataRendered()) {
      this.table._renderViewport();

      if (this.table.scrollToSelection) {
        this.table.revealSelection();
      }
    }
    this.trigger('complete');
  }
}

export interface UpdateBufferEventMap extends EventMap {
  'complete': Event;
}
