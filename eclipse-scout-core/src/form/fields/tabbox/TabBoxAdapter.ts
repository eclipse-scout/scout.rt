/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AdapterData, CompositeFieldAdapter} from '../../../index';

export class TabBoxAdapter extends CompositeFieldAdapter {

  constructor() {
    super();
    this._addRemoteProperties(['selectedTab']);
  }

  override exportAdapterData(adapterData: AdapterData): AdapterData {
    adapterData = super.exportAdapterData(adapterData);
    delete adapterData.selectedTab;
    return adapterData;
  }
}
