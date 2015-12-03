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

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.dto.fixture.FormPropertyExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MainBoxPropertyExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData.SecondUseOfTemplateBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.PropertyExtensionData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.TreeBoxPropertyExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.TreeBoxPropertyExtensionData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.TreeBoxToTemplateField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.TreeBoxToTemplateFieldData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormDataPropertyExtensionTest extends AbstractLocalExtensionTestCase {
  @Test
  public void testPropertyExtensionFormFieldExplicit() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(MainBoxPropertyExtension.class, MainBox.class);
    BEANS.get(IExtensionRegistry.class).register(PropertyExtensionData.class, OrigFormData.class);
    doFormFieldTest();
  }

  @Test
  public void testPropertyExtensionFormFieldAnnotation() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(MainBoxPropertyExtension.class);
    BEANS.get(IExtensionRegistry.class).register(PropertyExtensionData.class);
    doFormFieldTest();
  }

  @Test
  public void testPropertyExtensionFormExplicit() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(FormPropertyExtension.class, OrigForm.class);
    BEANS.get(IExtensionRegistry.class).register(PropertyExtensionData.class, OrigFormData.class);
    doFormTest();
  }

  @Test
  public void testPropertyExtensionToFormFieldContribution() throws Exception {
    // contribute a new field to the second template use in the form
    BEANS.get(IExtensionRegistry.class).register(TreeBoxToTemplateField.class, org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox.SecondUseOfTemplateBox.class);
    BEANS.get(IExtensionRegistry.class).register(TreeBoxToTemplateFieldData.class, SecondUseOfTemplateBox.class);

    // add a property to the contribution from above
    BEANS.get(IExtensionRegistry.class).register(TreeBoxPropertyExtension.class, TreeBoxToTemplateField.class);
    BEANS.get(IExtensionRegistry.class).register(TreeBoxPropertyExtensionData.class, TreeBoxToTemplateFieldData.class);

    doExtensionOfContributionTest();
  }

  private void doExtensionOfContributionTest() throws Exception {
    Long exportTestVal = Long.valueOf(501);
    Long importTestVal = Long.valueOf(601);

    OrigForm f = new OrigForm();
    TreeBoxPropertyExtension propertyExtensionClient = f.getSecondUseOfTemplateBox().getContribution(TreeBoxToTemplateField.class).getExtension(TreeBoxPropertyExtension.class);
    propertyExtensionClient.setLongValue(exportTestVal);

    // test formData export
    OrigFormData data = new OrigFormData();
    f.exportFormData(data);
    TreeBoxPropertyExtensionData propertyContributionShared = data.getSecondUseOfTemplateBox().getContribution(TreeBoxToTemplateFieldData.class).getContribution(TreeBoxPropertyExtensionData.class);
    Assert.assertEquals(exportTestVal, propertyContributionShared.getLongValue());

    // test formData import
    propertyContributionShared.setLongValue(importTestVal);
    f.importFormData(data);
    Assert.assertEquals(importTestVal, propertyExtensionClient.getLongValue());
  }

  private void doFormTest() throws Exception {
    Long exportTestVal = Long.valueOf(201);
    Long importTestVal = Long.valueOf(301);
    OrigForm f = new OrigForm();
    f.getExtension(FormPropertyExtension.class).setLongValue(exportTestVal);

    // test formData export
    OrigFormData data = new OrigFormData();
    f.exportFormData(data);
    PropertyExtensionData propertyExtension = data.getContribution(PropertyExtensionData.class);
    Assert.assertNotNull(propertyExtension);
    Assert.assertEquals(exportTestVal, propertyExtension.getLongValueProperty().getValue());

    // test formData import
    propertyExtension.setLongValue(importTestVal);
    f.importFormData(data);
    Assert.assertEquals(importTestVal, f.getExtension(FormPropertyExtension.class).getLongValue());
  }

  private void doFormFieldTest() throws Exception {
    Long exportTestVal = Long.valueOf(101);
    Long importTestVal = Long.valueOf(401);
    OrigForm f = new OrigForm();
    f.getMainBox().getExtension(MainBoxPropertyExtension.class).setLongValue(exportTestVal);

    // test formData export
    OrigFormData data = new OrigFormData();
    f.exportFormData(data);
    PropertyExtensionData propertyExtension = data.getContribution(PropertyExtensionData.class);
    Assert.assertNotNull(propertyExtension);
    Assert.assertEquals(exportTestVal, propertyExtension.getLongValueProperty().getValue());

    // test formData import
    propertyExtension.setLongValue(importTestVal);
    f.importFormData(data);
    Assert.assertEquals(importTestVal, f.getMainBox().getExtension(MainBoxPropertyExtension.class).getLongValue());
  }
}
