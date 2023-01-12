/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, ModelAdapter, objects, TableTileGridMediator} from '../index';

export class TableTileGridMediatorAdapter extends ModelAdapter {
  /**
   * Static method to modify the prototype of TableTileGridMediatorAdapter.
   */
  static modifyTableTileGridMediator() {
    if (!App.get().remote) {
      return;
    }

    // tiles are already provided by the backend in classic mode
    objects.replacePrototypeFunction(TableTileGridMediator, 'loadTiles', function() {
      if (this.modelAdapter) {
        // nop in classic mode
        return;
      }
      return this.loadTilesOrig();
    }, true);

    // handled by the java mediator
    objects.replacePrototypeFunction(TableTileGridMediator, '_onTableRowsInserted', function(event) {
      if (this.modelAdapter) {
        // nop in classic mode
        return;
      }
      return this._onTableRowsInsertedOrig(event);
    }, true);

    // handled by the java mediator
    objects.replacePrototypeFunction(TableTileGridMediator, '_onTableRowsDeleted', function(event) {
      if (this.modelAdapter) {
        // nop in classic mode
        return;
      }
      return this._onTableRowsDeletedOrig(event);
    }, true);

    // handled by the java mediator
    objects.replacePrototypeFunction(TableTileGridMediator, '_onTableAllRowsDeleted', function(event) {
      if (this.modelAdapter) {
        // nop in classic mode
        return;
      }
      return this._onTableAllRowsDeletedOrig(event);
    }, true);
  }
}

App.addListener('bootstrap', TableTileGridMediatorAdapter.modifyTableTileGridMediator);
