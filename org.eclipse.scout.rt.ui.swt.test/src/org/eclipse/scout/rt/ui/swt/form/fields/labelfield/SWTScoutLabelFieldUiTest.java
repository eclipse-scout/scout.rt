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
import org.eclipse.scout.rt.ui.swt.AbstractSwtEnvironment;
import org.eclipse.scout.rt.ui.swt.keystroke.ISwtKeyStrokeFilter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link SwtScoutLabelField}
 */
public class SWTScoutLabelFieldUiTest {
  TestSwtScoutLabelField testLabelField;

  @Before
  public void setup() {
    testLabelField = new TestSwtScoutLabelField();
    testLabelField.setScoutObjectAndSwtEnvironment();
    testLabelField.initializeSwt(new Shell(Display.getDefault()));
  }

  @Test
  public void testSwtFieldInitiallyEnabled() {
    assertTrue(testLabelField.getSwtField().isEnabled());
  }

  @Test
  public void testSwtFieldDisabledWhenNotSelecteable() {
    setModelSelectable(false);
    assertFalse(testLabelField.getSwtField().isEnabled());
  }

  @Test
  public void testSwtFieldEnabledWhenSelecteable() {
    setModelSelectable(false);
    setModelSelectable(true);
    assertTrue(testLabelField.getSwtField().isEnabled());
  }

  private void setModelSelectable(boolean selectable) {
    testLabelField.getScoutObject().setSelectable(selectable);
    testLabelField.handleScoutPropertyChange(ILabelField.PROP_SELECTABLE, testLabelField.getScoutObject());
  }

  class TestSwtScoutLabelField extends SwtScoutLabelField {

    public void setScoutObjectAndSwtEnvironment() {
      super.setScoutObjectAndSwtEnvironment(createTestScoutField(), createTestSwtEnvironment());
    }

    private AbstractLabelField createTestScoutField() {
      return new AbstractLabelField() {
      };
    }

    private AbstractSwtEnvironment createTestSwtEnvironment() {
      return new AbstractSwtEnvironment(null, null, null) {
        @Override
        public void addKeyStrokeFilter(Widget c, ISwtKeyStrokeFilter filter) {
          //ignore key strokes in test
        }
      };
    }
  }

}
