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
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.classid.ClassIdentifier;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.extension.dto.fixture.MultipleExtGroupBoxExtension;
import org.eclipse.scout.rt.shared.extension.dto.fixture.SpecialStringField;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.BottomBox.FirstNameField;
import org.eclipse.scout.rt.shared.extension.fixture.BasicForm.MainBox.TopBox.NameField;
import org.eclipse.scout.rt.shared.extension.fixture.BasicFormExtension;
import org.eclipse.scout.rt.shared.extension.fixture.InvalidExtension;
import org.eclipse.scout.rt.shared.extension.fixture.NameFieldExtension;
import org.eclipse.scout.rt.shared.extension.fixture.NestedBasicFormExtension;
import org.eclipse.scout.rt.shared.extension.fixture.PojoContainerExtension;
import org.eclipse.scout.rt.shared.extension.fixture.StaticPojoContainerExtension;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ExtensionRegistryExtensionRegistrationTest {

  private ExtensionRegistryTest m_clientExtensionManager;

  @Before
  public void before() {
    m_clientExtensionManager = new ExtensionRegistryTest();
  }

  @Test
  public void testBasicFormExtension() {
    m_clientExtensionManager.register(BasicFormExtension.class, BasicForm.class);
    assertBasicFormExtension();
  }

  @Test
  public void testBasicFormExtensionAutoDetection() {
    m_clientExtensionManager.register(BasicFormExtension.class);
    assertBasicFormExtension();
  }

  private void assertBasicFormExtension() {
    assertEquals(1, m_clientExtensionManager.getExtensionMap().size());
    Set<ExtensionRegistryItem> items = m_clientExtensionManager.getModelExtensionItemsFor(BasicForm.class);
    assertEquals(1, items.size());
    assertModelExtensionItem(CollectionUtility.firstElement(items), BasicForm.class, null, BasicFormExtension.class, 1);
  }

  @Test
  public void testNameFieldExtension() {
    m_clientExtensionManager.register(NameFieldExtension.class, NameField.class);
    assertNameFieldExtension();
  }

  @Test
  public void testNameFieldExtensionAutoDetection() {
    m_clientExtensionManager.register(NameFieldExtension.class);
    assertNameFieldExtension();
  }

  private void assertNameFieldExtension() {
    assertEquals(1, m_clientExtensionManager.getExtensionMap().size());
    Set<ExtensionRegistryItem> items = m_clientExtensionManager.getModelExtensionItemsFor(NameField.class);
    assertEquals(1, items.size());
    assertModelExtensionItem(CollectionUtility.firstElement(items), NameField.class, null, NameFieldExtension.class, 1);
  }

  @Test
  public void testRemoveNestedBasicFormExtension() {
    m_clientExtensionManager.register(NestedBasicFormExtension.class, BasicForm.class);
    assertNestedBasicFormExtension();
    boolean changed = m_clientExtensionManager.deregister(NestedBasicFormExtension.class);
    assertTrue(changed);
    assertEmptyExtensionManager();
  }

  private void assertEmptyExtensionManager() {
    assertEquals(0, m_clientExtensionManager.getExtensionMap().size());

    Set<ExtensionRegistryItem> items = m_clientExtensionManager.getModelExtensionItemsFor(BasicForm.class);
    assertEquals(0, items.size());

    items = m_clientExtensionManager.getModelExtensionItemsFor(FirstNameField.class);
    assertEquals(0, items.size());

    items = m_clientExtensionManager.getModelExtensionItemsFor(NameField.class);
    assertEquals(0, items.size());
  }

  @Test
  public void testNestedBasicFormExtension() {
    m_clientExtensionManager.register(NestedBasicFormExtension.class, BasicForm.class);
    assertNestedBasicFormExtension();
  }

  @Test
  public void testNestedBasicFormExtensionAutoDetection() {
    m_clientExtensionManager.register(NestedBasicFormExtension.class);
    assertNestedBasicFormExtension();
  }

  private void assertNestedBasicFormExtension() {
    assertEquals(3, m_clientExtensionManager.getExtensionMap().size());

    Set<ExtensionRegistryItem> items = m_clientExtensionManager.getModelExtensionItemsFor(BasicForm.class);
    assertEquals(1, items.size());
    assertModelExtensionItem(CollectionUtility.firstElement(items), BasicForm.class, null, NestedBasicFormExtension.class, 1);

    try {
      m_clientExtensionManager.pushScope(BasicForm.class);
      items = m_clientExtensionManager.getModelExtensionItemsFor(FirstNameField.class);
      assertEquals(1, items.size());
      assertModelExtensionItem(CollectionUtility.firstElement(items), FirstNameField.class, NestedBasicFormExtension.class, NestedBasicFormExtension.FirstNameFieldExtension.class, 2);

      items = m_clientExtensionManager.getModelExtensionItemsFor(NameField.class);
      assertEquals(1, items.size());
      assertModelExtensionItem(CollectionUtility.firstElement(items), NameField.class, NestedBasicFormExtension.class, NestedBasicFormExtension.NameFieldExtension.class, 3);
    }
    finally {
      m_clientExtensionManager.popScope();
    }
  }

  @Test
  public void testStaticPojoContainerExtension() {
    // add first name before name because eclipse compiler sorts nested classes by name
    m_clientExtensionManager.register(StaticPojoContainerExtension.FirstNameFieldExtension.class);
    m_clientExtensionManager.register(StaticPojoContainerExtension.NameFieldExtension.class);
    assertStaticPojoContainerExtension();
  }

  @Test(expected = IllegalExtensionException.class)
  public void testStaticPojoContainerExtensionAutoRegister() {
    m_clientExtensionManager.register(StaticPojoContainerExtension.class);
  }

  private void assertStaticPojoContainerExtension() {
    assertEquals(2, m_clientExtensionManager.getExtensionMap().size());

    Set<ExtensionRegistryItem> items = m_clientExtensionManager.getModelExtensionItemsFor(FirstNameField.class);
    assertEquals(1, items.size());
    assertModelExtensionItem(CollectionUtility.firstElement(items), FirstNameField.class, null, StaticPojoContainerExtension.FirstNameFieldExtension.class, 1);

    items = m_clientExtensionManager.getModelExtensionItemsFor(NameField.class);
    assertEquals(1, items.size());
    assertModelExtensionItem(CollectionUtility.firstElement(items), NameField.class, null, StaticPojoContainerExtension.NameFieldExtension.class, 2);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testContributionToWrongOwner() {
    m_clientExtensionManager.register(SpecialStringField.class, NameField.class);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testExtensionToWrongOwner() {
    m_clientExtensionManager.register(MultipleExtGroupBoxExtension.class, NameField.class);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testPojoContainerExtension() {
    m_clientExtensionManager.register(PojoContainerExtension.NameFieldExtension.class, NameField.class);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testPojoContainerExtensionAutoRegister() {
    m_clientExtensionManager.register(PojoContainerExtension.class);
  }

  @Test(expected = IllegalExtensionException.class)
  public void testInvalidExtension() {
    m_clientExtensionManager.register(InvalidExtension.class);
  }

  private void assertModelExtensionItem(ExtensionRegistryItem item, Class<?> expectedOriginalClass, Class<?> expectedDeclaringClass, Class<?> expectingExtensionClass, long expectedOrder) {
    assertSame(expectedOriginalClass, item.getOriginalClass());
    assertSame(expectedDeclaringClass, item.getDeclaringClass());
    assertSame(expectingExtensionClass, item.getExtensionClass());
    assertEquals(expectedOrder, item.getOrder());
  }

  /**
   * extension to make the protected super methods accessible.
   */
  protected static class ExtensionRegistryTest extends ExtensionRegistry {
    @Override
    protected Map<ClassIdentifier, List<ExtensionRegistryItem>> getExtensionMap() {
      return super.getExtensionMap();
    }

    @Override
    protected Set<ExtensionRegistryItem> getModelExtensionItemsFor(Class<?> ownerClass) {
      return super.getModelExtensionItemsFor(ownerClass);
    }
  }
}
