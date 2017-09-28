/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * This class is used to extend an existing Scout object. In order to use the extension feature
 * you must subclass scout.Extension an implement an init method where you register the methods
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
scout.Extension = function() {
};

scout.Extension.prototype.extend = function(extended, funcName) {
  var origFunc = extended[funcName];
  var extension = this;
  var wrapper = function() {
    extension.extended = this;
    extension.next = origFunc.bind(this);
    return extension[funcName].apply(extension, arguments);
  };
  extended[funcName] = wrapper;
};

/**
 * Calls scout.create for each extension class in the given extensions array.
 *
 * @param extensions an Array of strings containing extension class names
 * @static
 */
scout.Extension.install = function(extensions) {
  extensions.forEach(function(ext) {
    scout.create(ext);
  });
};

