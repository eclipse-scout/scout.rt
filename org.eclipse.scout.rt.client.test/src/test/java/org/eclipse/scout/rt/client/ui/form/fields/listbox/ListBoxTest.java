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

import java.util.HashSet;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
import org.eclipse.scout.testing.client.form.DynamicStringField;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractListBox}
 */
@RunWith(PlatformTestRunner.class)
public class ListBoxTest extends AbstractListBox<Long> {
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

}
