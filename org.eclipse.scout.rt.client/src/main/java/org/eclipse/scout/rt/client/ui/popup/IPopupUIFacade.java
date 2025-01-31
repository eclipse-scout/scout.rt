/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.popup;

public interface IPopupUIFacade {

  /**
   * Request to close the popup<br>
   * This request might be ignored when the popup is not ready for closing or in pending state.
   */
  void firePopupClosingFromUI();
}
