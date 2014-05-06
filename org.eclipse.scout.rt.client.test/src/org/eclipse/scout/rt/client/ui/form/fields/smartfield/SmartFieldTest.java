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
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.Activator;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartFieldTest.TestForm.MainBox.StyleField;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.lookup.TestingLookupService;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceRegistration;

/**
 * Smartfield with rows having tooltip/color/icon. When some rows do not have tooltip/color/icon this info should be
 * taken from the initial values of tooltip/color/icon of the field.
 */
@RunWith(ScoutClientTestRunner.class)
public class SmartFieldTest {
  public static final String ICON_BOOKMARK = "bookmark";
  public static final String ICON_FILE = "file";

  protected TestForm form;
  private List<ServiceRegistration> reg;

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

    public StyleField getStyleField() {
      return getFieldByClass(StyleField.class);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class StyleField extends AbstractSmartField<Long> {
        @Override
        protected String getConfiguredLabel() {
          return "Style";
        }

        @Override
        protected boolean getConfiguredAutoAddDefaultMenus() {
          return false;
        }

        @Override
        protected String getConfiguredTooltipText() {
          return "Default tooltip";
        }

        @Override
        protected String getConfiguredIconId() {
          return ICON_BOOKMARK;
        }

        @Override
        protected String getConfiguredBackgroundColor() {
          return "000000";
        }

        @Override
        protected String getConfiguredForegroundColor() {
          return "cccccc";
        }

        @Override
        protected String getConfiguredFont() {
          return "bold";
        }

        @Override
        protected Class<? extends ILookupCall<Long>> getConfiguredLookupCall() {
          return StyleLookupCall.class;
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

  public static class StyleLookupService extends TestingLookupService {
    @Override
    public void initializeService(ServiceRegistration registration) {
      List<ILookupRow<Long>> rows = new ArrayList<ILookupRow<Long>>();
      rows.add(new LookupRow<Long>(10L, "Red", ICON_FILE, "Red tooltip", "ff8888", "880000", FontSpec.parse("italic")));
      rows.add(new LookupRow<Long>(20L, "Yellow", ICON_FILE, "Yellow tooltip", "ffff88", "888800", FontSpec.parse("italic")));
      rows.add(new LookupRow<Long>(30L, "Green", ICON_FILE, "Green tooltip", "88ff88", "008800", FontSpec.parse("italic")));
      rows.add(new LookupRow<Long>(40L, "Blue", ICON_FILE, "Blue tooltip", "8888ff", "000088", FontSpec.parse("italic")));
      rows.add(new LookupRow<Long>(50L, "Empty"));
      setRows(rows);
    }
  }

  public static class StyleLookupCall extends LookupCall<Long> {
    private static final long serialVersionUID = 1L;

    @Override
    protected final Class<? extends ILookupService<Long>> getConfiguredService() {
      return StyleLookupService.class;
    }
  }

  @Before
  public void setUp() throws Throwable {
    reg = TestingUtility.registerServices(Activator.getDefault().getBundle(), 0, new StyleLookupService());
    form = new TestForm();
    form.startForm();
  }

  @Test
  public void testSmartfieldStyle() throws Throwable {

    StyleField f = form.getStyleField();
    //model-side test
    f.setValue(10L);
    assertFieldStyle(f, ICON_FILE, "Red tooltip", "ff8888", "880000", "italic");
    f.setValue(50L);
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
    f.setValue(20L);
    assertFieldStyle(f, ICON_FILE, "Yellow tooltip", "ffff88", "888800", "italic");
    f.setValue(null);
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
    //ui-side test
    f.getUIFacade().setTextFromUI("Red");
    assertFieldStyle(f, ICON_FILE, "Red tooltip", "ff8888", "880000", "italic");
    f.getUIFacade().setTextFromUI("Empty");
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
    f.getUIFacade().setTextFromUI("Yellow");
    assertFieldStyle(f, ICON_FILE, "Yellow tooltip", "ffff88", "888800", "italic");
    f.getUIFacade().setTextFromUI(null);
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
    //proposal-side test
    f.acceptProposal(new LookupRow<Long>(10L, "Red", ICON_FILE, "Red tooltip", "ff8888", "880000", FontSpec.parse("italic")));
    assertFieldStyle(f, ICON_FILE, "Red tooltip", "ff8888", "880000", "italic");
    f.acceptProposal(new LookupRow<Long>(50L, "Empty"));
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
    f.acceptProposal(new LookupRow<Long>(20L, "Yellow", ICON_FILE, "Yellow tooltip", "ffff88", "888800", FontSpec.parse("italic")));
    assertFieldStyle(f, ICON_FILE, "Yellow tooltip", "ffff88", "888800", "italic");
    f.setValue(null);
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");

  }

  @After
  public void tearDown() throws Throwable {
    TestingUtility.unregisterServices(reg);
    form.doClose();
  }

  @Test
  public void testSmartfieldMenus() {

    List<IMenu> smartfieldMenus = form.getStyleField().getMenus();
    Assert.assertEquals("Smartfield should have 2 menus", 2, smartfieldMenus.size());
    Assert.assertEquals("TestMenu1", smartfieldMenus.get(0).getText());
    Assert.assertEquals("&TestMenu1", smartfieldMenus.get(0).getTextWithMnemonic());
    Assert.assertEquals("alternate-2", smartfieldMenus.get(0).getKeyStroke());

    Assert.assertEquals("TestMenu2", smartfieldMenus.get(1).getText());
    Assert.assertEquals("T&estMenu2", smartfieldMenus.get(1).getTextWithMnemonic());
    Assert.assertEquals("control-alternate-f11", smartfieldMenus.get(1).getKeyStroke());

    List<IKeyStroke> smartfieldKeyStrokes = form.getStyleField().getContributedKeyStrokes();
    Assert.assertNotNull("KeyStrokes of Smartfield should not be null", smartfieldKeyStrokes);
    Assert.assertEquals("Smartfield should have 2 keyStrokes registered", 2, smartfieldKeyStrokes.size());
  }

  private static void assertFieldStyle(StyleField f, String icon, String tt, String bg, String fg, String font) {
    String expectedStyle = tt + ", " + bg + ", " + fg + ", " + (font != null ? FontSpec.parse(font).toPattern() : null);
    String actualStyle = f.getTooltipText() + ", " + f.getBackgroundColor() + ", " + f.getForegroundColor() + ", " + (f.getFont() != null ? f.getFont().toPattern() : null);
    assertEquals(expectedStyle, actualStyle);
  }
}
