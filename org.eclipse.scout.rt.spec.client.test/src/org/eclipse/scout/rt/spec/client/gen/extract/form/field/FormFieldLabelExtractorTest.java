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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.spec.client.gen.filter.DefaultDocFilter;
import org.eclipse.scout.rt.spec.client.gen.filter.IDocFilter;
import org.junit.Test;

/**
 * Test for {@link #FormFieldLabelExtractor}
 */
public class FormFieldLabelExtractorTest {

  @Test
  public void testGetTextFlat() {
    AbstractFormField testFormField = mock(AbstractFormField.class);
    String testLabel = "testLabel";
    when(testFormField.getProperty(IFormField.PROP_LABEL)).thenReturn(testLabel);

    FormFieldLabelExtractor ex = createExtractor();
    String text = ex.getText(testFormField);
    assertEquals("Doc Text Invalid", testLabel, text);
  }

  private static FormFieldLabelExtractor createExtractor() {
    List<IDocFilter<IFormField>> filters = new ArrayList<IDocFilter<IFormField>>();
    filters.add(new DefaultDocFilter<IFormField>());
    FormFieldLabelExtractor ex = new FormFieldLabelExtractor(true, filters);
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

    FormFieldLabelExtractor ex = createExtractor();
    String text = ex.getText(testFormField);
    assertEquals("Doc Text Invalid", FormFieldLabelExtractor.INDENT + FormFieldLabelExtractor.INDENT + " " + testLabel, text);
  }

  @Test
  public void testGetLevel() {
    AbstractGroupBox mainBox = mock(AbstractGroupBox.class);
    when(mainBox.isMainBox()).thenReturn(true);

    AbstractGroupBox superSuperField = mock(AbstractGroupBox.class);
    when(superSuperField.getLabel()).thenReturn("If label is present group box is not transparent.");
    when(superSuperField.getParentField()).thenReturn(mainBox);

    AbstractGroupBox superField = mock(AbstractGroupBox.class);
    when(superField.isVisible()).thenReturn(true);
    when(superField.getLabel()).thenReturn("If label is present group box is not transparent.");
    when(superField.getParentField()).thenReturn(superSuperField);

    AbstractFormField testFormField = mock(AbstractFormField.class);

    FormFieldLabelExtractor ex = createExtractor();
    assertEquals(0, ex.getLevel(testFormField));

    when(testFormField.getParentField()).thenReturn(superField);
    assertEquals(2, ex.getLevel(testFormField));

    when(superField.getLabel()).thenReturn(null);
    assertEquals(1, ex.getLevel(testFormField));

    when(superSuperField.getLabel()).thenReturn(null);
    assertEquals(0, ex.getLevel(testFormField));

  }

}
