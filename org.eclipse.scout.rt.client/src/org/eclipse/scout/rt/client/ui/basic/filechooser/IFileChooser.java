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
package org.eclipse.scout.rt.client.ui.basic.filechooser;

import java.io.File;
import java.util.List;

public interface IFileChooser {

  File getDirectory();

  String getFileName();

  List<String> getFileExtensions();

  /**
   * Load or Save
   * <ul>
   * <li><code>true</code> loads the file from the file system into the application.</li>
   * <li><code>false</code> saves the file from the application to the file system. Attention: This does not work in
   * RAP/Web-UI</li>
   * </ul>
   */
  boolean isTypeLoad();

  /**
   * Folder or File
   */
  boolean isFolderMode();

  boolean isMultiSelect();

  /**
   * start file choosing and block until a result is available
   * 
   * @return {@link #getFiles()}
   */
  List<File> startChooser();

  /**
   * set result value and close chooser
   */
  void setFiles(List<File> files);

  List<File> getFiles();

}
