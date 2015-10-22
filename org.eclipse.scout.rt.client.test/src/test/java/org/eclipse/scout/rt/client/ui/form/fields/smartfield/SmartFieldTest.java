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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartFieldTest.TestForm.MainBox.StyleField;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.rt.testing.shared.services.lookup.TestingLookupService;
import org.eclipse.scout.testing.client.form.FormHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Smartfield with rows having tooltip/color/icon. When some rows do not have tooltip/color/icon this info should be
 * taken from the initial values of tooltip/color/icon of the field.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SmartFieldTest {
  public static final String ICON_BOOKMARK = "bookmark";
  public static final String ICON_FILE = "file";

  private static List<IBean<?>> m_beans;

  protected TestForm m_form;
  private List<IBean<?>> m_reg;

  @BeforeClass
  public static void beforeClass() throws Exception {
    m_beans = TestingUtility.registerBeans(new BeanMetaData(StyleLookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    TestingUtility.unregisterBeans(m_beans);
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
    @PostConstruct
    protected void initializeService() {
      List<ILookupRow<Long>> rows = new ArrayList<ILookupRow<Long>>();
      rows.add(new LookupRow<Long>(10L, "Red")
          .withIconId(ICON_FILE)
          .withTooltipText("Red tooltip")
          .withBackgroundColor("ff8888")
          .withForegroundColor("880000")
          .withFont(FontSpec.parse("italic")));
      rows.add(new LookupRow<Long>(20L, "Yellow")
          .withIconId(ICON_FILE)
          .withTooltipText("Yellow tooltip")
          .withBackgroundColor("ffff88")
          .withForegroundColor("888800")
          .withFont(FontSpec.parse("italic")));
      rows.add(new LookupRow<Long>(30L, "Green")
          .withIconId(ICON_FILE)
          .withTooltipText("Green tooltip")
          .withBackgroundColor("88ff88")
          .withForegroundColor("008800")
          .withFont(FontSpec.parse("italic")));
      rows.add(new LookupRow<Long>(40L, "Blue")
          .withIconId(ICON_FILE)
          .withTooltipText("Blue tooltip")
          .withBackgroundColor("8888ff")
          .withForegroundColor("000088")
          .withFont(FontSpec.parse("italic")));
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
    m_reg = TestingUtility.registerBeans(
        new BeanMetaData(StyleLookupService.class)
            .withApplicationScoped(true));
    m_form = new TestForm();
    m_form.startForm();
  }

  @Test
  public void testStyle_SetValue() throws Throwable {
    StyleField f = m_form.getStyleField();
    f.setValue(10L);
    assertFieldStyle(f, ICON_FILE, "Red tooltip", "ff8888", "880000", "italic");
    f.setValue(50L);
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
    f.setValue(20L);
    assertFieldStyle(f, ICON_FILE, "Yellow tooltip", "ffff88", "888800", "italic");
    f.setValue(null);
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
  }

  @Test
  public void testStyle_UIFacade() throws Throwable {
    StyleField f = m_form.getStyleField();
    f.getUIFacade().acceptProposalFromUI("Red", false, false);
    assertFieldStyle(f, ICON_FILE, "Red tooltip", "ff8888", "880000", "italic");
    f.getUIFacade().acceptProposalFromUI("Empty", false, false);
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
    f.getUIFacade().acceptProposalFromUI("Yellow", false, false);
    assertFieldStyle(f, ICON_FILE, "Yellow tooltip", "ffff88", "888800", "italic");
    f.getUIFacade().acceptProposalFromUI(null, false, false);
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
  }

  @Test
  public void testStyle_AcceptProposal() throws Throwable {
    StyleField f = m_form.getStyleField();
    f.acceptProposal(new LookupRow<Long>(10L, "Red")
        .withIconId(ICON_FILE)
        .withTooltipText("Red tooltip")
        .withBackgroundColor("ff8888")
        .withForegroundColor("880000")
        .withFont(FontSpec.parse("italic")));
    assertFieldStyle(f, ICON_FILE, "Red tooltip", "ff8888", "880000", "italic");
    f.acceptProposal(new LookupRow<Long>(50L, "Empty"));
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
    f.acceptProposal(new LookupRow<Long>(20L, "Yellow")
        .withIconId(ICON_FILE)
        .withTooltipText("Yellow tooltip")
        .withBackgroundColor("ffff88")
        .withForegroundColor("888800")
        .withFont(FontSpec.parse("italic")));
    assertFieldStyle(f, ICON_FILE, "Yellow tooltip", "ffff88", "888800", "italic");
    f.setValue(null);
    assertFieldStyle(f, ICON_BOOKMARK, "Default tooltip", "000000", "cccccc", "bold");
  }

  @After
  public void tearDown() throws Throwable {
    TestingUtility.unregisterBeans(m_reg);
    m_form.doClose();
  }

  @Test
  public void testSmartfieldMenus() {
    List<IMenu> smartfieldMenus = m_form.getStyleField().getMenus();
    assertEquals("Smartfield should have 2 menus", 2, smartfieldMenus.size());
    assertEquals("TestMenu1", smartfieldMenus.get(0).getText());
    assertEquals("&TestMenu1", smartfieldMenus.get(0).getTextWithMnemonic());
    assertEquals("alternate-2", smartfieldMenus.get(0).getKeyStroke());

    assertEquals("TestMenu2", smartfieldMenus.get(1).getText());
    assertEquals("T&estMenu2", smartfieldMenus.get(1).getTextWithMnemonic());
    assertEquals("control-alternate-f11", smartfieldMenus.get(1).getKeyStroke());
  }

  private static void assertFieldStyle(StyleField f, String icon, String tt, String bg, String fg, String font) {
    String expectedStyle = tt + ", " + bg + ", " + fg + ", " + (font != null ? FontSpec.parse(font).toPattern() : null);
    String actualStyle = f.getTooltipText() + ", " + f.getBackgroundColor() + ", " + f.getForegroundColor() + ", " + (f.getFont() != null ? f.getFont().toPattern() : null);
    assertEquals(expectedStyle, actualStyle);
  }

  /**
   * @param bc
   * @return
   */
  protected PropertyChangeListener getUnblockOnSearchResultListener(final IBlockingCondition bc) {
    PropertyChangeListener bcListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (IContentAssistFieldLookupRowFetcher.PROP_SEARCH_RESULT.equals(evt.getPropertyName())) {
          bc.setBlocking(false);
        }
      }
    };
    return bcListener;
  }

  @Test
  public void testDefaultSelectionWithOnlyOneResult() throws Exception {
    IBlockingCondition bc = Jobs.getJobManager().createBlockingCondition("loadProposals", true);

    PropertyChangeListener bcListener = getUnblockOnSearchResultListener(bc);
    m_form.getStyleField().getLookupRowFetcher().addPropertyChangeListener(bcListener);
    m_form.getStyleField().getUIFacade().openProposalChooserFromUI("*", false);
    bc.waitFor();

    bc.setBlocking(true);
    m_form.getStyleField().getLookupRowFetcher().addPropertyChangeListener(bcListener);
    m_form.getStyleField().getUIFacade().proposalTypedFromUI("Y");
    bc.waitFor();

    // verifies one (and only, unique) result is available and selected
    assertTrue(m_form.getStyleField().isProposalChooserRegistered());
    TableProposalChooser<?> tableProposalChooser = (TableProposalChooser<?>) m_form.getStyleField().getProposalChooser();
    IContentAssistFieldTable<?> resultTable = tableProposalChooser.getModel();
    assertEquals(1, resultTable.getSelectedRowCount());
    assertEquals(1, resultTable.getRowCount());
    ILookupRow<?> expectedLookupRow = resultTable.getSelectedLookupRow();
    tableProposalChooser.execResultTableRowClicked(resultTable.getRow(0));

    assertEquals("Yellow", m_form.getStyleField().getDisplayText());
    assertEquals(Long.valueOf(20L), m_form.getStyleField().getValue());
    assertNotNull(m_form.getStyleField().getCurrentLookupRow());
    assertEquals(expectedLookupRow, m_form.getStyleField().getCurrentLookupRow());
  }

  @Test
  public void testNoDefaultSelectionWithMoreThanOneResult() throws Exception {
    IBlockingCondition bc = Jobs.getJobManager().createBlockingCondition("loadProposals", true);

    m_form.getStyleField().getLookupRowFetcher().addPropertyChangeListener(getUnblockOnSearchResultListener(bc));
    m_form.getStyleField().getUIFacade().openProposalChooserFromUI("*", false);
    bc.waitFor();

    // select a proposal from the proposal chooser table
    assertTrue(m_form.getStyleField().isProposalChooserRegistered());
    TableProposalChooser<?> tableProposalChooser = (TableProposalChooser<?>) m_form.getStyleField().getProposalChooser();
    IContentAssistFieldTable<?> resultTable = tableProposalChooser.getModel();
    assertEquals(0, resultTable.getSelectedRowCount());
    assertTrue(resultTable.getRowCount() > 1);
  }

}
