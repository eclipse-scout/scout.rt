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
import {CompositeFieldAdapter} from '../../../index';

export default class TabBoxAdapter extends CompositeFieldAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['selectedTab']);
  }

  /**
   * @override ModelAdapter.js
   */
  exportAdapterData(adapterData) {
    adapterData = super.exportAdapterData(adapterData);
    delete adapterData.selectedTab;
    return adapterData;
  }
}
