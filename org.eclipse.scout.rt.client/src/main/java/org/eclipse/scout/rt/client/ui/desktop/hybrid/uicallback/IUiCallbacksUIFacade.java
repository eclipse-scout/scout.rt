/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.hybrid.uicallback;

import org.eclipse.scout.rt.dataobject.IDoEntity;

public interface IUiCallbacksUIFacade {

  /**
   * Called if the UI callback was successful.
   *
   * @param callbackId
   *     The id of the callback.
   * @param result
   *     The optional resulting {@link IDoEntity}.
   */
  void fireCallbackDone(String callbackId, IDoEntity result);

  /**
   * Called if the UI returned an error (UiCallbackErrorDo).
   *
   * @param callbackId
   *          The id of the callback
   * @param message
   *          The optional error message.
   * @param code
   *          The optional error code.
   */
  void fireCallbackFailed(String callbackId, String message, String code);
}
