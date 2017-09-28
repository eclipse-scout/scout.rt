/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;

/**
 * Don't cache table page search form contents, releaseUnusedPages before every page reload
 */
public class SmallMemoryPolicy extends AbstractMemoryPolicy {

  /**
   * clear table before loading new data, thus disabling "replaceRow" mechanism but saving memory
   */
  @Override
  public void beforeTablePageLoadData(IPageWithTable<?> page) {
    //make sure inactive outlines have no selection that "keeps" the pages
    IDesktop desktop = ClientSessionProvider.currentSession().getDesktop();
    for (IOutline o : desktop.getAvailableOutlines()) {
      if (o != desktop.getOutline()) {
        o.selectNode(null);
      }
    }
    desktop.releaseUnusedPages();

    if (page.getTable() != null) {
      page.getTable().discardAllRows();
    }
  }

  @Override
  public void pageSearchFormStarted(IPageWithTable<?> p) {
    //nop
  }

  @Override
  public void pageCreated(IPage<?> p) {
    //nop
  }

  @Override
  public void pageTableCreated(IPage<?> p) {
    //nop
  }

  @Override
  public String toString() {
    return "Small";
  }
}
