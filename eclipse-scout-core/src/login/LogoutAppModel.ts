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

export interface LogoutAppModel extends AppModel {
  /**
   * The url to the logo. Default is 'logo.png'.
   */
  logoUrl?: string;

  /**
   * The url to use by the login again button. Default is './';
   */
  loginUrl?: string;

  /**
   * Texts to be used in the logout box. By default the texts provided by the <scout-texts> tags are used, see {@link texts.readFromDOM}. Otherwise the texts will only be in English.
   */
  texts?: Record<string, string>;
}
