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
package org.eclipse.scout.rt.shared.data.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.shared.data.form.properties.AbstractPropertyData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.form.DynamicCancelButton;
import org.eclipse.scout.testing.client.form.DynamicForm;
import org.eclipse.scout.testing.client.form.DynamicGroupBox;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the import behavior of properties form a form data into its form. Untouched form data properties are not
 * expected to be imported into the form.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormDataImportWithPropertiesTest {

  private static final boolean EXPECTED_BOOLEAN = true;
  private static final int EXPECTED_INTEGER = 42;
  private static final String EXPECTED_TEXT = "a test text";

  @Test
  public void testImportFormData_untouchedProperties() throws Exception {
    DynamicGroupBox mainBox = new DynamicGroupBox(new DynamicCancelButton());
    final DynamicFormWithProperties f = new DynamicFormWithProperties("Form1", mainBox);
    try {
      f.start(new FormHandler());

      // set initial values
      f.setPrimitiveBoolean(EXPECTED_BOOLEAN);
      f.setPrimitiveInteger(EXPECTED_INTEGER);
      f.setText(EXPECTED_TEXT);

      // import untouched form data
      f.importFormData(new DynamicFormDataWithProperties());

      // all form properties are expected unchanged
      assertEquals(EXPECTED_BOOLEAN, f.isPrimitiveBoolean());
      assertEquals(EXPECTED_INTEGER, f.getPrimitiveInteger());
      assertEquals(EXPECTED_TEXT, f.getText());
    }
    finally {
      f.doClose();
    }
  }

  @Test
  public void testImportFormData_modifiedProperties() throws Exception {
    DynamicGroupBox mainBox = new DynamicGroupBox(new DynamicCancelButton());
    final DynamicFormWithProperties f = new DynamicFormWithProperties("Form1", mainBox);
    try {
      f.start(new FormHandler());

      // set initial values
      f.setPrimitiveBoolean(EXPECTED_BOOLEAN);
      f.setPrimitiveInteger(EXPECTED_INTEGER);
      f.setText(EXPECTED_TEXT);

      // import modified form data
      DynamicFormDataWithProperties formData = new DynamicFormDataWithProperties();
      formData.setPrimitiveBoolean(false);
      formData.setPrimitiveInteger(102);
      formData.setText(null);
      f.importFormData(formData);

      // check properties on form
      assertFalse(f.isPrimitiveBoolean());
      assertEquals(102, f.getPrimitiveInteger());
      assertNull(f.getText());
    }
    finally {
      f.doClose();
    }
  }

  public static final class DynamicFormWithProperties extends DynamicForm {
    private boolean m_primitiveBoolean;
    private int m_primitiveInteger;
    private String m_text;

    private DynamicFormWithProperties(String title, IGroupBox mainBox) {
      super(title, mainBox);
    }

    @FormData
    public boolean isPrimitiveBoolean() {
      return m_primitiveBoolean;
    }

    @FormData
    public void setPrimitiveBoolean(boolean primitiveBoolean) {
      m_primitiveBoolean = primitiveBoolean;
    }

    @FormData
    public int getPrimitiveInteger() {
      return m_primitiveInteger;
    }

    @FormData
    public void setPrimitiveInteger(int primitiveInteger) {
      m_primitiveInteger = primitiveInteger;
    }

    @FormData
    public String getText() {
      return m_text;
    }

    @FormData
    public void setText(String text) {
      m_text = text;
    }
  }

  public static final class DynamicFormDataWithProperties extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public class PrimitiveBooleanProperty extends AbstractPropertyData<Boolean> {
      private static final long serialVersionUID = 1L;
    }

    public class PrimitiveIntegerProperty extends AbstractPropertyData<Integer> {
      private static final long serialVersionUID = 1L;
    }

    public class TextProperty extends AbstractPropertyData<String> {
      private static final long serialVersionUID = 1L;
    }

    public PrimitiveBooleanProperty getPrimitiveBooleanProperty() {
      return getPropertyByClass(PrimitiveBooleanProperty.class);
    }

    public PrimitiveIntegerProperty getPrimitiveIntegerProperty() {
      return getPropertyByClass(PrimitiveIntegerProperty.class);
    }

    public TextProperty getTextProperty() {
      return getPropertyByClass(TextProperty.class);
    }

    public boolean isPrimitiveBoolean() {
      return getPrimitiveBooleanProperty().getValue() == null ? false : getPrimitiveBooleanProperty().getValue();
    }

    public void setPrimitiveBoolean(boolean primitiveBoolean) {
      getPrimitiveBooleanProperty().setValue(primitiveBoolean);
    }

    public int getPrimitiveInteger() {
      return getPrimitiveIntegerProperty().getValue() == null ? 0 : getPrimitiveIntegerProperty().getValue();
    }

    public void setPrimitiveInteger(int primitiveInteger) {
      getPrimitiveIntegerProperty().setValue(primitiveInteger);
    }

    public String getText() {
      return getTextProperty().getValue();
    }

    public void setText(String text) {
      getTextProperty().setValue(text);
    }
  }
}
