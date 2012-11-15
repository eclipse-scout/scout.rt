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

  void setAutoCloseMillis(long millis);

  String getCopyPasteText();

  void setCopyPasteText(String s);

  boolean isOpen();

  /**
   * start a blocking message box and wait until it returns the result
   * (YES,NO,CANCEL)
   */
  int startMessageBox();

  int startMessageBox(int defaultResult);
}
