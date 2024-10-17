/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.*;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.AbstractFormExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.AbstractGroupBoxExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWithFieldExtensionFormTest.TestForm.MainBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests loading and storing to xml of forms with extensions containing fields.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class StoreAndLoadXmlWithFieldExtensionFormTest extends AbstractLocalExtensionTestCase {

  @After
  public void cleanup() {
    BEANS.get(IExtensionRegistry.class).deregister(TestFormExtensionWithOneField.class);
    BEANS.get(IExtensionRegistry.class).deregister(TestFormExtensionWithInvalidField.class);
  }

  @Test
  public void testExtensionFieldOnForm() {
    BEANS.get(IExtensionRegistry.class).register(TestFormExtensionWithOneField.class, TestForm.class);

    // setup initial form with extension containing data
    TestForm form = new TestForm();
    TestFormExtensionWithOneField formExtension = form.getExtension(TestFormExtensionWithOneField.class);
    assertEquals(2, form.getAllExtensions().size());
    assertNotNull(formExtension);
    form.getTextField().setValue("textFieldValue");
    formExtension.getExtensionTextField().setValue("extensionTextFieldValue");
    String xml = form.storeToXmlString();

    TestForm target = new TestForm();
    TestFormExtensionWithOneField targetExtension = target.getExtension(TestFormExtensionWithOneField.class);
    assertNotNull(targetExtension);
    assertTrue(target.loadFromXmlString(xml));
    assertEquals("textFieldValue", target.getTextField().getValue());
    assertEquals("extensionTextFieldValue", targetExtension.getExtensionTextField().getValue());
  }

  @Test
  public void testExtensionFieldMissingOnForm() {
    BEANS.get(IExtensionRegistry.class).register(TestFormExtensionWithOneField.class, TestForm.class);

    // setup initial form with extension containing data
    TestForm form = new TestForm();
    TestFormExtensionWithOneField formExtension = form.getExtension(TestFormExtensionWithOneField.class);
    assertEquals(2, form.getAllExtensions().size());
    assertNotNull(formExtension);
    form.getTextField().setValue("textFieldValue");
    formExtension.getExtensionTextField().setValue("extensionTextFieldValue");
    String xml = form.storeToXmlString();

    // deregister extension and try to load XML
    BEANS.get(IExtensionRegistry.class).deregister(TestFormExtensionWithOneField.class);

    TestForm target = new TestForm();
    assertNull(target.getExtension(TestFormExtensionWithOneField.class));
    assertFalse(target.loadFromXmlString(xml));
    assertEquals("textFieldValue", target.getTextField().getValue());
  }

  @Test
  public void testExtensionFieldInvalidType() {
    BEANS.get(IExtensionRegistry.class).register(TestFormExtensionWithOneField.class, TestForm.class);

    // setup initial form with extension containing data
    TestForm form = new TestForm();
    TestFormExtensionWithOneField formExtension = form.getExtension(TestFormExtensionWithOneField.class);
    assertEquals(2, form.getAllExtensions().size());
    assertNotNull(formExtension);
    form.getTextField().setValue("textFieldValue");
    formExtension.getExtensionTextField().setValue("extensionTextFieldValue");
    String xml = form.storeToXmlString();

    // switch extension and try to load XML
    BEANS.get(IExtensionRegistry.class).deregister(TestFormExtensionWithOneField.class);
    BEANS.get(IExtensionRegistry.class).register(TestFormExtensionWithInvalidField.class, TestForm.class);

    TestForm target = new TestForm();
    assertNull(target.getExtension(TestFormExtensionWithOneField.class));
    TestFormExtensionWithInvalidField targetExtension = target.getExtension(TestFormExtensionWithInvalidField.class);
    assertNotNull(targetExtension);
    assertFalse(target.loadFromXmlString(xml));
    assertEquals("textFieldValue", target.getTextField().getValue());
  }

  public static final class TestForm extends AbstractForm {

    public MainBox.GroupBox.TextField getTextField() {
      return getFieldByClass(MainBox.GroupBox.TextField.class);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public MainBox.GroupBox getGroupBox() {
      return getFieldByClass(MainBox.GroupBox.class);
    }

    @Order(10.0)
    public class MainBox extends AbstractGroupBox {

      @Order(10.0)
      public class GroupBox extends AbstractGroupBox {

        @Order(10.0)
        public class TextField extends AbstractStringField {
        }
      }
    }
  }

  public static final class TestFormExtensionWithOneField extends AbstractFormExtension<TestForm> {

    public TestFormExtensionWithOneField(TestForm owner) {
      super(owner);
    }

    public GroupBoxExtension.ExtensionTextField getExtensionTextField() {
      return getOwner().getFieldByClass(GroupBoxExtension.ExtensionTextField.class);
    }

    public class GroupBoxExtension extends AbstractGroupBoxExtension<TestForm.MainBox.GroupBox> {

      public GroupBoxExtension(MainBox.GroupBox owner) {
        super(owner);
      }

      @Order(10)
      public class ExtensionTextField extends AbstractStringField {
      }
    }
  }

  public static final class TestFormExtensionWithInvalidField extends AbstractFormExtension<TestForm> {

    public TestFormExtensionWithInvalidField(TestForm owner) {
      super(owner);
    }

    public GroupBoxExtension.ExtensionTextField getExtensionTextField() {
      return getOwner().getFieldByClass(GroupBoxExtension.ExtensionTextField.class);
    }

    public class GroupBoxExtension extends AbstractGroupBoxExtension<TestForm.MainBox.GroupBox> {

      public GroupBoxExtension(MainBox.GroupBox owner) {
        super(owner);
      }

      @Order(10)
      public class ExtensionTextField extends AbstractIntegerField { // invalid type on purpose
      }
    }
  }
}
