package org.eclipse.scout.rt.ui.swing.form.fields.labelfield;

import static org.junit.Assert.assertTrue;

import javax.swing.JComponent;
import javax.swing.JTextPane;

import org.easymock.EasyMock;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.swing.ext.JTextPaneEx;
import org.eclipse.scout.rt.ui.swing.text.HTMLStyledTextCreator;
import org.eclipse.scout.rt.ui.swing.text.IStyledTextCreator;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SwingScoutLabelField}
 * 
 * @since 3.10.0-M5
 */
public class SwingScoutLabelFieldTest {

  private TestSwingScoutLabelField m_label;

  @Before
  public void setup() {
    m_label = new TestSwingScoutLabelField();
    m_label.setSwingField(m_label.createLabelField());
  }

  @Test
  public void testNoWrapText() {
    m_label.setTextWrapFromScout(false);
    String text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("white-space: nowrap"));

    m_label.setTextWrapFromScout(true);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("white-space: normal"));
  }

  @Test
  public void testVerticalAlignment() {
    m_label.setVerticalAlignmentFromScout(0);
    String text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("valign=\"middle\""));

    m_label.setVerticalAlignmentFromScout(-1);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("valign=\"top\""));

    m_label.setVerticalAlignmentFromScout(1);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("valign=\"bottom\""));
  }

  @Test
  public void testHorizontalAlignment() {
    m_label.setHorizontalAlignmentFromScout(-1);
    String text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("align=\"left\""));

    m_label.setHorizontalAlignmentFromScout(0);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("align=\"center\""));

    m_label.setHorizontalAlignmentFromScout(1);
    text = ((JTextPane) m_label.getSwingField()).getText();
    assertTrue(text.contains("align=\"right\""));
  }

  private static class TestSwingScoutLabelField extends SwingScoutLabelField {

    private IStyledTextCreator m_styledTextCreator = new HTMLStyledTextCreator();

    @Override
    public void setSwingField(JComponent swingField) {
      super.setSwingField(swingField);
    }

    @Override
    public JTextPaneEx createLabelField() {
      return super.createLabelField();
    }

    @Override
    protected void setTextWrapFromScout(boolean b) {
      super.setTextWrapFromScout(b);
    }

    @Override
    public ILabelField getScoutObject() {
      ILabelField scoutObject = EasyMock.createNiceMock(ILabelField.class);
      EasyMock.expect(scoutObject.getGridData()).andReturn(new GridData(0, 0, 0, 0, 0, 0));
      EasyMock.replay(scoutObject);
      return scoutObject;
    }

    @Override
    public IStyledTextCreator getStyledTextCreator() {
      return m_styledTextCreator;
    }
  }

}
