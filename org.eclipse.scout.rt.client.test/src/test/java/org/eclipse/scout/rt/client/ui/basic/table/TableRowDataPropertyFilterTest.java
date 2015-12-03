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
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.TableRowDataPropertyFilter;
import org.junit.Test;

/**
 * @since 3.10.0-M4
 */
@SuppressWarnings("unused")
public class TableRowDataPropertyFilterTest {

  @Test
  public void testTableRowDataPropertyFilter() throws Exception {
    TableRowDataPropertyFilter propertyFilter = new TableRowDataPropertyFilter();
    FastPropertyDescriptor[] props = BeanUtility.getFastPropertyDescriptors(P_TableRowData.class, AbstractTableRowData.class, propertyFilter);
    Map<String, FastPropertyDescriptor> propertyDescriptorsByName = new HashMap<String, FastPropertyDescriptor>();
    for (FastPropertyDescriptor prop : props) {
      propertyDescriptorsByName.put(prop.getName(), prop);
    }
    assertEquals(3, propertyDescriptorsByName.size());
    assertTrue(propertyDescriptorsByName.containsKey("intColumn"));
    assertTrue(propertyDescriptorsByName.containsKey("booleanColumn"));
    assertTrue(propertyDescriptorsByName.containsKey("objectColumn"));
  }

  private static class P_TableRowData extends AbstractTableRowData {
    private static final long serialVersionUID = 1L;

    private Object m_objectColumn;
    private int m_intColumn;
    private boolean m_booleanColumn;
    private String m_stringWithoutGetterColumn;
    private String m_stringWithoutSetterColumn;

    public Object getObjectColumn() {
      return m_objectColumn;
    }

    public void setObjectColumn(Object objectColumn) {
      m_objectColumn = objectColumn;
    }

    public int getIntColumn() {
      return m_intColumn;
    }

    public void setIntColumn(int intColumn) {
      m_intColumn = intColumn;
    }

    public boolean isBooleanColumn() {
      return m_booleanColumn;
    }

    public void setBooleanColumn(boolean booleanColumn) {
      m_booleanColumn = booleanColumn;
    }

    public void setStringWithoutGetterColumn(String stringWithoutGetterColumn) {
      m_stringWithoutGetterColumn = stringWithoutGetterColumn;
    }

    public String getStringWithoutSetterColumn() {
      return m_stringWithoutSetterColumn;
    }
  }
}
