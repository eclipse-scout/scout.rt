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
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.ITypeWithClassId;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormFieldTest.TestForm2.MainBox.SimpleGroupBox2;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormFieldTest.TestFormWithClassId.MainBox.TestFieldDuplicateClassId1;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormFieldTest.TestFormWithClassId.MainBox.TestFieldDuplicateClassId2;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormFieldTest.TestFormWithClassId.MainBox.TestFieldWithoutClassId;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormFieldTest.TestFormWithGroupBoxes.MainBox.GroupBox1;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormFieldTest.TestFormWithGroupBoxes.MainBox.GroupBox2;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fixture.AbstractTemplateUsingOtherTemplateGroupBox;
import org.eclipse.scout.rt.client.ui.form.fixture.AbstractTestGroupBox;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link AbstractFormField}
 * 
 * @since 3.10.0
 */
public class AbstractFormFieldTest {
  private static final String DUPLICATE_CLASS_ID = "DUPLICATE";
  private static final String TEST_CLASS_ID = "TEST_CLASS_ID";

  /**
   * Tests that {@link AbstractFormField#classId()} returns the annotation value for {@link ClassId}.
   */
  @Test
  public void testClassIdAnnotated() {
    TestClassWithClassId testClassWithClassId = new TestClassWithClassId();
    String classId = testClassWithClassId.classId();
    assertEquals("classId should match annotated id", TEST_CLASS_ID, classId);
  }

  /**
   * Tests that {@link AbstractFormField#classId()} returns the annotation value for {@link ClassId}.
   * 
   * @throws ProcessingException
   */
  @Test
  public void testClassIdAnnotatedInForm() throws ProcessingException {
    TestForm2 testForm = new TestForm2();
    SimpleGroupBox2 testField = testForm.getFieldByClass(TestForm2.MainBox.SimpleGroupBox2.class);
    String classId = testField.classId();
    assertEquals("classId should match annotated id", TEST_CLASS_ID, classId);
  }

  @Test
  public void testClassIdGenerated() {
    SimpleTestFormField testField = new SimpleTestFormField();
    String classId = testField.classId();
    assertEquals("class id: no hierarchy", testField.getClass().getSimpleName(), classId);
  }

  @Test
  public void testClassIdWithFormClassId() throws ProcessingException {
    TestFormWithClassId testForm = new TestFormWithClassId();
    TestFieldWithoutClassId field = testForm.getFieldByClass(TestFieldWithoutClassId.class);
    assertEquals("class id not as expected", field.getClass().getSimpleName() + ITypeWithClassId.ID_CONCAT_SYMBOL + TEST_CLASS_ID, field.classId());
  }

  @Test
  public void testDuplicateClassId() throws ProcessingException {
    TestFormWithClassId testForm = new TestFormWithClassId();
    TestFieldDuplicateClassId1 field1 = testForm.getFieldByClass(TestFieldDuplicateClassId1.class);
    TestFieldDuplicateClassId2 field2 = testForm.getFieldByClass(TestFieldDuplicateClassId2.class);
    assertEquals("class id not as expected", DUPLICATE_CLASS_ID + ITypeWithClassId.ID_CONCAT_SYMBOL + "1", field1.classId());
    assertEquals("class id not as expected", DUPLICATE_CLASS_ID + ITypeWithClassId.ID_CONCAT_SYMBOL + "2", field2.classId());
  }

  /**
   * Tests the generated {@link AbstractFormField#classId()} for injected sibling fields.
   * 
   * @throws ProcessingException
   */
  @Test
  public void testClassIdInjectedFields() throws ProcessingException {
    verifyNoDuplicateClassIds(new TestForm().getAllFields());
  }

  /**
   * Tests the generated {@link AbstractFormField#classId()} for injected sibling fields.
   * 
   * @throws ProcessingException
   */
  @Test
  public void testClassIdTemplateGroupBox() throws ProcessingException {
    TestFormWithGroupBoxes form = new TestFormWithGroupBoxes();
    verifyNoDuplicateClassIds(form.getAllFields());
    String completeClassId = form.getGroupBox1().getUsingOtherTemplateBox().getText1Field().classId();
    String fieldClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(form.getGroupBox1().getUsingOtherTemplateBox().getText1Field().getClass());
    String parentGroupboxClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(form.getGroupBox1().getUsingOtherTemplateBox().getClass(), true);
    String parentParentGroupboxClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(form.getGroupBox1().getClass(), true);
    String expectedClassId = fieldClassId + ITypeWithClassId.ID_CONCAT_SYMBOL + parentGroupboxClassId + ITypeWithClassId.ID_CONCAT_SYMBOL + parentParentGroupboxClassId;
    Assert.assertEquals(expectedClassId, completeClassId);
  }

  private void verifyNoDuplicateClassIds(IFormField[] formFields) {
    Set<String> ids = new HashSet<String>();
    for (IFormField f : formFields) {
      String classId = f.classId();
      assertFalse("Duplicate classid" + classId, ids.contains(classId));
      ids.add(classId);
    }
  }

  //test classes
  @ClassId(TEST_CLASS_ID)
  class TestClassWithClassId extends AbstractFormField {
  }

  @ClassId(TEST_CLASS_ID)
  class TestFormWithClassId extends AbstractForm {

    public TestFormWithClassId() throws ProcessingException {
      super();
    }

    public class MainBox extends AbstractGroupBox {
      @Order(10.0)
      public class TestFieldWithoutClassId extends AbstractFormField {
      }

      @Order(20.0)
      @ClassId(DUPLICATE_CLASS_ID)
      public class TestFieldDuplicateClassId1 extends AbstractFormField {
      }

      @Order(30.0)
      @ClassId(DUPLICATE_CLASS_ID)
      public class TestFieldDuplicateClassId2 extends AbstractFormField {
      }
    }
  }

  class SimpleTestFormField extends AbstractFormField {
  }

  /**
   * Form with two injected fields
   */
  class TestForm extends AbstractForm {

    public TestForm() throws ProcessingException {
      super();
    }

    public class SimpleGroupBox extends AbstractGroupBox {

      @Override
      protected void injectFieldsInternal(List<IFormField> fieldList) {
        SimpleTestFormField s1 = new SimpleTestFormField();
        SimpleTestFormField s2 = new SimpleTestFormField();
        fieldList.add(s1);
        fieldList.add(s2);
        super.injectFieldsInternal(fieldList);
      }
    }
  }

  /**
   * Form with two group boxes with inner fields
   */
  class TestFormWithGroupBoxes extends AbstractForm {

    public TestFormWithGroupBoxes() throws ProcessingException {
      super();
    }

    public GroupBox1 getGroupBox1() {
      return getFieldByClass(GroupBox1.class);
    }

    public GroupBox2 getGroupBox2() {
      return getFieldByClass(GroupBox2.class);
    }

    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class GroupBox1 extends AbstractTemplateUsingOtherTemplateGroupBox {
      }

      @Order(20.0)
      public class GroupBox2 extends AbstractTemplateUsingOtherTemplateGroupBox {
      }

    }

  }

  class TestForm2 extends AbstractForm {
    public TestForm2() throws ProcessingException {
      super();
    }

    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      @ClassId(TEST_CLASS_ID)
      public class SimpleGroupBox2 extends AbstractTestGroupBox {
      }
    }

  }

}
