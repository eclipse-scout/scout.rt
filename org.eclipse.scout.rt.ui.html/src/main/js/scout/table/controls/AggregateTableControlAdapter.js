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
scout.AggregateTableControlAdapter = function() {
  scout.AggregateTableControlAdapter.parent.call(this);
};
scout.inherits(scout.AggregateTableControlAdapter, scout.TableControlAdapter);

scout.AggregateTableControlAdapter.modifyAggregateTableControlPrototype = function() {
  if (!scout.app.remote) {
    return;
  }

  // _onTableColumnStructureChanged
  scout.objects.replacePrototypeFunction(scout.AggregateTableControl, '_onTableColumnStructureChanged', function(vararg) {
    if (this.modelAdapter) {
      this._updateEnabledAndSelectedState();
    } else {
      this._onTableColumnStructureChangedOrig();
    }
  }, true);
};

scout.addAppListener('bootstrap', scout.AggregateTableControlAdapter.modifyAggregateTableControlPrototype);
