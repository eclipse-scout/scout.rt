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
package org.eclipse.scout.rt.client.ui.form.fields.button;

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.ButtonTest.TestForm.MainBox.PushButton1;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractButton}
 *
 * @since 3.10.0-M4
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ButtonTest {

  public static class TestForm extends AbstractForm {

    public TestForm() {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "Test Form";
    }

    public void startForm() {
      startInternal(new FormHandler());
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public AbstractButton getPushButton1() {
      return getFieldByClass(PushButton1.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class PushButton1 extends AbstractButton {

        @Override
        protected void execClickAction() {
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

          @Override
          protected void execAction() {
            MessageBoxes.createOk().withHeader("click").withBody("it").show();
          }
        }

        @Order(20)
        public class TestMenu2 extends AbstractMenu {
          @Override
          protected String getConfiguredText() {
            return "T&estMenu2";
          }

          @Override
          protected String getConfiguredKeyStroke() {
            return "control-alternate-f11";
          }

          @Override
          protected void execAction() {
            MessageBoxes.createOk().withHeader("click").withBody("it").show();
          }
        }
      }
    }
  }

  private TestForm m_form;

  @Before
  public void setUp() throws Throwable {
    m_form = new TestForm();
    m_form.startForm();
  }

  @Test
  public void testMenusAndKeyStrokes() {
    List<IMenu> pushButton1Menus = m_form.getPushButton1().getMenus();
    Assert.assertEquals("PushButton1 should have 2 menus", 2, pushButton1Menus.size());
    Assert.assertEquals("TestMenu1", pushButton1Menus.get(0).getText());
    Assert.assertEquals("&TestMenu1", pushButton1Menus.get(0).getTextWithMnemonic());
    Assert.assertEquals("alternate-2", pushButton1Menus.get(0).getKeyStroke());

    Assert.assertEquals("TestMenu2", pushButton1Menus.get(1).getText());
    Assert.assertEquals("T&estMenu2", pushButton1Menus.get(1).getTextWithMnemonic());
    Assert.assertEquals("control-alternate-f11", pushButton1Menus.get(1).getKeyStroke());

  }

  @After
  public void tearDown() throws Throwable {
    m_form.doClose();
  }
}
