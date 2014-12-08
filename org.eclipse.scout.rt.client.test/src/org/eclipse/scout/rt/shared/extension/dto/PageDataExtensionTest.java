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
package org.eclipse.scout.rt.shared.extension.dto;

import java.math.BigDecimal;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultiColumnExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultiColumnExtensionData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigPageWithTable;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigPageWithTableData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigPageWithTableData.OrigPageWithTableRowData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.ThirdIntegerColumn;
import org.eclipse.scout.rt.shared.extension.dto.fixture.ThirdIntegerColumnData;
import org.eclipse.scout.service.SERVICES;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class PageDataExtensionTest extends AbstractLocalExtensionTestCase {
  @Test
  public void testPageDataSingleExtensionExplicit() throws Exception {
    SERVICES.getService(IExtensionRegistry.class).register(ThirdIntegerColumn.class, OrigPageWithTable.Table.class);
    SERVICES.getService(IExtensionRegistry.class).register(ThirdIntegerColumnData.class, OrigPageWithTableRowData.class);
    doTestSingle();
  }

  @Test
  public void testPageDataMultipleExtensionAnnotation() throws Exception {
    SERVICES.getService(IExtensionRegistry.class).register(MultiColumnExtension.class);
    SERVICES.getService(IExtensionRegistry.class).register(MultiColumnExtensionData.class);
    doTestMulti();
  }

  private void doTestMulti() throws Exception {
    Long EXT_TEST_VAL1_EXPORT = Long.valueOf(6);
    Double EXT_TEST_VAL2_EXPORT = Double.valueOf(7);
    Long EXT_TEST_VAL1_IMPORT = Long.valueOf(8);
    Double EXT_TEST_VAL2_IMPORT = Double.valueOf(9);

    // setup table
    OrigPageWithTable pwt = new OrigPageWithTable();
    pwt.getTable().addRowsByMatrix(new Object[][]{{new BigDecimal("1"), Long.valueOf(2), EXT_TEST_VAL1_EXPORT, EXT_TEST_VAL2_EXPORT}});
    int columnCount = pwt.getTable().getColumnSet().getColumnCount();
    Assert.assertEquals(4, columnCount);
    Assert.assertEquals(1, pwt.getTable().getRowCount());
    Assert.assertEquals(EXT_TEST_VAL1_EXPORT, pwt.getTable().getRow(0).getCell(2).getValue());
    Assert.assertEquals(EXT_TEST_VAL2_EXPORT, pwt.getTable().getRow(0).getCell(3).getValue());

    // test export
    OrigPageWithTableData data = new OrigPageWithTableData();
    pwt.getTable().exportToTableBeanData(data);
    Assert.assertEquals(EXT_TEST_VAL1_EXPORT, data.getRows()[0].getContribution(MultiColumnExtensionData.class).getThirdLong());
    Assert.assertEquals(EXT_TEST_VAL2_EXPORT, data.getRows()[0].getContribution(MultiColumnExtensionData.class).getFourthDouble());

    // test import
    data.getRows()[0].getContribution(MultiColumnExtensionData.class).setThirdLong(EXT_TEST_VAL1_IMPORT);
    data.getRows()[0].getContribution(MultiColumnExtensionData.class).setFourthDouble(EXT_TEST_VAL2_IMPORT);
    pwt.getTable().importFromTableBeanData(data);
    Assert.assertEquals(EXT_TEST_VAL1_IMPORT, pwt.getTable().getRow(0).getCell(2).getValue());
    Assert.assertEquals(EXT_TEST_VAL2_IMPORT, pwt.getTable().getRow(0).getCell(3).getValue());
  }

  private void doTestSingle() throws Exception {
    Long ORIG_TEST_VAL_EXPORT = Long.valueOf(66);
    Integer EXT_TEST_VAL_EXPORT = Integer.valueOf(77);
    BigDecimal ORIG_TEST_VAL_IMPORT = new BigDecimal("88");
    Integer EXT_TEST_VAL_IMPORT = Integer.valueOf(99);

    // setup table
    OrigPageWithTable pwt = new OrigPageWithTable();
    pwt.getTable().addRowsByMatrix(new Object[][]{
        {new BigDecimal("22"), Long.valueOf(33), Integer.valueOf(44)},
        {new BigDecimal("55"), ORIG_TEST_VAL_EXPORT, EXT_TEST_VAL_EXPORT}
    });
    int columnCount = pwt.getTable().getColumnSet().getColumnCount();
    Assert.assertEquals(3, columnCount);
    Assert.assertEquals(2, pwt.getTable().getRowCount());
    Assert.assertEquals(ORIG_TEST_VAL_EXPORT, pwt.getTable().getRow(1).getCell(1).getValue());
    Assert.assertEquals(EXT_TEST_VAL_EXPORT, pwt.getTable().getRow(1).getCell(2).getValue());

    // test export
    OrigPageWithTableData data = new OrigPageWithTableData();
    pwt.getTable().exportToTableBeanData(data);
    Assert.assertEquals(ORIG_TEST_VAL_EXPORT, data.getRows()[1].getSecondSmart());
    Assert.assertEquals(EXT_TEST_VAL_EXPORT, data.getRows()[1].getContribution(ThirdIntegerColumnData.class).getThirdInteger());

    // test import
    data.getRows()[1].setFirstBigDecimal(ORIG_TEST_VAL_IMPORT);
    data.getRows()[1].getContribution(ThirdIntegerColumnData.class).setThirdInteger(EXT_TEST_VAL_IMPORT);
    pwt.getTable().importFromTableBeanData(data);
    Assert.assertEquals(ORIG_TEST_VAL_IMPORT, pwt.getTable().getRow(1).getCell(0).getValue());
    Assert.assertEquals(EXT_TEST_VAL_IMPORT, pwt.getTable().getRow(1).getCell(2).getValue());
  }
}
