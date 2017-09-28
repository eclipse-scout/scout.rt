/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MainBoxPropertyExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.PropertyExtensionData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.WrappedFormFieldOuterForm;
import org.eclipse.scout.rt.shared.extension.dto.fixture.WrappedFormFieldOuterFormData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 5.2
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class WrappedFormExtensionTest extends AbstractLocalExtensionTestCase {

  private static final Long TEST_LONG = 42L;
  private static final String TEST_STRING = "test";

  @Test
  public void testExportOuterForm() {
    BEANS.get(IExtensionRegistry.class).register(MainBoxPropertyExtension.class, MainBox.class);
    BEANS.get(IExtensionRegistry.class).register(PropertyExtensionData.class, OrigFormData.class);

    OrigForm innerForm = new OrigForm();
    innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).setLongValue(TEST_LONG);

    WrappedFormFieldOuterForm outerForm = new WrappedFormFieldOuterForm();
    outerForm.getWrappedFormField().setInnerForm(innerForm);
    outerForm.getStringField().setValue(TEST_STRING);

    WrappedFormFieldOuterFormData formData = new WrappedFormFieldOuterFormData();
    outerForm.exportFormData(formData);

    assertEquals(TEST_STRING, formData.getString().getValue());
    assertEquals(TEST_LONG, innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).getLongValue());
  }

  @Test
  public void testExportInnerForm() {
    BEANS.get(IExtensionRegistry.class).register(MainBoxPropertyExtension.class, MainBox.class);
    BEANS.get(IExtensionRegistry.class).register(PropertyExtensionData.class, OrigFormData.class);

    OrigForm innerForm = new OrigForm();
    innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).setLongValue(TEST_LONG);

    WrappedFormFieldOuterForm outerForm = new WrappedFormFieldOuterForm();
    outerForm.getWrappedFormField().setInnerForm(innerForm);
    outerForm.getStringField().setValue(TEST_STRING);

    OrigFormData formData = new OrigFormData();
    innerForm.exportFormData(formData);

    assertEquals(TEST_LONG, formData.getContribution(PropertyExtensionData.class).getLongValue());
  }

  @Test
  public void testImportOuterForm() {
    BEANS.get(IExtensionRegistry.class).register(MainBoxPropertyExtension.class, MainBox.class);
    BEANS.get(IExtensionRegistry.class).register(PropertyExtensionData.class, OrigFormData.class);

    OrigForm innerForm = new OrigForm();
    innerForm.getFirstStringField().setValue(TEST_STRING);
    innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).setLongValue(TEST_LONG);

    WrappedFormFieldOuterForm outerForm = new WrappedFormFieldOuterForm();
    outerForm.getWrappedFormField().setInnerForm(innerForm);

    WrappedFormFieldOuterFormData formData = new WrappedFormFieldOuterFormData();
    formData.getString().setValue(TEST_STRING);
    outerForm.importFormData(formData);

    assertEquals(TEST_STRING, outerForm.getStringField().getValue());
    assertEquals(TEST_LONG, innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).getLongValue());
  }

  @Test
  public void testImportInnerForm() {
    BEANS.get(IExtensionRegistry.class).register(MainBoxPropertyExtension.class, MainBox.class);
    BEANS.get(IExtensionRegistry.class).register(PropertyExtensionData.class, OrigFormData.class);

    OrigForm innerForm = new OrigForm();

    WrappedFormFieldOuterForm outerForm = new WrappedFormFieldOuterForm();
    outerForm.getWrappedFormField().setInnerForm(innerForm);

    OrigFormData formData = new OrigFormData();
    formData.getFirstString().setValue(TEST_STRING);
    formData.getContribution(PropertyExtensionData.class).setLongValue(TEST_LONG);
    innerForm.importFormData(formData);

    assertEquals(TEST_STRING, innerForm.getFirstStringField().getValue());
    assertEquals(TEST_LONG, innerForm.getMainBox().getExtension(MainBoxPropertyExtension.class).getLongValue());
  }
}
