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
import {Event, EventEmitter, objects, scout, strings} from '../index';

export default class PropertyEventEmitter extends EventEmitter {
  constructor() {
    super();
    this.events.registerSubTypePredicate('propertyChange', (event, propertyName) => {
      return event.propertyName === propertyName;
    });
  }

  /**
   * Sets a new value for a specific property. If the new value is the same value as the old one, nothing happens.
   * Otherwise, {@link _setProperty} is used to set the property and trigger a property change event.
   * <p>
   * This default behavior can be overridden by implementing a custom \_setXy function where XY is the property name.
   * If such a function exists, it will be called instead of {@link _setProperty}
   * @param {string} propertyName the name of the property
   * @param {any} newValue the new value the property should get
   * @return {boolean} true if the property has been changed, false if not.
   */
  setProperty(propertyName, value) {
    if (objects.equals(this[propertyName], value)) {
      return false;
    }
    this._callSetProperty(propertyName, value);
    return true;
  }

  _callSetProperty(propertyName, value) {
    let setFuncName = '_set' + strings.toUpperCaseFirstLetter(propertyName);
    if (this[setFuncName]) {
      this[setFuncName](value);
    } else {
      this._setProperty(propertyName, value);
    }
  }

  /**
   * Sets the value of the property 'propertyName' to 'newValue' and then triggers a propertyChange event for that property.
   * <p>
   * It is possible to prevent the setting of the property value by using {@link Event.preventDefault}.
   *
   * @param {string} propertyName the name of the property
   * @param {any} newValue the new value the property should get
   * @return {boolean} true if the property has been changed, false if not.
   */
  _setProperty(propertyName, newValue) {
    scout.assertParameter('propertyName', propertyName);
    let oldValue = this[propertyName];
    if (objects.equals(oldValue, newValue)) {
      return false;
    }
    this[propertyName] = newValue;
    let event = this.triggerPropertyChange(propertyName, oldValue, newValue);
    if (event.defaultPrevented) {
      // Revert to old value if property change should be prevented
      this[propertyName] = oldValue;
      return false; // not changed
    }
    return true;
  }

  /**
   * Triggers a property change for a single property.
   */
  triggerPropertyChange(propertyName, oldValue, newValue) {
    scout.assertParameter('propertyName', propertyName);
    let event = new Event({
      propertyName: propertyName,
      oldValue: oldValue,
      newValue: newValue
    });
    this.trigger('propertyChange', event);
    return event;
  }

  /**
   * Calls the setter of the given property name if it exists. A setter has to be named setXy, where Xy is the property name.
   * If there is no setter for the property name, {@link setProperty} is called.
   */
  callSetter(propertyName, value) {
    let setterFuncName = 'set' + strings.toUpperCaseFirstLetter(propertyName);
    if (this[setterFuncName]) {
      this[setterFuncName](value);
    } else {
      this.setProperty(propertyName, value);
    }
  }
}
