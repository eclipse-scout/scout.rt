package org.eclipse.scout.rt.ui.swing.form.fields.htmlfield;

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
import org.eclipse.scout.rt.client.ui.form.fields.htmlfield.IHtmlField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SwingScoutHtmlField}
 * 
 * @since 4.0.0-M7
 */
public class SwingScoutHtmlFieldUiTest {

  private TestSwingScoutHtmlField m_htmlfield;
  private TestSwingScoutHtmlEditorField m_htmlEditorField;

  @Before
  public void setup() {
    m_htmlfield = new TestSwingScoutHtmlField();
    m_htmlfield.initializeSwing();

    m_htmlEditorField = new TestSwingScoutHtmlEditorField();
    m_htmlEditorField.initializeSwing();
  }

  @Test
  public void testForegroundColor() {
    m_htmlfield.setEnabledFromScout(true);
    Color foregroundColorEnabled = m_htmlfield.getSwingHtmlField().getForeground();
    assertNotNull(foregroundColorEnabled);
    m_htmlfield.setEnabledFromScout(false);
    Color backgroundColorDisabled = m_htmlfield.getSwingHtmlField().getForeground();
    assertNotNull(backgroundColorDisabled);
    assertNotEquals("enabled foregroundColor should be different from disabled foregroundColor", foregroundColorEnabled, backgroundColorDisabled);
  }

  @Test
  public void testForegroundColorHtmlEditorField() {
    m_htmlEditorField.setEnabledFromScout(true);
    Color foregroundColorEnabled = m_htmlEditorField.getSwingHtmlField().getForeground();
    assertNotNull(foregroundColorEnabled);
    m_htmlEditorField.setEnabledFromScout(false);
    Color backgroundColorDisabled = m_htmlEditorField.getSwingHtmlField().getForeground();
    assertNotNull(backgroundColorDisabled);
    assertNotEquals("enabled foregroundColor should be different from disabled foregroundColor", foregroundColorEnabled, backgroundColorDisabled);
  }

  @Test
  public void testJTextPaneSelectable() {
    m_htmlfield.setEnabledFromScout(true);
    assertTrue("The JTextPane must be selectable by the user", m_htmlfield.getSwingHtmlField().isEnabled());
    assertFalse("The JTextPane should not be editable", m_htmlfield.getSwingHtmlField().isEditable());
    m_htmlfield.setEnabledFromScout(false);
    assertTrue("The JTextPane must be selectable by the user", m_htmlfield.getSwingHtmlField().isEnabled());
    assertFalse("The JTextPane should not be editable", m_htmlfield.getSwingHtmlField().isEditable());

    m_htmlEditorField.setEnabledFromScout(true);
    assertTrue("The JTextPane must be selectable by the user", m_htmlEditorField.getSwingHtmlField().isEnabled());
    assertTrue("The JTextPane should be editable", m_htmlEditorField.getSwingHtmlField().isEditable());
    m_htmlEditorField.setEnabledFromScout(false);
    assertTrue("The JTextPane must be selectable by the user", m_htmlEditorField.getSwingHtmlField().isEnabled());
    assertTrue("The JTextPane should be editable", m_htmlEditorField.getSwingHtmlField().isEditable());
  }

  private static class TestSwingScoutHtmlField extends SwingScoutHtmlField {

    @Override
    public IHtmlField getScoutObject() {
      IHtmlField scoutObject = mock(IHtmlField.class);
      when(scoutObject.isHtmlEditor()).thenReturn(false);
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

  private static class TestSwingScoutHtmlEditorField extends TestSwingScoutHtmlField {

    @Override
    public IHtmlField getScoutObject() {
      IHtmlField scoutObject = mock(IHtmlField.class);
      when(scoutObject.isHtmlEditor()).thenReturn(true);
      when(scoutObject.getGridData()).thenReturn(new GridData(0, 0, 0, 0, 0, 0));
      return scoutObject;
    }
  }

}
