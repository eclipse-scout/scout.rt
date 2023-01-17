/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop;

import java.util.List;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.index.AbstractMultiValueIndex;
import org.eclipse.scout.rt.platform.index.IndexedStore;

/**
 * {@link IndexedStore} for {@link IMessageBox} objects.
 *
 * @since 5.1
 */
@Bean
public class MessageBoxStore extends IndexedStore<IMessageBox> {

  private final P_DisplayParentIndex m_displayParentIndex = registerIndex(new P_DisplayParentIndex());
  private final P_ApplicationModalIndex m_applicationModalIndex = registerIndex(new P_ApplicationModalIndex());

  /**
   * Returns all <code>MessageBoxes</code> which are attached to the given {@link IDisplayParent} in the order as
   * inserted.
   */
  public List<IMessageBox> getByDisplayParent(final IDisplayParent displayParent) {
    return m_displayParentIndex.get(displayParent);
  }

  /**
   * Returns <code>true</code> if this store contains 'application-modal' message boxes, or <code>false</code> if not.
   */
  public boolean containsApplicationModalMessageBoxes() {
    return !getApplicationModalMessageBoxes().isEmpty();
  }

  public List<IMessageBox> getApplicationModalMessageBoxes() {
    return m_applicationModalIndex.get(Boolean.TRUE);
  }

  // ====  Index definitions ==== //

  private class P_DisplayParentIndex extends AbstractMultiValueIndex<IDisplayParent, IMessageBox> {

    @Override
    protected IDisplayParent calculateIndexFor(final IMessageBox messageBox) {
      return messageBox.getDisplayParent();
    }
  }

  private class P_ApplicationModalIndex extends AbstractMultiValueIndex<Boolean, IMessageBox> {

    @Override
    protected Boolean calculateIndexFor(final IMessageBox messageBox) {
      return messageBox.getDisplayParent() == ClientSessionProvider.currentSession().getDesktop();
    }
  }
}
