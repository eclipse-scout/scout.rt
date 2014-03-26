package org.eclipse.scout.rt.ui.swing.form.fields.labelfield;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JTextPane;

import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.swing.ISwingEnvironment;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.text.HTMLStyledTextCreator;
import org.eclipse.scout.rt.ui.swing.text.IStyledTextCreator;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SwingScoutLabelField}
 *
 * @since 3.10.0-M5
 */
public class SwingScoutLabelFieldUiTest {

  private static final int LABEL_HORIZONTAL_ALIGNMENT_LEFT = -1;
  private static final int LABEL_HORIZONTAL_ALIGNMENT_CENTER = 0;
  private static final int LABEL_HORIZONTAL_ALIGNMENT_RIGHT = 1;
  private static final int LABEL_VERTICAL_ALIGNMENT_TOP = -1;
  private static final int LABEL_VERTICAL_ALIGNMENT_MIDDLE = 0;
  private static final int LABEL_VERTICAL_ALIGNMENT_BOTTOM = 1;

  private TestSwingScoutLabelField m_label;

  @Before
  public void setup() {
    m_label = new TestSwingScoutLabelField();
    m_label.setSwingField(m_label.createLabelField());
    m_label.initializeSwing();
  }

  @Test
  public void testNoWrapText() throws Exception {
    m_label.setTextWrapFromScout(false);
    String text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("white-space: nowrap"));

    m_label.setTextWrapFromScout(true);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("white-space: normal"));
  }

  @Test
  public void testVerticalAlignment() {
    m_label.setVerticalAlignmentFromScout(LABEL_VERTICAL_ALIGNMENT_MIDDLE);
    String text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("valign=\"middle\""));

    m_label.setVerticalAlignmentFromScout(LABEL_VERTICAL_ALIGNMENT_TOP);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("valign=\"top\""));

    m_label.setVerticalAlignmentFromScout(LABEL_VERTICAL_ALIGNMENT_BOTTOM);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("valign=\"bottom\""));
  }

  @Test
  public void testHorizontalAlignment() {
    m_label.setHorizontalAlignmentFromScout(LABEL_HORIZONTAL_ALIGNMENT_LEFT);
    String text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("align=\"left\""));

    m_label.setHorizontalAlignmentFromScout(LABEL_HORIZONTAL_ALIGNMENT_CENTER);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("align=\"center\""));

    m_label.setHorizontalAlignmentFromScout(LABEL_HORIZONTAL_ALIGNMENT_RIGHT);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("align=\"right\""));
  }

  @Test
  public void testForegroundColor() {
    m_label.setEnabledFromScout(true);
    Color enabledForegroundColor = m_label.getSwingLabelField().getForeground();
    assertNotNull(enabledForegroundColor);

    m_label.setEnabledFromScout(false);
    Color disabledForegroundColor = m_label.getSwingLabelField().getForeground();
    assertNotNull(disabledForegroundColor);

    assertNotEquals("enabled foregroundColor should be different from disabled foregroundColor", enabledForegroundColor, disabledForegroundColor);
  }

  @Test
  public void testSelectable() {
    m_label.setEnabledFromScout(true);
    assertTrue("JTextPaneEx should be enabled, otherwise text selecion is not possible", m_label.getSwingLabelField().isEnabled());
    m_label.setEnabledFromScout(false);
    assertTrue("JTextPaneEx should be enabled, otherwise text selecion is not possible", m_label.getSwingLabelField().isEnabled());

    m_label.setSelectableFromScout(true);
    assertNotNull(m_label.getSwingLabelField().getHighlighter());
    assertNotNull(m_label.getSwingLabelField().getTransferHandler());

    m_label.setSelectableFromScout(false);
    assertNull(m_label.getSwingLabelField().getHighlighter());
    assertNull(m_label.getSwingLabelField().getTransferHandler());

    m_label.setSelectableFromScout(true);
    assertNotNull(m_label.getSwingLabelField().getHighlighter());
    assertNotNull(m_label.getSwingLabelField().getTransferHandler());
  }

  private static class TestSwingScoutLabelField extends SwingScoutLabelField {

    private IStyledTextCreator m_styledTextCreator = new HTMLStyledTextCreator();

    @Override
    public void setSwingField(JComponent swingField) {
      super.setSwingField(swingField);
    }

    @Override
    public ILabelField getScoutObject() {
      ILabelField scoutObject = createNiceMock(ILabelField.class);
      expect(scoutObject.getGridData()).andReturn(new GridData(0, 0, 0, 0, 0, 0));
      replay(scoutObject);
      return scoutObject;
    }

    @Override
    public ISwingEnvironment getSwingEnvironment() {
      ISwingEnvironment environment = createNiceMock(ISwingEnvironment.class);
      expect(environment.createStatusLabel(isA(IFormField.class))).andReturn(new JStatusLabelEx());
      replay(environment);
      return environment;
    }

    @Override
    public IStyledTextCreator getStyledTextCreator() {
      return m_styledTextCreator;
    }
  }

}
