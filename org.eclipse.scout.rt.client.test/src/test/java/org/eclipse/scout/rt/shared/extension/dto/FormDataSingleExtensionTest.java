/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import java.math.BigDecimal;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.IllegalExtensionException;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.SingleFormExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.SingleFormExtension.SecondBigDecimalField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.SingleFormExtensionData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormDataSingleExtensionTest extends AbstractLocalExtensionTestCase {

  @Test(expected = IllegalExtensionException.class)
  public void testFormDataSingleExtensionExplicitInnerType() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(SecondBigDecimalField.class, MainBox.class);
    BEANS.get(IExtensionRegistry.class).register(SingleFormExtensionData.class, OrigFormData.class);
    doTest();
  }

  @Test
  public void testFormDataSingleExtensionExplicit() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(SingleFormExtension.class, OrigForm.class);
    BEANS.get(IExtensionRegistry.class).register(SingleFormExtensionData.class, OrigFormData.class);
    doTest();
  }

  @Test
  public void testFormDataSingleExtensionAnnotation() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(SingleFormExtension.class);
    BEANS.get(IExtensionRegistry.class).register(SingleFormExtensionData.class);
    doTest();
  }

  private void doTest() throws Exception {
    // create and test form
    OrigForm origForm = new OrigForm();
    origForm.initForm();
    assertEquals(OrigForm.STRING_FIELD_ORIG_VALUE, origForm.getFirstStringField().getValue());
    assertEquals(SingleFormExtension.BIG_DECIMAL_FIELD_ORIG_VALUE, origForm.getFieldByClass(SecondBigDecimalField.class).getValue());

    // test formData export
    OrigFormData data = new OrigFormData();
    origForm.exportFormData(data);
    assertEquals(OrigForm.STRING_FIELD_ORIG_VALUE, data.getFirstString().getValue());
    assertEquals(SingleFormExtension.BIG_DECIMAL_FIELD_ORIG_VALUE, data.getContribution(SingleFormExtensionData.class).getSecondBigDecimal().getValue());

    // test formData import
    String changedFirstStringVal = "my changed value";
    BigDecimal changedSecondBigDecimalVal = new BigDecimal("100.22");
    data.getFirstString().setValue(changedFirstStringVal);
    data.getContribution(SingleFormExtensionData.class).getSecondBigDecimal().setValue(changedSecondBigDecimalVal);
    origForm.importFormData(data);
    assertEquals(changedFirstStringVal, origForm.getFirstStringField().getValue());
    assertEquals(changedSecondBigDecimalVal, origForm.getFieldByClass(SecondBigDecimalField.class).getValue());
  }
}
