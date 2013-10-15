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

import java.util.List;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.Activator;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartFieldTest.TestForm.MainBox.StyleField;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.lookup.TestingLookupService;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
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
        protected Class<? extends LookupCall> getConfiguredLookupCall() {
          return StyleLookupCall.class;
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
      setRows(new LookupRow[]{
          new LookupRow(10L, "Red", ICON_FILE, "Red tooltip", "ff8888", "880000", FontSpec.parse("italic")),
          new LookupRow(20L, "Yellow", ICON_FILE, "Yellow tooltip", "ffff88", "888800", FontSpec.parse("italic")),
          new LookupRow(30L, "Green", ICON_FILE, "Green tooltip", "88ff88", "008800", FontSpec.parse("italic")),
          new LookupRow(40L, "Blue", ICON_FILE, "Blue tooltip", "8888ff", "000088", FontSpec.parse("italic")),
          new LookupRow(50L, "Empty"),});
    }
  }

  public static class StyleLookupCall extends LookupCall {
    private static final long serialVersionUID = 1L;

    @Override
    protected final Class<? extends ILookupService> getConfiguredService() {
      return StyleLookupService.class;
    }
  }

  @Test
  public void testSmartfieldStyle() throws Throwable {
    //register services
    List<ServiceRegistration> reg = TestingUtility.registerServices(Activator.getDefault().getBundle(), 0, new StyleLookupService());
    try {
      //start form
      form = new TestForm();
      form.startForm();
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
      f.acceptProposal(new LookupRow(10L, "Red", ICON_FILE, "Red tooltip", "ff8888", "880000", FontSpec.parse("italic")));
      assertFieldStyle(f, ICON_FILE, "Red tooltip", "ff8888", "880000", "italic");
      f.acceptProposal(new LookupRow(50L, "Empty"));
      assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
      f.acceptProposal(new LookupRow(20L, "Yellow", ICON_FILE, "Yellow tooltip", "ffff88", "888800", FontSpec.parse("italic")));
      assertFieldStyle(f, ICON_FILE, "Yellow tooltip", "ffff88", "888800", "italic");
      f.setValue(null);
      assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
      //close
      form.doClose();
    }
    finally {
      TestingUtility.unregisterServices(reg);
    }
  }

  private static void assertFieldStyle(StyleField f, String icon, String tt, String bg, String fg, String font) {
    String expectedStyle = tt + ", " + bg + ", " + fg + ", " + (font != null ? FontSpec.parse(font).toPattern() : null);
    String actualStyle = f.getTooltipText() + ", " + f.getBackgroundColor() + ", " + f.getForegroundColor() + ", " + (f.getFont() != null ? f.getFont().toPattern() : null);
    assertEquals(expectedStyle, actualStyle);
  }
}
