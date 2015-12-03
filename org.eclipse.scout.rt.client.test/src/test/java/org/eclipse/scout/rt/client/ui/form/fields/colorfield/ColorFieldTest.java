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
package org.eclipse.scout.rt.client.ui.form.fields.colorfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.colorfield.ColorFieldTest.TestForm.MainBox.ColorField01;
import org.eclipse.scout.rt.client.ui.form.fields.colorfield.ColorFieldTest.TestForm.MainBox.ColorField01.TestMenu1;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ColorFieldTest {

  protected TestForm m_form;

  @Before
  public void setUp() {
    m_form = new TestForm();
    m_form.startForm();
  }

  @Test
  public void testParseValue() {
    ColorField01 field = m_form.getColorField01();
    field.getUIFacade().parseAndSetValueFromUI("120 135 160");
    assertEquals("#7887A0", field.getValue());

    field.getUIFacade().parseAndSetValueFromUI("50-160-240");
    assertEquals("#32A0F0", field.getValue());

    field.getUIFacade().parseAndSetValueFromUI("#FF8000");
    assertEquals("#FF8000", field.getValue());

    field.getUIFacade().parseAndSetValueFromUI("0x008080");
    assertEquals("#008080", field.getValue());

    field.getUIFacade().parseAndSetValueFromUI("0XFFFF00");
    assertEquals("#FFFF00", field.getValue());
  }

  @Test
  public void testParseInvalidValues() {
    // valid
    ColorField01 field = m_form.getColorField01();
    field.getUIFacade().parseAndSetValueFromUI("120 135 160");
    assertEquals("#7887A0", field.getValue());
    assertNull(field.getErrorStatus());

    // invalid hex
    field.getUIFacade().parseAndSetValueFromUI("#5989A4E");
    assertEquals("#7887A0", field.getValue());
    assertNotNull(field.getErrorStatus());

  }

  @Test
  public void testInvalidRgbValues() {
    ColorField01 field = m_form.getColorField01();
    field.getUIFacade().parseAndSetValueFromUI("120 135 160");
    assertEquals("#7887A0", field.getValue());
    assertNull(field.getErrorStatus());

    // invalid rgb
    field.getUIFacade().parseAndSetValueFromUI("256-200-200");
    assertEquals("#7887A0", field.getValue());
    assertNotNull(field.getErrorStatus());

    // reset to valid
    field.getUIFacade().parseAndSetValueFromUI("#1010A0");
    assertEquals("#1010A0", field.getValue());
    assertNull(field.getErrorStatus());

    // invalid rgb
    field.getUIFacade().parseAndSetValueFromUI("-100-200-200");
    assertEquals("#1010A0", field.getValue());
    assertNotNull(field.getErrorStatus());
  }

  @Test
  public void testMenus() {
    ColorField01 field = m_form.getColorField01();
    assertEquals(1, field.getMenus().size());
    assertEquals(TestMenu1.class, field.getMenus().get(0).getClass());
  }

  @After
  public void tearDown() throws Throwable {
    m_form.doClose();
  }

  public static class TestForm extends AbstractForm {

    public TestForm() {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "SmartField Form";
    }

    public void startForm() {
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
