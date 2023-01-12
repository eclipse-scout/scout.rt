/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';

/**
 * Utility functions for "sessionStorage" and "localStorage" that ignore any errors.
 * Errors can occur e.g. in "private mode" on Safari.
 */
export const webstorage = {
  getItem(storage: Storage, key: string): string {
    if (!storage) {
      return;
    }
    try {
      return storage.getItem(key);
    } catch (err) {
      $.log.error('Error while reading "' + key + '" from web storage: ' + err);
    }
  },

  getItemFromSessionStorage(key: string): string {
    try {
      return webstorage.getItem(sessionStorage, key);
    } catch (err) {
      $.log.error('Error while reading "' + key + '" from session storage: ' + err);
    }
  },

  getItemFromLocalStorage(key: string): string {
    try {
      return webstorage.getItem(localStorage, key);
    } catch (err) {
      $.log.error('Error while reading "' + key + '" from local storage: ' + err);
    }
  },

  setItem(storage: Storage, key: string, value: string) {
    if (!storage) {
      return;
    }
    try {
      return storage.setItem(key, value);
    } catch (err) {
      $.log.error('Error while storing "' + key + '" in web storage: ' + err);
    }
  },

  setItemToSessionStorage(key: string, value: string) {
    try {
      return webstorage.setItem(sessionStorage, key, value);
    } catch (err) {
      $.log.error('Error while storing "' + key + '" in session storage: ' + err);
    }
  },

  setItemToLocalStorage(key: string, value: string) {
    try {
      return webstorage.setItem(localStorage, key, value);
    } catch (err) {
      $.log.error('Error while storing "' + key + '" in local storage: ' + err);
    }
  },

  removeItem(storage: Storage, key: string) {
    if (!storage) {
      return;
    }
    try {
      return storage.removeItem(key);
    } catch (err) {
      $.log.error('Error while removing "' + key + '" from web storage: ' + err);
    }
  },

  removeItemFromSessionStorage(key: string) {
    try {
      return webstorage.removeItem(sessionStorage, key);
    } catch (err) {
      $.log.error('Error while removing "' + key + '" from session storage: ' + err);
    }
  },

  removeItemFromLocalStorage(key: string) {
    try {
      return webstorage.removeItem(localStorage, key);
    } catch (err) {
      $.log.error('Error while removing "' + key + '" from local storage: ' + err);
    }
  },

  clear(storage: Storage) {
    if (!storage) {
      return;
    }
    try {
      return storage.clear();
    } catch (err) {
      $.log.error('Error while clearing web storage: ' + err);
    }
  },

  clearSessionStorage() {
    try {
      return webstorage.clear(sessionStorage);
    } catch (err) {
      $.log.error('Error while clearing session storage: ' + err);
    }
  },

  clearLocalStorage() {
    try {
      return webstorage.clear(localStorage);
    } catch (err) {
      $.log.error('Error while clearing local storage: ' + err);
    }
  }
};
