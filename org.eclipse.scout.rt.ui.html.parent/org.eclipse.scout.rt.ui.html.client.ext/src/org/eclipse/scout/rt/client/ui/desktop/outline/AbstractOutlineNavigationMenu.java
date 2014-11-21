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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeListener;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage5;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormMenuType;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.ui.menu.AbstractMenu5;

// TODO AWE: (navi) wenn klick in baum, wieder detailForm anzeigen
// - home button implementieren, wenn auf top-level

public abstract class AbstractOutlineNavigationMenu extends AbstractMenu5 {

  private IOutline m_outline;
  private String m_text1;
  private String m_text2;

  public AbstractOutlineNavigationMenu(IOutline outline, String text1, String text2) {
    super(false);
    m_outline = outline;
    m_text1 = TEXTS.get(text1);
    m_text2 = TEXTS.get(text2);
    callInitializer();
    m_outline.addTreeListener(new TreeListener() {

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
    });
  }

  @Override
  public boolean isInheritAccessibility() {
    return false;
  }

  private void updateMenuState() {
    IPage activePage = m_outline.getActivePage();
    setText(isDrill(activePage) ? m_text1 : m_text2);
    setEnabled(activePage.getTreeLevel() > 0);
  }

  @Override
  protected String getConfiguredText() {
    return m_text1;
  }

  @Override
  protected Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.hashSet(FormMenuType.System, getMenuType());
  }

  private boolean isDrill(IPage page) {
    if (page instanceof IPage5) {
      IPage5 page5 = (IPage5) page;
      if (page5 instanceof IPageWithTable) {
        return true;
      }
      else {
        if (page5.getDetailForm() != null && isDetail(page5)) {
          return false;
        }
        else {
          return true;
        }
      }
    }
    else {
      return true;
    }
  }

  @Override
  protected void execAction() throws ProcessingException {
    IPage activePage = m_outline.getActivePage();
    if (isDrill(activePage)) {
      doDrill(activePage);
    }
    else {
      showDetail(activePage);
    }
  }

  final IOutline getOutline() {
    return m_outline;
  }

  abstract boolean isDetail(IPage5 page5);

  abstract void showDetail(IPage page);

  abstract void doDrill(IPage page);

  abstract IMenuType getMenuType();

}
