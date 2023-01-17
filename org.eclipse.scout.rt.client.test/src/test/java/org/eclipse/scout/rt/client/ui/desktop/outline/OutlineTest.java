/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Stack;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractOutline}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class OutlineTest {

  @Test
  public void testEvents() {
    AbstractOutline o = new AbstractOutline() {
    };
    final SavingTreeListener listener = new SavingTreeListener();
    o.addTreeListener(listener);
    o.setTreeChanging(true);
    o.fireBeforeDataLoaded(mock(IPage.class)); // unbuffered
    o.fireAfterDataLoaded(mock(IPage.class)); // unbuffered
    o.firePageChanged(mock(IPage.class));
    o.firePageChanged(mock(IPage.class));
    o.fireAfterTableInit(mock(IPage.class)); // unbuffered
    o.setTreeChanging(false);
    o.dispose(); // fires afterDispose event, is sent as buffered event but since tree isn't changing anymore no buffering is applied

    // check number and order of unbuffered events fired during the treeChanging period
    Stack<? extends TreeEvent> firedEvents = listener.getUnbufferedEvents();
    assertEquals(6, firedEvents.size());
    assertEquals(OutlineEvent.TYPE_PAGE_AFTER_DISPOSE, firedEvents.pop().getType());

    TreeEvent e1 = firedEvents.pop();
    assertEquals(OutlineEvent.TYPE_PAGE_CHANGED, e1.getType());
    TreeEvent e2 = firedEvents.pop();
    assertEquals(OutlineEvent.TYPE_PAGE_CHANGED, e2.getType());
    assertNotEquals(e1.getNode(), e2.getNode());

    assertEquals(OutlineEvent.TYPE_PAGE_AFTER_TABLE_INIT, listener.getUnbufferedEvents().pop().getType());
    assertEquals(OutlineEvent.TYPE_PAGE_AFTER_DATA_LOADED, listener.getUnbufferedEvents().pop().getType());
    assertEquals(OutlineEvent.TYPE_PAGE_BEFORE_DATA_LOADED, listener.getUnbufferedEvents().pop().getType());
  }

  private static class SavingTreeListener extends TreeAdapter {
    private Stack<TreeEvent> m_unbuffered = new Stack<>();

    @Override
    public void treeChanged(TreeEvent e) {
      // keep track of all unbuffered events.
      m_unbuffered.push(e);
    }

    public Stack<TreeEvent> getUnbufferedEvents() {
      return m_unbuffered;
    }
  }

  @Test
  public void testDefaultDetailForm() {
    TestOutline o = new TestOutline(true);

    assertNotNull("DefaultDetailForm was not instantiated", o.getDefaultDetailForm());
    assertTrue("DefaultDetailForm was not started", o.getDefaultDetailForm().isFormStarted());

    IForm form = o.getDefaultDetailForm();

    o.dispose();
    assertNull("DefaultDetailForm should be null", o.getDefaultDetailForm());
    assertTrue("DefaultDetailForm should have been closed", form.isFormClosed());
  }

  public static class TestOutline extends AbstractOutline {

    public TestOutline(boolean callInitializer) {
      super(true);
    }

    @Override
    protected Class<? extends IForm> getConfiguredDefaultDetailForm() {
      return TestForm.class;
    }
  }

  public static class TestForm extends AbstractForm {

    public TestForm() {
      super();
    }

    public TestForm(boolean callInitializer) {
      super(true);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
    }
  }
}
