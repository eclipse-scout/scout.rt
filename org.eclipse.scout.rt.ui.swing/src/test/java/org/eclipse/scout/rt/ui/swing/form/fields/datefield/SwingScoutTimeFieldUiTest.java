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
package org.eclipse.scout.rt.ui.swing.form.fields.datefield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.client.ui.action.menu.root.IValueFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.junit.Test;

/**
 * Tests for {@link SwingScoutTimeField}
 *
 * @since 4.1.0
 */
public class SwingScoutTimeFieldUiTest {

  @Test
  public void testEnabledDisabled() {
    P_SwingScoutTimeField timeField = new P_SwingScoutTimeField();
    timeField.initializeSwing();
    timeField.setEnabledFromScout(true);
    assertTrue("TimeField should be enabled", timeField.isTimeChooserEnabled());

    timeField.setEnabledFromScout(false);
    assertFalse("TimeField should be disabled", timeField.isTimeChooserEnabled());

    timeField.setEnabledFromScout(true);
    assertTrue("TimeField should be enabled", timeField.isTimeChooserEnabled());
  }

  private class P_SwingScoutTimeField extends SwingScoutTimeField {
    @Override
    public ISwingEnvironment getSwingEnvironment() {
      ISwingEnvironment environment = mock(ISwingEnvironment.class);
      when(environment.createStatusLabel(any(IFormField.class))).thenReturn(new JStatusLabelEx());
      return environment;
    }

    @Override
    public IDateField getScoutObject() {
      IDateField dateField = mock(IDateField.class);
      when(dateField.getGridData()).thenReturn(new GridData(1, 1, 1, 1, 1, 1));
      when(dateField.getContextMenu()).thenReturn(mock(IValueFieldContextMenu.class));
      return dateField;
    }
  }
}
