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
package org.eclipse.scout.rt.ui.swing.form.fields.smartfield;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.rt.client.ui.action.menu.root.IValueFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SwingScoutSmartField}
 *
 * @since 4.1.0
 */
public class SwingScoutSmartFieldUiTest {

  P_SwingScoutSmartField m_smartField;
  AbstractDocument m_ad;

  @Before
  public void setup() {
    m_smartField = new P_SwingScoutSmartField();
    m_smartField.initializeSwing();
    assertTrue("precondition not met, swing field is not a JTextComponent", m_smartField.getSwingField() instanceof JTextComponent);
    assertTrue("precondition not met, document filter is not a BasicDocumentFilter", m_smartField.getSwingField() instanceof JTextComponent);
    m_ad = (AbstractDocument) ((JTextComponent) m_smartField.getSwingField()).getDocument();
    ((BasicDocumentFilter) m_ad.getDocumentFilter()).setMaxLength(20);
  }

  @Test
  public void testReplace() throws BadLocationException {
    m_smartField.setDisplayTextFromScout("test text");
    // null behaviour
    m_ad.replace(0, 9, null, null);
    assertTrue("".equals(m_ad.getText(0, m_ad.getLength())));

    m_smartField.setDisplayTextFromScout("test text");
    // behaviour with an empty string
    m_ad.replace(0, 9, "", null);
    assertTrue(m_ad.getText(0, m_ad.getLength()).equals("")); // TODO

    m_smartField.setDisplayTextFromScout("test text");
    // behaviour with a regular string
    m_ad.replace(0, 9, "test", null);
    assertTrue(m_ad.getText(0, m_ad.getLength()).equals("test"));

    m_smartField.setDisplayTextFromScout("test text");
    // behaviour with a very long string
    m_ad.replace(0, 9, "test with a very long string", null);
    assertTrue(m_ad.getText(0, m_ad.getLength()).equals("test with a very lon"));

  }

  @Test
  public void testInsert() throws BadLocationException {

    m_smartField.setDisplayTextFromScout("test text");
    // null behaviour
    m_ad.insertString(4, null, null);
    assertTrue(m_ad.getText(0, m_ad.getLength()).equals("test text"));

    m_smartField.setDisplayTextFromScout("test text");
    // behaviour with an empty string
    m_ad.insertString(4, "", null);
    assertTrue(m_ad.getText(0, m_ad.getLength()).equals("test text"));

    m_smartField.setDisplayTextFromScout("test text");
    // behaviour with a regular string
    m_ad.insertString(4, " test", null);
    assertTrue(m_ad.getText(0, m_ad.getLength()).equals("test test text"));

    m_smartField.setDisplayTextFromScout("test text");
    // behaviour with a very long string
    m_ad.insertString(4, " test with a very long string", null);
    assertTrue(m_ad.getText(0, m_ad.getLength()).equals("test test with  text"));

  }

  private class P_SwingScoutSmartField extends SwingScoutSmartField {
    @Override
    public ISwingEnvironment getSwingEnvironment() {
      ISwingEnvironment environment = mock(ISwingEnvironment.class);
      when(environment.createStatusLabel(any(ISmartField.class))).thenReturn(new JStatusLabelEx());
      return environment;
    }

    @Override
    public ISmartField getScoutObject() {
      ISmartField smartField = mock(ISmartField.class);
      when(smartField.getGridData()).thenReturn(new GridData(1, 1, 1, 1, 1, 1));
      when(smartField.getContextMenu()).thenReturn(mock(IValueFieldContextMenu.class));
      return smartField;
    }
  }

}
