/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

public interface IFormUIFacade {

  void fireFormActivatedFromUI();

  /**
   * Request to close the form<br>
   * This request might be ignored when the form is not ready for closing or in pending state.
   */
  void fireFormClosingFromUI();

  /**
   * Notification that ui closed the form view<br>
   * This request is a forced close of the form from the ui and closes the form model regardless of its state.
   */
  void fireFormKilledFromUI();
}
