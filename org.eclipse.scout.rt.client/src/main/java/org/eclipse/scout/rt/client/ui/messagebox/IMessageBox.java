/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.messagebox;

import org.eclipse.scout.commons.beans.IPropertyObserver;

public interface IMessageBox extends IPropertyObserver {

  /**
   * Result status YES
   */
  int YES_OPTION = 0;
  /**
   * Result status NO
   */
  int NO_OPTION = 1;
  /**
   * Result status CANCEL
   */
  int CANCEL_OPTION = 2;

  /*
   * Model observer
   */
  void addMessageBoxListener(MessageBoxListener listener);

  void removeMessageBoxListener(MessageBoxListener listener);

  /*
   * ui messaging
   */
  IMessageBoxUIFacade getUIFacade();

  String iconId();

  IMessageBox iconId(String iconId);

  int severity();

  IMessageBox severity(int severity);

  String header();

  IMessageBox header(String header);

  String body();

  IMessageBox body(String body);

  String hiddenText();

  IMessageBox hiddenText(String hiddenText);

  String yesButtonText();

  IMessageBox yesButtonText(String yesButtonText);

  String noButtonText();

  IMessageBox noButtonText(String noButtonText);

  String cancelButtonText();

  IMessageBox cancelButtonText(String cancelButtonText);

  long autoCloseMillis();

  /**
   * To close the message box automatically after the specified period of time. By default, the result
   * {@link #CANCEL_OPTION} is returned after being closed. This can be changed by using {@link #start(int)} to
   * construct the message box.
   *
   * @param autoCloseMillis
   *          timeout [ms]
   */
  IMessageBox autoCloseMillis(long autoCloseMillis);

  String copyPasteText();

  IMessageBox copyPasteText(String copyPasteText);

  /**
   * To query whether the message box is open or closed.
   *
   * @return <code>true</code> if the message box is open, <code>false</code> if closed.
   */
  boolean isOpen();

  /**
   * Opens a message box. This call blocks until the message box is closed.
   *
   * @return The close result ({@link #YES_OPTION}, {@link #NO_OPTION}, {@link #CANCEL_OPTION}).
   */
  int start();

  /**
   * Opens a message box. This call blocks until the message box is closed.
   *
   * @param defaultResult
   *          default result to return if not closed by the user (e.g. by auto-close timer).
   * @return The close result ({@link #YES_OPTION}, {@link #NO_OPTION}, {@link #CANCEL_OPTION}).
   */
  int start(int defaultResult);
}
