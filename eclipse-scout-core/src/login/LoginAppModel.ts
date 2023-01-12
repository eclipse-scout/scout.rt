/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AppModel} from '../index';

export interface LoginAppModel extends AppModel {
  /**
   * The url to the logo. Default is 'logo.png'.
   */
  logoUrl?: string;

  /**
   * The url used for the authentication. Default is 'auth'.
   */
  authUrl?: string;

  /**
   * Name of the user parameter in the data object sent with the authentication request. Default is 'user'.
   */
  userDataKey?: string;

  /**
   * Name of the password parameter in the data object sent with the authentication request. Default is 'password'.
   */
  passwordDataKey?: string;

  /**
   * Additional parameters for the data object sent with the authentication request. Default is an empty object.
   */
  additionalData?: Record<string, any>;

  /**
   * The ajax options used for the authentication request. By default, only the type is set to POST, but it will be extended with the url and the data.
   */
  ajaxOptions?: JQuery.AjaxSettings;

  /**
   * The url to redirect to after a successful login. If not specified the {@link LoginBox.prepareRedirectUrl} function is used to compute the redirectUrl.
   */
  redirectUrl?: string;

  /**
   * Function that is called on the redirectUrl before opening it. Default is {@link LoginBox.prepareRedirectUrl}.
   */
  prepareRedirectUrlFunc?: (url: string) => string;

  /**
   * If set a message is displayed above the user field. Default is undefined.
   */
  messageKey?: string;

  /**
   * Texts to be used in the login box. By default, the texts provided by the <scout-texts> tags are used, see {@link texts.readFromDOM}. Otherwise, the texts will only be in English.
   */
  texts?: Record<string, string>;
}
