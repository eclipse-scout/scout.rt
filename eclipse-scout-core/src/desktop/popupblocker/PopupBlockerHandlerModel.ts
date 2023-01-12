/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ObjectModel, PopupBlockerHandler, Session} from '../../index';

export interface PopupBlockerHandlerModel extends ObjectModel<PopupBlockerHandler> {
  session?: Session;

  /**
   * A boolean indicating if the popup-window should have a back reference to the origin window.
   * By default, this parameter is false because of security reasons.
   * Only trusted sites may be allowed to access the opener window and potentially modify the origin web application!
   * @see https://mathiasbynens.github.io/rel-noopener/
   */
  preserveOpener?: boolean;
}
