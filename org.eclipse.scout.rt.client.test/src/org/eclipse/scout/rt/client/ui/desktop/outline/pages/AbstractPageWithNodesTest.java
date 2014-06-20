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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractPageWithNodes}
 */
@RunWith(ScoutClientTestRunner.class)
public class AbstractPageWithNodesTest {

  @Test
  public void testRenameChildPages() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    assertNotNull(desktop);

    desktop.setAvailableOutlines(CollectionUtility.arrayList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);

    IOutline outline = desktop.getOutline();
    assertNotNull(outline);
    assertSame(PageWithTableOutline.class, outline.getClass());

    IPage page = outline.getActivePage();
    assertNotNull(page);

    AbstractPageWithNodes parentPage = (AbstractPageWithNodes) desktop.getOutline().getActivePage();
    ITreeNode parentPageNode = desktop.getOutline().getSelectedNode();
    ITreeNode childPageNode = parentPageNode.getChildNode(0);

    Assert.assertEquals("Parent page", parentPageNode.getCell().getText());
    Assert.assertEquals("Child page", childPageNode.getCell().getText());
    Assert.assertEquals("Child page", parentPage.getInternalTable().getRow(0).getCell(0).getText()); //this is the childPages name in the table

    //update the child node's cell text
    childPageNode.getCellForUpdate().setText("my new long text");
    Assert.assertEquals("my new long text", childPageNode.getCell().getText());
    Assert.assertEquals("my new long text", parentPage.getInternalTable().getRow(0).getCell(0).getText()); //text must also be changed in the parent's table

    //rename again
    childPageNode.getCellForUpdate().setText("Child page");
    Assert.assertEquals("Parent page", parentPageNode.getCell().getText());
    Assert.assertEquals("Child page", childPageNode.getCell().getText());
    Assert.assertEquals("Child page", parentPage.getInternalTable().getRow(0).getCell(0).getText());

    //rename on table, must be reflected to the tree
    parentPage.getInternalTable().getRow(0).getCellForUpdate(0).setText("my new long text");
    Assert.assertEquals("my new long text", parentPage.getInternalTable().getRow(0).getCell(0).getText());
    Assert.assertEquals("Parent page", parentPageNode.getCell().getText());
    Assert.assertEquals("my new long text", childPageNode.getCell().getText());
  }

  public static class PageWithTableOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(List<IPage> pageList) throws ProcessingException {
      ParentItemNodePage parentPage = new ParentItemNodePage();
      pageList.add(parentPage);
    }
  }

  public static class ParentItemNodePage extends AbstractPageWithNodes {

    @Override
    protected String getConfiguredTitle() {
      return "Parent page";
    }

    @Override
    protected void execCreateChildPages(List<IPage> pageList) throws ProcessingException {
      ItemNodePage childPage = new ItemNodePage();
      pageList.add(childPage);
    }
  }

  public static class ItemNodePage extends AbstractPageWithNodes {

    @Override
    protected String getConfiguredTitle() {
      return "Child page";
    }
  }
}
