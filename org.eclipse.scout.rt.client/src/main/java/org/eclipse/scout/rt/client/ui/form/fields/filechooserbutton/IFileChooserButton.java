/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

public interface IFileChooserButton extends IValueField<BinaryResource> {

  String PROP_MAXIMUM_UPLOAD_SIZE = "maximumUploadSize";
  String PROP_FILE_EXTENSIONS = "fileExtensions";
  String PROP_ICON_ID = "iconId";

  /**
   * Default maximum upload size.
   */
  long DEFAULT_MAXIMUM_UPLOAD_SIZE = 50 * 1024 * 1024; // default: 50 MiB

  void setFileExtensions(List<String> fileExtensions);

  List<String> getFileExtensions();

  /**
   * @return the filename of the value (BinaryResource) or null
   */
  String getFileName();

  /**
   * @return the content-length of the value (BinaryResource) or null
   */
  int getFileSize();

  /**
   * @param maximumUploadSize
   *     maximum size for upload in bytes.
   */
  void setMaximumUploadSize(long maximumUploadSize);

  /**
   * @return maximum size for upload in bytes
   */
  long getMaximumUploadSize();

  String getIconId();

  void setIconId(String iconId);
}
