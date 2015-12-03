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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.shared.extension.IllegalExtensionException;
import org.eclipse.scout.rt.shared.extension.dto.fixture.AbstractTemplateBox.GroupBoxInTemplateField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.AbstractTemplateBoxData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.OrigFormData.SecondUseOfTemplateBox;
import org.eclipse.scout.rt.shared.extension.dto.fixture.SpecialStringField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.SpecialStringFieldData;
import org.eclipse.scout.rt.shared.extension.dto.fixture.TreeBoxToTemplateField;
import org.eclipse.scout.rt.shared.extension.dto.fixture.TreeBoxToTemplateFieldData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormDataTemplateExtensionTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testFormDataTemplateExtensionFromAnnotation() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(TreeBoxToTemplateField.class);
    BEANS.get(IExtensionRegistry.class).register(TreeBoxToTemplateFieldData.class);
    doTestAllTemplateUses();
  }

  @Test
  public void testFormDataTemplateExtensionExplicit() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(TreeBoxToTemplateField.class, GroupBoxInTemplateField.class);
    BEANS.get(IExtensionRegistry.class).register(TreeBoxToTemplateFieldData.class, AbstractTemplateBoxData.class);
    doTestAllTemplateUses();
  }

  @Test
  public void testFormDataTemplateExtensionExplicitOnlyOneTemplateUse() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(TreeBoxToTemplateField.class, org.eclipse.scout.rt.shared.extension.dto.fixture.OrigForm.MainBox.SecondUseOfTemplateBox.class);
    BEANS.get(IExtensionRegistry.class).register(TreeBoxToTemplateFieldData.class, SecondUseOfTemplateBox.class);
    doTestOnlyInOneTemplateUse();
  }

  @Test
  public void testFormDataValueFieldTemplate() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(SpecialStringField.class);
    BEANS.get(IExtensionRegistry.class).register(SpecialStringFieldData.class);
    doValueFieldTemplateTest();
  }

  private void doValueFieldTemplateTest() throws Exception {
    String changedValue = "other value";
    OrigForm origForm = new OrigForm();
    origForm.initForm();

    assertEquals(SpecialStringField.INIT_VAL, origForm.getFieldByClass(SpecialStringField.class).getValue());

    // test export formData
    OrigFormData data = new OrigFormData();
    origForm.exportFormData(data);
    SpecialStringFieldData contributionData = data.getContribution(SpecialStringFieldData.class);
    assertEquals(SpecialStringField.INIT_VAL, contributionData.getValue());

    // test import formData
    contributionData.setValue(changedValue);
    origForm.importFormData(data);
    assertEquals(changedValue, origForm.getFieldByClass(SpecialStringField.class).getValue());
  }

  private void doTestOnlyInOneTemplateUse() throws Exception {
    // create and test form
    Set<Integer> valueOfSecondTree = CollectionUtility.hashSet(8, 9, 10);
    OrigForm origForm = new OrigForm();
    origForm.initForm();
    origForm.getSecondUseOfTemplateBox().getFieldByClass(TreeBoxToTemplateField.class).setValue(valueOfSecondTree);
    assertEquals(OrigForm.STRING_TEMPLATE_1_3, origForm.getFirstUseOfTemplateBox().getThirdStringInTemplateField().getValue());
    assertNull(origForm.getFirstUseOfTemplateBox().getGroupBoxInTemplateField().getFieldByClass(TreeBoxToTemplateField.class));
    assertEquals(valueOfSecondTree, origForm.getSecondUseOfTemplateBox().getFieldByClass(TreeBoxToTemplateField.class).getValue());

    // test export formData
    OrigFormData data = new OrigFormData();
    origForm.exportFormData(data);
    assertEquals(OrigForm.STRING_TEMPLATE_1_2, data.getFirstUseOfTemplateBox().getSecondStringInTemplate().getValue());
    assertEquals(OrigForm.STRING_TEMPLATE_2_3, data.getSecondUseOfTemplateBox().getThirdStringInTemplate().getValue());
    assertEquals(valueOfSecondTree, data.getSecondUseOfTemplateBox().getContribution(TreeBoxToTemplateFieldData.class).getValue());
    IllegalExtensionException expectedException = null;
    try {
      data.getFirstUseOfTemplateBox().getContribution(TreeBoxToTemplateFieldData.class);
    }
    catch (IllegalExtensionException e) {
      expectedException = e;
    }
    assertNotNull(expectedException);

    // test import formData
    String changedValueOfSecondStringInTemplate = "woeiru";
    String changedValueOfThirdStringInTemplate = "qecxv";
    Set<Integer> changedValueOfContributedTree = CollectionUtility.hashSet(11, 12, 13);
    data.getFirstUseOfTemplateBox().getSecondStringInTemplate().setValue(changedValueOfSecondStringInTemplate);
    data.getSecondUseOfTemplateBox().getThirdStringInTemplate().setValue(changedValueOfThirdStringInTemplate);
    data.getSecondUseOfTemplateBox().getContribution(TreeBoxToTemplateFieldData.class).setValue(changedValueOfContributedTree);
    origForm.importFormData(data);
    assertEquals(changedValueOfSecondStringInTemplate, origForm.getFirstUseOfTemplateBox().getSecondStringInTemplateField().getValue());
    assertEquals(changedValueOfThirdStringInTemplate, origForm.getSecondUseOfTemplateBox().getThirdStringInTemplateField().getValue());
    assertEquals(changedValueOfContributedTree, origForm.getSecondUseOfTemplateBox().getFieldByClass(TreeBoxToTemplateField.class).getValue());
  }

  private void doTestAllTemplateUses() throws Exception {
    // create and test form
    Set<Integer> valueOfSecondTree = CollectionUtility.hashSet(5, 6, 7);
    OrigForm origForm = new OrigForm();
    origForm.initForm();
    origForm.getSecondUseOfTemplateBox().getGroupBoxInTemplateField().getFieldByClass(TreeBoxToTemplateField.class).setValue(valueOfSecondTree);
    assertEquals(OrigForm.STRING_FIELD_ORIG_VALUE, origForm.getFirstStringField().getValue());
    assertEquals(OrigForm.STRING_TEMPLATE_1_1, origForm.getFirstUseOfTemplateBox().getFirstStringInTemplateField().getValue());
    assertEquals(TreeBoxToTemplateField.LIST_BOX_DEFAULT_VAL, origForm.getFirstUseOfTemplateBox().getGroupBoxInTemplateField().getFieldByClass(TreeBoxToTemplateField.class).getValue());
    assertEquals(valueOfSecondTree, origForm.getSecondUseOfTemplateBox().getGroupBoxInTemplateField().getFieldByClass(TreeBoxToTemplateField.class).getValue());

    // test export formData
    OrigFormData data = new OrigFormData();
    origForm.exportFormData(data);
    assertEquals(OrigForm.STRING_FIELD_ORIG_VALUE, data.getFirstString().getValue());
    assertEquals(OrigForm.STRING_TEMPLATE_2_2, data.getSecondUseOfTemplateBox().getSecondStringInTemplate().getValue());
    assertEquals(TreeBoxToTemplateField.LIST_BOX_DEFAULT_VAL, data.getFirstUseOfTemplateBox().getContribution(TreeBoxToTemplateFieldData.class).getValue());
    assertEquals(valueOfSecondTree, data.getSecondUseOfTemplateBox().getContribution(TreeBoxToTemplateFieldData.class).getValue());

    // test import formData
    String changedFirstStringValue = "asdf";
    String changedSecondStringInTemplateValue = "jklö";
    Set<Integer> changedValueOfFirstContributedTree = CollectionUtility.hashSet(11, 12, 13);
    Set<Integer> changedValueOfSecondContributedTree = CollectionUtility.hashSet(14, 15, 16);
    data.getFirstString().setValue(changedFirstStringValue);
    data.getSecondUseOfTemplateBox().getSecondStringInTemplate().setValue(changedSecondStringInTemplateValue);
    data.getFirstUseOfTemplateBox().getContribution(TreeBoxToTemplateFieldData.class).setValue(changedValueOfFirstContributedTree);
    data.getSecondUseOfTemplateBox().getContribution(TreeBoxToTemplateFieldData.class).setValue(changedValueOfSecondContributedTree);
    origForm.importFormData(data);
    assertEquals(changedFirstStringValue, origForm.getFirstStringField().getValue());
    assertEquals(changedSecondStringInTemplateValue, origForm.getSecondUseOfTemplateBox().getSecondStringInTemplateField().getValue());
    assertEquals(changedValueOfFirstContributedTree, origForm.getFirstUseOfTemplateBox().getGroupBoxInTemplateField().getFieldByClass(TreeBoxToTemplateField.class).getValue());
    assertEquals(changedValueOfSecondContributedTree, origForm.getSecondUseOfTemplateBox().getGroupBoxInTemplateField().getFieldByClass(TreeBoxToTemplateField.class).getValue());
  }
}
