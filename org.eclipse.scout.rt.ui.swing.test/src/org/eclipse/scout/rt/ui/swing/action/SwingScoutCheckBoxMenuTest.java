/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JCheckBoxMenuItem;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.AbstractCheckBoxMenu;
import org.eclipse.scout.rt.client.ui.action.menu.checkbox.ICheckBoxMenu;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SwingScoutCheckBoxMenuTest {

  private ISwingEnvironment m_env;

  private P_TestCheckBoxMenu m_scoutField;
  private SwingScoutCheckBoxMenu<ICheckBoxMenu> m_swingScoutField;
  private JCheckBoxMenuItem m_swingField;

  private BooleanHolder m_execSelectionChangedHolder;
  private BooleanHolder m_execToggleActionHolder;
  private AtomicInteger m_execActionCount;

  @Before
  public void before() {
    m_env = mock(ISwingEnvironment.class);

    // create the members to verify the calls to 'execAction' and 'execSelectionChanged'.
    m_execSelectionChangedHolder = new BooleanHolder(null);
    m_execToggleActionHolder = new BooleanHolder(null);
    m_execActionCount = new AtomicInteger();

    // make dispatching into Scout model thread synchronously.
    when(m_env.invokeScoutLater(any(Runnable.class), anyLong())).then(new Answer<Job>() {

      @Override
      public Job answer(InvocationOnMock invocation) throws Throwable {
        Runnable runnable = (Runnable) invocation.getArguments()[0];
        runnable.run();
        return null;
      }
    });

    // create model and widget.
    m_scoutField = new P_TestCheckBoxMenu();
    m_swingScoutField = new SwingScoutCheckBoxMenu<ICheckBoxMenu>();
    m_swingScoutField.createField(m_scoutField, m_env);
    m_swingField = m_swingScoutField.getSwingField();

    resetBooleanHolders();
  }

  @Test
  public void testClickEvent() throws InterruptedException {
    // Verify initial status.
    assertNull(m_execSelectionChangedHolder.getValue());
    assertNull(m_execToggleActionHolder.getValue());
    assertEquals(0, m_execActionCount.get());

    // Test 1: Check the menu item.
    m_swingField.doClick();
    assertTrue(m_execSelectionChangedHolder.getValue());
    assertTrue(m_execToggleActionHolder.getValue());
    assertEquals(1, m_execActionCount.get());

    resetBooleanHolders();

    // Test 2: Uncheck the menu item.
    m_swingField.doClick();
    assertFalse(m_execSelectionChangedHolder.getValue());
    assertFalse(m_execToggleActionHolder.getValue());
    assertEquals(2, m_execActionCount.get());
  }

  private void resetBooleanHolders() {
    m_execSelectionChangedHolder.setValue(null);
    m_execToggleActionHolder.setValue(null);
  }

  private class P_TestCheckBoxMenu extends AbstractCheckBoxMenu {

    @Override
    protected void execSelectionChanged(boolean selection) throws ProcessingException {
      m_execSelectionChangedHolder.setValue(selection);
    }

    @Override
    protected void execAction() throws ProcessingException {
      m_execActionCount.incrementAndGet();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void execToggleAction(boolean selected) throws ProcessingException {
      m_execToggleActionHolder.setValue(selected);
    }
  }
}
