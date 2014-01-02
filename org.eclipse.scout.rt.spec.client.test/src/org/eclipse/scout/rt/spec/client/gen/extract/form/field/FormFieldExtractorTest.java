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

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.junit.Test;

/**
 * Test for {@link FormFieldPropertyExtractor}
 */
public class FormFieldExtractorTest {

  /**
   * Test {@link FormFieldPropertyExtractor#getText(IFormField)} for a label.
   */
  @Test
  public void testPropertyExtractorLabel() {
    AbstractFormField testFormField = mock(AbstractFormField.class);
    String testLabel = "testLabel";
    when(testFormField.getProperty(IFormField.PROP_LABEL)).thenReturn(testLabel);
    FormFieldPropertyExtractor ex = new FormFieldPropertyExtractor(IFormField.PROP_LABEL, "Label");
    String text = ex.getText(testFormField);
    assertEquals("Boolean Doc Text Invalid", testLabel, text);
  }

  /**
   * Test {@link FormFieldPropertyExtractor#getText(IFormField)} for a label.
   */
  @Test
  public void testPropertyExtractorNullLabel() {
    AbstractFormField testFormField = mock(AbstractFormField.class);
    when(testFormField.getProperty(IFormField.PROP_LABEL)).thenReturn(null);
    FormFieldPropertyExtractor ex = new FormFieldPropertyExtractor(IFormField.PROP_LABEL, "Label");
    String text = ex.getText(testFormField);
    assertEquals("Boolean Doc Text Invalid", "", text);
  }

  /**
   * Test {@link FormFieldBooleanPropertyExtractor#getText(IFormField)} for the enabled property.
   */
  @Test
  public void testPropertyExtractorEnabled() {
    AbstractFormField testFormField = mock(AbstractFormField.class);
    when(testFormField.getProperty(IFormField.PROP_ENABLED)).thenReturn(true);
    FormFieldBooleanPropertyExtractor ex = new FormFieldBooleanPropertyExtractor(IFormField.PROP_ENABLED, "Enabled");
    String text = ex.getText(testFormField);
    String trueText = TEXTS.get(FormFieldBooleanPropertyExtractor.DOC_ID_TRUE);

    assertEquals("Boolean Doc Text Invalid", trueText, text);
  }

  /**
   * Test {@link FormFieldBooleanPropertyExtractor#getText(IFormField)} for the enabled property.
   */
  @Test
  public void testTableFieldTypeWithLabelExtractor() {
    String testLabel = "testLabel";
    AbstractTableField testField = mock(AbstractTableField.class);
    when(testField.getLabel()).thenReturn(testLabel);
    TableFieldTypeWithLabelExtractor<AbstractTableField> ex = new TableFieldTypeWithLabelExtractor<AbstractTableField>();
    String expectedText = testField.getClass().getSimpleName() + " (" + testLabel + ")";
    String text = ex.getText(testField);

    assertEquals("Boolean Doc Text Invalid", expectedText, text);
  }

  /**
   * Test {@link FormFieldBooleanPropertyExtractor#getText(IFormField)} for the enabled property.
   */
  @Test
  public void testTableFieldTypeWithLabelExtractorNullLabel() {
    AbstractTableField testField = mock(AbstractTableField.class);
    when(testField.getLabel()).thenReturn(null);
    TableFieldTypeWithLabelExtractor<AbstractTableField> ex = new TableFieldTypeWithLabelExtractor<AbstractTableField>();
    String expectedText = testField.getClass().getSimpleName();
    String text = ex.getText(testField);

    assertEquals("Boolean Doc Text Invalid", expectedText, text);
  }

}
