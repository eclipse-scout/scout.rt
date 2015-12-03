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
package org.eclipse.scout.rt.client.extension.ui.basic.table;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.AbstractPersonTable;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.AllPersonTable;
import org.eclipse.scout.rt.client.extension.ui.basic.table.fixture.OtherPersonTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.junit.Test;

public class TableMoveColumnsTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testMoveColumn() {
    BEANS.get(IExtensionRegistry.class).registerMove(AllPersonTable.CityColumn.class, 30);

    AllPersonTable table = new AllPersonTable();
    assertEquals(Arrays.<IColumn<?>> asList(table.getNameColumn(), table.getAgeColumn(), table.getCityColumn()), table.getColumnSet().getColumns());
    assertEquals(30, table.getCityColumn().getOrder(), 0);
  }

  @Test
  public void testMoveColumns() {
    BEANS.get(IExtensionRegistry.class).registerMove(AllPersonTable.CityColumn.class, 5);
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractPersonTable.AgeColumn.class, 10);
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractPersonTable.NameColumn.class, 20);

    AllPersonTable table = new AllPersonTable();
    assertEquals(Arrays.<IColumn<?>> asList(table.getCityColumn(), table.getAgeColumn(), table.getNameColumn()), table.getColumnSet().getColumns());
    assertEquals(5, table.getCityColumn().getOrder(), 0);
    assertEquals(10, table.getAgeColumn().getOrder(), 0);
    assertEquals(20, table.getNameColumn().getOrder(), 0);
  }

  @Test
  public void testMoveColumnMultipleTimes() {
    BEANS.get(IExtensionRegistry.class).registerMove(AllPersonTable.CityColumn.class, 30);
    BEANS.get(IExtensionRegistry.class).registerMove(AllPersonTable.CityColumn.class, 5);

    AllPersonTable table = new AllPersonTable();
    assertEquals(Arrays.<IColumn<?>> asList(table.getCityColumn(), table.getNameColumn(), table.getAgeColumn()), table.getColumnSet().getColumns());
    assertEquals(5, table.getCityColumn().getOrder(), 0);
  }

  @Test
  public void testMoveColumnInAbstractTable() {
    BEANS.get(IExtensionRegistry.class).registerMove(AbstractPersonTable.AgeColumn.class, 5);

    AllPersonTable allTable = new AllPersonTable();
    assertEquals(Arrays.<IColumn<?>> asList(allTable.getAgeColumn(), allTable.getNameColumn(), allTable.getCityColumn()), allTable.getColumnSet().getColumns());
    assertEquals(5, allTable.getAgeColumn().getOrder(), 0);

    OtherPersonTable otherTable = new OtherPersonTable();
    assertEquals(Arrays.<IColumn<?>> asList(otherTable.getAgeColumn(), otherTable.getNameColumn(), otherTable.getPhoneNumberColumn()), otherTable.getColumnSet().getColumns());
    assertEquals(5, otherTable.getAgeColumn().getOrder(), 0);
  }
}
