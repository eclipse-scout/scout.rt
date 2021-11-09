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
import {App, ObjectFactory, scout, texts} from '../index';
import $ from 'jquery';

export default class LogoutApp extends App {

  constructor() {
    super();
  }

  /**
   * @inheritDoc
   * @param {{bootstrap?: {textsUrl?, localesUrl?, codesUrl?}}} [options] see {@link App.init}
   * @param {string} [options.logoUrl] the url to the logo. Default is 'logo.png'.
   * @param {string} [options.loginUrl] the url to use by the login again button. Default is './';
   * @param {object} [options.texts] texts to be used in the logout box. By default the texts provided by the <scout-texts> tags are used, see {@link texts.readFromDOM}.
   * Otherwise the texts will only be in English.
   */
  init(options) {
    return super.init(options);
  }

  /**
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

    let logoutBox = scout.create('LogoutBox', options);
    logoutBox.render($('body').addClass('logout-body'));
  }
}
