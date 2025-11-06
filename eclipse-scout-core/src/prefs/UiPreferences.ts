/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ErrorHandler, objects, ObjectWithType, scout, UiPreferencesDo, UiPreferencesStore, UiPreferencesUpdateDo} from '../index';
import $ from 'jquery';

let handlers = new Map<UiPreferencesHandlerId, UiPreferencesHandler>();

/**
 * A singleton that loads and stores all UI preferences for the current user. It is populated during the start of the
 * application, so the preferences data can be accessed synchronously. After the data is loaded, it is passed to
 * all registered {@link UiPreferencesHandler}s to handle component-specific parts the preferences data.
 */
export class UiPreferences implements ObjectWithType {

  /**
   * Registers a new handler to receive and provide parts of the global {@link UiPreferencesDo} object. Any value can
   * be used as the ID, as long it is the same that is later used to call {@link UiPreferences.scheduleStore}.
   *
   * If the given arguments are missing or if there is already a handler registered for the same ID, an error is thrown.
   */
  static registerHandler(handlerId: UiPreferencesHandlerId, handler: UiPreferencesHandler) {
    scout.assertParameter('handlerId', handlerId);
    scout.assertParameter('handler', handler);
    if (handlers.has(handlerId)) {
      throw new Error('Already registered'); // prevent re-registration
    }
    handlers.set(handlerId, handler);
  }

  /**
   * Unregisters the previously registered {@link UiPreferencesHandler}. Normally, this only has to be done to replace
   * and registered handlers with a different instance for the same ID.
   */
  static unregisterHandler(handlerId: UiPreferencesHandlerId): boolean {
    return handlers.delete(handlerId);
  }

  /**
   * Returns all registered {@link UiPreferencesHandler}s.
   */
  static getHandlers(): UiPreferencesHandler[] {
    return [...handlers.values()];
  }

  /**
   * Returns the {@link UiPreferencesHandler} for the given ID, or `null` if not such handler was registered.
   */
  static getHandler(handlerId: UiPreferencesHandlerId): UiPreferencesHandler {
    return handlers.get(handlerId) ?? null;
  }

  // --------------------------------------

  objectType: string;

  protected _store: UiPreferencesStore;
  protected _storeTimeoutId = 0;
  protected _modifiedHandlers = new Set<UiPreferencesHandler>();

  /** Loaded preferences data (never `null`) */
  protected _preferences: UiPreferencesDo;

  // --------------------------------------

  constructor() {
    this._initStore();
  }

  protected _initStore() {
    this.replaceStore(scout.create(UiPreferencesStore));
  }

  /**
   * Replaces the {@link UiPreferencesStore} for this singleton object. This is intended to be used in tests only.
   * In a normal application, the store should not be changed dynamically. Instead, register the desired store
   * implementation via {@link ObjectFactory}.
   *
   * **Important:** This method clears internal data structures, but does *not* automatically reload preferences
   * from the new store. To do so, {@link load} has to be called manually.
   *
   * @returns the old store
   */
  replaceStore(store: UiPreferencesStore): UiPreferencesStore {
    let oldStore = this._store;
    this._store = scout.assertParameter('store', store);
    this._initPreferences(null); // reset cached data
    return oldStore;
  }

  bootstrap(): JQuery.Promise<void> {
    return $.resolvedPromise()
      .then(() => this._subscribeForUpdates())
      .then(() => this.load());
  }

  /**
   * Loads preferences from the {@link UiPreferencesStore} into this singleton object.
   */
  load(): JQuery.Promise<void> {
    return this._store.load()
      .then(preferences => this._initPreferences(preferences));
  }

  /**
   * Writes the current state of this singleton object to the {@link UiPreferencesStore}.
   */
  store(): JQuery.Promise<void> {
    this._processModifiedHandlers();
    return this._store.store(this._preferences);
  }

  /**
   * Schedules a task to call {@link store}. This method is to be called whenever a preference has been changed.
   * By scheduling a task rather than storing immediately, we can coalesce multiple store requests into a single one.
   *
   * The given argument specifies which {@link UiPreferencesHandler} has been modified. Before the data is actually
   * stored, all modified handlers will be called back to update the global {@link UiPreferencesDo} object. If this
   * method is called without argument, _all_ registered handlers are marked as modified.
   */
  scheduleStore(modifiedHandlerId?: UiPreferencesHandlerId) {
    this._markHandlerAsModified(modifiedHandlerId);

    clearTimeout(this._storeTimeoutId);
    this._storeTimeoutId = setTimeout(() => {
      this.store()
        .catch(error => {
          // Unable to store UI preferences -> log silently
          scout.create(ErrorHandler, {displayError: false, sendError: true}).handle(error);
        });
    });
  }

  /**
   * Clears all scheduled {@link store} tasks without actually storing anything (useful in tests).
   */
  tearDown() {
    clearTimeout(this._storeTimeoutId);
  }

  /**
   * Marks the given handler as modified, i.e. the handler's export method will be called before the preferences
   * data is stored. If no handler is specified, _all_ handlers are marked as modified.
   */
  protected _markHandlerAsModified(handlerId?: UiPreferencesHandlerId) {
    if (handlerId) {
      let handler = UiPreferences.getHandler(handlerId);
      if (handler) {
        this._modifiedHandlers.add(handler);
      }
    } else {
      UiPreferences.getHandlers().forEach(handler => this._modifiedHandlers.add(handler));
    }
  }

  /**
   * Processes the list of modified handlers, i.e. calls each handler's export method. After that, the list is reset.
   */
  protected _processModifiedHandlers() {
    this._modifiedHandlers.forEach(handler => handler.exportPreferences(this._preferences));
    this._modifiedHandlers.clear();
  }

  protected _subscribeForUpdates(): JQuery.Promise<void> {
    return this._store.subscribeForUpdates(event => this._onPreferencesUpdate(event));
  }

  protected _onPreferencesUpdate(update: UiPreferencesUpdateDo) {
    this._initPreferences(update?.preferences);
  }

  protected _initPreferences(preferences: UiPreferencesDo) {
    this._preferences = preferences || scout.create(UiPreferencesDo); // never null
    UiPreferences.getHandlers().forEach(handler => handler.importPreferences(this._preferences));
  }
}

export const uiPreferences = objects.createSingletonProxy(UiPreferences);

/**
 * An ID that identifiers a registered {@link UiPreferencesHandler}. Any value is allowed, as long as it is unique
 * (no two handlers can share the same ID) and the same value is used when calling {@link UiPreferences.scheduleStore}.
 */
export type UiPreferencesHandlerId = any;

/**
 * A small object that can extract and update parts of the global {@link UiPreferencesDo} object.
 */
export interface UiPreferencesHandler {
  /**
   * Extracts component-specific parts of the global {@link UiPreferencesDo} object into internal data structures.
   */
  importPreferences: (preferences: UiPreferencesDo) => void;
  /**
   * Updates the component-specific parts of the given {@link UiPreferencesDo} object from the internal data structures.
   */
  exportPreferences: (preferences: UiPreferencesDo) => void;
}

