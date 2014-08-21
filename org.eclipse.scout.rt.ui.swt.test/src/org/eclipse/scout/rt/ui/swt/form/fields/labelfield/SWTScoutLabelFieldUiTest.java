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
package org.eclipse.scout.rt.ui.swt.form.fields.labelfield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.swt.test.SwtTestingUtility;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link SwtScoutLabelField}
 */
public class SWTScoutLabelFieldUiTest {
  private TestSwtScoutLabelField m_testLabelField;

  @Before
  public void setup() {
    m_testLabelField = new TestSwtScoutLabelField();
    m_testLabelField.setScoutObjectAndSwtEnvironment();
    m_testLabelField.initializeSwt(new Shell(Display.getDefault()));
  }

  @Test
  public void testSwtFieldInitiallyEnabled() {
    assertTrue(m_testLabelField.getSwtField().isEnabled());
  }

  @Test
  public void testSwtFieldDisabledWhenNotSelecteable() {
    setModelSelectable(false);
    assertFalse(m_testLabelField.getSwtField().isEnabled());
  }

  @Test
  public void testSwtFieldEnabledWhenSelecteable() {
    setModelSelectable(false);
    setModelSelectable(true);
    assertTrue(m_testLabelField.getSwtField().isEnabled());
  }

  private void setModelSelectable(boolean selectable) {
    m_testLabelField.getScoutObject().setSelectable(selectable);
    m_testLabelField.handleScoutPropertyChange(ILabelField.PROP_SELECTABLE, m_testLabelField.getScoutObject());
  }

  class TestSwtScoutLabelField extends SwtScoutLabelField {

    public void setScoutObjectAndSwtEnvironment() {
      super.setScoutObjectAndSwtEnvironment(createTestScoutField(), SwtTestingUtility.createTestSwtEnvironment());
    }

    private AbstractLabelField createTestScoutField() {
      return new AbstractLabelField() {
      };
    }
  }

}
