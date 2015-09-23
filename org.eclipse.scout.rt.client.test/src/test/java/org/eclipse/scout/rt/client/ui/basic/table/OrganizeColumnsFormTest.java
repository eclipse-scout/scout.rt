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
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.AllAccessControlService;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link OrganizeColumnsForm}
 *
 * @since 4.1.0
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class OrganizeColumnsFormTest {

  private IBean<?> m_reg;

  @Before
  public void before() {
    m_reg = TestingUtility.registerWithTestingOrder(AllAccessControlService.class);
  }

  @After
  public void after() {
    TestingUtility.unregisterBean(m_reg);
  }

  @Test
  public void testNoNPEInExecDrop() throws ProcessingException {
    ITable table = mock(ITable.class);
    List<ITableRow> list = new LinkedList<ITableRow>();
    list.add(mock(ITableRow.class));
    JavaTransferObject transfer = mock(JavaTransferObject.class);
    when(transfer.getLocalObjectAsList(ITableRow.class)).thenReturn(list);
    OrganizeColumnsForm form = new OrganizeColumnsForm(table);
    try {
      form.getColumnsTableField().getTable().execDrop(null, null);
      form.getColumnsTableField().getTable().execDrop(null, transfer);
      form.getColumnsTableField().getTable().execDrop(mock(ITableRow.class), null);
    }
    catch (NullPointerException e) {
      fail("Null-Argument should not lead to NullPointerException " + e);
    }
  }

  /**
   * The table should be reset to the original state after the form has been canceled.
   *
   * @throws InterruptedException
   */
  @Test
  public void testDiscardChanges() throws ProcessingException, InterruptedException {
    TestPage page = new TestOutline().getPage();
    page.ensureChildrenLoaded();
    ITable table = page.getTable();

    OrganizeColumnsForm form = new OrganizeColumnsForm(table);
    form.start();
    form.reload();
    // apply chages
    form.getColumnsTableField().getTable().checkRow(0, false);
    assertFalse(table.getColumns().get(0).isVisible());

    // discard and check state
    form.getDiscardChangesButton().doClick();
    assertTrue(table.getColumns().get(0).isVisible());
  }

  public static class TestOutline extends AbstractOutline {
    private TestPage m_page;

    public TestPage getPage() {
      return m_page;
    }

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) throws ProcessingException {
      TestPage page = new TestPage();
      page.initPage();
      pageList.add(page);
      m_page = page;
    }

  }

  public static class TestPage extends AbstractPageWithNodes {

    @Override
    protected String getConfiguredTitle() {
      return "Parent";
    }

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) throws ProcessingException {
      pageList.add(new ChildPage());
    }
  }

  public static class ChildPage extends AbstractPageWithNodes {
    @Override
    protected String getConfiguredTitle() {
      return "Child";
    }
  }

}
