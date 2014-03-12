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
package org.eclipse.scout.rt.spec.client.gen.extract.form.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.spec.client.SpecUtility;
import org.eclipse.scout.rt.spec.client.gen.filter.DefaultDocFilter;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;
import org.junit.Test;

/**
 * Test for {@link #FormFieldLabelExtractor}
 */
public class FormFieldLabelExtractorTest {

  private String m_indent = SpecUtility.getDocConfigInstance().getIndent();

  @Test
  public void testGetTextFlat() {
    AbstractFormField testFormField = mock(AbstractFormField.class);
    String testLabel = "testLabel";
    when(testFormField.getProperty(IFormField.PROP_LABEL)).thenReturn(testLabel);

    FormFieldLabelExtractor ex = createExtractor(true);
    String text = ex.getText(testFormField);
    assertFalse("Doc Text Invalid", text.startsWith(m_indent));
    assertTrue("Doc Text Invalid", text.contains(testLabel));
  }

  private static FormFieldLabelExtractor createExtractor(boolean hierarchic) {
    List<IDocFilter<IFormField>> filters = new ArrayList<IDocFilter<IFormField>>();
    filters.add(new DefaultDocFilter<IFormField>());
    FormFieldLabelExtractor ex = new FormFieldLabelExtractor(hierarchic, filters);
    return ex;
  }

  @Test
  public void testGetTextHierarchic() {
    AbstractGroupBox mainBox = mock(AbstractGroupBox.class);
    when(mainBox.isBorderVisible()).thenReturn(true);
    when(mainBox.isMainBox()).thenReturn(true);

    AbstractGroupBox superSuperField = mock(AbstractGroupBox.class);
    when(superSuperField.isVisible()).thenReturn(true);
    when(superSuperField.getLabel()).thenReturn("Label must be present in order not to be transparent for doc.");
    when(superSuperField.getParentField()).thenReturn(mainBox);

    AbstractGroupBox superField = mock(AbstractGroupBox.class);
    when(superField.isVisible()).thenReturn(true);
    when(superField.getLabel()).thenReturn("Label must be present in order not to be transparent for doc.");
    when(superField.getParentField()).thenReturn(superSuperField);

    AbstractFormField testFormField = mock(AbstractFormField.class);
    String testLabel = "testLabel";
    when(testFormField.getProperty(IFormField.PROP_LABEL)).thenReturn(testLabel);
    when(testFormField.getParentField()).thenReturn(superField);

    FormFieldLabelExtractor ex = createExtractor(true);
    String text = ex.getText(testFormField);
    assertTrue("Doc Text Invalid", text.startsWith(m_indent + m_indent));
    assertTrue("Doc Text Invalid", text.contains(testLabel));
  }

  @Test
  public void testGetLevel() {
    List<AbstractGroupBox> groupBoxTree = createGroupBoxTree();
    AbstractGroupBox superSuperBox = groupBoxTree.get(1);
    when(superSuperBox.classId()).thenReturn("12345678");
    AbstractGroupBox superBox = groupBoxTree.get(2);
    when(superBox.classId()).thenReturn("1234567");

    AbstractFormField testFormField = mock(AbstractFormField.class);
    when(testFormField.classId()).thenReturn("123456");

    FormFieldLabelExtractor ex = createExtractor(true);
    assertEquals(0, ex.getLevel(testFormField));

    when(testFormField.getParentField()).thenReturn(superBox);
    assertEquals(2, ex.getLevel(testFormField));

    when(superBox.getLabel()).thenReturn(null);
    assertEquals(1, ex.getLevel(testFormField));

    when(superSuperBox.getLabel()).thenReturn(null);
    assertEquals(0, ex.getLevel(testFormField));
  }

  @Test
  public void testGetIndentation() {
    List<AbstractGroupBox> groupBoxTree = createGroupBoxTree();
    AbstractGroupBox superSuperBox = groupBoxTree.get(1);
    AbstractGroupBox superBox = groupBoxTree.get(2);
    AbstractFormField testFormField = mock(AbstractFormField.class);
    when(testFormField.getParentField()).thenReturn(superBox);

    // hierarchic
    FormFieldLabelExtractor exHierarchic = createExtractor(true);
    assertEquals("Expected 0 indent levels", "", exHierarchic.getIndentation(superSuperBox));
    assertEquals("Expected 1 indent levels", m_indent, exHierarchic.getIndentation(superBox));
    assertEquals("Expected 2 indent levels", m_indent + m_indent, exHierarchic.getIndentation(testFormField));

    //flat
    FormFieldLabelExtractor exFlat = createExtractor(false);
    assertEquals("Expected no indentation", "", exFlat.getIndentation(superSuperBox));
    assertEquals("Expected no indentation", "", exFlat.getIndentation(superBox));
    assertEquals("Expected no indentation", "", exFlat.getIndentation(testFormField));
  }

  /**
   * Creates a MainBox (GroupBox) containing a GroupBox which contains a third GroupBox
   * 
   * @return the created tree as a flat list
   */
  private List<AbstractGroupBox> createGroupBoxTree() {
    AbstractGroupBox mainBox = mock(AbstractGroupBox.class);
    when(mainBox.isMainBox()).thenReturn(true);

    AbstractGroupBox innerBox = mock(AbstractGroupBox.class);
    when(innerBox.getLabel()).thenReturn("If label is present group box is not transparent.");
    when(innerBox.getParentField()).thenReturn(mainBox);

    AbstractGroupBox innerInnerBox = mock(AbstractGroupBox.class);
    when(innerInnerBox.isVisible()).thenReturn(true);
    when(innerInnerBox.getLabel()).thenReturn("If label is present group box is not transparent.");
    when(innerInnerBox.getParentField()).thenReturn(innerBox);

    ArrayList<AbstractGroupBox> list = new ArrayList<AbstractGroupBox>();
    list.add(mainBox);
    list.add(innerBox);
    list.add(innerInnerBox);
    return list;
  }

}
