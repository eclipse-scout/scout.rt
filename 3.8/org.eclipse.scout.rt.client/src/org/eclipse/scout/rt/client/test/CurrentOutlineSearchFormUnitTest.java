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
package org.eclipse.scout.rt.client.test;

import org.eclipse.scout.rt.client.services.common.test.ClientTestUtility;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;

public class CurrentOutlineSearchFormUnitTest extends DrilldownOutlineUnitTest {

  @Override
  protected String getConfiguredTitle() {
    return "Current outline search form";
  }

  @Override
  public void run() throws Exception {
    clearVisitCache();
    IPage activePage = ClientTestUtility.getDesktop().getOutline().getActivePage();
    if (activePage instanceof IPageWithTable) {
      IPageWithTable page = (IPageWithTable) activePage;
      testSearchForm(page, page.getSearchFormInternal());
    }
  }
}
