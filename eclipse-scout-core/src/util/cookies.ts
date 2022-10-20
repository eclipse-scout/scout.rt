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
import {scout} from '../index';

export function get(name, doc) {
  doc = doc || document;
  let prefix = name + '=';
  let cookies = doc.cookie.split(';');
  for (let i = 0; i < cookies.length; i++) {
    let cookie = cookies[i].trim();
    if (cookie.indexOf(prefix) === 0) {
      return cookie.substring(prefix.length);
    }
  }
  return null;
}

/**
 * Sets a cookie.
 *
 * @param maxAge If specified the cookie will be persistent, otherwise it will be a session cookie.
 */
export function set(name, value, maxAge, path) {
  value = scout.nvl(value, '');
  maxAge = scout.nvl(maxAge, -1);

  let cookie = name + '=' + value;
  if (maxAge > -1) {
    let expires = new Date();
    expires.setTime(expires.getTime() + maxAge * 1000);
    cookie += ';max-age=' + maxAge + ';expires=' + expires;
  }
  if (path) {
    cookie += ';path=' + path;
  }
  document.cookie = cookie; // Does not override existing cookies with a different name
}

export default {
  get,
  set
};
