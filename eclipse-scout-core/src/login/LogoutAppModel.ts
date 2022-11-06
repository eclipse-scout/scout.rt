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

export default interface LogoutAppModel extends AppModel {
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
