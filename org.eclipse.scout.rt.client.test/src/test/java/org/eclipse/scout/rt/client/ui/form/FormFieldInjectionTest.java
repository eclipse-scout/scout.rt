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
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.FormFieldInjectionTestForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.FormFieldInjectionTestForm.MainBox.TopBox.StringField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 4.1
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormFieldInjectionTest {

  @Test
  public void testContainerFields() throws Exception {
    TestingFormFieldInjection injection = new TestingFormFieldInjection();
    FormFieldInjectionTestForm form = new FormFieldInjectionTestForm(injection);

    // build expected filterFields container field lists
    List<List<ICompositeField>> expectedFilterFieldsContainerFields = new ArrayList<List<ICompositeField>>();
    expectedFilterFieldsContainerFields.add(Arrays.<ICompositeField> asList(form.getMainBox()));
    expectedFilterFieldsContainerFields.add(Arrays.<ICompositeField> asList(form.getMainBox(), form.getTopBox()));

    // build expected injectFields container field lists -> is just the reverse list of filterFields, because it starts client-first
    List<List<ICompositeField>> expectedInjectFieldsContainerFields = new ArrayList<List<ICompositeField>>(expectedFilterFieldsContainerFields);
    Collections.reverse(expectedInjectFieldsContainerFields);

    // verify container fields
    assertEquals("filter fields has unexpected container fields", expectedFilterFieldsContainerFields, injection.getFilterFieldsContainerFields());
    assertEquals("inject fields has unexpected container fields", expectedInjectFieldsContainerFields, injection.getInjectFieldsContainerFields());
  }

  public static class TestingFormFieldInjection implements IFormFieldInjection {

    private final List<List<ICompositeField>> m_injectFieldsContainerFields = new ArrayList<List<ICompositeField>>();
    private final List<List<ICompositeField>> m_filterFieldsContainerFields = new ArrayList<List<ICompositeField>>();

    @Override
    public void injectFields(IFormField container, OrderedCollection<IFormField> fields) {
      System.out.println("injectFields: " + container);
      m_injectFieldsContainerFields.add(new ArrayList<ICompositeField>(FormFieldInjectionThreadLocal.getContainerFields()));
    }

    @Override
    public void filterFields(IFormField container, List<Class<? extends IFormField>> fieldList) {
      System.out.println("filterFields: " + container);
      m_filterFieldsContainerFields.add(new ArrayList<ICompositeField>(FormFieldInjectionThreadLocal.getContainerFields()));
    }

    public List<List<ICompositeField>> getInjectFieldsContainerFields() {
      return m_injectFieldsContainerFields;
    }

    public List<List<ICompositeField>> getFilterFieldsContainerFields() {
      return m_filterFieldsContainerFields;
    }
  }

  public static class FormFieldInjectionTestForm extends AbstractForm {

    private IFormFieldInjection m_injection;

    public FormFieldInjectionTestForm(IFormFieldInjection injection) {
      super(false);
      m_injection = injection;
      callInitializer();
    }

    @Override
    protected void initConfig() {
      try {
        FormFieldInjectionThreadLocal.push(m_injection);
        super.initConfig();
      }
      finally {
        FormFieldInjectionThreadLocal.pop(m_injection);
      }
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public TopBox getTopBox() {
      return getFieldByClass(TopBox.class);
    }

    public StringField getStringField() {
      return getFieldByClass(StringField.class);
    }

    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class TopBox extends AbstractGroupBox {

        @Order(10)
        public class StringField extends AbstractStringField {
        }
      }
    }
  }
}
