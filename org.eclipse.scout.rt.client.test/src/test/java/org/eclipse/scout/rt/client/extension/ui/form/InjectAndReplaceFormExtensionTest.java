/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form;

import static org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.EXEC_VALIDATE_VALUE_OPERATION_NAME;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedForm;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedForm.BottomDetailBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedForm.BottomDetailBox.BottomDetailBoxField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedForm.CountryField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedForm.SalutationFieldEx;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedFormExtendedGroupWithField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedFormExtendedGroupWithField.TopBoxExtension.TopBoxStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.TopBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm.MainBox.TopBox.SalutationField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigFormStringFieldContribution;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigFormStringFieldExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class InjectAndReplaceFormExtensionTest extends AbstractLocalExtensionTestCase {

  /**
   * check behavior of an original field.
   */
  @Test
  public void testOrigForm() {
    OrigForm form = new OrigForm();
    form.getSalutationField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(SalutationField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * extend an original field (registration on original field).
   */
  @Test
  public void testOrigFormWithExtensionOnOrigForm() {
    BEANS.get(IExtensionRegistry.class).register(OrigFormStringFieldExtension.class, SalutationField.class);

    OrigForm form = new OrigForm();
    form.getSalutationField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(SalutationField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME),
        OrigForm.formatOperationLogEntry(OrigFormStringFieldExtension.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * check behavior of an original field (registration on extended field).
   */
  @Test
  public void testOrigFormWithExtensionOnExtendedForm() {
    BEANS.get(IExtensionRegistry.class).register(OrigFormStringFieldExtension.class, SalutationFieldEx.class);

    OrigForm form = new OrigForm();
    form.getSalutationField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(SalutationField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * contribute to an original field.
   */
  @Test
  public void testOrigFormWithContributionOnOrigForm() {
    BEANS.get(IExtensionRegistry.class).register(OrigFormStringFieldContribution.class, TopBox.class);

    OrigForm form = new OrigForm();
    form.getFieldByClass(OrigFormStringFieldContribution.class).setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(OrigFormStringFieldContribution.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * check behavior of a replaced field.
   */
  @Test
  public void testExtededFormReplace() {
    ExtendedForm form = new ExtendedForm();
    form.getSalutationField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(SalutationField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME),
        OrigForm.formatOperationLogEntry(SalutationFieldEx.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * extend behavior of a replaced field (registration on original field).
   */
  @Test
  public void testExtendedFormReplaceWithExtensionOnOrigForm() {
    BEANS.get(IExtensionRegistry.class).register(OrigFormStringFieldExtension.class, SalutationField.class);

    ExtendedForm form = new ExtendedForm();
    form.getSalutationField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(SalutationField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME),
        OrigForm.formatOperationLogEntry(SalutationFieldEx.class, EXEC_VALIDATE_VALUE_OPERATION_NAME),
        OrigForm.formatOperationLogEntry(OrigFormStringFieldExtension.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * extend behavior of a replaced field (registration on replaced field).
   */
  @Test
  public void testExtendedFormReplaceWithExtensionOnExtendedForm() {
    BEANS.get(IExtensionRegistry.class).register(OrigFormStringFieldExtension.class, SalutationFieldEx.class);

    ExtendedForm form = new ExtendedForm();
    form.getSalutationField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(SalutationField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME),
        OrigForm.formatOperationLogEntry(SalutationFieldEx.class, EXEC_VALIDATE_VALUE_OPERATION_NAME),
        OrigForm.formatOperationLogEntry(OrigFormStringFieldExtension.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * check behavior of an injected field.
   */
  @Test
  public void testExtededFormInject() {
    ExtendedForm form = new ExtendedForm();
    form.getCountryField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(CountryField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * extend behavior of an injected field.
   */
  @Test
  public void testExtededFormInjectWithExtension() {
    BEANS.get(IExtensionRegistry.class).register(OrigFormStringFieldExtension.class, CountryField.class);

    ExtendedForm form = new ExtendedForm();
    form.getCountryField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(CountryField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME),
        OrigForm.formatOperationLogEntry(OrigFormStringFieldExtension.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * check behavior of a field of an injected container.
   */
  @Test
  public void testExtededFormInjectContainer() {
    ExtendedForm form = new ExtendedForm();
    form.getBottomDetailBoxField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(BottomDetailBoxField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * extend behavior of a field of an injected container.
   */
  @Test
  public void testExtededFormInjectContainerWithExtension() {
    BEANS.get(IExtensionRegistry.class).register(OrigFormStringFieldExtension.class, BottomDetailBoxField.class);

    ExtendedForm form = new ExtendedForm();
    form.getBottomDetailBoxField().setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(BottomDetailBoxField.class, EXEC_VALIDATE_VALUE_OPERATION_NAME),
        OrigForm.formatOperationLogEntry(OrigFormStringFieldExtension.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * contribute new field to an injected container.
   */
  @Test
  public void testExtededFormInjectContainerWithContribution() {
    BEANS.get(IExtensionRegistry.class).register(OrigFormStringFieldContribution.class, BottomDetailBox.class);

    ExtendedForm form = new ExtendedForm();
    form.getFieldByClass(OrigFormStringFieldContribution.class).setValue("test");
    form.getOperations();
    assertOperations(form.getOperations(),
        OrigForm.formatOperationLogEntry(OrigFormStringFieldContribution.class, EXEC_VALIDATE_VALUE_OPERATION_NAME));
  }

  /**
   * extend an original group in a form extension (registration on original form).
   */
  @Test
  public void testExtendedFormExtendedGroupWithField() {
    BEANS.get(IExtensionRegistry.class).register(ExtendedFormExtendedGroupWithField.class);

    OrigForm form = new OrigForm();
    assertTrue(form.getFieldByClass(TopBoxStringField.class).isInitConfigDone());
  }

  public static void assertOperations(List<String> actual, String... expected) {
    ArrayList<String> expectedList = CollectionUtility.arrayList(expected);
    assertEquals(expectedList, actual);
  }
}
