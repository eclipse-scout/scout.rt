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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractPageWithNodes}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractPageWithNodesTest {

  @Test
  public void testInitNodePage() throws Exception {
    ParentItemNodePage pItem = new ParentItemNodePage();
    pItem.initPage();
    assertEquals("Parent page", pItem.getCell().getText());
  }

  @Test
  public void testPageNodeText() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(CollectionUtility.arrayList(new PageWithNodeOutline()));
    desktop.setOutline(PageWithNodeOutline.class);
    desktop.activateFirstPage();
    ITreeNode parentPageNode = desktop.getOutline().getSelectedNode();
    assertEquals("Parent page", parentPageNode.getCell().getText());
  }

  @Test
  public void testSetupOutlinePage() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    assertNotNull(desktop);

    desktop.setAvailableOutlines(CollectionUtility.arrayList(new PageWithNodeOutline()));
    desktop.setOutline(PageWithNodeOutline.class);
    desktop.activateFirstPage();

    IOutline outline = desktop.getOutline();
    assertNotNull(outline);
    assertSame(PageWithNodeOutline.class, outline.getClass());

    IPage<?> page = outline.getActivePage();
    assertNotNull(page);
  }

  @Test
  public void testRenameChildPages() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(CollectionUtility.arrayList(new PageWithNodeOutline()));
    desktop.setOutline(PageWithNodeOutline.class);
    desktop.activateFirstPage();

    AbstractPageWithNodes parentPage = (AbstractPageWithNodes) desktop.getOutline().getActivePage();
    ITreeNode parentPageNode = desktop.getOutline().getSelectedNode();
    ITreeNode childPageNode = parentPageNode.getChildNode(0);

    assertEquals("Parent page", parentPageNode.getCell().getText());
    assertEquals("Child page", childPageNode.getCell().getText());
    assertEquals("Child page", parentPage.getTable().getRow(0).getCell(0).getText()); //this is the childPages name in the table

    //update the child node's cell text
    childPageNode.getCellForUpdate().setText("my new long text");
    assertEquals("my new long text", childPageNode.getCell().getText());
    assertEquals("my new long text", parentPage.getTable().getRow(0).getCell(0).getText()); //text must also be changed in the parent's table

    //rename again
    childPageNode.getCellForUpdate().setText("Child page");
    assertEquals("Parent page", parentPageNode.getCell().getText());
    assertEquals("Child page", childPageNode.getCell().getText());
    assertEquals("Child page", parentPage.getTable().getRow(0).getCell(0).getText());

    //rename on table, must be reflected to the tree
    parentPage.getTable().getRow(0).getCellForUpdate(0).setText("my new long text");
    assertEquals("my new long text", parentPage.getTable().getRow(0).getCell(0).getText());
    assertEquals("Parent page", parentPageNode.getCell().getText());
    assertEquals("my new long text", childPageNode.getCell().getText());
  }

  @Test
  public void testModelContextOfInitPageAndInitTable() {
    IForm mockForm = Mockito.mock(IForm.class);
    IOutline mockOutline = Mockito.mock(IOutline.class);
    ClientRunContexts
        .copyCurrent()
        .withOutline(mockOutline, true)
        .withForm(mockForm)
        .run(new IRunnable() {
          @Override
          public void run() throws Exception {
            IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
            assertNotNull(desktop);

            desktop.setAvailableOutlines(CollectionUtility.arrayList(new PageWithNodeOutline()));
            desktop.setOutline(PageWithNodeOutline.class);
            desktop.activateFirstPage();

            IOutline outline = desktop.getOutline();
            assertNotNull(outline);
            assertSame(PageWithNodeOutline.class, outline.getClass());

            IPage<?> page = outline.getActivePage();
            assertNotNull(page);
            assertSame(ParentItemNodePage.class, page.getClass());
            ParentItemNodePage nodePage = (ParentItemNodePage) page;

            // init page
            ModelContext initPageContext = nodePage.getInitPageContext();
            assertNotNull(initPageContext);
            assertSame(desktop, initPageContext.getDesktop());
            assertSame(outline, initPageContext.getOutline());
            assertNull(initPageContext.getForm()); // no context form must be set

            // init table
            ModelContext initTableContext = nodePage.getInitTableContext();
            assertNotNull(initTableContext);
            assertSame(desktop, initTableContext.getDesktop());
            assertSame(outline, initTableContext.getOutline());
            assertNull(initTableContext.getForm()); // no context form must be set
          }
        });
  }

  public static class PageWithNodeOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new ParentItemNodePage());
    }
  }

  public static class ParentItemNodePage extends AbstractPageWithNodes {

    private final FinalValue<ModelContext> m_initPageContext = new FinalValue<>();
    private final FinalValue<ModelContext> m_initTableContext = new FinalValue<>();

    public ModelContext getInitPageContext() {
      return m_initPageContext.get();
    }

    public ModelContext getInitTableContext() {
      return m_initTableContext.get();
    }

    @Override
    protected String getConfiguredTitle() {
      return "Parent page";
    }

    @Override
    protected void execInitPage() {
      m_initPageContext.set(ModelContext.copyCurrent());
      super.execInitPage();
    }

    @Override
    protected void execInitTable() {
      m_initTableContext.set(ModelContext.copyCurrent());
      super.execInitTable();
    }

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new ItemNodePage());
    }
  }

  public static class ItemNodePage extends AbstractPageWithNodes {

    @Override
    protected String getConfiguredTitle() {
      return "Child page";
    }
  }
}
