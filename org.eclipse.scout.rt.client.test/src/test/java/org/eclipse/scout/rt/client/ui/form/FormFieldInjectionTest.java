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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestForm.MainBox.TopBox.StringField;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestFormEx.BottomBox.BooleanField;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestFormEx.StringField2;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestFormEx.TopBoxEx;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestFormExEx.BooleanField2;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestFormExEx.BooleanField2Ex;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestFormExEx.StringField3;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestFormExEx.StringField4;
import org.eclipse.scout.rt.client.ui.form.FormFieldInjectionTest.TestFormExEx.TopBoxExEx;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.extension.InjectFieldTo;
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

  public static class TestForm extends AbstractForm {

    public TestForm(boolean callInitializer) {
      super(callInitializer);
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

  public static class FormFieldInjectionTestForm extends TestForm {

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
  }

  /**
   * Extended TestForm with replaced TopBox
   */
  public static class TestFormEx extends TestForm {

    public TestFormEx(boolean callInitializer) {
      super(callInitializer);
    }

    @Replace
    public class TopBoxEx extends MainBox.TopBox {
      public TopBoxEx(MainBox container) {
        container.super();
      }
    }

    @Order(20)
    @InjectFieldTo(TopBox.class)
    public class StringField2 extends AbstractStringField {
    }

    @Order(20)
    @InjectFieldTo(MainBox.class)
    public class BottomBox extends AbstractGroupBox {
      @Order(10)
      public class BooleanField extends AbstractStringField {
      }
    }
  }

  /**
   * Extended TestFormEx again and replace TopBoxEx again
   */
  public static class TestFormExEx extends TestFormEx {

    public TestFormExEx(boolean callInitializer) {
      super(callInitializer);
    }

    @Replace
    public class TopBoxExEx extends TopBoxEx {
      public TopBoxExEx(MainBox container) {
        super(container);
      }
    }

    @Order(20)
    @InjectFieldTo(TopBox.class) // inject field to replaced TopBox
    public class StringField3 extends AbstractStringField {
    }

    @Order(30)
    @InjectFieldTo(TopBoxEx.class) // inject field to replaced TopBoxEx
    public class StringField4 extends AbstractStringField {
    }

    @Order(50)
    @InjectFieldTo(BottomBox.class)
    public class BooleanField2 extends AbstractStringField {
    }

    @Order(60)
    @Replace // replace BooleanField2 field which was injected in same container
    public class BooleanField2Ex extends BooleanField2 {
    }
  }

  @Test
  public void testReplaceContainerWithInjectedFieldsBasic() {
    TestForm form = new TestForm(true);
    assertTrue(form.getTopBox() instanceof TopBox);
    assertNotNull(form.getFieldByClass(StringField.class)); // field is part of TopBox
    assertNull(form.getFieldByClass(StringField2.class)); // field not available in TestForm
    assertNull(form.getFormFieldReplacementsInternal()); // no injected fields in TestForm
  }

  @Test
  public void testReplaceContainerWithInjectedFieldsLevel1() {
    // TestFormEx extends TestForm and replaces the TopBox, a field within TestFormEx is injected to the replaced TopBox
    TestFormEx formEx = new TestFormEx(true);
    assertTrue(formEx.getTopBox() instanceof TopBoxEx);
    assertNotNull(formEx.getFieldByClass(StringField.class));
    assertNotNull(formEx.getFieldByClass(StringField2.class)); // field injected to the replaced TopBox must be available
    assertNotNull(formEx.getTopBox().getFieldByClass(StringField.class));
    assertNotNull(formEx.getTopBox().getFieldByClass(StringField2.class));
    assertNotNull(formEx.getFieldByClass(BooleanField.class));
    assertNull(formEx.getFieldByClass(BooleanField2.class));

    Map<Class<?>, Class<? extends IFormField>> expectedReplacementMap = new HashMap<>();
    expectedReplacementMap.put(TopBox.class, TopBoxEx.class);
    assertEquals(expectedReplacementMap, formEx.getFormFieldReplacementsInternal());
  }

  @Test
  public void testReplaceContainerWithInjectedFieldsLevel2() {
    // TestFormExEx extends TestFormEx and replaces the TopBoxEx, a field within TestFormExEx is injected to the replaced TopBox
    TestFormExEx formExEx = new TestFormExEx(true);

    assertTrue(formExEx.getTopBox() instanceof TopBoxEx);
    assertTrue(formExEx.getTopBox() instanceof TopBoxExEx);
    assertNotNull(formExEx.getFieldByClass(StringField.class));
    assertNotNull(formExEx.getFieldByClass(StringField2.class)); // field injected to the replaced TopBox must be available
    assertNotNull(formExEx.getFieldByClass(StringField3.class)); // field injected to the replaced TopBox must be available
    assertNotNull(formExEx.getFieldByClass(StringField4.class)); // field injected to the replaced TopBoxEx must be available
    assertNotNull(formExEx.getTopBox().getFieldByClass(StringField.class));
    assertNotNull(formExEx.getTopBox().getFieldByClass(StringField2.class)); // field injected to the replaced TopBox must be available
    assertNotNull(formExEx.getTopBox().getFieldByClass(StringField3.class)); // field injected to the replaced TopBox must be available
    assertNotNull(formExEx.getTopBox().getFieldByClass(StringField4.class)); // field injected to the replaced TopBoxEx must be available
    assertNotNull(formExEx.getFieldByClass(BooleanField.class));
    assertNotNull(formExEx.getFieldByClass(BooleanField2.class));
    assertNotNull(formExEx.getFieldByClass(BooleanField2Ex.class));
    assertTrue(formExEx.getFieldByClass(BooleanField2.class) instanceof BooleanField2Ex);

    Map<Class<?>, Class<? extends IFormField>> expectedReplacementMap = new HashMap<>();
    expectedReplacementMap.put(TopBox.class, TopBoxExEx.class);
    expectedReplacementMap.put(TopBoxEx.class, TopBoxExEx.class);
    expectedReplacementMap.put(BooleanField2.class, BooleanField2Ex.class);
    assertEquals(expectedReplacementMap, formExEx.getFormFieldReplacementsInternal());
  }
}
