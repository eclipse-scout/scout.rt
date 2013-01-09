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
package org.eclipse.scout.rt.client.test.ui.form;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.IPropertyFilter;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.test.ui.form.ImportFormDataWithFiltersTest.FilterImportTestForm.MainBox.SubBox.TestSubStringField;
import org.eclipse.scout.rt.client.test.ui.form.ImportFormDataWithFiltersTest.FilterImportTestForm.MainBox.TestStringField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.ExcludeFormFieldFilter;
import org.eclipse.scout.rt.client.ui.form.fields.IFormFieldFilter;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for
 * {@link org.eclipse.scout.rt.client.ui.form.AbstractForm#importFormData(AbstractFormData, boolean,IPropertyFilter, IFormFieldFilter) }
 */
public class ImportFormDataWithFiltersTest {

  /**
   * Tests that field data is imported, if no filter is set.
   * 
   * @throws ProcessingException
   *           if form instantiation fails
   */
  @Test
  public void testFormDataImportNoFilters() throws ProcessingException {
    FilterImportTestForm form = new FilterImportTestForm();
    ImportTestFormData data = new ImportTestFormData();
    data.getTestSubString().setValue("test");
    data.getTestString().setValue("test1");
    form.importFormData(data, true, null, null);
    Assert.assertEquals(data.getTestString().getValue(), form.getTestStringField().getValue());
    Assert.assertEquals(data.getTestSubString().getValue(), form.getTestSubStringField().getValue());
  }

  /**
   * Tests that field data not imported, if ExcludeFormFieldFilter is used with this field.
   * 
   * @throws ProcessingException
   *           if form instantiation fails
   */
  @Test
  public void testFormDataImportWithFieldFilter() throws ProcessingException {
    FilterImportTestForm form = new FilterImportTestForm();
    ImportTestFormData data = new ImportTestFormData();
    data.getTestString().setValue("test");
    data.getTestSubString().setValue("test1");
    ExcludeFormFieldFilter exclusionFilter = new ExcludeFormFieldFilter(form.getTestStringField());
    form.importFormData(data, true, null, exclusionFilter);
    Assert.assertNull(form.getTestStringField().getValue());
    Assert.assertEquals(data.getTestSubString().getValue(), form.getTestSubStringField().getValue());
  }

  /**
   * Tests that field data not imported, if ExcludeFormFieldFilter is used with this field, if initial value is set
   * explicitly.
   * 
   * @throws ProcessingException
   *           if form instantiation fails
   */
  @Test
  public void testFormDataImportWithFieldFilterAndInitValue() throws ProcessingException {
    FilterImportTestForm form = new FilterImportTestForm();
    form.getTestStringField().setValue("init");
    ImportTestFormData data = new ImportTestFormData();
    data.getTestString().setValue("test");
    ExcludeFormFieldFilter exclusionFilter = new ExcludeFormFieldFilter(form.getTestStringField());
    form.importFormData(data, true, null, exclusionFilter);
    Assert.assertEquals("init", form.getTestStringField().getValue());
  }

  /**
   * Tests that field data imported, if an empty ExcludeFormFieldFilter is used.
   * 
   * @throws ProcessingException
   *           if form instantiation fails
   */
  @Test
  public void testFormDataImportWithEmptyFieldFilter() throws ProcessingException {
    FilterImportTestForm form = new FilterImportTestForm();
    ImportTestFormData data = new ImportTestFormData();
    data.getTestString().setValue("test");
    ExcludeFormFieldFilter exclusionFilter = new ExcludeFormFieldFilter();
    form.importFormData(data, true, null, exclusionFilter);
    Assert.assertEquals(data.getTestString().getValue(), form.getTestStringField().getValue());
  }

  class FilterImportTestForm extends AbstractForm {

    public FilterImportTestForm() throws ProcessingException {
      super();
    }

    public TestSubStringField getTestSubStringField() {
      return getFieldByClass(TestSubStringField.class);
    }

    public TestStringField getTestStringField() {
      return getFieldByClass(TestStringField.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class SubBox extends AbstractGroupBox {

        @Order(20.0)
        public class TestSubStringField extends AbstractStringField {

          @Override
          protected Class<? extends IValueField> getConfiguredMasterField() {
            return TestStringField.class;
          }

        }
      }

      @Order(20.0)
      public class TestStringField extends AbstractStringField {
      }
    }
  }

  static class ImportTestFormData extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public ImportTestFormData() {
    }

    public TestString getTestString() {
      return getFieldByClass(TestString.class);
    }

    public TestSubString getTestSubString() {
      return getFieldByClass(TestSubString.class);
    }

    public static class TestString extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

      public TestString() {
      }
    }

    public static class TestSubString extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;

      public TestSubString() {
      }
    }
  }

}