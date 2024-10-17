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

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWithFieldFilterFormTest.TestForm.MainBox.GroupBox.ExcludedStringField;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlWithFieldFilterFormTest.TestForm.MainBox.GroupBox.IncludedStringField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests exporting a form with a "store to xml field filter"
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class StoreAndLoadXmlWithFieldFilterFormTest {

  static final String TEST_DATA = "testdata";

  @Test
  public void test() {
    TestForm f = new TestForm();
    try {
      f.startModify();
      assertEquals(TEST_DATA, f.getIncludedStringField().getValue());
      assertEquals(TEST_DATA, f.getExcludedStringField().getValue());

      //store xml
      String xml = f.storeToXmlString();
      assertTrue(xml.contains("IncludedStringField"));
      assertFalse(xml.contains("ExcludedStringField"));

      // clear form
      f.getExcludedStringField().setValue(null);
      f.getIncludedStringField().setValue(null);
      assertNull(f.getIncludedStringField().getValue());
      assertNull(f.getExcludedStringField().getValue());

      // load xml
      f.loadFromXmlString(xml);
      assertEquals(TEST_DATA, f.getIncludedStringField().getValue()); // restored from xml
      assertNull(f.getExcludedStringField().getValue()); // still null, not included in the xml export
    }
    finally {
      f.doClose();
    }
  }

  public static class TestForm extends AbstractForm {

    public TestForm() {
      super();
      // custom filter
      setStoreToXmlFieldFilter(field -> !field.getClass().equals(ExcludedStringField.class));
    }

    @Override
    protected String getConfiguredTitle() {
      return "TestForm";
    }

    public void startModify() {
      startInternal(new ModifyHandler());
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public ExcludedStringField getExcludedStringField() {
      return getFieldByClass(ExcludedStringField.class);
    }

    public IncludedStringField getIncludedStringField() {
      return getFieldByClass(IncludedStringField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class GroupBox extends AbstractGroupBox {

        @Order(10)
        public class IncludedStringField extends AbstractStringField {
        }

        @Order(20)
        public class ExcludedStringField extends AbstractStringField {
        }
      }
    }

    @Order(20)
    public class CloseButton extends AbstractCloseButton {
    }

    public class ModifyHandler extends AbstractFormHandler {
      @Override
      protected void execLoad() {
        getIncludedStringField().setValue(TEST_DATA);
        getExcludedStringField().setValue(TEST_DATA);
      }
    }
  }
}
