/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import $ from 'jquery';

/**
 * Utility functions for "sessionStorage" and "localStorage" that ignore any errors.
 * Errors can occur e.g. in "private mode" on Safari.
 */

export function getItem(storage, key) {
  if (!storage) {
    return;
  }
  try {
    return storage.getItem(key);
  } catch (err) {
    $.log.error('Error while reading "' + key + '" from web storage: ' + err);
  }
}

export function getItemFromSessionStorage(key) {
  try {
    return getItem(sessionStorage, key);
  } catch (err) {
    $.log.error('Error while reading "' + key + '" from session storage: ' + err);
  }
}

export function getItemFromLocalStorage(key) {
  try {
    return getItem(localStorage, key);
  } catch (err) {
    $.log.error('Error while reading "' + key + '" from local storage: ' + err);
  }
}

export function setItem(storage, key, value) {
  if (!storage) {
    return;
  }
  try {
    return storage.setItem(key, value);
  } catch (err) {
    $.log.error('Error while storing "' + key + '" in web storage: ' + err);
  }
}

export function setItemToSessionStorage(key, value) {
  try {
    return setItem(sessionStorage, key, value);
  } catch (err) {
    $.log.error('Error while storing "' + key + '" in session storage: ' + err);
  }
}

export function setItemToLocalStorage(key, value) {
  try {
    return setItem(localStorage, key, value);
  } catch (err) {
    $.log.error('Error while storing "' + key + '" in local storage: ' + err);
  }
}

export function removeItem(storage, key) {
  if (!storage) {
    return;
  }
  try {
    return storage.removeItem(key);
  } catch (err) {
    $.log.error('Error while removing "' + key + '" from web storage: ' + err);
  }
}

export function removeItemFromSessionStorage(key) {
  try {
    return removeItem(sessionStorage, key);
  } catch (err) {
    $.log.error('Error while removing "' + key + '" from session storage: ' + err);
  }
}

export function removeItemFromLocalStorage(key) {
  try {
    return removeItem(localStorage, key);
  } catch (err) {
    $.log.error('Error while removing "' + key + '" from local storage: ' + err);
  }
}

export function clear(storage) {
  if (!storage) {
    return;
  }
  try {
    return storage.clear();
  } catch (err) {
    $.log.error('Error while clearing web storage: ' + err);
  }
}

export function clearSessionStorage() {
  try {
    return clear(sessionStorage);
  } catch (err) {
    $.log.error('Error while clearing session storage: ' + err);
  }
}

export function clearLocalStorage() {
  try {
    return clear(localStorage);
  } catch (err) {
    $.log.error('Error while clearing local storage: ' + err);
  }
}

export default {
  clear,
  clearSessionStorage,
  clearLocalStorage,
  getItem,
  getItemFromSessionStorage,
  getItemFromLocalStorage,
  removeItem,
  removeItemFromSessionStorage,
  removeItemFromLocalStorage,
  setItem,
  setItemToSessionStorage,
  setItemToLocalStorage
};
