/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.filechooserfield;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

public interface IFileChooserField extends IValueField<BinaryResource> {

  String PROP_FILE_ICON_ID = "fileIconId";
  String PROP_MAXIMUM_UPLOAD_SIZE = "maximumUploadSize";

  /**
   * default maximum upload size
   */
  long DEFAULT_MAXIMUM_UPLOAD_SIZE = 50 * 1024 * 1024; // default: 50 MB;

  void setShowFileExtension(boolean b);

  boolean isShowFileExtension();

  void setFileExtensions(List<String> extensions);

  List<String> getFileExtensions();

  void setFileIconId(String s);

  String getFileIconId();

  IFileChooser getFileChooser();

  // Convenience file getter
  String getFileName();

  int getFileSize();

  IFileChooserFieldUIFacade getUIFacade();

  /**
   * @param maximumUploadSize
   *          maximum size for upload in bytes.
   */
  void setMaximumUploadSize(long maximumUploadSize);

  /**
   * @return maximum size for upload in bytes
   */
  long getMaximumUploadSize();
}
