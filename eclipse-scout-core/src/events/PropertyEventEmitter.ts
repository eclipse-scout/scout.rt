/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, EventEmitter, InitModelOf, objects, PropertyChangeEvent, PropertyEventMap, scout, strings} from '../index';
import $ from 'jquery';

export class PropertyEventEmitter extends EventEmitter {
  declare eventMap: PropertyEventMap;
  declare self: PropertyEventEmitter;
  initialized: boolean;
  /**
   * Contains the names of properties that are decorated grouped by the decoration. Subclasses can add their own decorations.
   */
  propertyDecorations: Record<PropertyDecoration, Set<string>>;
  protected _propertyDimensions: Record<string, Record<string, boolean>>; // <MultiDimensionalProperty, <Dimension, value>>
  protected _propertyDimensionDefaults: Record<string, boolean>; // <MultiDimensionalProperty, defaultValue>
  protected _propertyDimensionAliases: Record<string, { propertyName: string } & Required<PropertyDimensionAliasConfig>>; // <alias, {MultiDimensionalProperty} & config>

  constructor() {
    super();
    this.events.registerSubTypePredicate('propertyChange', (event: PropertyChangeEvent, propertyName) => event.propertyName === propertyName);
    this.propertyDecorations = {
      computed: new Set<string>()
    };
    this._propertyDimensions = {};
    this._propertyDimensionDefaults = {};
    this._propertyDimensionAliases = {};
    this.initialized = false;
  }

  init(model: InitModelOf<this>) {
    model = model || {} as InitModelOf<this>;
    this._init(model);
    this.initialized = true;
  }

  protected _init(model: InitModelOf<this>) {
    for (const [propertyName, value] of Object.entries(model)) {
      this._initProperty(propertyName, value);
    }
    this._initMultiDimensionalProperties(model);
  }

  /**
   * Called during initialization to write a property onto the property event emitter.
   *
   * Can be overridden to add a special init behavior for certain properties.
   */
  protected _initProperty(propertyName: string, value: any) {
    if (value === undefined || this.isPropertyDimensionAlias(propertyName)) {
      // Ignore undefined values as $.extend does.
      // Ignore properties that are actually just aliases for dimensions. Since they typically have a getter but no setter, writing them would fail.
      return;
    }
    this._writeProperty(propertyName, value);
  }

  /**
   * Sets a new value for a specific property.
   * If the new value is the same value as the old one, nothing happens.
   *
   * There are different kinds of properties.
   * - If the property is a multidimensional property, an alias for a dimension or a specific dimension, the call will be delegated to {@link setMultiDimensionalProperty} resp. {@link setPropertyDimension}.
   * - Otherwise, the call will be delegated {@link setPropertyInternal} which takes care of actually setting the property.
   *
   * @param propertyName the name of the property
   * @param newValue the new value the property should get
   * @returns true if the property has been changed, false if not.
   */
  setProperty(propertyName: string, value: any): boolean {
    if (this.isMultiDimensionalProperty(propertyName)) {
      return this.setMultiDimensionalProperty(propertyName, value);
    }
    let [name, dimension] = this.extractPropertyDimension(propertyName);
    if (name && dimension) {
      return this.setPropertyDimension(name, dimension, value);
    }
    return this.setPropertyInternal(propertyName, value);
  }

  /**
   * Sets a new value for a specific property.
   *
   * If the new value is the same value as the old one, nothing happens.
   * Otherwise, {@link _setProperty} is used to set the property and trigger a property change event.
   *
   * This default behavior can be overridden by implementing a custom \_setXy function where XY is the property name.
   * If such a function exists, it will be called instead of {@link _setProperty}
   *
   * @param propertyName the name of the property
   * @param newValue the new value the property should get
   * @returns true if the property has been changed, false if not.
   */
  protected setPropertyInternal(propertyName: string, value: any): boolean {
    if (objects.equals(this.getProperty(propertyName), value)) {
      return false;
    }
    this._callSetProperty(propertyName, value);
    return true;
  }

  /**
   * Returns the name of the property that is used to store the value.
   *
   * - For regular properties, the name won't be adjusted and returned as it is.
   * - For computed properties, the name will be prefixed with '_'.
   */
  adaptPropertyName(propertyName: string): string {
    if (this.isComputedProperty(propertyName)) {
      return '_' + propertyName;
    }
    return propertyName;
  }

  /**
   * Uses {@link _readProperty} to return the value of the property.
   */
  getProperty(propertyName: string): any {
    return this._readProperty(propertyName);
  }

  /**
   * Adapts the propertyName if necessary using {@link adaptPropertyName} and returns the value of the property.
   *
   * If the propertyName is a dimension or an alias for a dimension, {@link _readPropertyDimension} is used to read the property from the dimension object.
   */
  protected _readProperty(propertyName: string) {
    propertyName = this.adaptPropertyName(propertyName);

    // Check if the propertyName is an alias for a dimension or contains a dimension, e.g. propertyName-dimension
    let [dimensionPropertyName, dimension] = this.extractPropertyDimension(propertyName);
    if (dimensionPropertyName && dimension) {
      return this._readPropertyDimension(dimensionPropertyName, dimension);
    }

    // Regular properties are stored on the emitter itself
    return this[propertyName];
  }

  /**
   * Adapts the propertyName if necessary using {@link adaptPropertyName} and writes the value of the property onto the event emitter.
   *
   * If the propertyName is a dimension or an alias for a dimension, the value won't be written onto the emitter directly.
   * Instead, {@link _writePropertyDimension} is used to write the property to the dimension object.
   */
  protected _writeProperty(propertyName: string, value: any) {
    propertyName = this.adaptPropertyName(propertyName);

    // Check if the propertyName is an alias for a dimension or contains a dimension, e.g. propertyName-dimension
    let [dimensionPropertyName, dimension] = this.extractPropertyDimension(propertyName);
    if (dimensionPropertyName && dimension) {
      this._writePropertyDimension(dimensionPropertyName, dimension, value);
      if (dimension === 'default' && !this.initialized) {
        // Default value of default dimension may be set in constructor -> If a value is passed, it should override that default.
        // This is necessary to support setting the dimension in the model using the form 'propertyName-dimension': value
        this[dimensionPropertyName] = value;
      }
      return;
    }

    // Regular properties are stored on the emitter itself
    this[propertyName] = value;
  }

  protected _callSetProperty(propertyName: string, value: any) {
    let setFuncName = '_set' + strings.toUpperCaseFirstLetter(propertyName);
    if (this[setFuncName]) {
      this[setFuncName](value);
    } else {
      this._setProperty(propertyName, value);
    }
  }

  /**
   * Sets the value of the property 'propertyName' to 'newValue' and then triggers a propertyChange event for that property.
   *
   * It is possible to prevent the setting of the property value by using {@link Event.preventDefault}.
   *
   * @internal
   * @param propertyName the name of the property
   * @param newValue the new value the property should get
   * @returns true if the property has been changed, false if not.
   */
  _setProperty(propertyName: string, newValue: any): boolean {
    scout.assertParameter('propertyName', propertyName);
    let oldValue = this._readProperty(propertyName);
    if (objects.equals(oldValue, newValue)) {
      return false;
    }
    this._writeProperty(propertyName, newValue);
    let event = this.triggerPropertyChange(propertyName, oldValue, newValue);
    if (event.defaultPrevented) {
      // Revert to old value if property change should be prevented
      this._writeProperty(propertyName, oldValue);
      return false; // not changed
    }
    return true;
  }

  /**
   * Triggers a property change for a single property.
   */
  triggerPropertyChange<T>(propertyName: string, oldValue: T, newValue: T): PropertyChangeEvent<T, this> {
    scout.assertParameter('propertyName', propertyName);
    return this.trigger('propertyChange', {
      propertyName: propertyName,
      oldValue: oldValue,
      newValue: newValue
    }) as PropertyChangeEvent<T, this>;
  }

  /**
   * Calls the setter of the given property name if it exists. A setter has to be named setXy, where Xy is the property name.
   * If there is no setter for the property name, {@link setProperty} is called.
   */
  callSetter(propertyName: string, value: any) {
    let setterFuncName = 'set' + strings.toUpperCaseFirstLetter(propertyName);
    if (this[setterFuncName]) {
      this[setterFuncName](value);
    } else {
      this.setProperty(propertyName, value);
    }
  }

  /**
   * Adds the given properties to the list of computed properties to mark them as computed.
   * @see computedProperties
   */
  protected _addComputedProperties(properties: string[]) {
    this._decorateProperties('computed', properties);
  }

  /**
   * @see computedProperties
   */
  isComputedProperty(propertyName: string): boolean {
    return this.isPropertyDecorated('computed', propertyName);
  }

  /**
   * Contains the names of the properties that should be computed.
   *
   * - A regular property will be set on the object and therefore accessible by the property name (e.g. object.property).
   * - A computed property will be prefixed with an '_' when set on the object and therefore needs a getter to make it accessible.
   *   This makes it possible to compute the state of a property when it is accessed without having to modify the actual state.
   */
  get computedProperties(): Set<string> {
    return this.propertyDecorations['computed'];
  }

  protected _decorateProperties(decoration: keyof this['propertyDecorations'], properties: string | string[]) {
    properties = arrays.ensure(properties);
    for (const property of properties) {
      this.propertyDecorations[decoration as string].add(property);
    }
  }

  protected _undecorateProperties(decoration: keyof this['propertyDecorations'], properties: string | string[]) {
    properties = arrays.ensure(properties);
    for (const property of properties) {
      this.propertyDecorations[decoration as string].delete(property);
    }
  }

  isPropertyDecorated(decoration: keyof this['propertyDecorations'], property: string) {
    return this.propertyDecorations[decoration as string].has(property);
  }

  /**
   * Marks a property as multidimensional.
   *
   * A multidimensional property will be computed based on its dimensions. The computation result will always be a boolean, see {@link computeMultiDimensionalProperty}.
   * @param defaultValue is used for dimensions that are not know or set and affects the computation of the value, see {@link computeMultiDimensionalProperty}.
   */
  protected _addMultiDimensionalProperty(propertyName: string, defaultValue: boolean) {
    this._propertyDimensionDefaults[propertyName] = defaultValue;
    if (this.getProperty(propertyName) === undefined) {
      this.setProperty(propertyName, this.computeMultiDimensionalProperty(propertyName));
    }
  }

  /**
   * @returns the list of all registered multidimensional properties.
   */
  get multiDimensionalProperties(): string[] {
    return Object.keys(this._propertyDimensionDefaults);
  }

  /**
   * @returns whether a property is multidimensional
   */
  isMultiDimensionalProperty(propertyName: string): boolean {
    return propertyName in this._propertyDimensionDefaults;
  }

  /**
   * If the value is an object, the entries in the object are expected to be property dimensions and need to have the correct data type (`boolean`).
   * In that case, {@link setPropertyDimensions} is used to set the dimensions.
   * Otherwise, the `default` dimension for the given property is set using {@link setPropertyDimension}.
   */
  setMultiDimensionalProperty(propertyName: string, value: boolean | Record<string, boolean>): boolean {
    if (!objects.isNullOrUndefined(value) && typeof value === 'object' && Object.values(value).every(val => typeof val === 'boolean')) {
      return this.setPropertyDimensions(propertyName, value);
    }
    return this.setPropertyDimension(propertyName, 'default', !!value);
  }

  /**
   * Called during initialization to write the property dimensions and compute the multidimensional properties.
   *
   * It expects the properties to be present on the event emitter. These properties are used as default values for the dimensions.
   * If the properties are not present or undefined, the defaults from {@link _propertyDimensionDefaults} are used.
   * If the value is a boolean, the default dimension is written. If it is an object, every dimension in the object is written.
   * If a dimension has an alias and the model contains a property with the name of the alias, that property has priority over the dimension passed as part of the dimensional object.
   *
   * Afterward, the multidimensional properties are replaced with the computed values.
   */
  protected _initMultiDimensionalProperties(model: InitModelOf<this>) {
    for (const propertyName of this.multiDimensionalProperties) {
      this._initMultiDimensionalProperty(propertyName, this[propertyName]);
    }
    for (const [alias, config] of Object.entries(this._propertyDimensionAliases)) {
      if (model[alias] !== undefined) {
        this._writePropertyDimension(config.propertyName, config.dimension, model[alias]);
      }
    }
    this._updateMultiDimensionalProperties();
  }

  protected _initMultiDimensionalProperty(propertyName: string, value: boolean | Record<string, boolean>) {
    if (typeof value === 'object') {
      this._writePropertyDimensions(propertyName, value);
    } else {
      this._writePropertyDimension(propertyName, 'default', value);
    }
  }

  /**
   * Computes and sets the value for every multidimensional property.
   */
  protected _updateMultiDimensionalProperties() {
    for (const propertyName of this.multiDimensionalProperties) {
      this._updateMultiDimensionalProperty(propertyName);
    }
  }

  /**
   * Computes and sets the value for the given multidimensional property.
   */
  protected _updateMultiDimensionalProperty(propertyName: string): boolean {
    return this.setPropertyInternal(propertyName, this.computeMultiDimensionalProperty(propertyName));
  }

  /**
   * Computes the value for the given multidimensional property.
   * If the value of a dimension is different from the given defaultValue, the result of the computation will be the negated defaultValue.
   *
   * Examples:
   * - The default value is true. If no dimension is set, the value of the multidimensional property will be true.
   *   If at least one dimension is false, the value of the multidimensional property will be false.
   * - The default value is false. If no dimension is set, the value of the multidimensional property will be false.
   *   If at least one dimension is true, the value of the multidimensional property will be true.
   *
   * @param excludedDimensions the dimensions to not consider for the computation. By default, no dimensions are excluded.
   * @returns the computed value for the given property based on its dimensions.
   */
  computeMultiDimensionalProperty(propertyName: string, excludedDimensions?: string[]): boolean {
    let defaultValue = this.getPropertyDimensionDefault(propertyName);
    for (const dimension of Object.keys(this.getPropertyDimensions(propertyName))) {
      if (excludedDimensions && excludedDimensions.includes(dimension)) {
        continue;
      }
      // Dimensions having the default value are not in the map -> As soon as there is at least one entry, the loop can be aborted
      return !defaultValue;
    }
    // No values found that are different from the default value -> return defaultValue
    return defaultValue;
  }

  /**
   * Sets the value for the given property dimension by using {@link setPropertyDimensions}.
   */
  setPropertyDimension(propertyName: string, dimension: string, value: boolean): boolean {
    let dimensions = $.extend({}, this._propertyDimensions[propertyName], {[dimension]: value});
    return this.setPropertyDimensions(propertyName, dimensions);
  }

  /**
   * Sets the new dimensional values for the given property.
   *
   * For each dimension, a property change event is triggered with the property name and the dimension separated by '-'.
   *
   * Example: For the property 'prop' and the dimension 'dim' there will be a property change event with the name 'prop-dim'.
   * You can listen for this event in the same way as for regular property change events which is: `emitter.on('propertyChange:prop-dim')`.
   *
   * After setting the dimensions and triggering the events, the main property will be updated with the new value based on every dimension.
   * @param propertyName the name of the multidimensional property
   * @param dimension the new dimensions to be set for the given propertyName. If a specific dimension is not set in the map, the default value for that multidimensional property is used.
   *                  Hence, if the map is empty, the multidimensional property will be set to the default value
   */
  setPropertyDimensions(propertyName: string, dimensions: Record<string, boolean>): boolean {
    // If a dimension is not set, it either means the dimension does not exist or it should point to the default value.
    // To support the second case, the dimension object is enriched with the default values for the existing keys.
    // This also makes sure that passing an empty dimension object will reset the dimensions to the default (which eventually deletes them, see _writePropertyDimension)
    dimensions = $.extend({}, dimensions);
    for (const dimension of Object.keys(this._propertyDimensions[propertyName] || {})) {
      dimensions[dimension] = scout.nvl(dimensions[dimension], this.getPropertyDimensionDefault(propertyName, dimension));
    }

    let changed = false;
    for (const [dimension, value] of Object.entries(dimensions)) {
      let internalPropertyName = propertyName + '-' + dimension;
      let alias = this.getAliasForPropertyDimension(propertyName, dimension);
      if (alias) {
        internalPropertyName = alias;
      }
      if (this.setPropertyInternal(internalPropertyName, value)) {
        changed = true;
      }
    }

    // Do nothing if no dimension has changed and the computed property has been set.
    // If the computed property has not been set yet, it needs to be done now.
    if (!changed && this.getProperty(propertyName) !== undefined) {
      return false;
    }

    // Update computed property based on dimensions
    return this._updateMultiDimensionalProperty(propertyName);
  }

  /**
   * @returns the value for a specific property dimension or the default value of the property if the dimension is not set or known.
   */
  getPropertyDimension(propertyName: string, dimension: string): boolean {
    return this._readPropertyDimension(propertyName, dimension);
  }

  /**
   * @returns an object containing all dimensions with their values for a specific property.
   */
  getPropertyDimensions(propertyName: string): Record<string, boolean> {
    return $.extend({}, this._propertyDimensions[propertyName]);
  }

  /**
   * @returns the existing dimensions for a property extended with the given dimension
   */
  extendPropertyDimensions(propertyName: string, dimension: string, value: boolean) {
    return $.extend(this.getPropertyDimensions(propertyName), {[dimension]: value});
  }

  /**
   * Adds an alias for a property dimension with the same name. If a dimension has an alias, the dimension looks like a regular property but behaves like a dimension.
   *
   * Meaning: {@link setProperty}/{@link getProperty} can be called using the alias only without the need to add the name of the multidimensional property as well separated with `-`.
   * The same applies to the propertyChange event: changing the property will also trigger a propertyChange event with the name of the alias (e.g. propertyChange:alias).
   * Additionally, because it is actually a dimension, setting the property will also update the multidimensional property considering this dimension.
   *
   * To further simulate a regular property, it is recommended to add a getter with the name of the alias.
   */
  protected _addPropertyDimensionAlias(propertyName: string, alias: string, config?: PropertyDimensionAliasConfig) {
    config = $.extend({inverted: false, dimension: alias}, config);
    this._propertyDimensionAliases[alias] = {propertyName, ...config as Required<PropertyDimensionAliasConfig>};
  }

  isPropertyDimensionAlias(dimension: string): boolean {
    return !!this._propertyDimensionAliases[dimension];
  }

  /**
   * @returns the alias for a property dimension, if there is an alias registered for that dimension
   */
  getAliasForPropertyDimension(propertyName: string, dimension: string): string {
    for (const [alias, config] of Object.entries(this._propertyDimensionAliases)) {
      if (config.propertyName === propertyName && config.dimension === dimension) {
        return alias;
      }
    }
    return null;
  }

  /**
   * @returns a tuple with the property name and dimension for an alias, if there is an alias registered for that name.
   */
  getPropertyDimensionForAlias(alias: string): [string, string] {
    let config = this._propertyDimensionAliases[alias];
    if (config) {
      return [config.propertyName, config.dimension];
    }
    return [null, null];
  }

  /**
   * @returns the default value for a multidimensional property. If a dimension is specified and that dimension belongs to an inverted alias, the default value will be inverted.
   */
  getPropertyDimensionDefault(propertyName: string, dimension?: string): boolean {
    let defaultValue = this._propertyDimensionDefaults[propertyName];
    if (this._propertyDimensionAliases[dimension]?.inverted) {
      return !defaultValue;
    }
    return defaultValue;
  }

  protected _readPropertyDimension(propertyName: string, dimension: string): boolean {
    let dimensions = this._propertyDimensions[propertyName] || {};
    return scout.nvl(dimensions[dimension], this.getPropertyDimensionDefault(propertyName, dimension));
  }

  /**
   * Writes a specific dimension. If the dimension has the same value as the default value of the property, the dimension will be deleted.
   */
  protected _writePropertyDimension(propertyName: string, dimension: string, value: boolean) {
    let dimensions = this._propertyDimensions[propertyName] || {};
    if (value === this.getPropertyDimensionDefault(propertyName, dimension)) {
      delete dimensions[dimension];
    } else {
      dimensions[dimension] = value;
    }
    this._propertyDimensions[propertyName] = dimensions;
  }

  /**
   * Overrides all existing dimensions for a property with the new ones.
   */
  protected _writePropertyDimensions(propertyName: string, dimensions: Record<string, boolean>) {
    for (const [dimension, value] of Object.entries(dimensions || {})) {
      this._writePropertyDimension(propertyName, dimension, value);
    }
  }

  /**
   * If the propertyName contains the name of a multidimensional property and a dimension separated by '-',
   * an array with the name of the property and the name of the dimension is returned.
   * Otherwise, the returned array contains two null values.
   */
  extractPropertyDimension(propertyName: string): [string, string] {
    // Check if the propertyName is an alias for a dimension
    let [name, dimension] = this.getPropertyDimensionForAlias(propertyName);
    if (name && dimension) {
      return [name, dimension];
    }

    // Check if the propertyName contains a dimension, e.g. propertyName-dimension
    if (!propertyName.includes('-')) {
      return [null, null];
    }
    [name, dimension] = propertyName.split('-');
    if (this.isMultiDimensionalProperty(name)) {
      return [name, dimension];
    }
    return [null, null];
  }
}

export type PropertyDecoration = 'computed';

export interface PropertyDimensionAliasConfig {
  /**
   * If set to true, the value will be inverted for the computation of the multidimensional property.
   *
   * Example: If the multidimensional property is `enabled` with the defaultValue `true` and the alias is called `locked`, the element should be disabled if locked is true instead of false -> alias needs to be inverted.
   *
   * Default is false.
   */
  inverted?: boolean;
  /**
   * If specified, this name will be used as dimension name.
   *
   * This is relevant when the dimension is accessed directly without using the alias, e.g. using {@link setPropertyDimension} {@link getPropertyDimension}.
   *
   * If not specified, the alias will be used as dimension name.
   */
  dimension?: string;
}
