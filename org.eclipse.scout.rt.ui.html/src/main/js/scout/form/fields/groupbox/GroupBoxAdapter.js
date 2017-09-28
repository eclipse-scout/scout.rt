/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.GroupBoxAdapter = function() {
  scout.GroupBoxAdapter.parent.call(this);
  this._addRemoteProperties(['expanded']);
};
scout.inherits(scout.GroupBoxAdapter, scout.CompositeFieldAdapter);

/**
 * @override
 */
scout.GroupBoxAdapter.prototype._initModel = function(model, parent) {
  model = scout.GroupBoxAdapter.parent.prototype._initModel.call(this, model, parent);
  // Set logical grid to null -> Calculation happens on server side
  model.logicalGrid = null;
  return model;
};
