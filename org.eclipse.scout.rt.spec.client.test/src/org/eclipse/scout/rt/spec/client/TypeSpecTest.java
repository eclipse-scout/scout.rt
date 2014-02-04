/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.spec.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link AbstractTypeSpecTest}, {@link FieldTypesSpecTest}, {@link ColumnTypesSpecTest}
 */
public class TypeSpecTest {

  private FieldTypesSpecTest m_fieldTypesSpecTest;
  private ColumnTypesSpecTest m_columnTypesSpecTest;

  @Before
  public void setup() {
    m_fieldTypesSpecTest = new FieldTypesSpecTest();
    m_columnTypesSpecTest = new ColumnTypesSpecTest();
  }

  @Test
  public void testAcceptClassFieldTypeSpec() {
    assertTrue("AbstractStringField should be accepted, since it is a field and doc text is available.", m_fieldTypesSpecTest.acceptClass(AbstractStringField.class));
    assertFalse("AbstractBooleanColumn should not be accepted, since it is not a field.", m_fieldTypesSpecTest.acceptClass(AbstractBooleanColumn.class));
    assertFalse("TestStringField should not be accepted, since doc text is not available.", m_fieldTypesSpecTest.acceptClass(TestStringField.class));
  }

  @Test
  public void testAcceptClassColumnTypeSpec() {
    assertFalse("AbstractStringField should not be accepted, since it is not a column.", m_columnTypesSpecTest.acceptClass(AbstractStringField.class));
    assertTrue("AbstractBooleanColumn should be accepted, since it is a column and doc text is available.", m_columnTypesSpecTest.acceptClass(AbstractBooleanColumn.class));
    assertFalse("TestStringField should not be accepted, since it's neither a column and doc text is not available.", m_columnTypesSpecTest.acceptClass(TestStringField.class));
    assertFalse("TestStringColumn should not be accepted, since doc text is not available.", m_columnTypesSpecTest.acceptClass(TestStringColumn.class));
  }

  @Test
  public void testGetAllClassesColumnTypeSpec() throws ProcessingException {
    assertTrue("Expected AbstractBooleanColumn to be found.", Arrays.asList(m_columnTypesSpecTest.getAllClasses()).contains(AbstractBooleanColumn.class));
  }

  @Test
  public void testGetAllClassesFieldsTypeSpec() throws ProcessingException {
    ArrayList<Class> expectedClasses = new ArrayList<Class>();
    expectedClasses.add(AbstractStringField.class);
    expectedClasses.add(AbstractDateField.class);
    assertTrue("Expected AbstractStringField to be found.", Arrays.asList(m_fieldTypesSpecTest.getAllClasses()).containsAll(expectedClasses));
  }

  private class TestStringField extends AbstractStringField {
  }

  private class TestStringColumn extends AbstractColumn {
  }

}
