/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, objects, scout} from '../index';

/**
 * Delegates events between two {@link EventEmitter}s.
 */
export default class EventDelegator {

  /**
   * @param {EventEmitter} source
   * @param {EventEmitter} target
   * @param {object} [options]
   * @param {boolean} [options.callSetter] True, to call the setter on the target on a property change event.
   * @param {string[]} [options.delegateProperties] An array of all properties to be delegated from the source to the target when changed on the source. Default is [];
   * @param {string[]} [options.excludeProperties] An array of all properties to be excluded from delegating to the target in all cases. Default is [].
   * @param {string[]} [options.delegateEvents] An array of all events to be delegated from the source to the target when triggered on the source. Default is [].
   * @param {string[]} [options.delegateAllProperties] True, to delegate all property changes from the source to the target. Default is false.
   * @param {string[]} [options.delegateAllEvents] True, to delegate all events from the source to the target. Default is false.
   */
  constructor(source, target, options) {
    options = options || {};
    this.source = source;
    this.target = target;
    this.callSetter = scout.nvl(options.callSetter, true);
    this.delegateProperties = options.delegateProperties || [];
    this.excludeProperties = options.excludeProperties || [];
    this.delegateEvents = options.delegateEvents || [];
    this.delegateAllProperties = !!options.delegateAllProperties;
    this.delegateAllEvents = !!options.delegateAllEvents;
    this._mirrorListener = null;
    this._destroyHandler = null;

    this._installSourceListener();
  }

  destroy() {
    this._uninstallSourceListener();
  }

  _installSourceListener() {
    if (this._mirrorListener) {
      throw new Error('source listeners already installed.');
    }
    this._mirrorListener = {
      func: this._onSourceEvent.bind(this)
    };
    this.source.events.addListener(this._mirrorListener);
    this._destroyHandler = this._uninstallSourceListener.bind(this);
    this.source.on('destroy', this._destroyHandler);
    this.target.on('destroy', this._destroyHandler);
  }

  _uninstallSourceListener() {
    if (this._mirrorListener) {
      this.source.events.removeListener(this._mirrorListener);
      this._mirrorListener = null;
    }
    if (this._destroyHandler) {
      this.source.off('destroy', this._destroyHandler);
      this.target.off('destroy', this._destroyHandler);
      this._destroyHandler = null;
    }
  }

  _onSourceEvent(event) {
    if (event.type === 'propertyChange') {
      this._onSourcePropertyChange(event);
    } else if (this.delegateAllEvents || this.delegateEvents.indexOf(event.type) > -1) {
      this.target.trigger(event.type, event);
    }
  }

  _onSourcePropertyChange(event) {
    if (this.excludeProperties.indexOf(event.propertyName) > -1) {
      return;
    }
    if (this.delegateAllProperties || this.delegateProperties.indexOf(event.propertyName) > -1) {
      if (EventDelegator.equalsProperty(event.propertyName, this.target, event.newValue)) {
        return;
      }
      if (this.callSetter) {
        (/** @type {PropertyEventEmitter} */ this.target).callSetter(event.propertyName, event.newValue);
      } else {
        this.target.trigger(event.type, event);
      }
    }
  }

  static equalsProperty(propName, obj, value) {
    let propValue = obj[propName];
    // Compare arrays using arrays.equals()
    if (Array.isArray(value) && Array.isArray(propValue)) {
      return arrays.equals(value, propValue);
    }
    return objects.equals(propValue, value);
  }

  static create(source, target, options) {
    if ((options.delegateProperties && options.delegateProperties.length > 0) ||
      (options.delegateEvents && options.delegateEvents.length > 0) ||
      options.delegateAllProperties ||
      options.delegateAllEvents) {
      return new EventDelegator(source, target, options);
    }
    return null;
  }
}
