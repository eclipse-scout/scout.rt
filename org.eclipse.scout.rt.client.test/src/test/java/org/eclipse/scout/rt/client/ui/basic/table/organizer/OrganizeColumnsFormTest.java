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
package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.dnd.JavaTransferObject;
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
  public void testNoNPEInExecDrop() {
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

  public static class TestOutline extends AbstractOutline {
    private TestPage m_page;

    public TestPage getPage() {
      return m_page;
    }

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
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
    protected void execCreateChildPages(List<IPage<?>> pageList) {
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
