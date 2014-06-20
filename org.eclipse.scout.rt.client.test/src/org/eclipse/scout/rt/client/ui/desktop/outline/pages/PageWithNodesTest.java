/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractPageWithNodes}
 * 
 * @since 4.0.0-M6
 */
@RunWith(ScoutClientTestRunner.class)
public class PageWithNodesTest {

  /**
   * Tests that {@link AbstractPage#execPageDataLoaded()} is called correctly on a {@link AbstractPageWithNodes}
   */
  @Test
  public void testExecPageDataLoaded() throws ProcessingException {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(Collections.singletonList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);
    IOutline outline = desktop.getOutline();
    PageWithNodes page = (PageWithNodes) outline.getActivePage();
    Assert.assertEquals(1, page.m_execPageDataLoadedCalled);
    page.reloadPage();
    page.reloadPage();
    page.reloadPage();
    Assert.assertEquals(4, page.m_execPageDataLoadedCalled);
  }

  public static class PageWithTableOutline extends AbstractOutline {

    @Override
    protected void execCreateChildPages(List<IPage> pageList) throws ProcessingException {
      pageList.add(new PageWithNodes());
    }
  }

  public static class PageWithNodes extends AbstractPageWithNodes {

    public int m_execPageDataLoadedCalled = 0;

    @Override
    protected void execPageDataLoaded() throws ProcessingException {
      super.execPageDataLoaded();
      m_execPageDataLoadedCalled++;
    }

    @Override
    protected void execCreateChildPages(List<IPage> pageList) throws ProcessingException {
      pageList.add(new PageWithNode());
      pageList.add(new PageWithNode());
      pageList.add(new PageWithNode());
      pageList.add(new PageWithNode());
    }
  }

  public static class PageWithNode extends AbstractPageWithNodes {

  }
}
