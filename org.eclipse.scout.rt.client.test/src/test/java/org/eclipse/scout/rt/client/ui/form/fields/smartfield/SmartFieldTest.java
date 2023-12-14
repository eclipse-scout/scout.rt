/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.smartfield;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.SmartFieldTest.TestForm.MainBox.StyleField;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.ILookupService;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
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
 * <p>
 * Note: since Scout 7.0.300 and later the "styles" like fore-/background color are not copied automatically from the
 * selected lookup-row to the field. In earlier versions of this test the methods testStyle* checked if the styles have
 * been applied to the field, but now we only check if the right lookup-row is set. Styles must be applied manually in
 * the execChangedValue() method.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class SmartFieldTest {

  public static final String ICON_BOOKMARK = "bookmark";
  public static final String ICON_FILE = "file";

  private static List<IBean<?>> m_beans;
  private static final long ID_RED = 10L;
  private static final long ID_YELLOW = 20L;
  private static final long ID_GREEN = 30L;
  private static final long ID_BLUE = 40L;
  private static final long ID_EMPTY = 50L;

  protected TestForm m_form;
  private List<IBean<?>> m_reg;
  private StyleField m_styleField;

  @BeforeClass
  public static void beforeClass() {
    m_beans = BeanTestingHelper.get().registerBeans(new BeanMetaData(StyleLookupCall.class));
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(m_beans);
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
            return "TestMenu1";
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
            return "TestMenu2";
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

    private boolean m_allow = true;

    @PostConstruct
    protected void initializeService() {
      List<ILookupRow<Long>> rows = new ArrayList<>();
      rows.add(createRedLookupRow());
      rows.add(createYellowLookupRow());
      rows.add(new LookupRow<>(ID_GREEN, "Green")
          .withIconId(ICON_FILE)
          .withTooltipText("Green tooltip")
          .withBackgroundColor("88ff88")
          .withForegroundColor("008800")
          .withFont(FontSpec.parse("italic")));
      rows.add(new LookupRow<>(ID_BLUE, "Blue")
          .withIconId(ICON_FILE)
          .withTooltipText("Blue tooltip")
          .withBackgroundColor("8888ff")
          .withForegroundColor("000088")
          .withFont(FontSpec.parse("italic")));
      rows.add(new LookupRow<>(ID_EMPTY, "Empty"));
      setRows(rows);
    }

    public void allowLookup(boolean allow) {
      m_allow = allow;
    }

    protected void checkLookupAllowed() {
      if (!m_allow) {
        throw new AssertionError("No lookup should be performed for that test-case");
      }
    }

    @Override
    public List<ILookupRow<Long>> getDataByAll(ILookupCall<Long> call) {
      checkLookupAllowed();
      return super.getDataByAll(call);
    }

    @Override
    public List<ILookupRow<Long>> getDataByKey(ILookupCall<Long> call) {
      checkLookupAllowed();
      return super.getDataByKey(call);
    }

    @Override
    public List<ILookupRow<Long>> getDataByText(ILookupCall<Long> call) {
      checkLookupAllowed();
      return super.getDataByText(call);
    }

    @Override
    public List<ILookupRow<Long>> getDataByRec(ILookupCall<Long> call) {
      checkLookupAllowed();
      return super.getDataByRec(call);
    }

  }

  public static class StyleLookupCall extends LookupCall<Long> {
    private static final long serialVersionUID = 1L;

    @Override
    protected final Class<? extends ILookupService<Long>> getConfiguredService() {
      return StyleLookupService.class;
    }

    protected void allowLookup(boolean allow) {
      ((StyleLookupService) getLookupService()).allowLookup(allow);
    }
  }

  @Before
  public void setUp() {
    m_reg = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(StyleLookupService.class)
            .withApplicationScoped(true));
    m_form = new TestForm();
    m_form.startForm();
    m_styleField = m_form.getStyleField();
  }

  @Test
  public void testStyle_SetValue() {
    StyleField f = m_styleField;
    f.setValue(ID_RED);
    assertRedLookupRow(f);
    f.setValue(ID_EMPTY);
    assertLookupRow(f, ID_EMPTY);
    f.setValue(ID_YELLOW);
    assertYellowLookupRow(f);
    f.setValue(null);
    assertLookupRow(f, null);
  }

  private void assertLookupRow(StyleField f, Long expectedKey) {
    if (expectedKey == null) {
      assertNull(f.getLookupRow());
    }
    else {
      assertEquals(expectedKey, f.getLookupRow().getKey());
    }
  }

  private void assertYellowLookupRow(StyleField f) {
    assertLookupRow(f, ID_YELLOW);
  }

  private void assertRedLookupRow(StyleField f) {
    assertLookupRow(f, ID_RED);
  }

  private void assertGreenLookupRow(StyleField f) {
    assertLookupRow(f, ID_GREEN);
  }

  @Test
  public void testHierarchical_UIFacade() {
    StyleField f = m_styleField;
    f.setBrowseHierarchy(true);
    f.setValue(ID_RED);
    assertRedLookupRow(f);
    f.setValue(ID_EMPTY);
    assertLookupRow(f, ID_EMPTY);
    f.setValue(ID_YELLOW);
    assertYellowLookupRow(f);
    f.setValue(null);
    assertLookupRow(f, null);
  }

  @Test
  public void testHierarchicalBrowse() {
    StyleField f = m_styleField;
    f.setBrowseHierarchy(true);
    f.lookupByAll();
    waitForResult();

    ILookupCallResult result = f.getResult();
    @SuppressWarnings("unchecked")
    List<ILookupRow<?>> lookupRows = result.getLookupRows();
    assertEquals(5, lookupRows.size());
    assertEquals("Red", lookupRows.get(0).getText());
  }

  @Test
  public void testHierarchyNoResult() {
    StyleField f = m_styleField;
    f.setBrowseHierarchy(true);
    f.lookupByText("unknown");
    waitForResult();

    ILookupCallResult result = f.getResult();
    @SuppressWarnings("unchecked")
    List<ILookupRow<?>> lookupRows = result.getLookupRows();
    assertEquals(0, lookupRows.size());
  }

  @Test
  public void testThrowingLookupCall() {
    StyleField f = m_styleField;
    f.setBrowseHierarchy(true);
    ((StyleLookupCall) f.getLookupCall()).allowLookup(false);
    f.lookupByText("Red");
    waitForResult();

    ILookupCallResult result = f.getResult();
    @SuppressWarnings("unchecked")
    List<ILookupRow<?>> lookupRows = result.getLookupRows();
    assertEquals(0, lookupRows.size());
    assertNotNull(result.getException());
  }

  @Test
  public void testStyle_AcceptProposal() {
    StyleField f = m_styleField;
    f.getUIFacade().setLookupRowFromUI(createRedLookupRow());
    assertRedLookupRow(f);
    f.getUIFacade().setLookupRowFromUI(new LookupRow<>(ID_EMPTY, "Empty"));
    assertLookupRow(f, ID_EMPTY);
    f.getUIFacade().setLookupRowFromUI(createYellowLookupRow());
    assertYellowLookupRow(f);
    f.setValue(null);
    assertLookupRow(f, null);
  }

  private static ILookupRow<Long> createRedLookupRow() {
    return new LookupRow<>(ID_RED, "Red")
        .withIconId(ICON_FILE)
        .withTooltipText("Red tooltip")
        .withBackgroundColor("ff8888")
        .withForegroundColor("880000")
        .withFont(FontSpec.parse("italic"));
  }

  private static ILookupRow<Long> createYellowLookupRow() {
    return new LookupRow<>(ID_YELLOW, "Yellow")
        .withIconId(ICON_FILE)
        .withTooltipText("Yellow tooltip")
        .withBackgroundColor("ffff88")
        .withForegroundColor("888800")
        .withFont(FontSpec.parse("italic"));
  }

  /**
   * Only perform a lookup when its necessary. When user has selected a lookup row from the proposal-chooser no lookup
   * should be performed.
   */
  @Test
  public void testLookupOnlyWhenNecessary() {
    StyleField f = m_styleField;
    StyleLookupCall lookupCall = (StyleLookupCall) f.getLookupCall();
    // expect lookup is performed
    lookupCall.allowLookup(true);
    f.setValue(ID_RED);
    assertRedLookupRow(f);

    // expect lookup is not performed
    lookupCall.allowLookup(false);
    f.getUIFacade().setLookupRowFromUI(createYellowLookupRow());
    assertYellowLookupRow(f);

    // expect lookup is performed again
    // additionally this test ensures that the lookup-row from the previous test (value=20L)
    // is not reused by mistake.
    lookupCall.allowLookup(true);
    f.setValue(ID_GREEN);
    assertGreenLookupRow(f);
  }

  @After
  public void tearDown() {
    BeanTestingHelper.get().unregisterBeans(m_reg);
    m_form.doClose();
  }

  @Test
  public void testSmartfieldMenus() {
    List<IMenu> smartfieldMenus = m_styleField.getMenus();
    assertEquals("Smartfield should have 2 menus", 2, smartfieldMenus.size());
    assertEquals("TestMenu1", smartfieldMenus.get(0).getText());
    assertEquals("alternate-2", smartfieldMenus.get(0).getKeyStroke());

    assertEquals("TestMenu2", smartfieldMenus.get(1).getText());
    assertEquals("control-alternate-f11", smartfieldMenus.get(1).getKeyStroke());
  }

  /**
   * Test for ticket #168652.
   */
  @Test
  public void testDeleteProposal() {
    m_styleField.setValue(ID_RED);
    m_styleField.lookupByAll();
    waitUntilLookupRowsLoaded();
    m_styleField.getUIFacade().setLookupRowFromUI(null);
    assertNull(m_styleField.getValue());
  }

  /**
   * Waits for at most 30s until lookup rows are loaded.
   */
  private static void waitUntilLookupRowsLoaded() {
    Assertions.assertTrue(ModelJobs.isModelThread(), "must be invoked from model thread");

    // Wait until asynchronous load of lookup rows is completed and ready to be written back to the smart field.
    JobTestUtil.waitForMinimalPermitCompetitors(ModelJobs.newInput(ClientRunContexts.copyCurrent()).getExecutionSemaphore(), 2); // 2:= 'current model job' + 'smartfield fetch model job'
    // Yield the current model job permit, so that the lookup rows can be written into the model.
    ModelJobs.yield();
  }

  private void waitForResult() {
    final IBlockingCondition bc = Jobs.newBlockingCondition(true);

    m_styleField.getLookupRowFetcher().addPropertyChangeListener(evt -> {
      if (ISmartFieldLookupRowFetcher.PROP_SEARCH_RESULT.equals(evt.getPropertyName())) {
        bc.setBlocking(false);
      }
    });
    bc.waitFor();
  }

  @Test
  public void testRefreshDisplayText() {
    final StringHolder TEXT_HOLDER = new StringHolder("AAA");

    ISmartField<Long> field = new AbstractSmartField<Long>() {
    };
    field.setLookupCall(new LocalLookupCall<Long>() {
      private static final long serialVersionUID = 1L;

      @Override
      protected List<? extends ILookupRow<Long>> execCreateLookupRows() {
        List<ILookupRow<Long>> rows = new ArrayList<>();
        rows.add(new LookupRow<>(1001L, "Lalala"));
        rows.add(new LookupRow<>(1002L, TEXT_HOLDER.getValue()));
        return rows;
      }
    });

    assertEquals(null, field.getValue());
    assertEquals("", field.getDisplayText());

    field.setValue(1001L);
    assertEquals("Lalala", field.getDisplayText());

    field.setValue(1002L);
    assertEquals("AAA", field.getDisplayText());

    // Change holder --> lookup call will return a different text for the same key
    TEXT_HOLDER.setValue("BBB");
    field.refreshDisplayText(); // <-- required to reload the display text
    assertEquals("BBB", field.getDisplayText());

    // Check that it works with null as well
    field.setValue(null);
    assertEquals("", field.getDisplayText());
    field.refreshDisplayText();
    assertEquals("", field.getDisplayText());
  }

  @Test
  public void testInitActiveFilter() {
    ISmartField<Long> field = new AbstractSmartField<Long>() {
      @Override
      protected boolean getConfiguredActiveFilterEnabled() {
        return true;
      }
    };
    field.setActiveFilter(TriState.FALSE);
    field.markSaved();
    field.setActiveFilter(TriState.UNDEFINED);
    assertEquals(TriState.UNDEFINED, field.getActiveFilter());
    field.resetValue();
    assertEquals(TriState.FALSE, field.getActiveFilter());
  }
}
