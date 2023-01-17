/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.tablefield;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.ITableField;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.UiSessionTestUtility;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonOutline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.Outline;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.TablePage;
import org.eclipse.scout.rt.ui.html.json.fixtures.UiSessionMock;
import org.eclipse.scout.rt.ui.html.json.form.fields.tablefield.fixtures.TableField;
import org.eclipse.scout.rt.ui.html.json.table.fixtures.Table;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class JsonTableFieldTest {
  private UiSessionMock m_uiSession;

  @Before
  public void before() {
    m_uiSession = new UiSessionMock();
  }

  @Test
  public void testTableDisposal() {
    ITable table = new Table();
    ITableField<ITable> tableField = new TableField<>(table);
    JsonTableField<ITableField<?>> jsonTableField = UiSessionTestUtility.newJsonAdapter(m_uiSession, tableField);

    assertNotNull(jsonTableField.getAdapter(table));
    jsonTableField.dispose();
    assertNull(jsonTableField.getAdapter(table));
  }

  @Test
  public void testTableDisposalOnPropertyChange() {
    ITable table = new Table();
    ITable table2 = new Table();
    ITableField<ITable> tableField = new TableField<>(table);
    JsonTableField<ITableField<?>> jsonTableField = UiSessionTestUtility.newJsonAdapter(m_uiSession, tableField);

    //Switch table -> old one needs to be disposed
    assertNotNull(jsonTableField.getAdapter(table));
    tableField.setTable(table2, false);
    assertNull(jsonTableField.getAdapter(table));
    assertNotNull(jsonTableField.getAdapter(table2));
    assertTrue(jsonTableField.getAdapter(table2).isInitialized());

    jsonTableField.dispose();
    assertNull(jsonTableField.getAdapter(table2));
  }

  @Test
  public void testPreventTableDisposal() {
    // Create tablePage
    IPageWithTable<?> tablePage = createTablePageAndSelectNode();
    ITable tablePageTable = tablePage.getTable();
    JsonOutline<IOutline> jsonOutline = UiSessionTestUtility.newJsonAdapter(m_uiSession, tablePage.getOutline());
    Assert.assertNotNull(jsonOutline.getAdapter(tablePageTable));

    // Create table field which uses the table from the table page
    ITableField<ITable> tableField = new TableField<>();
    JsonTableField<ITableField<?>> jsonTableField = UiSessionTestUtility.newJsonAdapter(m_uiSession, tableField);
    tableField.setTable(tablePageTable, true);

    // Dispose table field -> table must not be disposed because table page still needs it
    jsonTableField.dispose();
    assertNotNull(jsonOutline.getAdapter(tablePageTable));
    assertTrue(jsonOutline.getAdapter(tablePageTable).isInitialized());
  }

  private IPageWithTable<?> createTablePageAndSelectNode() {
    TablePage tablePage = new TablePage(1, new TablePage.NodePageWithFormFactory());
    List<IPage<?>> pages = new ArrayList<>();
    pages.add(tablePage);
    IOutline outline = new Outline(pages);
    outline.selectNode(tablePage);

    return tablePage;
  }

  @Test
  public void testPreventTableDisposal2() {
    // Create tablePage
    IPageWithTable<?> tablePage = createTablePageAndSelectNode();
    ITable tablePageTable = tablePage.getTable();
    JsonOutline<IOutline> jsonOutline = UiSessionTestUtility.newJsonAdapter(m_uiSession, tablePage.getOutline());
    Assert.assertNotNull(jsonOutline.getAdapter(tablePageTable));

    // Create table field which uses the table from the table page
    ITableField<ITable> tableField = new TableField<>();
    JsonTableField<ITableField<?>> jsonTableField = UiSessionTestUtility.newJsonAdapter(m_uiSession, tableField);
    tableField.setTable(tablePageTable, true);

    // Switch table -> table must not be disposed because table page still needs it
    ITable table2 = new Table();
    tableField.setTable(table2, true);
    assertNotNull(jsonTableField.getAdapter(table2));
    assertTrue(jsonTableField.getAdapter(table2).isInitialized());
    assertNotNull(jsonOutline.getAdapter(tablePageTable));
    assertTrue(jsonOutline.getAdapter(tablePageTable).isInitialized());
  }
}
