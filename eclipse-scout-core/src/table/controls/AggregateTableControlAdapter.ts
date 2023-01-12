/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
