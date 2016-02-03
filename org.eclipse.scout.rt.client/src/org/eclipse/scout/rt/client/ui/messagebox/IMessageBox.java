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
import org.eclipse.scout.rt.client.ui.IHtmlCapable;

public interface IMessageBox extends IPropertyObserver, IHtmlCapable {

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

  String getTitle();

  void setTitle(String s);

  String getIconId();

  void setIconId(String iconId);

  int getSeverity();

  void setSeverity(int severity);

  String getIntroText();

  void setIntroText(String s);

  String getActionText();

  void setActionText(String s);

  String getHiddenText();

  void setHiddenText(String s);

  String getYesButtonText();

  void setYesButtonText(String s);

  String getNoButtonText();

  void setNoButtonText(String s);

  String getCancelButtonText();

  void setCancelButtonText(String s);

  long getAutoCloseMillis();

  /**
   * To close the message box automatically after the specified period of time. By default, the result
   * {@link #CANCEL_OPTION} is returned after being closed. This can be changed by using {@link #startMessageBox(int)}
   * to construct the message box.
   *
   * @param millis
   *          timeout [ms]
   */
  void setAutoCloseMillis(long millis);

  String getCopyPasteText();

  void setCopyPasteText(String s);

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
  int startMessageBox();

  /**
   * Opens a message box. This call blocks until the message box is closed.
   *
   * @param defaultResult
   *          default result to return if not closed by the user (e.g. by auto-close timer).
   * @return The close result ({@link #YES_OPTION}, {@link #NO_OPTION}, {@link #CANCEL_OPTION}).
   */
  int startMessageBox(int defaultResult);
}
