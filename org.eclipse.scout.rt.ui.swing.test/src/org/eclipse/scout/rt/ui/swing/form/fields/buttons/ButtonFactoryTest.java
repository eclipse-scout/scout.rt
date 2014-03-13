/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.form.fields.buttons;

import org.easymock.EasyMock;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractRadioButton;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.form.fields.ISwingScoutFormField;
import org.eclipse.scout.rt.ui.swing.form.fields.button.ButtonFactory;
import org.eclipse.scout.rt.ui.swing.form.fields.button.SwingScoutButton;
import org.eclipse.scout.rt.ui.swing.form.fields.button.SwingScoutLink;
import org.eclipse.scout.rt.ui.swing.form.fields.button.SwingScoutRadioButton;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ButtonFactory}
 *
 * @since 4.0.0-M7
 */
public class ButtonFactoryTest {

  private ISwingEnvironment m_swingEnvironment;
  private ButtonFactory m_factory;

  @Before
  public void setup() {
    m_factory = new ButtonFactory();
    m_swingEnvironment = EasyMock.createNiceMock(ISwingEnvironment.class);
  }

  @Test
  public void testRadioButton() {
    IFormField button = new P_RadioButton();
    ISwingScoutFormField<?> res = m_factory.createFormField(null, button, m_swingEnvironment);
    Assert.assertTrue(res instanceof SwingScoutRadioButton);
  }

  @Test
  public void testRadioButtonOld() {
    IFormField button = new P_RadioButtonOld();
    ISwingScoutFormField<?> res = m_factory.createFormField(null, button, m_swingEnvironment);
    Assert.assertTrue(res instanceof SwingScoutButton);
  }

  @Test
  public void testPushButton() {
    IFormField button = new P_PushButton();
    ISwingScoutFormField<?> res = m_factory.createFormField(null, button, m_swingEnvironment);
    Assert.assertTrue(res instanceof SwingScoutButton);
  }

  @Test
  public void testLink() {
    IFormField button = new P_Link();
    ISwingScoutFormField<?> res = m_factory.createFormField(null, button, m_swingEnvironment);
    Assert.assertTrue(res instanceof SwingScoutLink);
  }

  @Test
  public void testToggle() {
    IFormField button = new P_ToggleButton();
    ISwingScoutFormField<?> res = m_factory.createFormField(null, button, m_swingEnvironment);
    Assert.assertTrue(res instanceof SwingScoutButton);
  }

  @Test
  public void testNoButton() {
    IFormField label = new AbstractLabelField() {
    };
    ISwingScoutFormField<?> res = m_factory.createFormField(null, label, m_swingEnvironment);
    Assert.assertNull(res);
  }

  private class P_RadioButton extends AbstractRadioButton<Long> {

  }

  private class P_PushButton extends AbstractButton {
    @Override
    protected int getConfiguredDisplayStyle() {
      return DISPLAY_STYLE_DEFAULT;
    }
  }

  private class P_ToggleButton extends AbstractButton {
    @Override
    protected int getConfiguredDisplayStyle() {
      return DISPLAY_STYLE_TOGGLE;
    }
  }

  private class P_Link extends AbstractButton {
    @Override
    protected int getConfiguredDisplayStyle() {
      return DISPLAY_STYLE_LINK;
    }
  }

  private class P_RadioButtonOld extends AbstractButton {
    @Override
    protected int getConfiguredDisplayStyle() {
      return DISPLAY_STYLE_RADIO;
    }
  }
}
