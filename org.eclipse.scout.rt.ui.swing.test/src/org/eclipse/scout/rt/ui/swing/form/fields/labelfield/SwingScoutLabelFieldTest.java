package org.eclipse.scout.rt.ui.swing.form.fields.labelfield;

import static org.junit.Assert.assertTrue;

import javax.swing.JComponent;
import javax.swing.JTextPane;

import org.eclipse.scout.rt.ui.swing.ext.JTextPaneEx;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SwingScoutLabelField}
 * 
 * @since 3.10.0-M5
 */
public class SwingScoutLabelFieldTest {

  private TestSwingScoutLabelField label;

  @Before
  public void setup() {
    label = new TestSwingScoutLabelField();
    label.setSwingField(label.createLabelField());
  }

  @Test
  public void testNoWrapText() throws Exception {
    label.setTextWrapFromScout(false);
    String text = ((JTextPane) label.getSwingField()).getText();
    assertTrue(text.contains("white-space: nowrap"));

    label.setTextWrapFromScout(true);
    text = ((JTextPane) label.getSwingField()).getText();
    assertTrue(text.contains("white-space: normal"));
  }

  @Test
  public void testVerticalAlignment() {
    label.setVerticalAlignmentFromScout(0);
    String text = ((JTextPane) label.getSwingField()).getText();
    assertTrue(text.contains("valign=\"middle\""));

    label.setVerticalAlignmentFromScout(-1);
    text = ((JTextPane) label.getSwingField()).getText();
    assertTrue(text.contains("valign=\"top\""));

    label.setVerticalAlignmentFromScout(1);
    text = ((JTextPane) label.getSwingField()).getText();
    assertTrue(text.contains("valign=\"bottom\""));
  }

  @Test
  public void testHorizontalAlignment() {
    label.setHorizontalAlignmentFromScout(-1);
    String text = ((JTextPane) label.getSwingField()).getText();
    assertTrue(text.contains("align=\"left\""));

    label.setHorizontalAlignmentFromScout(0);
    text = ((JTextPane) label.getSwingField()).getText();
    assertTrue(text.contains("align=\"center\""));

    label.setHorizontalAlignmentFromScout(1);
    text = ((JTextPane) label.getSwingField()).getText();
    assertTrue(text.contains("align=\"right\""));
  }

  private static class TestSwingScoutLabelField extends SwingScoutLabelField {
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
  }

}
