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
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldTest.TestForm2.MainBox.SimpleGroupBox2;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldTest.TestFormWithClassId.MainBox.TestFieldDuplicateClassId1;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldTest.TestFormWithClassId.MainBox.TestFieldDuplicateClassId2;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldTest.TestFormWithClassId.MainBox.TestFieldWithoutClassId;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldTest.TestFormWithGroupBoxes.MainBox.GroupBox1;
import org.eclipse.scout.rt.client.ui.form.fields.FormFieldTest.TestFormWithGroupBoxes.MainBox.GroupBox2;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.InvalidSequenceStatus;
import org.eclipse.scout.rt.client.ui.form.fixture.AbstractTemplateUsingOtherTemplateGroupBox;
import org.eclipse.scout.rt.client.ui.form.fixture.AbstractTestGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.MultiStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit tests for {@link AbstractFormField}
 *
 * @since 3.10.0
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormFieldTest {
  private static final String DUPLICATE_CLASS_ID = "DUPLICATE";
  private static final String TEST_FIELD_CLASS_ID = "TEST_FIELD_CLASS_ID";
  private static final String TEST_FORM_ID = "TEST_FORM_CLASS_ID";
  private static final String TEST_GROUP_BOX_ID = "TEST_GROUP_BOX_CLASS_ID";

  /**
   * Tests that {@link AbstractFormField#classId()} returns the annotation value for {@link ClassId}.
   */
  @Test
  public void testClassIdAnnotated() {
    TestClassWithClassId testClassWithClassId = new TestClassWithClassId();
    String classId = testClassWithClassId.classId();
    assertEquals("classId should match annotated id", TEST_FIELD_CLASS_ID, classId);
  }

  /**
   * Tests that {@link AbstractFormField#classId()} returns the annotation value for {@link ClassId}.
   */
  @Test
  public void testClassIdAnnotatedInForm() {
    TestForm2 testForm = new TestForm2();
    SimpleGroupBox2 testField = testForm.getFieldByClass(TestForm2.MainBox.SimpleGroupBox2.class);
    String classId = testField.classId();
    assertEquals("classId should match annotated id", TEST_GROUP_BOX_ID, classId);
  }

  @Test
  public void testClassIdGenerated() {
    SimpleTestFormField testField = new SimpleTestFormField();
    String classId = testField.classId();
    assertEquals("class id: no hierarchy", testField.getClass().getSimpleName(), classId);
  }

  @Test
  public void testClassIdWithFormClassId() {
    TestFormWithClassId testForm = new TestFormWithClassId();
    TestFieldWithoutClassId field = testForm.getFieldByClass(TestFormWithClassId.MainBox.TestFieldWithoutClassId.class);
    assertEquals("class id not as expected", field.getClass().getSimpleName() + ITypeWithClassId.ID_CONCAT_SYMBOL + TEST_FORM_ID, field.classId());
  }

  @Test
  public void testDuplicateClassId() {
    TestFormWithClassId testForm = new TestFormWithClassId();
    TestFieldDuplicateClassId1 field1 = testForm.getFieldByClass(TestFieldDuplicateClassId1.class);
    TestFieldDuplicateClassId2 field2 = testForm.getFieldByClass(TestFieldDuplicateClassId2.class);
    assertEquals("class id not as expected", DUPLICATE_CLASS_ID + DUPLICATE_CLASS_ID + ITypeWithClassId.ID_CONCAT_SYMBOL + "1", field1.classId());
    assertEquals("class id not as expected", DUPLICATE_CLASS_ID + DUPLICATE_CLASS_ID + ITypeWithClassId.ID_CONCAT_SYMBOL + "2", field2.classId());
  }

  /**
   * Tests the generated {@link AbstractFormField#classId()} for injected sibling fields.
   */
  @Test
  public void testClassIdInjectedFields() {
    verifyNoDuplicateClassIds(new TestForm().getAllFields());
  }

  /**
   * Tests the generated {@link AbstractFormField#classId()} for injected sibling fields.
   */
  @Test
  public void testClassIdTemplateGroupBox() {
    TestFormWithGroupBoxes form = new TestFormWithGroupBoxes();
    verifyNoDuplicateClassIds(form.getAllFields());
    String completeClassId = form.getGroupBox1().getUsingOtherTemplateBox().getText1Field().classId();
    String fieldClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(form.getGroupBox1().getUsingOtherTemplateBox().getText1Field().getClass());
    String parentGroupboxClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(form.getGroupBox1().getUsingOtherTemplateBox().getClass(), true);
    String parentParentGroupboxClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(form.getGroupBox1().getClass(), true);
    String expectedClassId = fieldClassId + ITypeWithClassId.ID_CONCAT_SYMBOL + parentGroupboxClassId + ITypeWithClassId.ID_CONCAT_SYMBOL + parentParentGroupboxClassId;
    Assert.assertEquals(expectedClassId, completeClassId);
  }

  private void verifyNoDuplicateClassIds(List<IFormField> formFields) {
    Set<String> ids = new HashSet<String>();
    for (IFormField f : formFields) {
      String classId = f.classId();
      assertFalse("Duplicate classid" + classId, ids.contains(classId));
      ids.add(classId);
    }
  }

  //test classes
  @ClassId(TEST_FIELD_CLASS_ID)
  class TestClassWithClassId extends AbstractFormField {
  }

  @ClassId(TEST_FORM_ID)
  class TestFormWithClassId extends AbstractForm {

    public TestFormWithClassId() {
      super();
    }

    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class TestFieldWithoutClassId extends AbstractFormField {
      }

      @Order(20)
      @ClassId(DUPLICATE_CLASS_ID + DUPLICATE_CLASS_ID)
      // must be a calculation, otherwise the SCOUT SDK reports a compile error
      public class TestFieldDuplicateClassId1 extends AbstractFormField {
      }

      @Order(30)
      @ClassId(DUPLICATE_CLASS_ID + DUPLICATE_CLASS_ID)
      public class TestFieldDuplicateClassId2 extends AbstractFormField {
      }
    }
  }

  class SimpleTestFormField extends AbstractFormField {
  }

  class MandatoryTestFormField extends AbstractFormField {
    protected boolean iAmEmpty = false;

    @Override
    protected boolean execIsEmpty() {
      return iAmEmpty;
    }
  }

  /**
   * Form with two injected fields
   */
  class TestForm extends AbstractForm {

    public TestForm() {
      super();
    }

    public class SimpleGroupBox extends AbstractGroupBox {

      @Override
      protected void injectFieldsInternal(OrderedCollection<IFormField> fields) {
        SimpleTestFormField s1 = new SimpleTestFormField();
        SimpleTestFormField s2 = new SimpleTestFormField();
        fields.addLast(s1);
        fields.addLast(s2);
        super.injectFieldsInternal(fields);
      }
    }
  }

  /**
   * Form with two group boxes with inner fields
   */
  class TestFormWithGroupBoxes extends AbstractForm {

    public TestFormWithGroupBoxes() {
      super();
    }

    public GroupBox1 getGroupBox1() {
      return getFieldByClass(GroupBox1.class);
    }

    public GroupBox2 getGroupBox2() {
      return getFieldByClass(GroupBox2.class);
    }

    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class GroupBox1 extends AbstractTemplateUsingOtherTemplateGroupBox {
      }

      @Order(20)
      public class GroupBox2 extends AbstractTemplateUsingOtherTemplateGroupBox {
      }

    }

  }

  class TestForm2 extends AbstractForm {
    public TestForm2() {
      super();
    }

    public class MainBox extends AbstractGroupBox {

      @Order(10)
      @ClassId(TEST_GROUP_BOX_ID)
      public class SimpleGroupBox2 extends AbstractTestGroupBox {
      }
    }

  }

  @Test
  public void testStatusVisible_Default() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    assertTrue(testField.isStatusVisible());
  }

  /**
   * Tests if the property change listener is triggered when setStatusVisible() is called and if the property value is
   * changed as expected.
   */
  @Test
  public void testStatusVisible_setStatusVisible() throws Exception {
    final boolean[] called = {false};
    SimpleTestFormField testField = new SimpleTestFormField();
    testField.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("statusVisible".equals(evt.getPropertyName()) && evt.getNewValue().equals(Boolean.FALSE)) {
          called[0] = true;
        }
      }
    });
    testField.setStatusVisible(false);
    assertFalse(testField.isStatusVisible());
    assertTrue(called[0]);
  }

  @Test
  public void testGetErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    final MultiStatus ms = new MultiStatus();
    ms.add(new Status("error"));
    testField.setErrorStatus(ms);

    assertEquals(ms, testField.getErrorStatus());
    assertNotSame(ms, testField.getErrorStatus());
    assertNotSame(testField.getErrorStatus(), testField.getErrorStatus()); // get always returns a new object
  }

  @Test
  public void testSetSameErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    P_PropertyChangeEventCounter counter = new P_PropertyChangeEventCounter();
    testField.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, counter);

    final MultiStatus ms = new MultiStatus();
    ms.add(new Status("error"));
    testField.setErrorStatus(ms);
    assertEquals(ms, testField.getErrorStatus());
    assertNotSame(ms, testField.getErrorStatus());

    final MultiStatus ms2 = new MultiStatus();
    ms2.add(new Status("error")); // new object, but same content
    testField.setErrorStatus(ms2);
    assertEquals(ms2, testField.getErrorStatus());
    assertNotSame(ms2, testField.getErrorStatus());

    assertEquals(1, counter.getCount());
  }

  @Test
  public void testSetDifferentErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    P_PropertyChangeEventCounter counter = new P_PropertyChangeEventCounter();
    testField.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, counter);

    final MultiStatus ms = new MultiStatus();
    ms.add(new Status("error"));
    testField.setErrorStatus(ms);
    assertEquals(ms, testField.getErrorStatus());
    assertNotSame(ms, testField.getErrorStatus());

    final MultiStatus ms2 = new MultiStatus();
    ms2.add(new Status("another message")); // another object, should trigger its own event
    testField.setErrorStatus(ms2);
    assertEquals(ms2, testField.getErrorStatus());
    assertNotSame(ms2, testField.getErrorStatus());

    assertEquals(2, counter.getCount());
  }

  @Test
  public void testAddSameErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    P_PropertyChangeEventCounter counter = new P_PropertyChangeEventCounter();
    testField.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, counter);
    IMultiStatus status0 = testField.getErrorStatus();

    testField.addErrorStatus(new Status("error"));
    assertTrue(testField.getErrorStatus().containsStatus(Status.class));

    IMultiStatus status1 = testField.getErrorStatus();

    testField.addErrorStatus(new Status("error")); // no event, because same status already exists
    assertTrue(testField.getErrorStatus().containsStatus(Status.class));

    IMultiStatus status2 = testField.getErrorStatus();

    assertNotEquals(status0, status1);
    assertEquals(status1, status2);
    assertEquals(1, counter.getCount());
  }

  @Test
  public void testAddDifferentErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    P_PropertyChangeEventCounter counter = new P_PropertyChangeEventCounter();
    testField.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, counter);
    IMultiStatus status0 = testField.getErrorStatus();

    testField.addErrorStatus(new Status("error"));
    assertTrue(testField.getErrorStatus().containsStatus(Status.class));

    IMultiStatus status1 = testField.getErrorStatus();

    testField.addErrorStatus(new Status("another message"));
    assertTrue(testField.getErrorStatus().containsStatus(Status.class));

    IMultiStatus status2 = testField.getErrorStatus();

    assertNotEquals(status0, status1);
    assertNotEquals(status1, status2);
    assertEquals(2, counter.getCount());
  }

  @Test
  public void testAddRemoveAddErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    P_PropertyChangeEventCounter counter = new P_PropertyChangeEventCounter();
    testField.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, counter);
    IMultiStatus status0 = testField.getErrorStatus();

    testField.addErrorStatus(new Status("error"));
    assertTrue(testField.getErrorStatus().containsStatus(Status.class));

    IMultiStatus status1 = testField.getErrorStatus();

    testField.removeErrorStatus(Status.class);
    assertNull(testField.getErrorStatus());

    IMultiStatus status2 = testField.getErrorStatus();

    testField.addErrorStatus(new Status("error"));
    assertTrue(testField.getErrorStatus().containsStatus(Status.class));

    IMultiStatus status3 = testField.getErrorStatus();

    assertNotEquals(status0, status1);
    assertNotEquals(status1, status2);
    assertNotEquals(status2, status3);
    assertEquals(3, counter.getCount()); // adding + removing + adding
  }

  @Test
  public void testAddMultipleRemoveOneAddErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    P_PropertyChangeEventCounter counter = new P_PropertyChangeEventCounter();
    testField.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, counter);
    IMultiStatus status0 = testField.getErrorStatus();

    testField.addErrorStatus(new InvalidSequenceStatus("error"));
    assertTrue(testField.getErrorStatus().containsStatus(InvalidSequenceStatus.class));
    assertFalse(testField.getErrorStatus().containsStatus(ParsingFailedStatus.class));

    IMultiStatus status1 = testField.getErrorStatus();

    testField.addErrorStatus(new ParsingFailedStatus("error", "input"));
    assertTrue(testField.getErrorStatus().containsStatus(InvalidSequenceStatus.class));
    assertTrue(testField.getErrorStatus().containsStatus(ParsingFailedStatus.class));

    IMultiStatus status2 = testField.getErrorStatus();

    testField.removeErrorStatus(ParsingFailedStatus.class);
    assertTrue(testField.getErrorStatus().containsStatus(InvalidSequenceStatus.class));
    assertFalse(testField.getErrorStatus().containsStatus(ParsingFailedStatus.class));

    IMultiStatus status3 = testField.getErrorStatus();

    testField.addErrorStatus(new ParsingFailedStatus("error", "input"));
    assertTrue(testField.getErrorStatus().containsStatus(InvalidSequenceStatus.class));
    assertTrue(testField.getErrorStatus().containsStatus(ParsingFailedStatus.class));

    IMultiStatus status4 = testField.getErrorStatus();

    assertNotEquals(status0, status1);
    assertNotEquals(status1, status2);
    assertNotEquals(status2, status3);
    assertNotEquals(status3, status4);
    assertEquals(4, counter.getCount()); // add + add + remove + add
  }

  @Test
  public void testRemoveErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    P_PropertyChangeEventCounter counter = new P_PropertyChangeEventCounter();
    testField.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, counter);
    IMultiStatus status0 = testField.getErrorStatus();

    testField.addErrorStatus(new Status("error"));
    assertTrue(testField.getErrorStatus().containsStatus(Status.class));

    IMultiStatus status1 = testField.getErrorStatus();

    testField.removeErrorStatus(Status.class);
    assertNull(testField.getErrorStatus());

    IMultiStatus status2 = testField.getErrorStatus();

    assertNotEquals(status0, status1);
    assertNotEquals(status1, status2);
    assertEquals(2, counter.getCount());
  }

  @Test
  public void testRemoveWithRemainingErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    P_PropertyChangeEventCounter counter = new P_PropertyChangeEventCounter();
    testField.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, counter);

    IMultiStatus status0 = testField.getErrorStatus();

    testField.addErrorStatus(new InvalidSequenceStatus("error"));
    assertTrue(testField.getErrorStatus().containsStatus(InvalidSequenceStatus.class));
    assertFalse(testField.getErrorStatus().containsStatus(ParsingFailedStatus.class));

    IMultiStatus status1 = testField.getErrorStatus();

    testField.addErrorStatus(new ParsingFailedStatus("error", "input"));
    assertTrue(testField.getErrorStatus().containsStatus(InvalidSequenceStatus.class));
    assertTrue(testField.getErrorStatus().containsStatus(ParsingFailedStatus.class));

    IMultiStatus status2 = testField.getErrorStatus();

    testField.removeErrorStatus(ParsingFailedStatus.class);
    assertTrue(testField.getErrorStatus().containsStatus(InvalidSequenceStatus.class));
    assertFalse(testField.getErrorStatus().containsStatus(ParsingFailedStatus.class));

    IMultiStatus status3 = testField.getErrorStatus();

    assertNotEquals(status0, status1);
    assertNotEquals(status1, status2);
    assertNotEquals(status2, status3);
    assertEquals(3, counter.getCount()); // add + add + remove
  }

  @Test
  public void testRemoveNonExistingErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    P_PropertyChangeEventCounter counter = new P_PropertyChangeEventCounter();
    testField.addPropertyChangeListener(IFormField.PROP_ERROR_STATUS, counter);

    IMultiStatus status0 = testField.getErrorStatus();

    testField.addErrorStatus(new InvalidSequenceStatus("error"));
    assertTrue(testField.getErrorStatus().containsStatus(InvalidSequenceStatus.class));
    assertFalse(testField.getErrorStatus().containsStatus(ParsingFailedStatus.class));

    IMultiStatus status1 = testField.getErrorStatus();

    testField.removeErrorStatus(ParsingFailedStatus.class); // should not trigger an event
    assertTrue(testField.getErrorStatus().containsStatus(InvalidSequenceStatus.class));
    assertFalse(testField.getErrorStatus().containsStatus(ParsingFailedStatus.class));

    IMultiStatus status2 = testField.getErrorStatus();

    assertNotEquals(status0, status1);
    assertEquals(status1, status2);
    assertEquals(1, counter.getCount());
  }

  /**
   * A mandatory field is invalid, if it is empty
   */
  @Test
  public void testMandatoryFieldInvalid() {
    MandatoryTestFormField v = new MandatoryTestFormField();
    v.setMandatory(true);
    v.iAmEmpty = true;
    v.checkEmpty();
    assertFalse(v.isContentValid());
  }

  @Test
  public void testMandatoryFieldValid() {
    MandatoryTestFormField v = new MandatoryTestFormField();
    v.setMandatory(true);
    v.iAmEmpty = false;
    v.checkEmpty();
    assertTrue(v.isContentValid());
  }

  @Test
  public void testAddErrorStatusString() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    testField.addErrorStatus("error");
    assertTrue(testField.getErrorStatus().containsStatus(DefaultFieldStatus.class));
  }

  @Test
  public void testClearErrorStatus() throws Exception {
    SimpleTestFormField testField = new SimpleTestFormField();
    testField.addErrorStatus(new Status("error"));
    testField.clearErrorStatus();
    assertNull(testField.getErrorStatus());
  }

  private class P_PropertyChangeEventCounter implements PropertyChangeListener {

    private int m_count;

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      m_count++;
    }

    public int getCount() {
      return m_count;
    }
  }
}
