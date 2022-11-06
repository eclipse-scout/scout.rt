/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AppModel} from '../App';

export default interface LoginAppModel extends AppModel {
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
