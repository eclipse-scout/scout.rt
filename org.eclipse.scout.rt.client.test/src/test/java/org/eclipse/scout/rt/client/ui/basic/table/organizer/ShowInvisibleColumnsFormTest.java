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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ShowInvisibleColumnsForm.MainBox.GroupBox.ColumnsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ShowInvisibleColumnsForm.MainBox.GroupBox.ColumnsTableField.Table;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ShowInvisibleColumnsFormTest {

  private ITable m_tableMock;
  private IColumn<?> m_column1;
  private IColumn<?> m_column2;
  private IColumn<?> m_column3;
  private IColumn<?> m_column4;

  @Before
  public void before() {
    m_column1 = mockColumn("column1", false);
    m_column2 = mockColumn("column2", true);
    m_column3 = mockColumn("column3", false);
    m_column4 = mockColumn("column4", true);

    m_tableMock = Mockito.mock(ITable.class);
    ColumnSet mockColumnSet = Mockito.mock(ColumnSet.class);
    Mockito.when(mockColumnSet.getAllColumnsInUserOrder()).thenReturn(CollectionUtility.arrayList(m_column3, m_column4, m_column1, m_column2));
    Mockito.when(mockColumnSet.getColumns()).thenReturn(CollectionUtility.arrayList(m_column1, m_column2, m_column3, m_column4));
    Mockito.when(m_tableMock.getColumnSet()).thenReturn(mockColumnSet);
  }

  protected IColumn<?> mockColumn(String name, boolean visible) {
    IColumn<?> col = Mockito.mock(IColumn.class, name);
    IHeaderCell headerCell = Mockito.mock(IHeaderCell.class);
    when(headerCell.getText()).thenReturn(name);
    when(col.getHeaderCell()).thenReturn(headerCell);
    when(col.isVisible()).thenReturn(visible);
    when(col.isDisplayable()).thenReturn(true);
    when(col.isVisibleGranted()).thenReturn(true);
    return col;
  }

  protected ShowInvisibleColumnsForm getForm() {
    return new ShowInvisibleColumnsForm(m_tableMock);
  }

  @Test
  public void testModifyHandler() {
    ShowInvisibleColumnsForm form = getForm();
    form.startModify();

    ColumnsTableField tableField = form.getColumnsTableField();
    Table table = tableField.getTable();
    List<IColumn<?>> columns = table.getKeyColumn().getValues();
    assertEquals(Arrays.asList(m_column3, m_column1), columns);

    form.touch();
    form.doClose();
  }
}
