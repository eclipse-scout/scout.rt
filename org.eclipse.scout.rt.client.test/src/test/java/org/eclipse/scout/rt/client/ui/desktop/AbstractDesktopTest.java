/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop;

import static java.util.Collections.emptySet;
import static org.eclipse.scout.rt.platform.util.CollectionUtility.hashSet;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.deeplink.AbstractDeepLinkHandler;
import org.eclipse.scout.rt.client.deeplink.DeepLinkException;
import org.eclipse.scout.rt.client.deeplink.DeepLinks;
import org.eclipse.scout.rt.client.deeplink.IDeepLinkHandler;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.testenvironment.ui.desktop.TestEnvironmentDesktop;
import org.eclipse.scout.rt.client.ui.Coordinates;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopTest.P_CheckSaveTestForm.MainBox.MessageField;
import org.eclipse.scout.rt.client.ui.desktop.hybrid.BrowserCallbacks;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractDesktopTest {

  private static final Object TEST_DATA_TYPE_1 = new Object();
  private static final Object TEST_DATA_TYPE_2 = new Object();

  private IOutline m_outline;
  private AbstractDesktop m_desktop;

  private void prepareMockDesktopWithOutline() {
    ITreeNode node = Mockito.mock(ITreeNode.class);
    Mockito.when(node.getChildNodes()).thenReturn(Collections.emptyList());

    m_outline = Mockito.mock(IOutline.class);
    Mockito.when(m_outline.getActivePage()).thenReturn(null);
    Mockito.when(m_outline.getRootNode()).thenReturn(node);
    Mockito.when(m_outline.createDisplayParentRunContext()).thenReturn(ClientRunContexts.copyCurrent().withOutline(m_outline, true));

    m_desktop = new AbstractDesktop(false) {
      @Override
      public List<IOutline> getAvailableOutlines() {
        return Collections.singletonList(m_outline);
      }
    };
  }

  @Test
  public void testIsOutlineChanging_Default() {
    prepareMockDesktopWithOutline();
    assertFalse(m_desktop.isOutlineChanging());
  }

  /**
   * Tests whether the outlineChanged flag is set to true while setOutline() is running.
   */
  @Test
  public void testIsOutlineChanging_setOutline() {
    prepareMockDesktopWithOutline();
    final boolean[] called = {false};
    assertFalse(m_desktop.isOutlineChanging());
    m_desktop.addDesktopListener(e -> {
      if (DesktopEvent.TYPE_OUTLINE_CHANGED == e.getType()) {
        called[0] = true;
        assertTrue(m_desktop.isOutlineChanging());
      }
    });
    m_desktop.activateOutline(m_outline);
    assertTrue(called[0]);
    assertFalse(m_desktop.isOutlineChanging());
  }

  @Test
  public void testDisplayStyle() {
    IDesktop desktop;

    // 1. DISPLAY_STYLE_DEFAULT -> check defaults
    desktop = new AbstractDesktop() {

      @Override
      protected String getConfiguredDisplayStyle() {
        return IDesktop.DISPLAY_STYLE_DEFAULT;
      }
    };
    assertTrue(desktop.isNavigationVisible());
    assertTrue(desktop.isNavigationHandleVisible());
    assertTrue(desktop.isHeaderVisible());
    assertTrue(desktop.isBenchVisible());

    // 2. DISPLAY_STYLE_DEFAULT -> check that getConfigured... values are applied
    desktop = new AbstractDesktop() {

      @Override
      protected String getConfiguredDisplayStyle() {
        return IDesktop.DISPLAY_STYLE_DEFAULT;
      }

      @Override
      protected boolean getConfiguredNavigationVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredNavigationHandleVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredHeaderVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredBenchVisible() {
        return false;
      }
    };
    assertFalse(desktop.isNavigationVisible());
    assertFalse(desktop.isNavigationHandleVisible());
    assertFalse(desktop.isHeaderVisible());
    assertFalse(desktop.isBenchVisible());

    // 3. DISPLAY_STYLE_BENCH -> check that getConfigured... values are ignored
    desktop = new AbstractDesktop() {

      @Override
      protected String getConfiguredDisplayStyle() {
        return IDesktop.DISPLAY_STYLE_BENCH;
      }

      @Override
      protected boolean getConfiguredNavigationVisible() {
        return true;
      }

      @Override
      protected boolean getConfiguredNavigationHandleVisible() {
        return true;
      }

      @Override
      protected boolean getConfiguredHeaderVisible() {
        return true;
      }

      @Override
      protected boolean getConfiguredBenchVisible() {
        return false;
      }
    };
    assertFalse(desktop.isNavigationVisible());
    assertFalse(desktop.isNavigationHandleVisible());
    assertFalse(desktop.isHeaderVisible());
    assertTrue(desktop.isBenchVisible());

    // 4. DISPLAY_STYLE_COMPACT -> check that getConfigured... values are ignored
    desktop = new AbstractDesktop() {

      @Override
      protected String getConfiguredDisplayStyle() {
        return IDesktop.DISPLAY_STYLE_COMPACT;
      }

      @Override
      protected boolean getConfiguredNavigationVisible() {
        return false;
      }

      @Override
      protected boolean getConfiguredNavigationHandleVisible() {
        return true;
      }

      @Override
      protected boolean getConfiguredHeaderVisible() {
        return true;
      }

      @Override
      protected boolean getConfiguredBenchVisible() {
        return true;
      }
    };
    assertTrue(desktop.isNavigationVisible());
    assertFalse(desktop.isNavigationHandleVisible());
    assertFalse(desktop.isHeaderVisible());
    assertFalse(desktop.isBenchVisible());
  }

  /**
   * {@link AbstractDesktop#doBeforeClosingInternal()}
   */
  @Test
  @RunWithClientSession(value = TestEnvironmentClientSession.class, provider = ClientSessionProvider.class) // ensures that this test runs with its own clean desktop
  public void testClosingDoBeforeClosingInternal() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();

    boolean closing = desktop.doBeforeClosingInternal();
    assertTrue(closing);
  }

  @Test
  public void testNoSaveNeeded() {
    P_CheckSaveTestForm testForm = new P_CheckSaveTestForm();
    try {
      testForm.startNew();
      assertFalse(testForm.isSaveNeeded());
    }
    finally {
      testForm.doClose();
    }
  }

  @Test
  public void testSaveNeeded() {
    P_CheckSaveTestForm testForm = new P_CheckSaveTestForm();
    try {
      testForm.startNew();
      testForm.getMessageField().setValue("test");
      assertTrue(testForm.isSaveNeeded());
    }
    finally {
      testForm.doClose();
    }
  }

  @Test
  public void testUnsavedForms() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();

    P_CheckSaveTestForm testForm = new P_CheckSaveTestForm();
    try {
      testForm.startNew();
      testForm.getMessageField().setValue("test");
      assertTrue(desktop.getUnsavedForms().contains(testForm));
    }
    finally {
      testForm.doClose();
    }
  }

  @Test
  public void testUnsavedFormsWithFormSet() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();

    P_CheckSaveTestForm form_0 = new P_CheckSaveTestForm();
    P_CheckSaveTestForm form_1 = new P_CheckSaveTestForm();
    P_CheckSaveTestForm form_1_1 = new P_CheckSaveTestForm();
    P_CheckSaveTestForm form_1_2 = new P_CheckSaveTestForm();
    P_CheckSaveTestForm form_2 = new P_CheckSaveTestForm();
    P_CheckSaveTestForm form_2_1 = new P_CheckSaveTestForm();
    try {
      form_0.startNew();
      form_0.getMessageField().setValue("test");
      assertTrue(desktop.getUnsavedForms().contains(form_0));

      form_1.startNew();
      assertTrue(desktop.getUnsavedForms(CollectionUtility.hashSet(form_1)).isEmpty());
      form_1.getMessageField().setValue("test");
      assertTrue(desktop.getUnsavedForms(CollectionUtility.hashSet(form_1)).contains(form_1));

      form_2.startNew();
      assertTrue(desktop.getUnsavedForms(CollectionUtility.hashSet(form_1, form_2)).contains(form_1));

      form_2_1.setDisplayParent(form_2);
      form_2_1.startNew();
      assertTrue(desktop.getUnsavedForms(CollectionUtility.hashSet(form_1, form_2)).contains(form_1));
      form_2_1.getMessageField().setValue("test");
      assertTrue(desktop.getUnsavedForms(CollectionUtility.hashSet(form_1, form_2)).containsAll(CollectionUtility.hashSet(form_1, form_2_1)));

      form_1_1.setDisplayParent(form_1);
      form_1_1.startNew();
      assertTrue(desktop.getUnsavedForms(CollectionUtility.hashSet(form_1, form_2)).containsAll(CollectionUtility.hashSet(form_1, form_2_1)));
      form_1_1.getMessageField().setValue("test");
      assertTrue(desktop.getUnsavedForms(CollectionUtility.hashSet(form_1, form_2)).containsAll(CollectionUtility.hashSet(form_1, form_2_1, form_1_1)));

      form_1_2.setDisplayParent(form_1);
      form_1_2.startNew();
      assertTrue(desktop.getUnsavedForms(CollectionUtility.hashSet(form_1, form_2)).containsAll(CollectionUtility.hashSet(form_1, form_2_1, form_1_1)));
      form_1_2.getMessageField().setValue("test");
      assertTrue(desktop.getUnsavedForms(CollectionUtility.hashSet(form_1, form_2)).containsAll(CollectionUtility.hashSet(form_1, form_2_1, form_1_1, form_1_2)));
    }
    finally {
      form_0.doClose();
      form_1.doClose();
      form_1_1.doClose();
      form_1_2.doClose();
      form_2.doClose();
      form_2_1.doClose();
    }
  }

  @Test
  public void testUnsavedFormsChangesForm() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();

    P_CheckSaveTestForm form_1 = new P_CheckSaveTestForm();
    P_CheckSaveTestForm form_1_1 = new P_CheckSaveTestForm();
    P_CheckSaveTestForm form_1_1_1 = new P_CheckSaveTestForm();

    try {
      form_1.startNew();
      form_1.getMessageField().setValue("test");
      form_1_1.setDisplayParent(form_1);
      form_1_1.startNew();
      form_1_1.getMessageField().setValue("test");
      form_1_1_1.setDisplayParent(form_1_1);
      form_1_1_1.startNew();
      form_1_1_1.getMessageField().setValue("test");

      List<IForm> unsavedForms = desktop.getUnsavedForms(CollectionUtility.hashSet(form_1));
      assertTrue(unsavedForms.containsAll(CollectionUtility.hashSet(form_1, form_1_1, form_1_1_1)));
      UnsavedFormChangesForm f = new UnsavedFormChangesForm(unsavedForms, false);
      f.setHandler(f.new NewHandler());
      f.start();

      // the openForms listBox should only have one (checked) entry for the top form (form_1)
      assertEquals(f.getOpenFormsField().getValue().size(), 1);
      assertEquals(f.getOpenFormsField().getCheckedKeyCount(), 1);
      // the value of that single entry should be all the forms in the displayParent hierarchy with unsaved changes
      assertTrue(CollectionUtility.firstElement(f.getOpenFormsField().getValue()).containsAll(CollectionUtility.hashSet(form_1, form_1_1, form_1_1_1)));
    }
    finally {
      form_1.doClose();
      form_1_1.doClose();
      form_1_1_1.doClose();
    }
  }

  @Test
  public void testDataChangedSimple() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();

    final HashSet<Object> resultHolder = new HashSet<>();
    desktop.addDataChangeListener(e -> resultHolder.add(e.getDataType()), TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    assertEquals(CollectionUtility.hashSet(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2), resultHolder);
  }

  @Test
  public void testDataChangedSimpleDesktopInForeground() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();
    // move desktop to background (actually performed by the UI-layer only, but for testing purposes done manually)
    desktop.setInBackground(true);

    final Set<Object> types = new HashSet<>();
    final Set<Object> desktopInForegroundTypes = new HashSet<>();
    desktop.addDataChangeListener(e -> types.add(e.getDataType()), TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);
    desktop.addDataChangeDesktopInForegroundListener(e -> desktopInForegroundTypes.add(e.getDataType()), TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);
    assertEquals(hashSet(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2), types);
    assertEquals(emptySet(), desktopInForegroundTypes);

    // move desktop to foreground -> foreground listener is expected to be notified
    types.clear();
    desktop.setInBackground(false);
    assertEquals(emptySet(), types);
    assertEquals(hashSet(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2), desktopInForegroundTypes);
  }

  @Test
  public void testDataChangedChanging() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();

    final HashSet<Object> resultHolder = new HashSet<>();
    desktop.addDataChangeListener(e -> resultHolder.add(e.getDataType()), TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    desktop.setDataChanging(true);
    desktop.dataChanged(TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_1, TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_2, TEST_DATA_TYPE_2);
    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);
    desktop.dataChanged(TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_2);
    desktop.setDataChanging(false);
    assertEquals(CollectionUtility.hashSet(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2), resultHolder);
  }

  @Test
  public void testDataChangedChangingDesktopInForeground() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();
    // move desktop to background (actually performed by the UI-layer only, but for testing purposes done manually)
    desktop.setInBackground(true);

    final Set<Object> types = new HashSet<>();
    final Set<Object> desktopInForegroundTypes = new HashSet<>();
    desktop.addDataChangeListener(e -> types.add(e.getDataType()), TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);
    desktop.addDataChangeDesktopInForegroundListener(e -> desktopInForegroundTypes.add(e.getDataType()), TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    desktop.setDataChanging(true);
    desktop.dataChanged(TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_1, TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_2, TEST_DATA_TYPE_2);
    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);
    desktop.dataChanged(TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_2);
    desktop.setDataChanging(false);
    assertEquals(hashSet(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2), types);
    assertEquals(emptySet(), desktopInForegroundTypes);

    // move desktop to foreground -> foreground listener is expected to be notified
    types.clear();
    desktop.setInBackground(false);
    assertEquals(emptySet(), types);
    assertEquals(hashSet(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2), desktopInForegroundTypes);
  }

  @Test
  public void testGetDialogs() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();

    //                       form
    //        _________________|___________________________
    //       |                 |                          |
    //     form_1            form_2                     form_3
    //                  _______|________________          |
    //                 |           |            |      form_3_1
    //               form_2_1    form_2_2    form_2_3
    //       __________|_____                   |
    //       |               |              form_2_3_1
    //  form_2_1_1        form_2_1_2
    //                       |
    //                    form_2_1_2_1

    P_Form form = new P_Form("form");
    form.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form.setDisplayParent(desktop);
    form.start();

    P_Form form_1 = new P_Form("form_1");
    form_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_1.setDisplayParent(form);
    form_1.start();

    P_Form form_2 = new P_Form("form_2");
    form_2.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2.setDisplayParent(form);
    form_2.start();

    P_Form form_3 = new P_Form("form_3");
    form_3.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_3.setDisplayParent(form);
    form_3.start();

    P_Form form_2_1 = new P_Form("form_2_1");
    form_2_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_1.setDisplayParent(form_2);
    form_2_1.start();

    P_Form form_2_2 = new P_Form("form_2_2");
    form_2_2.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_2.setDisplayParent(form_2);
    form_2_2.start();

    P_Form form_2_3 = new P_Form("form_2_3");
    form_2_3.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_3.setDisplayParent(form_2);
    form_2_3.start();

    P_Form form_2_3_1 = new P_Form("form_2_3_1");
    form_2_3_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_3_1.setDisplayParent(form_2_3);
    form_2_3_1.start();

    P_Form form_3_1 = new P_Form("form_3_1");
    form_3_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_3_1.setDisplayParent(form_3);
    form_3_1.start();

    P_Form form_2_1_1 = new P_Form("form_2_1_1");
    form_2_1_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_1_1.setDisplayParent(form_2_1);
    form_2_1_1.start();

    P_Form form_2_1_2 = new P_Form("form_2_1_2");
    form_2_1_2.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_1_2.setDisplayParent(form_2_1);
    form_2_1_2.start();

    P_Form form_2_1_2_1 = new P_Form("form_2_1_2_1");
    form_2_1_2_1.setDisplayHint(IForm.DISPLAY_HINT_DIALOG);
    form_2_1_2_1.setDisplayParent(form_2_1_2);
    form_2_1_2_1.start();

    assertEquals(CollectionUtility.arrayList(form_1, form_2, form_3), desktop.getDialogs(form, false));
    assertEquals(CollectionUtility.arrayList(form_1, form_2_1_1, form_2_1_2_1, form_2_1_2, form_2_1, form_2_2, form_2_3_1, form_2_3, form_2, form_3_1, form_3), desktop.getDialogs(form, true));
  }

  @Test
  public void testGeolocation() throws InterruptedException, ExecutionException {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();
    Future<Coordinates> coordinatesFuture = desktop.requestGeolocation();
    GeoLocationResponseDo browserResponse = BEANS.get(GeoLocationResponseDo.class).withLatitude("1.0").withLongitude("2.0");
    BrowserCallbacks.get().getUIFacade().fireCallbackDone("requestGeolocation", browserResponse);
    Coordinates coordinates = coordinatesFuture.get();
    assertEquals("1.0", coordinates.getLatitude());
    assertEquals("2.0", coordinates.getLongitude());
  }

  @Test(expected = ProcessingException.class)
  public void testGeolocationFailure() throws Throwable {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();
    Future<Coordinates> coordinatesFuture = desktop.requestGeolocation();
    BrowserCallbacks.get().getUIFacade().fireCallbackFailed("requestGeolocation", "test failure", "-1");
    try {
      coordinatesFuture.get();
    }
    catch (ExecutionException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testDeepLinkHandling() throws Throwable {
    IBean<P_TestDeepLinks> reg = BEANS.getBeanManager().registerClass(P_TestDeepLinks.class);
    try {
      // Create a new desktop, because the IDesktop.CURRENT might be in a state where deep links are not handled (e.g. form open)
      final TestEnvironmentDesktop desktop = new TestEnvironmentDesktop();
      assertFalse(desktop.handleDeepLink(null));
      assertTrue(desktop.handleDeepLink("junittest-ok"));
      assertDeepLinkException(desktop, "junittest");
      assertDeepLinkException(desktop, "junittest-123");
      assertDeepLinkException(desktop, "doesnotexist");
      assertDeepLinkException(desktop, "doesnotexist-123");
    }
    finally {
      BEANS.getBeanManager().unregisterBean(reg);
    }
  }

  protected static void assertDeepLinkException(AbstractDesktop desktop, String deepLinkPath) {
    try {
      boolean handled = desktop.handleDeepLink(deepLinkPath);
      fail("Expected DeepLinkException, but '" + deepLinkPath + "' was " + (handled ? "" : "not ") + "handled");
    }
    catch (DeepLinkException e) { // NOSONAR
      // expected
    }
  }

  @Test
  public void testInit() {
    prepareMockDesktopWithOutline();

    m_desktop = new AbstractDesktop(true) {
      @Override
      public List<IOutline> getAvailableOutlines() {
        return Collections.singletonList(m_outline);
      }

      @Override
      protected void initConfig() {
        assertEquals(IDesktop.CURRENT.get(), this); // initConfig must be called within run context with reference to currently initializing desktop
        super.initConfig();
      }
    };

    Mockito.doAnswer((Answer<Void>) invocation -> {
      assertEquals(IDesktop.CURRENT.get(), m_desktop); // init must be called within run context with reference to currently initializing desktop
      return null;
    }).when(m_outline).init();

    m_desktop.init();
  }

  @ClassId("d090cc19-ba7a-4f79-b147-e58765a837fb")
  protected class P_CheckSaveTestForm extends AbstractForm {

    public P_CheckSaveTestForm() {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return TEXTS.get("AskIfSaveNeededForm");
    }

    public void startNew() {
      startInternal(new NewHandler());
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    public MessageField getMessageField() {
      return getFieldByClass(MessageField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class MessageField extends AbstractStringField {
      }

      @Order(20)
      public class OkButton extends AbstractOkButton {
      }

      @Order(30)
      public class CancelButton extends AbstractCancelButton {
      }
    }

    public class NewHandler extends AbstractFormHandler {
    }
  }

  protected class P_Form extends AbstractForm {

    private String m_identifier;

    public P_Form(String identifier) {
      m_identifier = identifier;
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
    }

    @Override
    public String toString() {
      return m_identifier;
    }
  }

  @IgnoreBean
  @Replace
  protected static class P_TestDeepLinks extends DeepLinks {

    @Override
    protected void collectDeepLinkHandlers(List<IDeepLinkHandler> handlers) {
      handlers.add(new P_TestDeepLinkHandler());
    }
  }

  @IgnoreBean
  protected static class P_TestDeepLinkHandler extends AbstractDeepLinkHandler {

    private static final String HANDLER_NAME = "junittest";

    public P_TestDeepLinkHandler() {
      super(defaultPattern(HANDLER_NAME, "[A-Za-z0-9_]+"));
    }

    @Override
    public String getName() {
      return HANDLER_NAME;
    }

    @Override
    protected void handleImpl(Matcher matcher) throws DeepLinkException {
      String target = matcher.group(1);
      if (StringUtility.equalsIgnoreCase(target, "ok")) {
        return; // ok, simulate "handled"
      }
      throw new DeepLinkException("Unsupported target: " + target);
    }
  }
}
