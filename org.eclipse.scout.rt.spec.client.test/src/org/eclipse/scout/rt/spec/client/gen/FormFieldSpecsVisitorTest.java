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
package org.eclipse.scout.rt.spec.client.gen;


/**
 * Test for {@link FormFieldSpecsVisitor}
 */
public class FormFieldSpecsVisitorTest {

//  /**
//   * Test {@link FormFieldSpecsVisitor} without filter
//   *
//   * @throws ProcessingException
//   */
//  @Test
//  public void testVisitor() throws ProcessingException {
//    SimplePersonForm testForm = new SimplePersonForm();
//    int fieldNr = testForm.getAllFields().length;
////    FormFieldSpecsVisitor v = new FormFieldSpecsVisitor(new TestConfigNoFilters());
//    testForm.visitFields(v);
//    String[][] rows = v.getFields().getTable().getCellTexts();
//    assertEquals(fieldNr, rows.length);
//  }
//
//  /**
//   * Test {@link FormFieldSpecsVisitor} for a form with {@link Doc#ignore()=true} group box.
//   *
//   * @throws ProcessingException
//   */
//  @Test
//  public void testIgnoreDoc() throws ProcessingException {
//    SimplePersonForm testForm = new SimplePersonForm();
//    FormFieldSpecsVisitor v = new FormFieldSpecsVisitor(new TestConfigIgnoreDoc());
//    testForm.visitFields(v);
//    String[][] rows = v.getFields().getTable().getCellTexts();
//    assertEquals(2, rows.length);
//    String MainBoxName = testForm.getMainBox().getClass().getName();
//    String Name2FieldName = testForm.getName2Field().getClass().getName();
//    assertEquals(MainBoxName, rows[0][0]);
//    assertEquals(Name2FieldName, rows[1][0]);
//  }
}
