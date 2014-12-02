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
package org.eclipse.scout.rt.ui.swing.form.fields.button;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractButton;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SwingScoutRadioButtonTest {
  private ISwingEnvironment m_env;

  private P_TestRadioButton m_scoutField;
  private SwingScoutRadioButton m_swingScoutField;
  private AbstractButton m_swingField;

  private BooleanHolder m_execSelectionChangedHolder;
  private BooleanHolder m_execToggleActionHolder;
  private AtomicInteger m_execClickActionCount;

  @Before
  public void before() {
    m_env = mock(ISwingEnvironment.class);

    // create the members to verify the calls to 'execAction' and 'execSelectionChanged'.
    m_execSelectionChangedHolder = new BooleanHolder(null);
    m_execToggleActionHolder = new BooleanHolder(null);
    m_execClickActionCount = new AtomicInteger();

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
    m_scoutField = new P_TestRadioButton();
    m_swingScoutField = new SwingScoutRadioButton();
    m_swingScoutField.createField(m_scoutField, m_env);
    m_swingField = m_swingScoutField.getSwingButton();

    resetBooleanHolders();
  }

  @Test
  public void testClickEvent() throws InterruptedException {
    // Verify initial status.
    assertNull(m_execSelectionChangedHolder.getValue());
    assertNull(m_execToggleActionHolder.getValue());
    assertEquals(0, m_execClickActionCount.get());

    // Test 1: Check the radio button.
    m_swingField.doClick();
    assertTrue(m_execSelectionChangedHolder.getValue().booleanValue());
    assertTrue(m_execToggleActionHolder.getValue().booleanValue());
    assertEquals(1, m_execClickActionCount.get());

    resetBooleanHolders();

    // Test 2: Check the radio button again.
    m_swingField.doClick();
    assertNull(m_execSelectionChangedHolder.getValue()); // change action is not called again.
    assertNull(m_execToggleActionHolder.getValue()); // change action is not called again.
    assertEquals(2, m_execClickActionCount.get()); // action count is incremented
  }

  private void resetBooleanHolders() {
    m_execSelectionChangedHolder.setValue(null);
    m_execToggleActionHolder.setValue(null);
  }

  private class P_TestRadioButton extends AbstractRadioButton<Long> {

    @Override
    protected void execSelectionChanged(boolean selection) throws ProcessingException {
      m_execSelectionChangedHolder.setValue(selection);
    }

    @Override
    protected void execClickAction() throws ProcessingException {
      m_execClickActionCount.incrementAndGet();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void execToggleAction(boolean selected) throws ProcessingException {
      m_execToggleActionHolder.setValue(selected);
    }
  }
}
