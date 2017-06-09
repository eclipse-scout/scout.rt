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
scout.TableUserFilter = function() {};

scout.TableUserFilter.prototype.init = function(model) {
  this.session = model.session;
  if (!this.session) {
    throw new Error('Session expected: ' + this);
  }
  this._init(model);
};

scout.TableUserFilter.prototype._init = function(model) {
  $.extend(this, model);
};

scout.TableUserFilter.prototype.createFilterAddedEventData = function() {
  return {
    filterType: this.filterType
  };
};

scout.TableUserFilter.prototype.createFilterRemovedEventData = function() {
  return {
    filterType: this.filterType
  };
};

scout.TableUserFilter.prototype.createKey = function() {
  return this.filterType;
};

scout.TableUserFilter.prototype.createLabel = function() {
  // to be implemented by subclasses
  return '';
};

scout.TableUserFilter.prototype.accept = function($row) {
  // to be implemented by subclasses
};
