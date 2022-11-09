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
import {objects} from '../index';

export type PropertyChangeEventFilterType = ((propertyName: string, value: any) => boolean);

export class PropertyChangeEventFilter {

  filters: PropertyChangeEventFilterType[];
  protected _filterProperties: Record<string, any>;
  protected _defaultFilter: PropertyChangeEventFilterType;

  constructor() {
    this._filterProperties = {};
    this._defaultFilter = this._propertiesFilter.bind(this);
    this.filters = [this._defaultFilter];
  }

  addFilterForProperties(properties: Record<string, any>) {
    objects.copyProperties(properties, this._filterProperties);
  }

  addFilter(filterFunc: PropertyChangeEventFilterType) {
    this.filters.push(filterFunc);
  }

  filter(propertyName: string, value: any): boolean {
    return this.filters.some(filterFunc => filterFunc(propertyName, value));
  }

  /**
   * Will accept the property if the name matches, it won't check the value.
   */
  addFilterForPropertyName(filteredPropertyName: string) {
    this.filters.push((propertyName, value) => propertyName === filteredPropertyName);
  }

  protected _propertiesFilter(propertyName: string, value: any): boolean {
    if (!this._filterProperties.hasOwnProperty(propertyName)) {
      return false;
    }
    let filterPropertyValue = this._filterProperties[propertyName];
    return objects.equals(filterPropertyValue, value);
  }

  reset() {
    this._filterProperties = {};
    this.filters = [this._defaultFilter];
  }
}
