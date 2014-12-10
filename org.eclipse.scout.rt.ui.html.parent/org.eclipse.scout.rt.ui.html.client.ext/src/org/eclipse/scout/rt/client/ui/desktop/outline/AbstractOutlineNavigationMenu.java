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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.ui.menu.AbstractMenu5;

// TODO AWE: (navi) home button implementieren, wenn auf top-level
// TODO BSH OutlineNavigationButton | Cleanup and comment this class
public abstract class AbstractOutlineNavigationMenu extends AbstractMenu5 {

  private IOutline m_outline;
  // TODO AWE: (scout) wir brauchen eine execDispose() methode - sonst kann der listener nie
  // entfernt werden (memory-leak). Evtl. können wir auf diese methode aber auch verzichten
  // wenn wir das menü konzeptionell ändern und diese beiden buttons neu zur outline gehören.
  private TreeListener m_treeListener = new TreeListener() {

    @Override
    public void treeChangedBatch(List<? extends TreeEvent> batch) {
      for (TreeEvent e : batch) {
        acceptEvent(e);
      }
    }

    @Override
    public void treeChanged(TreeEvent e) {
      acceptEvent(e);
    }

    private void acceptEvent(TreeEvent e) {
      if (TreeEvent.TYPE_NODES_SELECTED == e.getType() || OutlineEvent.TYPE_PAGE_CHANGED == e.getType()) {
        updateMenuState();
      }
    }
  };

  public AbstractOutlineNavigationMenu(IOutline outline) {
    super(false);
    m_outline = outline;
    callInitializer();
    m_outline.addTreeListener(m_treeListener);
  }

  @Override
  public boolean isInheritAccessibility() {
    return false;
  }

  protected void updateMenuState() {
    IPage activePage = m_outline.getActivePage();
    setEnabled(activePage != null && activePage.getTreeLevel() > 0);
  }

  @Override
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.hashSet(OutlineMenuType.Navigation, getMenuType());
  }

  protected abstract IMenuType getMenuType();

  public final IOutline getOutline() {
    return m_outline;
  }

}
