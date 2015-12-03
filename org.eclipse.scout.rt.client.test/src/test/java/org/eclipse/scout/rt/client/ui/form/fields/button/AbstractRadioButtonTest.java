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
package org.eclipse.scout.rt.client.ui.form.fields.button;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractRadioButton}
 *
 * @since 4.0.0-M7
 */
@RunWith(PlatformTestRunner.class)
public class AbstractRadioButtonTest {

  AbstractRadioButton<Long> m_radioButton;

  @Before
  public void setUp() {
    m_radioButton = new P_RadioButton();
    m_radioButton.initConfig();
  }

  @Test
  public void testInitialize() {
    assertEquals("RadioButton", m_radioButton.getLabel());
    assertEquals(IButton.DISPLAY_STYLE_RADIO, m_radioButton.getConfiguredDisplayStyle());
    assertEquals(Long.valueOf(17), m_radioButton.getRadioValue());
  }

  @Test
  public void testRadioValue() {
    assertEquals(Long.valueOf(17), m_radioButton.getConfiguredRadioValue());
    assertEquals(Long.valueOf(17), m_radioButton.getRadioValue());
    m_radioButton.setRadioValue(Long.valueOf(34L));
    assertEquals(Long.valueOf(34), m_radioButton.getRadioValue());
  }

  private class P_RadioButton extends AbstractRadioButton<Long> {
    @Override
    protected String getConfiguredLabel() {
      return "RadioButton";
    }

    @Override
    protected Long getConfiguredRadioValue() {
      return Long.valueOf(17L);
    }
  }
}
