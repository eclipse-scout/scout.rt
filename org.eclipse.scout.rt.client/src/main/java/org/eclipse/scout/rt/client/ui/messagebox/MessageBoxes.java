/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.messagebox;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Factory for message boxes.
 *
 * @since 6.0.0
 */
public class MessageBoxes {

  private MessageBoxes() {
    // factory, private constructor
  }

  /**
   * Creates a message box with no defined buttons.
   * <p>
   * Do not forget to call {@link #show()} at the end.
   */
  public static IMessageBox create() {
    return BEANS.get(IMessageBox.class);
  }

  /**
   * Creates a message box with one button labeled OK.
   * <p>
   * Do not forget to call {@link #show()} at the end.
   */
  public static IMessageBox createOk() {
    return MessageBoxes.create().
        yesButtonText(ScoutTexts.get("OkButton"));
  }

  /**
   * Creates e message box with yes and not buttons.
   */
  public static IMessageBox createYesNo() {
    return MessageBoxes.create().
        yesButtonText(ScoutTexts.get("YesButton")).
        noButtonText(ScoutTexts.get("NoButton"));
  }

  /**
   * Creates a message box with yes, no and cancel buttons.
   */
  public static IMessageBox createYesNoCancel() {
    return MessageBoxes.createYesNo().
        cancelButtonText(ScoutTexts.get("CancelButton"));
  }
}
