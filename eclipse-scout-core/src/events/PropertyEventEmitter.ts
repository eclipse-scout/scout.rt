/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, EventEmitter, objects, PropertyChangeEvent, PropertyEventMap, scout, strings} from '../index';

export class PropertyEventEmitter extends EventEmitter {
  declare eventMap: PropertyEventMap;
  declare self: PropertyEventEmitter;

  /**
   * Contains the names of properties that are decorated grouped by the decoration. Subclasses can add their own decorations.
   */
  propertyDecorations: Record<PropertyDecoration, Set<string>>;

  constructor() {
    super();
    this.events.registerSubTypePredicate('propertyChange', (event, propertyName) => event.propertyName === propertyName);
    this.propertyDecorations = {
      computed: new Set<string>()
    };
  }

  /**
   * Sets a new value for a specific property. If the new value is the same value as the old one, nothing happens.
   * Otherwise, {@link _setProperty} is used to set the property and trigger a property change event.
   *
   * This default behavior can be overridden by implementing a custom \_setXy function where XY is the property name.
   * If such a function exists, it will be called instead of {@link _setProperty}
   * @param propertyName the name of the property
   * @param newValue the new value the property should get
   * @returns true if the property has been changed, false if not.
   */
  setProperty(propertyName: string, value: any): boolean {
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
   * Adapts the propertyName if necessary using {@link  adaptPropertyName} and returns the value of the property.
   */
  getProperty(propertyName: string): any {
    propertyName = this.adaptPropertyName(propertyName);
    return this[propertyName];
  }

  /**
   * Adapts the propertyName if necessary using {@link  adaptPropertyName} and write the value of the property.
   */
  protected _writeProperty(propertyName: string, value: any) {
    propertyName = this.adaptPropertyName(propertyName);
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
    let oldValue = this.getProperty(propertyName);
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
}

export type PropertyDecoration = 'computed';
