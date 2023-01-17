/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop.fixtures;

import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.ui.html.json.desktop.fixtures.TablePage.Table;

@ClassId("d86a20c5-48eb-49f4-92dd-b72a27ca33fa")
public class TablePage extends AbstractPageWithTable<Table> {
  private INodePageFactory m_nodePageFactory;
  private int m_numRows;

  public TablePage(int numRows) {
    this(numRows, null);
  }

  public TablePage(int numRows, INodePageFactory nodePageFactory) {
    super(false);
    m_numRows = numRows;
    m_nodePageFactory = nodePageFactory;
    callInitializer();
  }

  @Override
  protected void execLoadData(SearchFilter filter) {
    getTable().fill(m_numRows);
  }

  @Order(10)
  @ClassId("437877cd-e632-41d5-afb8-eb4676491db5")
  public class Table extends org.eclipse.scout.rt.ui.html.json.table.fixtures.Table {

  }

  @Override
  protected IPage<?> execCreateChildPage(ITableRow row) {
    if (m_nodePageFactory == null) {
      return null;
    }

    return m_nodePageFactory.create(row);
  }

  public interface INodePageFactory {
    IPage create(ITableRow row);
  }

  public static class NodePageWithFormFactory implements INodePageFactory {
    @Override
    public IPage create(ITableRow row) {
      return new NodePageWithForm();
    }
  }
}
