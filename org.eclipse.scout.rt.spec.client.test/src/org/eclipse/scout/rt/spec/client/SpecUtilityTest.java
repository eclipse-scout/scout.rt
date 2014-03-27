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
package org.eclipse.scout.rt.spec.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.AbstractSmartField;
import org.eclipse.scout.rt.extension.client.ui.action.menu.AbstractExtensibleMenu;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu1;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu1.SubMenu1;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu2;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu2.SubExtensibleMenu;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu2.SubExtensibleMenu.SubSubMenu;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopExtensibleMenu2.SubMenu2;
import org.eclipse.scout.rt.spec.client.SpecUtilityTest.TestForm.MainBox.SmartField.TopMenu1;
import org.junit.Test;

/**
 * Tests for {@link SpecUtility}
 */
public class SpecUtilityTest {

  @Test
  public void testExpandMenuHierarchy() throws ProcessingException {
    List<IMenu> menus = SpecUtility.expandMenuHierarchy(new TestForm().getFieldByClass(SmartField.class).getMenus());
    assertEquals(7, menus.size());
    assertTrue(menus.get(0) instanceof TopMenu1);
    assertTrue(menus.get(1) instanceof TopExtensibleMenu1);
    assertTrue(menus.get(2) instanceof SubMenu1);
    assertTrue(menus.get(3) instanceof TopExtensibleMenu2);
    assertTrue(menus.get(4) instanceof SubMenu2);
    assertTrue(menus.get(5) instanceof SubExtensibleMenu);
    assertTrue(menus.get(6) instanceof SubSubMenu);
  }

  class TestForm extends AbstractForm {

    public TestForm() throws ProcessingException {
      super();
    }

    public class MainBox extends AbstractGroupBox {
      public class SmartField extends AbstractSmartField<Long> {
        @Order(10.0)
        public class TopMenu1 extends AbstractMenu {
        }

        @Order(20.0)
        public class TopExtensibleMenu1 extends AbstractExtensibleMenu {
          @Order(90.0)
          public class SubMenu1 extends AbstractMenu {
          }
        }

        @Order(30.0)
        public class TopExtensibleMenu2 extends AbstractExtensibleMenu {
          @Order(10.0)
          public class SubMenu2 extends AbstractMenu {
          }

          @Order(20.0)
          public class SubExtensibleMenu extends AbstractExtensibleMenu {
            @Order(10.0)
            public class SubSubMenu extends AbstractMenu {
            }
          }
        }
      }
    }
  }

}
