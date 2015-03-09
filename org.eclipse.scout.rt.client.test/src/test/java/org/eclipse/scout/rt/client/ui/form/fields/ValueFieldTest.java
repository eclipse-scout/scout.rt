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
package org.eclipse.scout.rt.client.ui.form.fields;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.ValueFieldTest.ValidateTestForm.MainBox.AField;
import org.eclipse.scout.rt.client.ui.form.fields.ValueFieldTest.ValidateTestForm.MainBox.BField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractValueField}.
 */
@RunWith(PlatformTestRunner.class)
public class ValueFieldTest {
  private static final String PARSE_ERROR_MESSAGE = "Parse Error Message";
  private static final String UNPARSABLE_VALUE = "unparsable";
  private static final String INVALID_VALUE = "invalid";
  private static final String EXCEPTION_VALUE = "EXCEPTION!!!";

  @Test
  public void testNoInitialError() {
    IValueField<String> v = new ParseErrorField();
    assertTrue(v.isContentValid());
  }

  @Test
  public void testParseError() {
    IValueField<String> v = new ParseErrorField();
    v.parseValue(UNPARSABLE_VALUE);
    assertEquals(PARSE_ERROR_MESSAGE, v.getErrorStatus().getMessage());
    assertEquals(IStatus.ERROR, v.getErrorStatus().getSeverity());
    assertFalse(v.isContentValid());
  }

  /**
   * A mandatory field is invalid, if it is empty
   */
  @Test
  public void testMandatoryFieldInvalid() {
    ParseErrorField v = new ParseErrorField();
    v.setMandatory(true);
    assertFalse(v.isContentValid());
  }

  @Test
  public void testMandatoryFieldValid() {
    ParseErrorField v = new ParseErrorField();
    v.setMandatory(true);
    v.setValue("valid");
    assertTrue(v.isContentValid());
  }

  @Test
  public void testResetParse() throws Exception {
    IValueField<String> v = new ParseErrorField();
    v.parseValue(UNPARSABLE_VALUE);
    v.parseValue("valid");
    assertTrue(v.isContentValid());
  }

  @Test
  public void testValidateError() {
    IValueField<String> v = new ParseErrorField();
    v.setValue(INVALID_VALUE);
    assertEquals(IStatus.ERROR, v.getErrorStatus().getSeverity());
    assertEquals(INVALID_VALUE, v.getErrorStatus().getMessage());
  }

  @Test
  public void testValidateException() {
    IValueField<String> v = new ParseErrorField();
    v.setValue(EXCEPTION_VALUE);
    assertEquals(IStatus.ERROR, v.getErrorStatus().getSeverity());
    assertTrue(v.getErrorStatus().getMessage().contains(EXCEPTION_VALUE));
  }

  @Test
  public void testParseValidateError() {
    IValueField<String> v = new ParseErrorField();
    v.parseValue(UNPARSABLE_VALUE);
    v.setValue(INVALID_VALUE);
    assertEquals(INVALID_VALUE, v.getErrorStatus().getMessage());
    assertEquals(IStatus.ERROR, v.getErrorStatus().getSeverity());
    assertFalse(v.isContentValid());
  }

  @Test
  public void testMultipleValidFields() throws ProcessingException {
    final ValidateTestForm f = new ValidateTestForm();
    f.setValidABCombination();
    assertTrue(f.getBField().isContentValid());
    assertTrue(f.getAField().isContentValid());
  }

  @Test
  public void testMultipleInvalidFields() throws ProcessingException {
    final ValidateTestForm f = new ValidateTestForm();
    f.setInvalidABCombination();
    assertInvalid(f.getBField(), ValidateTestForm.AB_ERROR);
    assertTrue(f.getAField().isContentValid());
  }

  @Test
  public void testValidationError() throws ProcessingException {
    final ValidateTestForm f = new ValidateTestForm();
    f.getBField().setValue(20);
    assertInvalid(f.getBField(), ValidateTestForm.B_MAX_ERROR);
    assertTrue(f.getAField().isContentValid());
  }

  @Test
  public void testValidationErrorManyTimes() throws ProcessingException {
    final ValidateTestForm f = new ValidateTestForm();
    f.setInvalidABCombination();
    f.setValidABCombination();
    f.getBField().setValue(20);
    f.getBField().setValue(30);
    assertInvalid(f.getBField(), ValidateTestForm.B_MAX_ERROR);
    assertTrue(f.getAField().isContentValid());
  }

  @Test
  public void testResetValidationError() throws ProcessingException {
    final ValidateTestForm f = new ValidateTestForm();
    f.getBField().setValue(20);
    f.setInvalidABCombination();
    f.getAField().setValue(null);
    f.getBField().setValue(null);
    assertTrue(f.getBField().isContentValid());
    assertTrue(f.getAField().isContentValid());
  }

  @Test
  public void testMultipleWithValidate() throws ProcessingException {
    final ValidateTestForm f = new ValidateTestForm();
    f.setInvalidABCombination(); // A=1,B=0: Message: A > B
    assertInvalid(f.getBField(), ValidateTestForm.AB_ERROR);
    assertTrue(f.getAField().isContentValid());
    f.getBField().setValue(20); // A=1,B=0, Message: B < 10, A > B
    assertInvalid(f.getBField(), ValidateTestForm.B_MAX_ERROR);
    assertEquals(2, f.getBField().getErrorStatus().getChildren().size());
    assertTrue(f.getAField().isContentValid());
    f.setInvalidABCombination(); // A=1,B=0, A > B
    assertInvalid(f.getBField(), ValidateTestForm.AB_ERROR);
    f.setValidABCombination();
    assertTrue(f.getBField().isContentValid());
    assertTrue(f.getAField().isContentValid());
  }

  private void assertInvalid(IFormField field, String expectedMessage) {
    assertFalse(field.isContentValid());
    assertEquals(expectedMessage, field.getErrorStatus().getMessage());
  }

  static class ParseErrorField extends AbstractValueField<String> {
    @Override
    protected String execParseValue(String text) throws ProcessingException {
      if (UNPARSABLE_VALUE.equals(text)) {
        throw new ProcessingException(PARSE_ERROR_MESSAGE);
      }
      return text;
    }

    @Override
    protected String execValidateValue(String text) throws ProcessingException {
      if (INVALID_VALUE.equals(text)) {
        throw new ProcessingException(INVALID_VALUE);
      }
      else if (EXCEPTION_VALUE.equals(text)) {
        throw new RuntimeException(EXCEPTION_VALUE);
      }
      return text;
    }
  }

  /**
   * Form with A < B, B < 10
   */
  static class ValidateTestForm extends AbstractForm {

    public static final String AB_ERROR = "Error B";
    public static final String B_MAX_ERROR = ">10";

    public ValidateTestForm() throws ProcessingException {
      super();
    }

    public AField getAField() {
      return getFieldByClass(AField.class);
    }

    public BField getBField() {
      return getFieldByClass(BField.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class AField extends AbstractValueField<Integer> {

        @Override
        protected void execChangedValue() throws ProcessingException {
          if (!isABValid()) {
            addErrorStatus("Error A");
          }
          else {
            clearABErrorStatus();
          }
        }
      }

      @Order(20.0)
      public class BField extends AbstractValueField<Integer> {

        @Override
        protected Integer execValidateValue(Integer rawValue) throws ProcessingException {
          if (rawValue != null && rawValue > 10) {
            throw new VetoException(B_MAX_ERROR);
          }
          return super.execValidateValue(rawValue);
        }

        @Override
        protected void execChangedValue() throws ProcessingException {
          if (!isABValid()) {
            addErrorStatus(AB_ERROR);
          }
          else {
            clearABErrorStatus();
          }
        }
      }

      private void clearABErrorStatus() {
        getAField().clearErrorStatus();
        getBField().clearErrorStatus();
      }

      private boolean isABValid() {
        final Integer a = getAField().getValue();
        final Integer b = getBField().getValue();
        return a == null || b == null || a < b;
      }

    }

    public void setInvalidABCombination() {
      getAField().setValue(1);
      getBField().setValue(0);
    }

    public void setValidABCombination() {
      getAField().setValue(0);
      getBField().setValue(1);
    }
  }

}
