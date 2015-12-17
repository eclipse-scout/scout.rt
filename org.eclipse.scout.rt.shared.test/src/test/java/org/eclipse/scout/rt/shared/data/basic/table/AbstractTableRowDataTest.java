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
package org.eclipse.scout.rt.shared.data.basic.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class AbstractTableRowDataTest {

  private static final String TEST_COLUMN_ID = "testColumnId";

  private TestingTableRowData m_rowData;

  @Before
  public void before() {
    m_rowData = new TestingTableRowData();
  }

  @Test
  public void testSetCustomColumnValue() {
    assertNull(m_rowData.getCustomValues());
    assertNull(m_rowData.getCustomValue(TEST_COLUMN_ID));

    String value = "TEST";
    m_rowData.setCustomValue(TEST_COLUMN_ID, value);
    assertNotNull(m_rowData.getCustomValues());
    assertEquals(value, m_rowData.getCustomValue(TEST_COLUMN_ID));
  }

  @Test
  public void testRemoveCustomColumnValue() {
    assertNull(m_rowData.getCustomValues());
    assertNull(m_rowData.removeCustomValue(TEST_COLUMN_ID));
    assertNull(m_rowData.getCustomValues());

    String value = "TEST";
    m_rowData.setCustomValue(TEST_COLUMN_ID, value);
    assertNotNull(m_rowData.getCustomValues());
    assertEquals(value, m_rowData.removeCustomValue(TEST_COLUMN_ID));
    assertNull(m_rowData.getCustomValues());
  }

  private static class TestingTableRowData extends AbstractTableRowData {
    private static final long serialVersionUID = 1L;
  }
}
