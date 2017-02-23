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
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.testing.client.form.DynamicStringField;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractListBox}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ListBoxTest extends AbstractListBox<Long> {

  private static List<IBean<?>> m_beans;

  @BeforeClass
  public static void beforeClass() throws Exception {
    m_beans = TestingUtility.registerBeans(new BeanMetaData(ListBoxLookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(m_beans);
  }

  private HashSet<Long> m_testValue;

  /**
   * Initialize test values
   */
  @Before
  public void setup() {
    m_testValue = new HashSet<Long>();
    m_testValue.add(1L);
  }

  @Override
  protected void execFilterLookupResult(ILookupCall<Long> call, List<ILookupRow<Long>> result) {
    result.add(new LookupRow<Long>(1L, "a"));
    result.add(new LookupRow<Long>(2L, "b"));
    result.add(new LookupRow<Long>(3L, "c"));
    result.add(new LookupRow<Long>(null, "null value"));
  }

  @Test
  public void testNoNullKeys() throws Exception {
    List<? extends ILookupRow<Long>> rows = execLoadTableData();
    assertEquals(3, rows.size());
  }

  @Test
  public void testGetForm() {
    IForm formMock = mock(IForm.class);
    DynamicStringField stringField = new DynamicStringField("id", "test");
    addField(stringField);
    try {
      setFormInternal(formMock);
      assertEquals(formMock, getForm());
      assertEquals(formMock, getListBoxFilterBox().getForm());
      assertEquals(formMock, stringField.getForm());
    }
    finally {
      removeField(stringField);
    }
  }

  /**
   * Test {@link #execIsEmpty} empty field
   */
  @Test
  public void testEmpty() {
    assertTrue(execIsEmpty());
    assertTrue(getValue().isEmpty());
    assertEquals(0, getCheckedKeyCount());
  }

  /**
   * Test {@link #execIsEmpty} non empty
   */
  @Test
  public void testNonEmpty() {
    setValue(m_testValue);
    assertFalse(execIsEmpty());
    ScoutAssert.assertSetEquals(m_testValue, getValue());
    assertEquals(1, getCheckedKeyCount());
    assertEquals(Long.valueOf(1L), getCheckedKey());
  }

  /**
   * Tests that the content is valid for a filled mandatory field. {@link #isContentValid()}
   */
  @Test
  public void testContentValid() {
    setValue(m_testValue);
    setMandatory(true);
    assertTrue(isMandatory());
    assertTrue(isContentValid());
  }

  /**
   * Tests that the content is valid for an empty mandatory field. {@link #isContentValid()}
   */
  @Test
  public void testContentInvalid() {
    setMandatory(true);
    assertTrue(isMandatory());
    assertFalse(isContentValid());
  }

  @Test
  public void testValidation() {
    ValidatingListBox listBox = new ValidatingListBox();
    listBox.initField();

    listBox.checkRow("g");
    assertEquals("g", listBox.getCheckedRowsString());
    listBox.uncheckRow("g");
    assertEquals("", listBox.getCheckedRowsString());

    // "b" cannot be checked
    listBox.checkRow("g");
    listBox.checkRow("b");
    assertEquals("g", listBox.getCheckedRowsString());

    // checking "c" checks "d"
    listBox.checkRow("c");
    assertEquals("dg", listBox.getCheckedRowsString());

    // checking "a" delects all other keys
    listBox.checkRow("a");
    assertEquals("a", listBox.getCheckedRowsString());
    listBox.checkRow("b");
    listBox.checkRow("c");
    listBox.checkRow("d");
    assertEquals("a", listBox.getCheckedRowsString());
  }

  public class ValidatingListBox extends AbstractListBox<String> {

    @Override
    protected Class<? extends ILookupCall<String>> getConfiguredLookupCall() {
      return ListBoxLookupCall.class;
    }

    @Override
    protected Set<String> execValidateValue(Set<String> rawValue) {
      if (rawValue != null) {
        // If "a" is checked, no other row may be checked
        if (rawValue.contains("a")) {
          rawValue.clear();
          rawValue.add("a");
        }
        // "b" cannot be checked
        if (rawValue.contains("b")) {
          rawValue.remove("b");
        }
        // checking "c" automatically checkes "d" instead
        if (rawValue.contains("c")) {
          rawValue.remove("c");
          rawValue.add("d");
        }
      }
      return super.execValidateValue(rawValue);
    }

    public String getCheckedRowsString() {
      ITable table = getTable();
      @SuppressWarnings("unchecked")
      IColumn<String> keyColumn = table.getColumnSet().getColumn(0);
      List<String> sortedKeys = new ArrayList<>();
      for (ITableRow row : table.getRows()) {
        if (row.isChecked()) {
          sortedKeys.add(keyColumn.getValue(row));
        }
      }
      Collections.sort(sortedKeys);
      return StringUtility.join("", sortedKeys);
    }

    public void checkRow(String key) {
      ITableRow row = findRow(key);
      if (row != null) {
        row.setChecked(true);
      }
    }

    public void uncheckRow(String key) {
      ITableRow row = findRow(key);
      if (row != null) {
        row.setChecked(false);
      }
    }

    public ITableRow findRow(String key) {
      ITable table = getTable();
      @SuppressWarnings("unchecked")
      IColumn<String> keyColumn = table.getColumnSet().getColumn(0);
      for (ITableRow row : table.getRows()) {
        if (ObjectUtility.equals(keyColumn.getValue(row), key)) {
          return row;
        }
      }
      return null;
    }
  }

  public static class ListBoxLookupCall extends LocalLookupCall<String> {
    private static final long serialVersionUID = 1L;

    @Override
    protected List<? extends ILookupRow<String>> execCreateLookupRows() {
      List<LookupRow<String>> result = new ArrayList<>();
      result.add(new LookupRow<>("a", "A"));
      result.add(new LookupRow<>("b", "B"));
      result.add(new LookupRow<>("c", "C"));
      result.add(new LookupRow<>("d", "D"));
      result.add(new LookupRow<>("e", "E"));
      result.add(new LookupRow<>("f", "F"));
      result.add(new LookupRow<>("g", "G"));
      return result;
    }
  }
}
