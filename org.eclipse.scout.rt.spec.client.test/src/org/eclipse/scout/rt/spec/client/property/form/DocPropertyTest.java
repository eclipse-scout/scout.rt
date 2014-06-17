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
package org.eclipse.scout.rt.spec.client.property.form;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.FormFieldBooleanPropertyExtractor;
import org.eclipse.scout.rt.spec.client.utility.SpecUtility;
import org.junit.Test;

/**
 * Test for {@link BooleanFormFieldDocProperty}
 */
public class DocPropertyTest {

  /**
   * For a mandatory field the Doc text returned by {@link BooleanFormFieldDocProperty#getText(IFormField)} should be
   * {@link BooleanFormFieldDocProperty#DOC_ID_TRUE}.
   */
  @Test
  public void testMandatoryTrueText() {
    IFormField mandatoryField = new AbstractFormField() {
    };
    mandatoryField.setMandatory(true);
    FormFieldBooleanPropertyExtractor p = new FormFieldBooleanPropertyExtractor(IFormField.PROP_MANDATORY, "m");

    String actualText = p.getText(mandatoryField);

    String expectedText = TEXTS.get(SpecUtility.DOC_ID_TRUE);
    assertEquals("Boolean Doc Text Invalid", expectedText, actualText);
  }

  /**
   * For a mandatory field the Doc text returned by {@link BooleanFormFieldDocProperty#getText(IFormField)} should be
   * {@link BooleanFormFieldDocProperty#DOC_ID_TRUE}.
   */
  @Test
  public void testMandatoryFalseText() {
    IFormField field = new AbstractFormField() {
    };
    FormFieldBooleanPropertyExtractor p = new FormFieldBooleanPropertyExtractor(IFormField.PROP_MANDATORY, "m");

    String actualText = p.getText(field);
    String expectedText = TEXTS.get(SpecUtility.DOC_ID_FALSE);
    assertEquals("Boolean Doc Text Invalid", expectedText, actualText);
  }

}
