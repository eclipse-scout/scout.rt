/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.extension.dto;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultipleExtGroupBoxExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultipleExtGroupBoxExtension.SecondDoubleField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultipleExtGroupBoxExtension.ThirdDateField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultipleExtGroupBoxExtensionData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData;
import org.eclipse.scout.service.SERVICES;
import org.junit.Test;

public class FormDataMultipleExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testFormDataMultipleExtensionsExplicit() throws Exception {
    SERVICES.getService(IExtensionRegistry.class).register(MultipleExtGroupBoxExtension.class, MainBox.class);
    SERVICES.getService(IExtensionRegistry.class).register(MultipleExtGroupBoxExtensionData.class, OrigFormData.class);
    doTest();
  }

  @Test
  public void testFormDataMultipleExtensionsFromAnnotation() throws Exception {
    SERVICES.getService(IExtensionRegistry.class).register(MultipleExtGroupBoxExtension.class);
    SERVICES.getService(IExtensionRegistry.class).register(MultipleExtGroupBoxExtensionData.class);
    doTest();
  }

  private void doTest() throws Exception {
    // create and test form
    OrigForm origForm = new OrigForm();
    origForm.initForm();
    assertEquals(OrigForm.STRING_FIELD_ORIG_VALUE, origForm.getFirstStringField().getValue());
    assertEquals(MultipleExtGroupBoxExtension.DOUBLE_FIELD_ORIG_VAL, origForm.getFieldByClass(SecondDoubleField.class).getValue());
    assertEquals(MultipleExtGroupBoxExtension.DATE_FIELD_ORIG_VAL, origForm.getFieldByClass(ThirdDateField.class).getValue());

    // test formData export
    OrigFormData data = new OrigFormData();
    origForm.exportFormData(data);
    assertEquals(OrigForm.STRING_FIELD_ORIG_VALUE, data.getFirstString().getValue());
    assertEquals(MultipleExtGroupBoxExtension.DOUBLE_FIELD_ORIG_VAL, data.getContribution(MultipleExtGroupBoxExtensionData.class).getSecondDouble().getValue());
    assertEquals(MultipleExtGroupBoxExtension.DATE_FIELD_ORIG_VAL, data.getContribution(MultipleExtGroupBoxExtensionData.class).getThirdDate().getValue());

    // test formData import
    String changedFirstStringValue = "a changed value";
    Double changedSecondDoubleValue = Double.valueOf(100.300032);
    Date changedThirdDateValue = getTestDate();
    data.getFirstString().setValue(changedFirstStringValue);
    data.getContribution(MultipleExtGroupBoxExtensionData.class).getSecondDouble().setValue(changedSecondDoubleValue);
    data.getContribution(MultipleExtGroupBoxExtensionData.class).getThirdDate().setValue(changedThirdDateValue);
    origForm.importFormData(data);
    assertEquals(changedFirstStringValue, origForm.getFirstStringField().getValue());
    assertEquals(changedSecondDoubleValue, origForm.getFieldByClass(SecondDoubleField.class).getValue());
    assertEquals(changedThirdDateValue, origForm.getFieldByClass(ThirdDateField.class).getValue());
  }

  private static Date getTestDate() {
    try {
      return new SimpleDateFormat("yyyyMMdd").parse("20141107");
    }
    catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
}
