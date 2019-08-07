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
scout.TableTileGridMediatorAdapter = function() {
  scout.TableTileGridMediatorAdapter.parent.call(this);
};
scout.inherits(scout.TableTileGridMediatorAdapter, scout.ModelAdapter);

/**
 * Static method to modify the prototype of TableTileGridMediatorAdapter.
 */
scout.TableTileGridMediatorAdapter.modifyTableTileGridMediator = function() {
  if (!scout.app.remote) {
    return;
  }

  // tiles are already provided by the backend in classic mode
  scout.objects.replacePrototypeFunction(scout.TableTileGridMediator, 'loadTiles', function() {
    if (this.modelAdapter) {
      // nop in classic mode
      return;
    }
    return this.loadTilesOrig();
  }, true);

  // handled by the java mediator
  scout.objects.replacePrototypeFunction(scout.TableTileGridMediator, '_onTableRowsInserted', function() {
    if (this.modelAdapter) {
      // nop in classic mode
      return;
    }
    return this._onTableRowsInsertedOrig();
  }, true);

  // handled by the java mediator
  scout.objects.replacePrototypeFunction(scout.TableTileGridMediator, '_onTableRowsDeleted', function() {
    if (this.modelAdapter) {
      // nop in classic mode
      return;
    }
    return this._onTableRowsDeletedOrig();
  }, true);

  // handled by the java mediator
  scout.objects.replacePrototypeFunction(scout.TableTileGridMediator, '_onTableAllRowsDeleted', function() {
    if (this.modelAdapter) {
      // nop in classic mode
      return;
    }
    return this._onTableAllRowsDeletedOrig();
  }, true);
};

scout.addAppListener('bootstrap', scout.TableTileGridMediatorAdapter.modifyTableTileGridMediator);
