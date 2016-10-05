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
scout.TabBoxAdapter = function() {
  scout.TabBoxAdapter.parent.call(this);
  this._addAdapterProperties(['tabItems', 'selectedTab']);
  this._addRemoteProperties(['selectedTab']);
};
scout.inherits(scout.TabBoxAdapter, scout.CompositeFieldAdapter);

/**
 * @override ModelAdapter.js
 */
scout.TabBoxAdapter.prototype.exportAdapterData = function(adapterData) {
  adapterData = scout.TabBoxAdapter.parent.prototype.exportAdapterData.call(this, adapterData);
  delete adapterData.selectedTab;
  return adapterData;
};
