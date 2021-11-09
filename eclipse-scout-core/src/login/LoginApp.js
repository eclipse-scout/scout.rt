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
import {App, LoginBox, ObjectFactory, scout, texts} from '../index';
import $ from 'jquery';

export default class LoginApp extends App {

  constructor() {
    super();
  }

  /**
   * @inheritDoc
   * @param {{bootstrap?: {textsUrl?, localesUrl?, codesUrl?}}} [options] see {@link App.init}
   * @param {string} [options.logoUrl] the url to the logo. Default is 'logo.png'.
   * @param {string} [options.authUrl] the url used for the authentication. Default is 'auth'.
   * @param {string} [options.userDataKey] name of the user parameter in the data object sent with the authentication request. Default is 'user'.
   * @param {string} [options.passwordDataKey] name of the password parameter in the data object sent with the authentication request. Default is 'password'.
   * @param {object} [options.additionalData] additional parameters for the data object sent with the authentication request. Default is an empty object.
   * @param {object} [options.ajaxOptions] the ajax options used for the authentication request. By default only the type is set to POST but it will be extended with the url and the data.
   * @param {string} [options.redirectUrl] the url to redirect to after a successful login. If not specified the prepareRedirectUrl function is used to compute the redirectUrl.
   * @param {function} [options.prepareRedirectUrl] function that is called on the redirectUrl before opening it. Default is {@link LoginBox.prepareRedirectUrl}.
   * @param {string} [options.messageKey] if set a message is displayed above the user field. Default is undefined.
   * @param {object} [options.texts] texts to be used in the login box. By default the texts provided by the <scout-texts> tags are used, see {@link texts.readFromDOM}.
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

    let loginBox = scout.create('LoginBox', options);
    loginBox.render($('body').addClass('login-body'));
  }
}
