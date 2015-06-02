/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columnfilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.junit.Test;

/**
 * Tests for {@link DefaultTableColumnFilterManager}
 */
public class DefaultTableColumnFilterManagerTest {

  @Test
  public void testGetAllFilters_emptyCollection() throws Exception {
    DefaultTableColumnFilterManager filterManager = new DefaultTableColumnFilterManager(mock(ITable.class));
    assertTrue("expected empty collection", filterManager.getFilters().isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetAllFilters_collectionUnmodifiable() throws Exception {
    DefaultTableColumnFilterManager filterManager = new DefaultTableColumnFilterManager(mock(ITable.class));
    ITableColumnFilter filter1 = mock(ITableColumnFilter.class);
    ITableColumnFilter filter2 = mock(ITableColumnFilter.class);
    Map filterMap = getFilterMapWithReflection(filterManager);
    filterMap.put(mock(IColumn.class), filter1);
    filterMap.put(mock(IColumn.class), filter2);

    Collection<ITableColumnFilter> allFilters = filterManager.getFilters();
    assertEquals(2, allFilters.size());
    boolean unsupportedOperationExceptionCaught = false;
    try {
      allFilters.remove(filter1);
    }
    catch (UnsupportedOperationException e) {
      unsupportedOperationExceptionCaught = true;
    }
    assertTrue("Expected collection to be unmodifiable!", unsupportedOperationExceptionCaught);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRemoveFilter() throws Exception {
    DefaultTableColumnFilterManager filterManager = new DefaultTableColumnFilterManager(mock(ITable.class));
    ITableColumnFilter filter1 = mock(ITableColumnFilter.class);
    ITableColumnFilter filter2 = mock(ITableColumnFilter.class);
    Map filterMap = getFilterMapWithReflection(filterManager);
    IColumn col1 = mock(IColumn.class);
    IColumn col2 = mock(IColumn.class);
    IColumn col3 = mock(IColumn.class);
    filterMap.put(col1, filter1);
    filterMap.put(col2, filter2);

    assertEquals(2, filterManager.getFilters().size());
    assertTrue(filterManager.removeFilter(col1));
    assertFalse(filterManager.removeFilter(col3));
    assertEquals(1, filterManager.getFilters().size());
    assertEquals(filter2, filterManager.getFilter(col2));
  }

  private Map getFilterMapWithReflection(DefaultTableColumnFilterManager filterManager) throws NoSuchFieldException, IllegalAccessException {
    Field f = filterManager.getClass().getDeclaredField("m_filterMap");
    f.setAccessible(true);
    Map filterMap = (Map) f.get(filterManager);
    return filterMap;
  }
}
