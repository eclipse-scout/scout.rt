/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.extension;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm;
import org.eclipse.scout.rt.shared.extension.fixture.BasicFormExtension;
import org.eclipse.scout.rt.shared.extension.fixture.BasicFormExtension02;
import org.eclipse.scout.rt.shared.extension.fixture.NameFieldExtension;
import org.eclipse.scout.rt.shared.extension.fixture.NameFieldExtension02;
import org.eclipse.scout.rt.shared.extension.fixture.NestedBasicFormExtension;
import org.eclipse.scout.rt.shared.extension.fixture.StaticPojoContainerExtension;
import org.eclipse.scout.rt.shared.extension.fixture.TopBoxExtension;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ExtensionRegistryInstantiationTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testBasicFormExtension() {
    BEANS.get(IExtensionRegistry.class).register(BasicFormExtension.class);

    BasicForm basicForm = new BasicForm();
    assertExtensions(basicForm.getAllExtensions(), BasicFormExtension.class);
  }

  @Test
  public void testNameFieldExtension() {
    BEANS.get(IExtensionRegistry.class).register(NameFieldExtension.class);

    BasicForm basicForm = new BasicForm();
    assertExtensions(basicForm.getAllExtensions());

    assertExtensions(basicForm.getMainBox().getAllExtensions());
    assertExtensions(basicForm.getTopBox().getAllExtensions());
    assertExtensions(basicForm.getNameField().getAllExtensions(), NameFieldExtension.class);
    assertExtensions(basicForm.getBottomBox().getAllExtensions());
    assertExtensions(basicForm.getFirstNameField().getAllExtensions());
  }

  @Test
  public void testNestedBasicFormExtension() {
    BEANS.get(IExtensionRegistry.class).register(NestedBasicFormExtension.class);

    BasicForm basicForm = new BasicForm();
    assertExtensions(basicForm.getAllExtensions(), NestedBasicFormExtension.class);

    assertExtensions(basicForm.getMainBox().getAllExtensions());
    assertExtensions(basicForm.getTopBox().getAllExtensions());
    assertExtensions(basicForm.getNameField().getAllExtensions(), NestedBasicFormExtension.NameFieldExtension.class);
    assertExtensions(basicForm.getBottomBox().getAllExtensions());
    assertExtensions(basicForm.getFirstNameField().getAllExtensions(), NestedBasicFormExtension.FirstNameFieldExtension.class);
  }

  @Test
  public void testStaticPojoContainerExtension() {
    BEANS.get(IExtensionRegistry.class).register(StaticPojoContainerExtension.NameFieldExtension.class);
    BEANS.get(IExtensionRegistry.class).register(StaticPojoContainerExtension.FirstNameFieldExtension.class);

    BasicForm basicForm = new BasicForm();
    assertExtensions(basicForm.getAllExtensions());

    assertExtensions(basicForm.getMainBox().getAllExtensions());
    assertExtensions(basicForm.getTopBox().getAllExtensions());
    assertExtensions(basicForm.getNameField().getAllExtensions(), StaticPojoContainerExtension.NameFieldExtension.class);
    assertExtensions(basicForm.getBottomBox().getAllExtensions());
    assertExtensions(basicForm.getFirstNameField().getAllExtensions(), StaticPojoContainerExtension.FirstNameFieldExtension.class);
  }

  @Test
  public void testExtensionOrder() {
    IExtensionRegistry registry = BEANS.get(IExtensionRegistry.class);
    registry.register(NameFieldExtension.class);
    registry.register(NameFieldExtension02.class);

    // test code
    BasicForm basicForm = new BasicForm();
    assertExtensions(basicForm.getAllExtensions());
    assertExtensions(basicForm.getNameField().getAllExtensions(), NameFieldExtension02.class, NameFieldExtension.class);
  }

  @Test
  public void testExtensionForGroupBoxWithSecondInnerField() {
    IExtensionRegistry registry = BEANS.get(IExtensionRegistry.class);
    registry.register(TopBoxExtension.class);

    // test code
    BasicForm basicForm = new BasicForm();
    assertExtensions(basicForm.getAllExtensions());
    assertExtensions(basicForm.getTopBox().getAllExtensions(), TopBoxExtension.class);
    List<IFormField> topBoxFields = basicForm.getTopBox().getFields();
    assertEquals(2, topBoxFields.size());
    assertTrue(topBoxFields.get(0) instanceof BasicForm.MainBox.TopBox.NameField);
    assertFalse(topBoxFields.get(0) instanceof TopBoxExtension.SecondNameField);
    assertTrue(topBoxFields.get(1) instanceof TopBoxExtension.SecondNameField);
  }

  @Test
  public void testBasicFormExtensionWithSubclassedFormHandler() {
    IExtensionRegistry registry = BEANS.get(IExtensionRegistry.class);
    registry.register(BasicFormExtension02.class);

    // test code
    BasicForm basicForm = new BasicForm();
    assertExtensions(basicForm.getAllExtensions(), BasicFormExtension02.class);
    // because no filter is applied, the form handler is instantiated too, which it shouldn't.
    // as soon as a filter checks which inner classes to instantiate, the assertion must be changed to 'assertNull'.
    assertNotNull(basicForm.getContribution(BasicFormExtension02.ReadOnlyModifyHandler.class));
  }

  private void assertExtensions(List<? extends IExtension<?>> extensions, Class<?>... classes) {

    if (classes == null || classes.length == 0) {
      assertEquals(1, extensions.size());
      return;
    }
    assertEquals(classes.length, extensions.size() - 1);
    for (int i = 0; i < classes.length; i++) {
      assertEquals(classes[i], extensions.get(i).getClass());
    }
  }
}
