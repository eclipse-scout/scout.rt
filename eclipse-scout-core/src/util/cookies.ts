/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout} from '../index';

export const cookies = {
  get(name: string, doc?: Document): string {
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
  },

  /**
   * Sets a cookie.
   *
   * @param maxAge If specified the cookie will be persistent, otherwise it will be a session cookie.
   */
  set(name: string, value?: string, maxAge?: number, path?: string) {
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
};
