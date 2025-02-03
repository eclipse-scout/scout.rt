/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.button;

/**
 * A Button has 2 aspects
 * <ol>
 * <li>System-button / NonSystem-button is marked by getSystemType()<br>
 * System buttons in a dialog have a pre-defined action handling
 * <li>Process-button / NonProcess-button is marked by isProcessButton()<br>
 * Process buttons are normally placed on dialogs button bar on the lower dialog bar
 * </ol>
 */
public interface IButtonUIFacade {

  void fireButtonClickFromUI();

  void setSelectedFromUI(boolean b);
}
