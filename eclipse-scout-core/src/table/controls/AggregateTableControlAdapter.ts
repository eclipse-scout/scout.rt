/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AggregateTableControl, App, objects, TableControlAdapter} from '../../index';

export class AggregateTableControlAdapter extends TableControlAdapter {
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
