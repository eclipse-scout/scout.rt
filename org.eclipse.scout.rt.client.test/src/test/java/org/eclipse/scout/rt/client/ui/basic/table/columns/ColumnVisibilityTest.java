/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ColumnVisibilityTest.TestTable.Test1Column;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ColumnVisibilityTest.TestTable.Test2Column;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit test for {@link IColumn} visibility
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ColumnVisibilityTest {

  private ClientUIPreferences m_uiPrefs;
  private Test1Column m_prefCol1;
  private Test2Column m_prefCol2;

  @Before
  public void before() throws Exception {
    // reset UI preferences
    final TestTable prefTable = new TestTable();
    m_prefCol1 = prefTable.getTest1Column();
    m_prefCol2 = prefTable.getTest2Column();
    m_uiPrefs = ClientUIPreferences.getInstance();
    m_uiPrefs.removeAllTableColumnPreferences(m_prefCol1, null, true);
    m_uiPrefs.removeAllTableColumnPreferences(m_prefCol2, null, true);
  }

  /**
   * Column that is configured displayable and that is set to not-displayable and back to displayable in the
   * execInitTable of {@link AbstractTable}.
   */
  @Test
  public void test_displayable_configuredTrue_initFalseTrue() throws Exception {
    TestTable table = new TestTable();
    table.initTable();
    Test1Column col1 = table.getTest1Column();

    assertTrue(col1.isDisplayable());
    assertTrue(col1.isVisible());
  }

  /**
   * Column that is configured not-displayable and that is set to displayable in the execInitTable of
   * {@link AbstractTable}.
   */
  @Test
  public void test_displayable_configuredFalse_initTrue() throws Exception {
    TestTable table = new TestTable();
    table.initTable();
    Test2Column col2 = table.getTest2Column();

    assertTrue(col2.isDisplayable());
    assertTrue(col2.isVisible());
  }

  /**
   * Column that is configured displayable and that is set to not-displayable and back to displayable in the
   * execInitTable of {@link AbstractTable}. In addition the column's preferences are set to invisible.
   */
  @Test
  public void test_displayable_configuredTrue_initFalseTrue_uiPrefsVisibleFalse() throws Exception {
    // set ui preferences
    m_prefCol1.setVisible(false);
    m_uiPrefs.setTableColumnPreferences(m_prefCol1);

    // perform test
    TestTable table = new TestTable();
    table.initTable();
    Test1Column col1 = table.getTest1Column();

    assertTrue(col1.isDisplayable());
    assertFalse(col1.isVisible());
  }

  /**
   * Column that is configured not-displayable and that is set to displayable in the execInitTable of
   * {@link AbstractTable}. In addition the column's preferences are set to invisible.
   */
  @Test
  public void test_displayable_configuredFalse_initTrue_uiPrefsVisibleFalse() throws Exception {
    // set ui preferences
    m_prefCol2.setVisible(false);
    m_uiPrefs.setTableColumnPreferences(m_prefCol2);

    // perform test
    TestTable table = new TestTable();
    table.initTable();
    Test2Column col2 = table.getTest2Column();

    assertTrue(col2.isDisplayable());
    assertFalse(col2.isVisible());
  }

  /**
   * Column that is configured displayable and that is set to not-displayable and back to displayable in the
   * execInitTable of {@link AbstractTable}. In addition the column's preferences are set to visible but visible granted
   * is set to false.
   */
  @Test
  public void test_displayable_configuredTrue_initFalseTrue_uiPrefsVisibleTrueVisibleGrantedFalse() throws Exception {
    // set ui preferences
    m_prefCol1.setVisible(true);
    m_prefCol1.setVisibleGranted(false);
    m_uiPrefs.setTableColumnPreferences(m_prefCol1);

    // perform test
    TestTable table = new TestTable();
    table.initTable();
    Test1Column col1 = table.getTest1Column();

    assertTrue(col1.isDisplayable());
    assertTrue(col1.isVisible());
  }

  /**
   * Column that is configured not-displayable and that is set to displayable in the execInitTable of
   * {@link AbstractTable}. In addition the column's preferences are set to visible but visible granted is set to false.
   */
  @Test
  public void test_displayable_configuredFalse_initTrue_uiPrefsVisibleTrueVisibleGrantedFalse() throws Exception {
    // set ui preferences
    m_prefCol2.setVisible(true);
    m_prefCol2.setVisibleGranted(false);
    m_uiPrefs.setTableColumnPreferences(m_prefCol2);

    // perform test
    TestTable table = new TestTable();
    table.initTable();
    Test2Column col2 = table.getTest2Column();

    assertTrue(col2.isDisplayable());
    assertTrue(col2.isVisible());
  }

  public class TestTable extends AbstractTable {

    public Test1Column getTest1Column() {
      return getColumnSet().getColumnByClass(Test1Column.class);
    }

    public Test2Column getTest2Column() {
      return getColumnSet().getColumnByClass(Test2Column.class);
    }

    @Override
    protected void execInitTable() {
      getTest1Column().setDisplayable(false);
      getTest1Column().setDisplayable(true);

      getTest2Column().setDisplayable(true);
    }

    @Order(10)
    public class Test1Column extends AbstractColumn<Object> {
      @Override
      public void setVisible(boolean b) {
        super.setVisible(b);
      }
    }

    @Order(20)
    public class Test2Column extends AbstractColumn<Object> {
      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }
    }

    /**
     * Additional column that is always visible to prevent sanity operation that resets all columns to its initial
     * configuration if no column is visible.
     */
    @Order(30)
    public class Test3Column extends AbstractColumn<Object> {
    }
  }

}
