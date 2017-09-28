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
scout.PropertyChangeEventFilter = function() {
  this._filterProperties = {};
};

scout.PropertyChangeEventFilter.prototype.addFilterForProperties = function(properties) {
  scout.objects.copyProperties(properties, this._filterProperties);
};

scout.PropertyChangeEventFilter.prototype.filter = function(propertyName, value) {
  if (!this._filterProperties.hasOwnProperty(propertyName)) {
    return false;
  }
  var filterPropertyValue = this._filterProperties[propertyName];
  return scout.objects.equals(filterPropertyValue, value);
};

scout.PropertyChangeEventFilter.prototype.reset = function() {
  this._filterProperties = {};
};
