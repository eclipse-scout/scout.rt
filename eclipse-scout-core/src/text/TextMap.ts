/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {objects} from '../index';

export class TextMap {

  map: Record<string, string>;
  parent: TextMap;

  constructor(textMap?: Record<string, string>) {
    this.map = textMap || {};
  }

  /**
   * Returns the text for the given key.
   * If the key does not exist in this text map, a lookup in the parent text map is done.
   *
   * @param textKey key to look up the text
   * @param vararg texts to replace the placeholders specified by {0}, {1}, etc.
   */
  get(textKey: string, ...vararg: any[]): string {
    if (!this._exists(textKey)) {
      if (this.parent) {
        return this.parent.get(textKey, ...vararg);
      }
      return '[undefined text: ' + textKey + ']';
    }
    let len = vararg.length;
    let text = this.map[textKey];

    if (len === 0) {
      return text;
    }

    for (let i = 0; i < len; i++) {
      text = text.replace(new RegExp('\\{' + (i) + '\\}', 'g'), vararg[i]);
    }
    return text;
  }

  optGet(textKey: string, defaultValue?: string, ...vararg: string[]): string {
    if (!this._exists(textKey)) {
      if (this.parent) {
        return this.parent.optGet(textKey, defaultValue, ...vararg);
      }
      return defaultValue;
    }
    return this.get(textKey, ...vararg);
  }

  exists(textKey: string): boolean {
    if (this._exists(textKey)) {
      return true;
    }
    if (this.parent) {
      return this.parent.exists(textKey);
    }
    return false;
  }

  protected _exists(textKey: string): boolean {
    return this.map.hasOwnProperty(textKey);
  }

  add(textKey: string, text: string) {
    this.map[textKey] = text;
  }

  /**
   * Adds all texts from the given textMap to this textMap
   * @param textMap either a plain object or a {@link TextMap}
   */
  addAll(textMap: Record<string, string> | TextMap) {
    if (!textMap) {
      return;
    }
    if (textMap instanceof TextMap) {
      textMap = textMap.map;
    }
    objects.copyOwnProperties(textMap, this.map);
  }

  setParent(parent: TextMap) {
    this.parent = parent;
  }

  remove(textKey: string) {
    delete this.map[textKey];
  }
}
