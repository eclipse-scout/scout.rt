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

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.resource.BinaryResource;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;

public interface IFileChooser {

  IFileChooserUIFacade getUIFacade();

  /**
   * @return the {@link IDisplayParent} to attach this {@link IFileChooser} to; is never <code>null</code>.
   */
  IDisplayParent getDisplayParent();

  /**
   * Sets the model element to attach this {@link IFileChooser} to. By default, the {@link IDisplayParent} is set
   * automatically from the current calling context when the {@link IFileChooser} is created. However, that parent can
   * be overwritten manually as long as not showing.
   *
   * @param displayParent
   *          like {@link IDesktop}, {@link IOutline} or {@link IForm}; must not be <code>null</code>.
   */
  void setDisplayParent(IDisplayParent displayParent);

  void addFileChooserListener(FileChooserListener listener);

  void removeFileChooserListener(FileChooserListener listener);

  List<String> getFileExtensions();

  boolean isMultiSelect();

  /**
   * start file choosing and block until a result is available
   *
   * @return {@link #getFiles()}
   */
  List<BinaryResource> startChooser() throws ProcessingException;

  /**
   * Sets the result and releases the blocking condition held by {@link #startChooser()}.
   */
  void setFiles(List<BinaryResource> result);

  /**
   * @return list of previously uploaded files using {@link #startChooser()} (empty list if chooser was not yet started)
   */
  List<BinaryResource> getFiles();
}
