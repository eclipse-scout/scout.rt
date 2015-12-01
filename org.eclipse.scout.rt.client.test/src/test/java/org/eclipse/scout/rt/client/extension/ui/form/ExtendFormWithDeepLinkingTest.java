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
package org.eclipse.scout.rt.client.extension.ui.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.eclipse.scout.extension.AbstractLocalExtensionTestCase;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateFieldsBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateFieldsBox.BottomStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateFieldsBox.TopStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateGroupsBox.BottomFieldsBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.AbstractTemplateGroupsBox.TopFieldsBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ContributedTestField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedForm;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.ExtendedFormExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.FirstTemplateBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.FirstTemplateBox.MiddleStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.MainBoxStringField;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.MultiTemplateUsageForm.MainBox.SecondTemplateBox;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.OrigForm;
import org.eclipse.scout.rt.client.extension.ui.form.fixture.TemplateStringFieldExtension;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
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
public class ExtendFormWithDeepLinkingTest extends AbstractLocalExtensionTestCase {

  @Test
  public void testSetup() throws Exception {
    MultiTemplateUsageForm form = new MultiTemplateUsageForm();
    assertTypes(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class, MainBoxStringField.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertTypes(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertTypes(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertTypes(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertTypes(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
  }

  @Test
  public void testContributeToAllTemplateUsages() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(ContributedTestField.class, AbstractTemplateFieldsBox.class);

    MultiTemplateUsageForm form = new MultiTemplateUsageForm();
    assertTypes(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class, MainBoxStringField.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertTypes(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertTypes(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class, ContributedTestField.class);
    assertTypes(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class, ContributedTestField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertTypes(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertTypes(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class, ContributedTestField.class);
    assertTypes(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class, ContributedTestField.class);
  }

  @Test
  public void testContributeOnlyToOneTemplateUsage() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(ContributedTestField.class, new ClassIdentifier(TopFieldsBox.class, AbstractTemplateFieldsBox.class), null);

    MultiTemplateUsageForm form = new MultiTemplateUsageForm();
    assertTypes(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class, MainBoxStringField.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertTypes(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertTypes(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class, ContributedTestField.class);
    assertTypes(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertTypes(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertTypes(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class, ContributedTestField.class);
    assertTypes(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
  }

  @Test
  public void testContributeOnlyToOneNestedTemplateUsage() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(ContributedTestField.class, new ClassIdentifier(FirstTemplateBox.class, TopFieldsBox.class, AbstractTemplateFieldsBox.class), null);

    MultiTemplateUsageForm form = new MultiTemplateUsageForm();
    assertTypes(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class, MainBoxStringField.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertTypes(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertTypes(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class, ContributedTestField.class);
    assertTypes(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertTypes(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertTypes(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
  }

  @Test
  public void testContributeOnlyToOneNestedTemplateUsageOverSpecified() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(ContributedTestField.class,
        new ClassIdentifier(MultiTemplateUsageForm.class, MultiTemplateUsageForm.MainBox.class, FirstTemplateBox.class, TopFieldsBox.class, AbstractTemplateFieldsBox.class), null);

    MultiTemplateUsageForm form = new MultiTemplateUsageForm();
    assertTypes(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class, MainBoxStringField.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertTypes(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertTypes(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class, ContributedTestField.class);
    assertTypes(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertTypes(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertTypes(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
  }

  @Test
  public void testExtendAllStringFields() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(TemplateStringFieldExtension.class, AbstractStringField.class);

    MultiTemplateUsageForm form = new MultiTemplateUsageForm();
    form.initForm();
    assertTypes(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class, MainBoxStringField.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertTypes(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertTypes(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertTypes(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertTypes(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // behavior checks
    assertEquals("A", form.getFirstTemplateBox().getTopFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getFirstTemplateBox().getTopFieldsBox().getBottomStringField().getValue());
    assertEquals("A", form.getMiddleStringField().getValue());
    assertEquals("A", form.getFirstTemplateBox().getBottomFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getFirstTemplateBox().getBottomFieldsBox().getBottomStringField().getValue());

    assertEquals("A", form.getSecondTemplateBox().getTopFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getSecondTemplateBox().getTopFieldsBox().getBottomStringField().getValue());
    assertEquals("A", form.getSecondTemplateBox().getBottomFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getSecondTemplateBox().getBottomFieldsBox().getBottomStringField().getValue());
  }

  @Test
  public void testExtendOnlyToOneTemplateUses() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(TemplateStringFieldExtension.class, new ClassIdentifier(TopFieldsBox.class, AbstractStringField.class), null);

    MultiTemplateUsageForm form = new MultiTemplateUsageForm();
    form.initForm();
    assertTypes(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class, MainBoxStringField.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertTypes(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertTypes(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertTypes(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertTypes(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // behavior checks
    assertEquals("A", form.getFirstTemplateBox().getTopFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getFirstTemplateBox().getTopFieldsBox().getBottomStringField().getValue());
    assertNull(form.getMiddleStringField().getValue());
    assertNull(form.getFirstTemplateBox().getBottomFieldsBox().getTopStringField().getValue());
    assertNull(form.getFirstTemplateBox().getBottomFieldsBox().getBottomStringField().getValue());

    assertEquals("A", form.getSecondTemplateBox().getTopFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getSecondTemplateBox().getTopFieldsBox().getBottomStringField().getValue());
    assertNull(form.getSecondTemplateBox().getBottomFieldsBox().getTopStringField().getValue());
    assertNull(form.getSecondTemplateBox().getBottomFieldsBox().getBottomStringField().getValue());
  }

  @Test
  public void testExtendOnlyToOneTemplateUsesOverSpecified() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(TemplateStringFieldExtension.class, new ClassIdentifier(MultiTemplateUsageForm.class, TopFieldsBox.class, AbstractStringField.class), null);

    MultiTemplateUsageForm form = new MultiTemplateUsageForm();
    form.initForm();
    assertTypes(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class, MainBoxStringField.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertTypes(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertTypes(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertTypes(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertTypes(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // behavior checks
    assertEquals("A", form.getFirstTemplateBox().getTopFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getFirstTemplateBox().getTopFieldsBox().getBottomStringField().getValue());
    assertNull(form.getMiddleStringField().getValue());
    assertNull(form.getFirstTemplateBox().getBottomFieldsBox().getTopStringField().getValue());
    assertNull(form.getFirstTemplateBox().getBottomFieldsBox().getBottomStringField().getValue());

    assertEquals("A", form.getSecondTemplateBox().getTopFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getSecondTemplateBox().getTopFieldsBox().getBottomStringField().getValue());
    assertNull(form.getSecondTemplateBox().getBottomFieldsBox().getTopStringField().getValue());
    assertNull(form.getSecondTemplateBox().getBottomFieldsBox().getBottomStringField().getValue());
  }

  @Test
  public void testExtendTemplate() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(TemplateStringFieldExtension.class, new ClassIdentifier(AbstractTemplateFieldsBox.class, AbstractStringField.class), null);

    MultiTemplateUsageForm form = new MultiTemplateUsageForm();
    form.initForm();
    assertTypes(form.getMainBox().getFields(), FirstTemplateBox.class, SecondTemplateBox.class, MainBoxStringField.class);

    // first template box
    FirstTemplateBox firstTemplateBox = form.getFirstTemplateBox();
    assertTypes(firstTemplateBox.getFields(), TopFieldsBox.class, MiddleStringField.class, BottomFieldsBox.class);
    assertTypes(firstTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(firstTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // second template box
    SecondTemplateBox secondTemplateBox = form.getSecondTemplateBox();
    assertTypes(secondTemplateBox.getFields(), TopFieldsBox.class, BottomFieldsBox.class);
    assertTypes(secondTemplateBox.getTopFieldsBox().getFields(), TopStringField.class, BottomStringField.class);
    assertTypes(secondTemplateBox.getBottomFieldsBox().getFields(), TopStringField.class, BottomStringField.class);

    // behavior checks
    assertEquals("A", form.getFirstTemplateBox().getTopFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getFirstTemplateBox().getTopFieldsBox().getBottomStringField().getValue());
    assertNull(form.getMiddleStringField().getValue());
    assertEquals("A", form.getFirstTemplateBox().getBottomFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getFirstTemplateBox().getBottomFieldsBox().getBottomStringField().getValue());

    assertEquals("A", form.getSecondTemplateBox().getTopFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getSecondTemplateBox().getTopFieldsBox().getBottomStringField().getValue());
    assertEquals("A", form.getSecondTemplateBox().getBottomFieldsBox().getTopStringField().getValue());
    assertEquals("A", form.getSecondTemplateBox().getBottomFieldsBox().getBottomStringField().getValue());
  }

  @Test
  public void testDeepLinkingWithExtendsAnnotationOrigForm() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(ExtendedFormExtension.class);

    OrigForm form = new OrigForm();
    form.initForm();

    assertTypes(form.getMainBox().getFields(), OrigForm.MainBox.TopBox.class, OrigForm.MainBox.BottomBox.class);
  }

  @Test
  public void testDeepLinkingWithExtendsAnnotationExtendedForm() throws Exception {
    BEANS.get(IExtensionRegistry.class).register(ExtendedFormExtension.class);

    ExtendedForm form = new ExtendedForm();
    form.initForm();

    assertTypes(form.getMainBox().getFields(), ExtendedFormExtension.DetailBox.class, OrigForm.MainBox.TopBox.class, OrigForm.MainBox.BottomBox.class);
  }

  protected static void assertTypes(List<?> objects, Class<?>... expectedTypes) {
    assertEquals(expectedTypes.length, CollectionUtility.size(objects));

    for (int i = 0; i < expectedTypes.length; i++) {
      assertSame(expectedTypes[i], objects.get(i).getClass());
    }
  }
}
