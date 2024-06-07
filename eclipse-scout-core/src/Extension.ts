/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, ObjectType, scout} from './index';

/**
 * This class is used to extend an existing Scout object. In order to use the extension feature
 * you must subclass Extension an implement an init method where you register the methods
 * you want to extend. Example:
 *
 * scout.MyExtension.prototype.init = function() {
 *   this.extend(scout.MyStringField.prototype, '_init');
 *   this.extend(scout.MyStringField.prototype, '_renderProperties');
 * };
 *
 * Then you implement methods with the same name and signature on the extension class. Example:
 *
 * scout.MyExtension.prototype._init = function(model) {
 *   this.next(model);
 *   this.extended.setProperty('bar', 'foo');
 * };
 *
 * The extension feature sets two properties on the extension instance before the extended method
 * is called. Note: the function scope (this) is set to the extension instance when the extended
 * function is called:
 *
 *   next: is a reference to the next extended function or the original function of the extended
 *         object, in case the current extension is the last extension in the extension chain.
 *
 *   extended: is the extended or original object.
 */
export class Extension<E> {
  extended: E;
  next: (...args: any[]) => any;

  extend(extended: E, funcName: string) {
    let origFunc = extended[funcName];
    let extension = this;
    extended[funcName] = function(...args) {
      extension.extended = this;
      extension.next = origFunc.bind(this);
      return extension[funcName](...args);
    };
  }

  /**
   * Calls {@link scout.create} for each extension class in the given extensions array.
   *
   * @param extensions a single objectType or an array of objectTypes
   */
  static install(extensions: string | ObjectType | (string | ObjectType)[]) {
    arrays.ensure(extensions).forEach(ext => {
      scout.create(ext);
    });
  }
}
