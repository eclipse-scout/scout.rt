/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.filechooser;

import java.util.List;

import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;

public interface IFileChooser {

  /**
   * default maximum upload size
   */
  long DEFAULT_MAXIMUM_UPLOAD_SIZE = 50 * 1024 * 1024; // default: 50 MiB

  IFileChooserUIFacade getUIFacade();

  /**
   * @return the {@link IDisplayParent} to attach this {@link IFileChooser} to; is never <code>null</code>.
   */
  IDisplayParent getDisplayParent();

  /**
   * Sets the display parent to attach this {@link IFileChooser} to.
   * <p>
   * A display parent is the anchor to attach this {@link IFileChooser} to, and affects its accessibility and modality
   * scope. Possible parents are {@link IDesktop}, {@link IOutline}, or {@link IForm}:
   * <ul>
   * <li>Desktop: {@link IFileChooser} is always accessible; blocks the entire desktop;</li>
   * <li>Outline: {@link IFileChooser} is only accessible when the given outline is active; only blocks the outline;
   * </li>
   * <li>Form: {@link IFileChooser} is only accessible when the given Form is active; only blocks the Form;</li>
   * </ul>
   *
   * @param displayParent
   *          like {@link IDesktop}, {@link IOutline}, {@link IForm}, or <code>null</code> to derive the
   *          {@link IDisplayParent} from the current calling context.
   */
  void setDisplayParent(IDisplayParent displayParent);

  IFastListenerList<FileChooserListener> fileChooserListeners();

  default void addFileChooserListener(FileChooserListener listener) {
    fileChooserListeners().add(listener);
  }

  default void removeFileChooserListener(FileChooserListener listener) {
    fileChooserListeners().remove(listener);
  }

  /**
   * @return a list containing any of media types, mime types and file extensions.
   *         <p>
   *         media and mime types are specified using at least a subtype (such containing a '/')
   *         <p>
   *         File extensions are specified without leading '.'
   *         <p>
   *         Example 1: [txt, csv, text/xml]
   *         <p>
   *         Example 2: [text/plain, text/csv, xml]
   */
  List<String> getFileExtensions();

  boolean isMultiSelect();

  /**
   * start file choosing and block until a result is available
   *
   * @return {@link #getFiles()}
   */
  List<BinaryResource> startChooser();

  /**
   * Sets the result and releases the blocking condition held by {@link #startChooser()}.
   */
  void setFiles(List<BinaryResource> result);

  /**
   * @return list of previously uploaded files using {@link #startChooser()} (empty list if chooser was not yet started)
   */
  List<BinaryResource> getFiles();

  /**
   * @param maximumUploadSize
   *          maximum size for upload in bytes.
   */
  void setMaximumUploadSize(long maximumUploadSize);

  /**
   * @return maximum size for upload in bytes
   */
  long getMaximumUploadSize();

  /**
   * Closes the file chooser (with no result) and resolves the blocking condition held by {@link #startChooser()}.
   */
  void doClose();
}
