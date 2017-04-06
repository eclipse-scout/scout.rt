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
package org.eclipse.scout.rt.client.ui.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.scout.rt.client.deeplink.AbstractDeepLinkHandler;
import org.eclipse.scout.rt.client.deeplink.DeepLinkException;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.testenvironment.ui.desktop.TestEnvironmentDesktop;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktopTest.P_CheckSaveTestForm.MainBox.MessageField;
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
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

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
    Mockito.when(node.getChildNodes()).thenReturn(Collections.<ITreeNode> emptyList());

    m_outline = Mockito.mock(IOutline.class);
    Mockito.when(m_outline.getActivePage()).thenReturn(null);
    Mockito.when(m_outline.getRootNode()).thenReturn(node);

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
    m_desktop.addDesktopListener(new DesktopListener() {
      @Override
      public void desktopChanged(DesktopEvent e) {
        if (DesktopEvent.TYPE_OUTLINE_CHANGED == e.getType()) {
          called[0] = true;
          assertTrue(m_desktop.isOutlineChanging());
        }
      }
    });
    m_desktop.setOutline(m_outline);
    assertTrue(called[0]);
    assertFalse(m_desktop.isOutlineChanging());
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
    System.out.println("test");
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
  public void testDataChangedSimple() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();

    final Holder<Object[]> resultHolder = new Holder<Object[]>(Object[].class);
    desktop.addDataChangeListener(new DataChangeListener() {

      @Override
      public void dataChanged(Object... dataTypes) {
        resultHolder.setValue(dataTypes);
      }
    }, TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    verifyDataChanged(resultHolder);
  }

  @Test
  public void testDataChangedChanging() {
    TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();

    final Holder<Object[]> resultHolder = new Holder<Object[]>(Object[].class);
    desktop.addDataChangeListener(new DataChangeListener() {

      @Override
      public void dataChanged(Object... dataTypes) {
        resultHolder.setValue(dataTypes);
      }
    }, TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);

    desktop.setDataChanging(true);
    desktop.dataChanged(TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_1, TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_2, TEST_DATA_TYPE_2);
    desktop.dataChanged(TEST_DATA_TYPE_1, TEST_DATA_TYPE_2);
    desktop.dataChanged(TEST_DATA_TYPE_1);
    desktop.dataChanged(TEST_DATA_TYPE_2);
    desktop.setDataChanging(false);
    verifyDataChanged(resultHolder);
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

  protected void verifyDataChanged(Holder<Object[]> resultHolder) {
    Object[] result = resultHolder.getValue();
    assertTrue(result.length == 2);
    assertTrue(result[0] == TEST_DATA_TYPE_1 && result[1] == TEST_DATA_TYPE_2
        || result[0] == TEST_DATA_TYPE_2 && result[1] == TEST_DATA_TYPE_1);
  }

  @Test
  public void testDeepLinkHandling() throws Throwable {
    IBean<P_TestDeepLinkHandler> reg = BEANS.getBeanManager().registerClass(P_TestDeepLinkHandler.class);
    try {
      TestEnvironmentDesktop desktop = (TestEnvironmentDesktop) IDesktop.CURRENT.get();
      assertFalse(desktop.handleDeepLink(null));
      assertTrue(desktop.handleDeepLink("junittest-ok"));
      assertDeepLinkException(desktop, "doesnotexist");
      assertDeepLinkException(desktop, "doesnotexist-123");
      assertDeepLinkException(desktop, "junittest");
      assertDeepLinkException(desktop, "junittest-123");
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
