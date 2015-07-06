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
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.Bean;

/**
 * {@link IndexedStore} for {@link IMessageBox} objects.
 *
 * @since 5.1
 */
@Bean
public class MessageBoxStore extends IndexedStore<IMessageBox> {

  private final P_DisplayParentIndex m_displayParentIndex = registerIndex(new P_DisplayParentIndex());

  /**
   * Returns all <code>MessageBoxes</code> which are attached to the given {@link IDisplayParent} in the order as
   * inserted.
   */
  public List<IMessageBox> getByDisplayParent(final IDisplayParent displayParent) {
    return m_displayParentIndex.get(displayParent);
  }

  // ====  Index definitions ==== //

  private class P_DisplayParentIndex extends AbstractMultiValueIndex<IDisplayParent, IMessageBox> {

    @Override
    protected IDisplayParent calculateIndexFor(final IMessageBox messageBox) {
      return messageBox.displayParent();
    }
  }
}
