/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AggregateTableControl} from '../../index';
import {objects} from '../../index';
import {scout} from '../../index';
import {App} from '../../index';
import {TableControlAdapter} from '../../index';

export default class AggregateTableControlAdapter extends TableControlAdapter {

constructor() {
  super();
}


static modifyAggregateTableControlPrototype() {
  if (!App.get().remote) {
    return;
  }

  // _onTableColumnStructureChanged
  objects.replacePrototypeFunction(AggregateTableControl, '_onTableColumnStructureChanged', function(vararg) {
    if (this.modelAdapter) {
      this._updateEnabledAndSelectedState();
    } else {
      this._onTableColumnStructureChangedOrig();
    }
  }, true);
}
}

App.addListener('bootstrap', AggregateTableControlAdapter.modifyAggregateTableControlPrototype);
