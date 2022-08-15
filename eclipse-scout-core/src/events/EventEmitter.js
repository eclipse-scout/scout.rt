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
import {EventSupport} from '../index';

export default class EventEmitter {

  constructor() {
    /** @type {EventSupport} */
    this.events = this._createEventSupport();
  }

  _createEventSupport() {
    return new EventSupport();
  }

  /**
   * @param {string} type
   * @param {Event|object} [event]
   */
  trigger(type, event) {
    event = event || {};
    event.source = this;
    this.events.trigger(type, event);
  }


  /**
   * Registers the given event handler for the event specified by the type param.
   * The function will only be called once. After that it is automatically de-registered using {@link off}.
   *
   * @param {string} type One or more event names separated by space.
   * @param {function} handler Event handler executed when the event is triggered. An event object is passed to the function as first parameter
   */
  one(type, handler) {
    this.events.one(type, handler);
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   *
   * @param {string} type One or more event names separated by space.
   * @param {function} handler Event handler executed when the event is triggered. An event object is passed to the function as first parameter.
   **/
  on(type, handler) {
    return this.events.on(type, handler);
  }

  /**
   * De-registers the given event handler for the event specified by the type param.
   *
   * @param {string} type One or more event names separated by space.<br/>
   *      Important: the string must be equal to the one used for {@link on} or {@link one}. This also applies if a string containing multiple types separated by space was used.
   * @param {function} [handler] The exact same event handler that was used for registration using {@link on} or {@link one}.
   *      If no handler is specified, all handlers are de-registered for the given type.
   */
  off(type, handler) {
    this.events.off(type, handler);
  }

  addListener(listener) {
    this.events.addListener(listener);
  }

  removeListener(listener) {
    this.events.removeListener(listener);
  }

  /**
   * Adds an event handler using {@link one} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   * @returns {Promise}
   */
  when(type) {
    return this.events.when(type);
  }
}
