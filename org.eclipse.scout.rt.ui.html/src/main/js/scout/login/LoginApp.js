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
import {LoginBox} from '../index';
import {scout} from '../index';
import {App} from '../index';

/**
 * init options:
 * - authUrl: URL to be used for the authentication. Default is 'auth'
 * - userDataKey: name of the user parameter in the data object sent with the authentication request. Default is 'user'.
 * - passwordDataKey: name of the password parameter in the data object sent with the authentication request. Default is 'password'.
 * - redirectUrl: URL to redirect to after a successful login. If not specified the prepareRedirectUrl function is used to compute the redirectUrl.
 * - prepareRedirectUrl: function that is called on the redirectUrl before opening it. Default is LoginBox.prepareRedirectUrl.
 * - logoUrl: default points to 'logo.png',
 * - messageKey: if set a message is displayed above the user field. Default is undefined.
 * - texts: texts to be used in the login box. Default texts are in English.
 */
export default class LoginApp extends App {

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

  var loginBox = scout.create('LoginBox', options);
  loginBox.render($('body'));
}
}
