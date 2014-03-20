package org.eclipse.scout.rt.ui.swing.form.fields.mailfield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.mailfield.IMailField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SwingScoutMailFieldField}
 * 
 * @since 4.0.0-M7
 */
public class SwingScoutMailFieldUiTest {

  private TestSwingScoutMailField m_mailfield;

  @Before
  public void setup() {
    m_mailfield = new TestSwingScoutMailField();
    m_mailfield.initializeSwing();
  }

  @Test
  public void testForegroundColor() {
    m_mailfield.setEnabledFromScout(true);
    Color foregroundColorEnabled = m_mailfield.getSwingMailField().getForeground();
    assertNotNull(foregroundColorEnabled);
    m_mailfield.setEnabledFromScout(false);
    Color backgroundColorDisabled = m_mailfield.getSwingMailField().getForeground();
    assertNotNull(backgroundColorDisabled);
    assertNotEquals("enabled foregroundColor should be different from disabled foregroundColor", foregroundColorEnabled, backgroundColorDisabled);
  }

  @Test
  public void testJTextPaneSelectable() {
    m_mailfield.setEnabledFromScout(true);
    assertTrue("The JTextPane must be selectable by the user", m_mailfield.getSwingMailField().isEnabled());
    assertFalse("The JTextPane should not be editable", m_mailfield.getSwingMailField().isEditable());
    m_mailfield.setEnabledFromScout(false);
    assertTrue("The JTextPane must be selectable by the user", m_mailfield.getSwingMailField().isEnabled());
    assertFalse("The JTextPane should not be editable", m_mailfield.getSwingMailField().isEditable());
  }

  private static class TestSwingScoutMailField extends SwingScoutMailField {

    @Override
    public IMailField getScoutObject() {
      IMailField scoutObject = mock(IMailField.class);
      when(scoutObject.isMailEditor()).thenReturn(false);
      when(scoutObject.getGridData()).thenReturn(new GridData(0, 0, 0, 0, 0, 0));
      return scoutObject;
    }

    @Override
    public ISwingEnvironment getSwingEnvironment() {
      ISwingEnvironment environment = mock(ISwingEnvironment.class);
      when(environment.createStatusLabel(any(IFormField.class))).thenReturn(new JStatusLabelEx());
      return environment;
    }
  }
}
