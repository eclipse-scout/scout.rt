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
package org.eclipse.scout.rt.client.ui.form.fields.filechooserfield;

import java.io.File;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

public interface IFileChooserField extends IValueField<String> {

  String PROP_FILE_ICON_ID = "fileIconId";

  String PROP_MAX_LENGTH = "maxLength";

  void setFolderMode(boolean b);

  boolean isFolderMode();

  void setShowDirectory(boolean b);

  boolean isShowDirectory();

  void setShowFileName(boolean b);

  boolean isShowFileName();

  void setShowFileExtension(boolean b);

  boolean isShowFileExtension();

  /**
   * Load or Save
   * <ul>
   * <li><code>true</code> loads the file from the file system into the application.</li>
   * <li><code>false</code> saves the file from the application to the file system. Attention: This does not work in
   * RAP/Web-UI</li>
   * </ul>
   */
  void setTypeLoad(boolean b);

  /**
   * @see #setTypeLoad(boolean)
   */
  boolean isTypeLoad();

  void setFileExtensions(String[] extensions);

  String[] getFileExtensions();

  void setDirectory(File d);

  File getDirectory();

  void setFileIconId(String s);

  String getFileIconId();

  void setMaxLength(int len);

  int getMaxLength();

  IMenu[] getMenus();

  boolean hasMenus();

  IFileChooser getFileChooser();

  // Convenience file getter
  File getValueAsFile();

  String getFileName();

  long getFileSize();

  boolean fileExists();

  IFileChooserFieldUIFacade getUIFacade();
}
