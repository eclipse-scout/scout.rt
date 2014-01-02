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
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.gen.extract.AbstractBooleanTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.FormFieldBooleanPropertyExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.form.field.FormFieldIdExtractor;
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
    AbstractBooleanTextExtractor<IFormField> p = new FormFieldBooleanPropertyExtractor(IFormField.PROP_MANDATORY, "m");

    String actualText = p.getText(mandatoryField);

    String expectedText = TEXTS.get(FormFieldBooleanPropertyExtractor.DOC_ID_TRUE);
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
    AbstractBooleanTextExtractor<IFormField> p = new FormFieldBooleanPropertyExtractor(IFormField.PROP_MANDATORY, "m");

    String actualText = p.getText(field);
    String expectedText = TEXTS.get(FormFieldBooleanPropertyExtractor.DOC_ID_FALSE);
    assertEquals("Boolean Doc Text Invalid", expectedText, actualText);
  }

//  /**
//   * Tests the {@link LinkableTypeProperty} for a simple form field
//   */
//  @Test
//  public void testLinkableTypeProperty() {
//    IFormField field = new TestFormField();
//    LinkableTypeProperty<IFormField> linkableTypeProperty = new LinkableTypeProperty<IFormField>();
//    String text = linkableTypeProperty.getText(field);
//    String expectedResult = "" +
//        "<" + LinkableTypeProperty.REPLACE_TAG_NAME + ">" +
//        "<" + LinkableTypeProperty.LINK_TAG_NAME + ">" +
//        "<" + LinkableTypeProperty.ID_TAG_NAME + ">" +
//        TestFormField.class.getName() +
//        "<" + LinkableTypeProperty.ID_TAG_NAME + "/>" +
//        "<" + LinkableTypeProperty.NAME_TAG_NAME + ">" +
//        TestFormField.class.getSimpleName() +
//        "<" + LinkableTypeProperty.NAME_TAG_NAME + "/>" +
//        "<" + LinkableTypeProperty.LINK_TAG_NAME + "/>" +
//        "<" + LinkableTypeProperty.REPLACE_TAG_NAME + "/>";
//    assertEquals(expectedResult, text);
//  }
//
//  /**
//   * Tests the {@link LinkableTypeProperty} for a {@link AbstractStringField}
//   */
//  @Test
//  public void testLinkableTypePropertyStringField() {
//    IFormField field = new TestStringField();
//    LinkableTypeProperty<IFormField> linkableTypeProperty = new LinkableTypeProperty<IFormField>();
//    String text = linkableTypeProperty.getText(field);
//    String expectedResult =
//        linkableTypeProperty.getStartTag(LinkableTypeProperty.REPLACE_TAG_NAME) +
//            linkableTypeProperty.createLink(TestStringField.class.getName(), TestStringField.class.getSimpleName()) +
//            linkableTypeProperty.createLink(AbstractStringField.class.getName(), AbstractStringField.class.getSimpleName()) +
//            linkableTypeProperty.createLink(AbstractValueField.class.getName(), AbstractValueField.class.getSimpleName())
//            + linkableTypeProperty.getEndTag(LinkableTypeProperty.REPLACE_TAG_NAME);
//    assertEquals(expectedResult, text);
//  }

  /**
   * The id of a simple Field should be its class name.
   */
  @Test
  public void testIDText() {
    IFormField testField = new TestFormField();
    FormFieldIdExtractor p = new FormFieldIdExtractor("Id");

    String actualText = p.getText(testField);
    assertEquals("Doc Text Invalid", TestFormField.class.getName(), actualText);
  }

  class TestFormField extends AbstractFormField {
  }

  class TestStringField extends AbstractStringField {
  }

}
