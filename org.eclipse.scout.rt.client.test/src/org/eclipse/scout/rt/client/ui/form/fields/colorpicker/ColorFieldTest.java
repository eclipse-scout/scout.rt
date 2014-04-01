/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.colorpicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.colorpicker.ColorFieldTest.TestForm.MainBox.ColorField01;
import org.eclipse.scout.rt.client.ui.form.fields.colorpicker.ColorFieldTest.TestForm.MainBox.ColorField01.TestMenu1;
import org.eclipse.scout.rt.client.ui.form.fields.colorpickerfield.AbstractColorField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceRegistration;

@RunWith(ScoutClientTestRunner.class)
public class ColorFieldTest {

  protected TestForm form;
  private List<ServiceRegistration> reg;

  @Before
  public void setUp() throws ProcessingException {
    form = new TestForm();
    form.startForm();
  }

  @Test
  public void testParseValue() {
    ColorField01 field = form.getColorField01();
    field.getUIFacade().setTextFromUI("120 135 160", false);
    assertEquals("#7887A0", field.getValue());

    field.getUIFacade().setTextFromUI("50-160-240", false);
    assertEquals("#32A0F0", field.getValue());

    field.getUIFacade().setTextFromUI("#FF8000", false);
    assertEquals("#FF8000", field.getValue());

    field.getUIFacade().setTextFromUI("0x008080", false);
    assertEquals("#008080", field.getValue());

    field.getUIFacade().setTextFromUI("0XFFFF00", false);
    assertEquals("#FFFF00", field.getValue());
  }

  @Test
  public void testParseInvalidValues() {
    // valid
    ColorField01 field = form.getColorField01();
    field.getUIFacade().setTextFromUI("120 135 160", false);
    assertEquals("#7887A0", field.getValue());
    assertNull(field.getErrorStatus());

    // invalid hex
    field.getUIFacade().setTextFromUI("#5989A4E", false);
    assertEquals("#7887A0", field.getValue());
    assertNotNull(field.getErrorStatus());

  }

  @Test
  public void testInvalidRgbValues() {
    ColorField01 field = form.getColorField01();
    field.getUIFacade().setTextFromUI("120 135 160", false);
    assertEquals("#7887A0", field.getValue());
    assertNull(field.getErrorStatus());

    // invalid rgb
    field.getUIFacade().setTextFromUI("256-200-200", false);
    assertEquals("#7887A0", field.getValue());
    assertNotNull(field.getErrorStatus());

    // reset to valid
    field.getUIFacade().setTextFromUI("#1010A0", false);
    assertEquals("#1010A0", field.getValue());
    assertNull(field.getErrorStatus());

    // invalid rgb
    field.getUIFacade().setTextFromUI("-100-200-200", false);
    assertEquals("#1010A0", field.getValue());
    assertNotNull(field.getErrorStatus());
  }

  @Test
  public void testMenus() {
    ColorField01 field = form.getColorField01();
    assertEquals(1, field.getMenus().size());
    assertEquals(TestMenu1.class, field.getMenus().get(0).getClass());
  }

  @After
  public void tearDown() throws Throwable {
    TestingUtility.unregisterServices(reg);
    form.doClose();
  }

  public static class TestForm extends AbstractForm {

    public TestForm() throws ProcessingException {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "SmartField Form";
    }

    public void startForm() throws ProcessingException {
      startInternal(new FormHandler());
    }

    public ColorField01 getColorField01() {
      return getFieldByClass(ColorField01.class);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class ColorField01 extends AbstractColorField {
        @Override
        protected String getConfiguredLabel() {
          return "Field 01";
        }

        @Order(10)
        public class TestMenu1 extends AbstractMenu {
          @Override
          protected String getConfiguredText() {
            return "&TestMenu1";
          }

          @Override
          protected String getConfiguredKeyStroke() {
            return "alternate-2";
          }
        }

      }

      @Order(100)
      public class CloseButton extends AbstractCloseButton {
        @Override
        protected String getConfiguredLabel() {
          return "Close";
        }
      }
    }
  }

}
