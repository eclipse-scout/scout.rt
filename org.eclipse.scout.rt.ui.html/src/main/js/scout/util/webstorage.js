/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Utility functions for "sessionStorage" and "localStorage" that ignore any errors.
 * Errors can occur e.g. in "private mode" on Safari.
 */
scout.webstorage = {

  getItem: function(storage, key) {
    try {
      return storage.getItem(key);
    } catch (err) {
      $.log.error('Error while reading "' + key + '" from web storage: ' + err);
    }
  },

  setItem: function(storage, key, value) {
    try {
      return storage.setItem(key, value);
    } catch (err) {
      $.log.error('Error while storing "' + key + '" in web storage: ' + err);
    }
  },

  removeItem: function(storage, key) {
    try {
      return storage.removeItem(key);
    } catch (err) {
      $.log.error('Error while removing "' + key + '" from web storage: ' + err);
    }
  },

  clear: function(storage) {
    try {
      return storage.clear();
    } catch (err) {
      $.log.error('Error while clearing web storage: ' + err);
    }
  }

};
