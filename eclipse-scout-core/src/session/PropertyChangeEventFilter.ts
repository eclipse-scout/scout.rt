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
import {objects} from '../index';

export default class PropertyChangeEventFilter {

  constructor() {
    this._filterProperties = {};
    this._propertiesFilter = this._propertiesFilter.bind(this);
    this.filters = [this._propertiesFilter];
  }

  addFilterForProperties(properties) {
    objects.copyProperties(properties, this._filterProperties);
  }

  addFilter(filterFunc) {
    this.filters.push(filterFunc);
  }

  filter(propertyName, value) {
    return this.filters.some(filterFunc => {
      return filterFunc(propertyName, value);
    });
  }

  /**
   * Will accept the property if the name matches, it won't check the value.
   */
  addFilterForPropertyName(filteredPropertyName) {
    this.filters.push((propertyName, value) => {
      return propertyName === filteredPropertyName;
    });
  }

  _propertiesFilter(propertyName, value) {
    if (!this._filterProperties.hasOwnProperty(propertyName)) {
      return false;
    }
    let filterPropertyValue = this._filterProperties[propertyName];
    return objects.equals(filterPropertyValue, value);
  }

  reset() {
    this._filterProperties = {};
    this.filters = [this._propertiesFilter];
  }
}
