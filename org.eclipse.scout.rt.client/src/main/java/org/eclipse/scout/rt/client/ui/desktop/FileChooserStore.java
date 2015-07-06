/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop;

import java.util.List;

import org.eclipse.scout.commons.index.AbstractMultiValueIndex;
import org.eclipse.scout.commons.index.IndexedStore;
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFileChooserParent;
import org.eclipse.scout.rt.platform.Bean;

/**
 * {@link IndexedStore} for {@link IFileChooser} objects.
 *
 * @since 5.1
 */
@Bean
public class FileChooserStore extends IndexedStore<IFileChooser> {

  private final P_FileChooserParentIndex m_fileChooserParentIndex = registerIndex(new P_FileChooserParentIndex());

  /**
   * Returns all <code>FileChoosers</code> which are attached to the given {@link IFileChooserParent} in the order as
   * inserted.
   */
  public List<IFileChooser> getByFileChooserParent(final IFileChooserParent fileChooserParent) {
    return m_fileChooserParentIndex.get(fileChooserParent);
  }

  // ====  Index definitions ==== //

  private class P_FileChooserParentIndex extends AbstractMultiValueIndex<IFileChooserParent, IFileChooser> {

    @Override
    protected IFileChooserParent calculateIndexFor(final IFileChooser fileChooser) {
      return fileChooser.getFileChooserParent();
    }
  }
}
