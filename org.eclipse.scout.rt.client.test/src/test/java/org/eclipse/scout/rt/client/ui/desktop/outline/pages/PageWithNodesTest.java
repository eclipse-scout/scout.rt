/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.ScoutInfoForm;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractPageWithNodes}
 *
 * @since 4.0.0-M6
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PageWithNodesTest {

  /**
   * Tests that {@link AbstractPage#execPageDataLoaded()} is called correctly on a {@link AbstractPageWithNodes}
   */
  @Test
  public void testExecPageDataLoaded() {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(Collections.singletonList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);
    desktop.activateFirstPage();
    IOutline outline = desktop.getOutline();
    PageWithNodes page = (PageWithNodes) outline.getActivePage();
    Assert.assertEquals(1, page.m_execPageDataLoadedCalled);
    page.reloadPage();
    page.reloadPage();
    page.reloadPage();
    Assert.assertEquals(4, page.m_execPageDataLoadedCalled);
  }

  @Test
  public void testLazyLoading() {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    PageWithTableOutline o = new PageWithTableOutline();
    desktop.setAvailableOutlines(Collections.singletonList(o));
    desktop.setOutline(PageWithTableOutline.class);
    desktop.activateFirstPage();

    Assert.assertTrue(o.getActivePage() instanceof PageWithNodes);
    PageWithNodes pageWithNodes = (PageWithNodes) o.getActivePage();
    Assert.assertEquals(1, pageWithNodes.m_execCreateChildPages);

    for (IPage<?> p : pageWithNodes.getChildPages()) {
      Assert.assertEquals(0, ((PageWithNode) p).m_execCreateChildPages);
      Assert.assertEquals(0, ((PageWithNode) p).m_execInitDetailForm);
    }

    IPage<?> firstChildPage = pageWithNodes.getChildPage(0);
    IPage<?> secondChildPage = pageWithNodes.getChildPage(1);
    firstChildPage.getTree().getUIFacade().setNodesSelectedFromUI(Collections.<ITreeNode> singletonList(firstChildPage));

    Assert.assertEquals(1, ((PageWithNode) firstChildPage).m_execCreateChildPages);
    Assert.assertEquals(1, ((PageWithNode) firstChildPage).m_execInitDetailForm);
    Assert.assertTrue(((PageWithNode) firstChildPage).m_firePageChanged > 0);

    Assert.assertEquals(0, ((PageWithNode) secondChildPage).m_execCreateChildPages);
    Assert.assertEquals(0, ((PageWithNode) secondChildPage).m_execInitDetailForm);
    Assert.assertEquals(0, ((PageWithNode) secondChildPage).m_firePageChanged);
  }

  private static class PageWithTableOutline extends AbstractOutline {

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithNodes());
    }
  }

  private static class PageWithNodes extends AbstractPageWithNodes {

    private int m_execPageDataLoadedCalled = 0;
    private int m_execCreateChildPages = 0;

    @Override
    protected void execPageDataLoaded() {
      super.execPageDataLoaded();
      m_execPageDataLoadedCalled++;
    }

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      m_execCreateChildPages++;
      pageList.add(new PageWithNode());
      pageList.add(new PageWithNode());
      pageList.add(new PageWithNode());
      pageList.add(new PageWithNode());
    }
  }

  private static class PageWithNode extends AbstractPageWithNodes {
    private int m_execCreateChildPages = 0;
    private int m_execInitDetailForm = 0;
    private int m_firePageChanged = 0;

    @Override
    protected Class<? extends IForm> getConfiguredDetailForm() {
      return ScoutInfoForm.class;
    }

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      m_execCreateChildPages++;
    }

    @Override
    protected void firePageChanged(IPage page) {
      super.firePageChanged(page);
      m_firePageChanged++;
    }

    @Override
    protected void execInitDetailForm() {
      m_execInitDetailForm++;
    }
  }
}
