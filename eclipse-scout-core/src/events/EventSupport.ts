/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, EventHandler, EventListener, scout, strings} from '../index';
import $ from 'jquery';

export class EventSupport {
  protected _eventListenersByType = new Map<string, Set<EventListener>>();
  protected _eventListenersByHandler = new Map<EventHandler, Set<EventListener>>();
  protected _subTypeProviders = new Map<string, EventSubTypeProvider>();

  protected _assertFunc(func: EventHandler) {
    if (!func) {
      throw new Error('Missing callback function');
    }
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   *
   * @param type One or more event names separated by space.
   * @param func Event handler executed when the event is triggered. An event object is passed to the function as first parameter.
   * @param origFunc Used internally when func is registered with {@link one}. The property is set on the listener
   *   object so the event-handler can be de-registered by using the original function.
   */
  on(type: string, func: EventHandler, origFunc?: EventHandler): EventListener {
    this._assertFunc(func);
    let listener = {
      type: type,
      func: func,
      origFunc: origFunc
    };
    this.addListener(listener);
    return listener;
  }

  /**
   * Registers the given event handler for the event specified by the type param.
   * The function will only be called once. After that it is automatically de-registered using {@link off}.
   *
   * @param type One or more event names separated by space.
   * @param func Event handler executed when the event is triggered. An event object is passed to the function as first parameter
   */
  one(type: string, func: EventHandler): EventListener {
    this._assertFunc(func);
    let offFunc = event => {
      this.off(type, offFunc);
      func(event);
    };
    return this.on(type, offFunc, func);
  }

  /**
   * De-registers the given event handler for the event specified by the type param.
   *
   * @param type One or more event names separated by space.<br/>
   *      Important: the string must be equal to the one used for {@link on} or {@link one}. This also applies if a string containing multiple types separated by space was used.
   * @param func The exact same event handler that was used for registration using {@link on} or {@link one}.
   *      If no handler is specified, all handlers are de-registered for the given type.
   */
  off(type: string, func?: EventHandler) {
    type = strings.trim(type);

    if (!type && !func) {
      // nothing to remove
      return;
    }

    // validates the types of a listener against the given type-filter
    const typePredicate = (listener: EventListener) => type === listener.type;

    if (func) {
      // func-filter is set -> get listeners by func
      const listenersByHandler = this._getListenersByHandler(func);
      if (!listenersByHandler.size) {
        // nothing to remove
        return;
      }

      // if type is set only process those listeners matching the typePredicate
      for (const listener of (type ? [...listenersByHandler].filter(typePredicate) : listenersByHandler)) {
        // remove listeners by handler
        listenersByHandler.delete(listener);

        // get additional handler of listener
        const additionalHandler = func === listener.func ? listener.origFunc : listener.func;
        if (additionalHandler) {
          // remove listeners by additional handler
          this._removeListenerByHandler(additionalHandler, listener);
        }

        if (!type) {
          // no type -> remove from all types
          for (const t of this._eventListenersByType.keys()) {
            this._removeListenerByType(t, listener);
          }
        }

        // type set -> remove from all matching listenersByType
        for (const t of this._splitTypes(type)) {
          this._removeListenerByType(t, listener);
        }
      }

      // remove entry from map if there are no listeners left
      if (listenersByHandler.size === 0) {
        this._eventListenersByHandler.delete(func);
      }
      return;
    }

    // type set -> remove from all matching listenersByType
    for (const t of this._splitTypes(type)) {
      // get all listeners for type
      const listenersByType = this._getListenersByType(t);

      // remove all listeners matching the type predicate
      for (const listener of [...listenersByType].filter(typePredicate)) {
        listenersByType.delete(listener);

        // remove listeners by handlers
        this._removeListenerByHandler(listener.func, listener);
        this._removeListenerByHandler(listener.origFunc, listener);
      }

      // remove entry from map if there are no listeners left
      if (listenersByType.size === 0) {
        this._eventListenersByType.delete(t);
      }
    }
  }

  /**
   * Adds an event handler using {@link one} and returns a promise.
   * The promise is resolved as soon as the event is triggered.
   */
  when(type: string): JQuery.Promise<Event> {
    let deferred = $.Deferred();
    this.one(type, deferred.resolve.bind(deferred));
    return deferred.promise();
  }

  /**
   * Adds the given {@link EventListener} if it is not present.
   */
  addListener(listener: EventListener) {
    if (!listener) {
      return;
    }

    // add listener for handlers
    this._addListenerByHandler(listener.func, listener);
    this._addListenerByHandler(listener.origFunc, listener);

    const listenerType = strings.trim(listener.type);
    if (!listenerType) {
      // listener has no type -> add for all events
      this._addListenerByType(null, listener);
      return;
    }

    // add listener for all types given
    const types = this._splitTypes(listenerType);
    for (const type of types) {
      this._addListenerByType(type, listener);
    }
  }

  /**
   * Removes the given {@link EventListener} if it is present.
   */
  removeListener(listener: EventListener) {
    if (!listener) {
      return;
    }

    // remove listeners by handlers
    this._removeListenerByHandler(listener.func, listener);
    this._removeListenerByHandler(listener.origFunc, listener);

    const listenerType = strings.trim(listener.type);
    if (!listenerType) {
      // listener has no type -> remove from all-events-listeners
      this._removeListenerByType(null, listener);
      return;
    }

    // remove listener for all types given
    const types = this._splitTypes(listenerType);
    for (const type of types) {
      // remove listeners by specific type
      this._removeListenerByType(type, listener);
    }
  }

  /**
   * Counts all {@link EventListener}s that match the given filters.
   *
   * The type-filter will match all listeners that are triggered by all given types.
   * For example if there are two listeners registered with type 'lorem' and 'lorem ipsum dolor' the filter 'lorem' will match 2 listeners.
   * In this example, the type-filter 'ipsum dolor' will match 1 listener like 'dolor' or 'dolor ipsum lorem'.
   *
   * The func-filter will match all listeners that are registered using this exact {@link EventHandler}.
   */
  count(type?: string, func?: EventHandler): number {
    if (this._eventListenersByType.size === 0) {
      return 0;
    }

    type = strings.trim(type);

    if (!type && !func) {
      // no type- and no func-filter -> combine all listener sets in order to remove duplicates (e.g. because a listener was added for multiple types) and return size
      return [...this._eventListenersByType.values()]
        .reduce((a, b) => new Set([...a, ...b]), new Set())
        .size;
    }

    const types = [...this._splitTypes(type)];
    // validates the types of a listener against all types of the given type-filter
    const typePredicate = (listener: EventListener) => {
      const listenerTypes = this._splitTypes(listener.type);
      return types.every(t => listenerTypes.has(t));
    };

    if (func) {
      // func-filter is set -> get listeners by func
      const listenersByHandler = this._getListenersByHandler(func);

      if (!type) {
        // only func-filter -> count all listeners found by func-filter
        return listenersByHandler.size;
      }

      // type-filter set -> only count those matching the typePredicate
      return [...listenersByHandler].filter(typePredicate).length;
    }

    // type-filter is set -> types contains at least one element
    // if listeners are registered for multiple types they are present in each single-type-set
    const listenersByType = this._getListenersByType(types[0]);

    // count all relevant listeners matching the predicate
    return [...listenersByType].filter(typePredicate).length;
  }

  /**
   * Returns a list of unique single types of all registered listeners.
   * Example: If there are two listeners registered for 'lorem' and 'lorem ipsum dolor' the multi-type is split and ['lorem', 'ipsum', 'dolor'] is returned.
   */
  types(): string[] {
    const types = new Set(this._eventListenersByType.keys());
    types.delete(null);
    return [...types];
  }

  trigger(type: string, event?: Event) {
    type = strings.trim(type);
    if (!type) {
      return;
    }

    event = event || {} as Event;
    event.type = type;

    // get relevant types, always add all-events-listeners, i.e. listeners registered for type = null
    const relevantTypes = new Set([null, event.type]);

    // consider subType if present
    const subTypeProvider = this._subTypeProviders.get(event.type);
    if (subTypeProvider) {
      const subType = subTypeProvider(event);
      if (subType) {
        relevantTypes.add(`${type}:${subType}`);
      }
    }

    // remove all types from relevantTypes for which no listeners exist
    for (const relevantType of relevantTypes) {
      if (!this._eventListenersByType.has(relevantType)) {
        relevantTypes.delete(relevantType);
      }
    }

    if (!relevantTypes.size) {
      // there are no relevant listeners -> return
      return;
    }

    // collect all listeners for the relevant types in a set in order to remove duplicates
    const listeners = [...relevantTypes]
      .map(t => this._getListenersByType(t))
      .reduce((a, b) => new Set([...a, ...b]), new Set());

    // trigger all listeners
    for (const listener of listeners) {
      listener.func(event);
    }
  }

  /**
   * Gets a {@link Set} of {@link EventListener}s for the requested type.
   * The result of this method is never `null`.
   * If the create-flag is set, the {@link Set} is added to {@link _eventListenersByType}.
   */
  protected _getListenersByType(type: string, create = false): Set<EventListener> {
    return this._getListenersByIdentifier(this._eventListenersByType, type || null, create);
  }

  /**
   * Adds an {@link EventListener} to {@link _eventListenersByType} for the given identifier.
   */
  protected _addListenerByType(type: string, listener: EventListener) {
    this._addListenerByIdentifier(this._eventListenersByType, type || null, listener);
  }

  /**
   * Removes an {@link EventListener} from {@link _eventListenersByType} for the given identifier.
   */
  protected _removeListenerByType(type: string, listener: EventListener) {
    this._removeListenerByIdentifier(this._eventListenersByType, type || null, listener);
  }

  /**
   * Gets a {@link Set} of {@link EventListener}s for the requested {@link EventHandler}.
   * The result of this method is never `null`.
   * If the create-flag is set, the {@link Set} is added to {@link _eventListenersByHandler}.
   */
  protected _getListenersByHandler(handler: EventHandler, create = false): Set<EventListener> {
    if (!handler) {
      return new Set();
    }

    return this._getListenersByIdentifier(this._eventListenersByHandler, handler, create);
  }

  /**
   * Adds an {@link EventListener} to {@link _eventListenersByHandler} for the given identifier.
   */
  protected _addListenerByHandler(handler: EventHandler, listener: EventListener) {
    if (!handler) {
      return;
    }

    this._addListenerByIdentifier(this._eventListenersByHandler, handler, listener);
  }

  /**
   * Removes an {@link EventListener} from {@link _eventListenersByHandler} for the given identifier.
   */
  protected _removeListenerByHandler(handler: EventHandler, listener: EventListener) {
    if (!handler) {
      return;
    }

    this._removeListenerByIdentifier(this._eventListenersByHandler, handler, listener);
  }

  /**
   * Gets a {@link Set} of {@link EventListener}s for the requested identifier from the given {@link Map}.
   * The result of this method is never `null`.
   * If the create-flag is set, the {@link Set} is added to the {@link Map}.
   */
  protected _getListenersByIdentifier<TIdentifier extends string | EventHandler>(listenerMap: Map<TIdentifier, Set<EventListener>>, identifier: TIdentifier, create = false): Set<EventListener> {
    if (!listenerMap) {
      return new Set();
    }

    let listeners = listenerMap.get(identifier);
    if (listeners) {
      return listeners;
    }

    listeners = new Set();
    if (!create) {
      return listeners;
    }

    listenerMap.set(identifier, listeners);
    return listeners;
  }

  /**
   * Adds an {@link EventListener} to the given {@link Map} for the given identifier.
   */
  protected _addListenerByIdentifier<TIdentifier extends string | EventHandler>(listenerMap: Map<TIdentifier, Set<EventListener>>, identifier: TIdentifier, listener: EventListener) {
    if (!listenerMap || !listener) {
      return;
    }

    this._getListenersByIdentifier(listenerMap, identifier, true).add(listener);
  }

  /**
   * Removes an {@link EventListener} from the given {@link Map} for the given identifier.
   */
  protected _removeListenerByIdentifier<TIdentifier extends string | EventHandler>(listenerMap: Map<TIdentifier, Set<EventListener>>, identifier: TIdentifier, listener: EventListener) {
    if (!listenerMap || !listener) {
      return;
    }

    const listeners = this._getListenersByIdentifier(listenerMap, identifier);
    listeners.delete(listener);

    // remove entry from map if there are no listeners left
    if (listeners.size === 0) {
      listenerMap.delete(identifier);
    }
  }

  /**
   * Splits the given type into unique single types (e.g.: 'lorem ipsum dolor' -> ['lorem', 'ipsum', 'dolor']).
   */
  protected _splitTypes(type: string): Set<string> {
    if (!type) {
      return new Set();
    }
    return new Set(type.split(' ').filter(Boolean));
  }

  /**
   * Registers a {@link EventSubTypeProvider} for a specific event type.
   */
  registerSubTypeProvider(type: string, provider: EventSubTypeProvider) {
    scout.assertParameter('type', type);
    scout.assertParameter('provider', provider);
    this._subTypeProviders.set(type, provider);
  }
}

/**
 * Builds a complete type for an {@link Event}.
 * Consider a `FancyEvent` that is always triggered with the type 'fancy' but has a property `subType` which can hold the values 'foo' or 'bar'.
 * If there is an {@link EventSubTypeProvider} returning this `subType`, listeners registered for 'fancy:foo' or 'fancy:bar' will be executed when the `subType` matches.
 */
export type EventSubTypeProvider = (type: Event) => string;
