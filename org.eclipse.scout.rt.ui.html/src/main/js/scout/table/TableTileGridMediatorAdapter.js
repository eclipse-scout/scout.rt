import {TableTileGridMediator} from '../index';
import {objects} from '../index';
import {ModelAdapter} from '../index';
import {scout} from '../index';
import {App} from '../index';

/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
export default class TableTileGridMediatorAdapter extends ModelAdapter {

constructor() {
  super();
}


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
  objects.replacePrototypeFunction(TableTileGridMediator, '_onTableRowsInserted', function() {
    if (this.modelAdapter) {
      // nop in classic mode
      return;
    }
    return this._onTableRowsInsertedOrig();
  }, true);

  // handled by the java mediator
  objects.replacePrototypeFunction(TableTileGridMediator, '_onTableRowsDeleted', function() {
    if (this.modelAdapter) {
      // nop in classic mode
      return;
    }
    return this._onTableRowsDeletedOrig();
  }, true);

  // handled by the java mediator
  objects.replacePrototypeFunction(TableTileGridMediator, '_onTableAllRowsDeleted', function() {
    if (this.modelAdapter) {
      // nop in classic mode
      return;
    }
    return this._onTableAllRowsDeletedOrig();
  }, true);
}
}

App.addListener('bootstrap', TableTileGridMediatorAdapter.modifyTableTileGridMediator);
