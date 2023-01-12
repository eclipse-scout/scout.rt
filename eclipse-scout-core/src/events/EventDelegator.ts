/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Event, EventDelegatorOptions, EventEmitter, EventHandler, EventListener, objects, PropertyChangeEvent, PropertyEventEmitter, scout} from '../index';

/**
 * Delegates events between two {@link EventEmitter}s.
 */
export class EventDelegator {
  source: EventEmitter;
  target: EventEmitter;
  callSetter: boolean;
  delegateProperties: string[];
  excludeProperties: string[];
  delegateEvents: string[];
  delegateAllProperties: boolean;
  delegateAllEvents: boolean;
  protected _mirrorListener: EventListener;
  protected _destroyHandler: EventHandler;

  constructor(source: EventEmitter, target: EventEmitter, options?: EventDelegatorOptions) {
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

  protected _installSourceListener() {
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

  protected _uninstallSourceListener() {
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

  protected _onSourceEvent(event: Event) {
    if (event.type === 'propertyChange') {
      this._onSourcePropertyChange(event as PropertyChangeEvent<any>);
    } else if (this.delegateAllEvents || this.delegateEvents.indexOf(event.type) > -1) {
      this.target.trigger(event.type, event);
    }
  }

  protected _onSourcePropertyChange(event: PropertyChangeEvent<any>) {
    if (this.excludeProperties.indexOf(event.propertyName) > -1) {
      return;
    }
    if (this.delegateAllProperties || this.delegateProperties.indexOf(event.propertyName) > -1) {
      if (EventDelegator.equalsProperty(event.propertyName, this.target, event.newValue)) {
        return;
      }
      if (this.callSetter) {
        (this.target as PropertyEventEmitter).callSetter(event.propertyName, event.newValue);
      } else {
        this.target.trigger(event.type, event);
      }
    }
  }

  static equalsProperty(propName: string, obj: object, value: any): boolean {
    let propValue = obj[propName];
    // Compare arrays using arrays.equals()
    if (Array.isArray(value) && Array.isArray(propValue)) {
      return arrays.equals(value as [], propValue as []);
    }
    return objects.equals(propValue, value);
  }

  static create(source: EventEmitter, target: EventEmitter, options: EventDelegatorOptions): EventDelegator {
    if ((options.delegateProperties && options.delegateProperties.length > 0) ||
      (options.delegateEvents && options.delegateEvents.length > 0) ||
      options.delegateAllProperties ||
      options.delegateAllEvents) {
      return new EventDelegator(source, target, options);
    }
    return null;
  }
}
