/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {texts} from '../index';
import {ObjectFactory} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';
import {App} from '../index';

/**
 * init options:
 * - loginUrl: URL to redirect after login again button click
 * - logoUrl: default points to 'logo.png'
 */
export default class LogoutApp extends App {

constructor() {
  super();
}


/**
 * Default adds polyfills too, not required here
 * @override
 */
_prepareEssentials(options) {
  ObjectFactory.get().init();
}

/**
 * No bootstrapping required
 * @override
 */
_doBootstrap(options) {
  return [];
}

_init(options) {
  options = options || {};
  options.texts = $.extend({}, texts.readFromDOM(), options.texts);
  this._prepareDOM();

  var logoutBox = scout.create('LogoutBox', options);
  logoutBox.render($('body'));
}
}
