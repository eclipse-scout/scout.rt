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
package org.eclipse.scout.rt.ui.html.json.messagebox;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFileChooserParent;

/**
 * Filter to accept file choosers attached to a specific {@link IFileChooserParent}.
 */
public class FileChooserParentFilter implements IFilter<IFileChooser> {

  private final IFileChooserParent m_fileChooserParent;

  public FileChooserParentFilter(final IFileChooserParent fileChooserParent) {
    m_fileChooserParent = fileChooserParent;
  }

  @Override
  public boolean accept(final IFileChooser fileChooser) {
    return m_fileChooserParent == fileChooser.getFileChooserParent();
  }
}
