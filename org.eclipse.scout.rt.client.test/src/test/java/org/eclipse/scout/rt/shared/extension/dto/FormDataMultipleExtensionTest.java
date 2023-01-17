/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension.dto;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultipleExtGroupBoxExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultipleExtGroupBoxExtension.SecondBigDecimalField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultipleExtGroupBoxExtension.ThirdDateField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultipleExtGroupBoxExtensionData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormDataMultipleExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testFormDataMultipleExtensionsExplicit() {
    BEANS.get(IExtensionRegistry.class).register(MultipleExtGroupBoxExtension.class, MainBox.class);
    BEANS.get(IExtensionRegistry.class).register(MultipleExtGroupBoxExtensionData.class, OrigFormData.class);
    doTest();
  }

  @Test
  public void testFormDataMultipleExtensionsFromAnnotation() {
    BEANS.get(IExtensionRegistry.class).register(MultipleExtGroupBoxExtension.class);
    BEANS.get(IExtensionRegistry.class).register(MultipleExtGroupBoxExtensionData.class);
    doTest();
  }

  private void doTest() {
    // create and test form
    OrigForm origForm = new OrigForm();
    origForm.init();
    assertEquals(OrigForm.STRING_FIELD_ORIG_VALUE, origForm.getFirstStringField().getValue());
    assertEquals(MultipleExtGroupBoxExtension.BIGDECIMAL_FIELD_ORIG_VAL, origForm.getFieldByClass(SecondBigDecimalField.class).getValue());
    assertEquals(MultipleExtGroupBoxExtension.DATE_FIELD_ORIG_VAL, origForm.getFieldByClass(ThirdDateField.class).getValue());

    // test formData export
    OrigFormData data = new OrigFormData();
    origForm.exportFormData(data);
    assertEquals(OrigForm.STRING_FIELD_ORIG_VALUE, data.getFirstString().getValue());
    assertEquals(MultipleExtGroupBoxExtension.BIGDECIMAL_FIELD_ORIG_VAL, data.getContribution(MultipleExtGroupBoxExtensionData.class).getSecondBigDecimal().getValue());
    assertEquals(MultipleExtGroupBoxExtension.DATE_FIELD_ORIG_VAL, data.getContribution(MultipleExtGroupBoxExtensionData.class).getThirdDate().getValue());

    // test formData import
    String changedFirstStringValue = "a changed value";
    BigDecimal changedSecondDoubleValue = BigDecimal.valueOf(100.300032);
    Date changedThirdDateValue = getTestDate();
    data.getFirstString().setValue(changedFirstStringValue);
    data.getContribution(MultipleExtGroupBoxExtensionData.class).getSecondBigDecimal().setValue(changedSecondDoubleValue);
    data.getContribution(MultipleExtGroupBoxExtensionData.class).getThirdDate().setValue(changedThirdDateValue);
    origForm.importFormData(data);
    assertEquals(changedFirstStringValue, origForm.getFirstStringField().getValue());
    assertEquals(changedSecondDoubleValue, origForm.getFieldByClass(SecondBigDecimalField.class).getValue());
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
