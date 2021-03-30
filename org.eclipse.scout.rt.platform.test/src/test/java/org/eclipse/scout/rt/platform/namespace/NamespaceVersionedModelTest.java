/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.namespace;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.namespace.NamespaceVersionedModel.VersionedItems;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

public class NamespaceVersionedModelTest {

  @Test
  public void testBasic() {
    NamespaceVersionedModel<INamespaceVersioned> inventory = createInventory(Arrays.asList("alfa", "bravo"), Arrays.asList(item("alfa-5")));
    assertItems(
        Arrays.asList(),
        inventory.getItems(versions(), versions(version("alfa-3"))));
    assertItems(
        Arrays.asList(),
        inventory.getItems(versions(version("alfa-0")), versions(version("alfa-3"))));
    assertItems(
        Arrays.asList(item("alfa-5")),
        inventory.getItems(versions(version("alfa-0")), versions(version("alfa-5"))));
    assertItems(
        Arrays.asList(item("alfa-5")),
        inventory.getItems(versions(version("alfa-3")), versions(version("alfa-5"))));
    assertItems(
        Arrays.asList(item("alfa-5")),
        inventory.getItems(versions(version("alfa-3")), versions(version("alfa-5"), version("bravo-0"))));
    assertItems(
        Arrays.asList(item("alfa-5")),
        inventory.getItems());
  }

  @Test
  public void testTransitivDependenciesTwoStage() {
    NamespaceVersionedModel<INamespaceVersioned> inventory = createInventory(
        item("alfa-4"),
        item("alfa-5"),
        item("charlie-8", dep("alfa-4")),
        item("charlie-9", dep("alfa-4")),
        item("charlie-10", dep("alfa-5")));

    assertItems(
        Arrays.asList(item("alfa-4"), item("charlie-8"), item("charlie-9"), item("alfa-5"), item("charlie-10")),
        inventory.getItems(versions(version("alfa-3"), version("charlie-7")), versions(version("alfa-5"), version("charlie-10"))));

    assertItems(
        Arrays.asList(item("alfa-4"), item("charlie-8"), item("charlie-9"), item("alfa-5"), item("charlie-10")),
        inventory.getItems());
  }

  @Test
  public void testTransitivDependenciesThreeStage() {
    NamespaceVersionedModel<INamespaceVersioned> inventory = createInventory(
        item("alfa-4"),
        item("alfa-5"),
        item("bravo-15", dep("alfa-4")),
        item("bravo-18", dep("alfa-5")),
        item("charlie-8", dep("alfa-4")),
        item("charlie-9", dep("bravo-15")),
        item("charlie-10", dep("bravo-18")));

    assertItems(
        Arrays.asList(item("alfa-4"), item("bravo-15"), item("charlie-8"), item("charlie-9"), item("alfa-5"), item("bravo-18"), item("charlie-10")),
        inventory.getItems(versions(version("alfa-3"), version("bravo-14"), version("charlie-7")), versions(version("alfa-5"), version("bravo-18"), version("charlie-10"))));

    assertItems(
        Arrays.asList(item("alfa-4"), item("bravo-15"), item("charlie-8"), item("charlie-9"), item("alfa-5"), item("bravo-18"), item("charlie-10")),
        inventory.getItems());
  }

  @Test
  public void testTransitivDependenciesThreeStageInconsistentInputVersions() {
    NamespaceVersionedModel<INamespaceVersioned> inventory = createInventory(
        item("alfa-4"),
        item("alfa-5"),
        item("bravo-15", dep("alfa-4")),
        item("bravo-18", dep("alfa-5")),
        item("charlie-8", dep("alfa-4")),
        item("charlie-9", dep("bravo-15")),
        item("charlie-10", dep("bravo-18")));

    VersionedItems<INamespaceVersioned> items = inventory.getItems(versions(version("alfa-5"), version("bravo-14"), version("charlie-7")), versions(version("alfa-5"), version("bravo-15"), version("charlie-10")));

    assertFalse(items.isValid());
    assertItems(Arrays.asList(item("bravo-15"), item("charlie-8"), item("charlie-9"), item("charlie-10")), items.getItems());
    assertItemsUnordered(Arrays.asList(item("bravo-18")), items.getUnsatisfiedDependencies());
  }

  @Test
  public void testValidations() {
    assertThrows(AssertionException.class, () -> createInventory(
        item("alfa-4", dep("charlie-3"), dep("charlie-4")), item("charlie-4"), item("charlie-3"))); // multiple dependencies to same name
    assertThrows(AssertionException.class, () -> createInventory(
        item("alfa-4", dep("charlie-8")),
        item("charlie-8", dep("bravo-6")),
        item("bravo-6", dep("alfa-4")))); // dependency cycle
    assertThrows(AssertionException.class, () -> createInventory(
        item("alfa-4", dep("charlie-8")),
        item("charlie-8", dep("bravo-6")),
        item("bravo-6", dep("alfa-4")))); // dependency cycle
    assertThrows(AssertionException.class, () -> createInventory(
        item("alfa-5", dep("charlie-3")),
        item("charlie-5", dep("alfa-3")))); // dependency cycle through transitivity
  }

  @Test
  public void testGetItemsDuplicateNamespace() {
    NamespaceVersionedModel<INamespaceVersioned> inventory = createInventory(
        item("alfa-4"),
        item("alfa-5"),
        item("charlie-8", dep("alfa-4")),
        item("charlie-9", dep("alfa-4")),
        item("charlie-10", dep("alfa-5")));

    inventory.getItems(versions(version("alfa-4"), version("charlie-8")), versions(version("alfa-5"), version("charlie-10"))); // no exception

    assertThrows(AssertionException.class, () -> inventory.getItems(versions(version("alfa-4"), version("alfa-5"), version("charlie-8")), versions(version("alfa-5"), version("charlie-10")))); // same from version
    assertThrows(AssertionException.class, () -> inventory.getItems(versions(version("alfa-4"), version("charlie-8")), versions(version("alfa-5"), version("charlie-10"), version("charlie-8")))); // same to version
    assertThrows(AssertionException.class,
        () -> inventory.getItems(versions(version("alfa-4"), version("alfa-5"), version("charlie-8"), version("charlie-10")), versions(version("alfa-4"), version("alfa-5"), version("charlie-10"), version("charlie-8")))); // same to version
  }

  protected void assertItems(List<? extends INamespaceVersioned> expected, VersionedItems<? extends INamespaceVersioned> actual) {
    assertTrue(actual.isValid());
    assertItems(expected, actual.getItems());
  }

  protected void assertItems(List<? extends INamespaceVersioned> expected, List<? extends INamespaceVersioned> actual) {
    assertEquals(
        expected.stream().map(this::comparableString).collect(Collectors.joining(", ")),
        actual.stream().map(this::comparableString).collect(Collectors.joining(", ")));
  }

  protected void assertItemsUnordered(Collection<? extends INamespaceVersioned> expected, Collection<? extends INamespaceVersioned> actual) {
    assertEquals(
        expected.stream().map(this::comparableString).collect(Collectors.toSet()),
        actual.stream().map(this::comparableString).collect(Collectors.toSet()));
  }

  protected String comparableString(INamespaceVersioned item) {
    return item.getVersion().unwrap();
  }

  protected NamespaceVersion version(String name) {
    return NamespaceVersion.of(name);
  }

  protected List<NamespaceVersion> versions(NamespaceVersion... versions) {
    return Arrays.asList(versions);
  }

  protected NamespaceVersion dep(String name) {
    return NamespaceVersion.of(name);
  }

  protected INamespaceVersioned item(String name, NamespaceVersion... dependencies) {
    return new P_TestingNamespaceVersioned(NamespaceVersion.of(name), Arrays.asList(dependencies));
  }

  protected NamespaceVersionedModel<INamespaceVersioned> createInventory(INamespaceVersioned... items) {
    return createInventory(Arrays.asList(items));
  }

  protected NamespaceVersionedModel<INamespaceVersioned> createInventory(Collection<INamespaceVersioned> items) {
    // orders names according appearance in test
    List<String> names = new ArrayList<>();
    items.stream().map(INamespaceVersioned::getVersion).map(NamespaceVersion::getNamespace).filter(n -> !names.contains(n)).forEach(names::add);
    items.stream().flatMap(m -> m.getDependencies().stream()).map(NamespaceVersion::getNamespace).filter(n -> !names.contains(n)).forEach(names::add);

    return createInventory(names, items);
  }

  protected NamespaceVersionedModel<INamespaceVersioned> createInventory(List<String> names, Collection<INamespaceVersioned> items) {
    return NamespaceVersionedModel.newBuilder().withNames(names).withItems(items).build();
  }

  protected static class P_TestingNamespaceVersioned implements INamespaceVersioned {

    private final NamespaceVersion m_version;
    private final Collection<NamespaceVersion> m_dependencies;

    public P_TestingNamespaceVersioned(NamespaceVersion version, Collection<NamespaceVersion> dependencies) {
      m_version = version;
      m_dependencies = dependencies;
    }

    @Override
    public NamespaceVersion getVersion() {
      return m_version;
    }

    @Override
    public Collection<NamespaceVersion> getDependencies() {
      return m_dependencies;
    }

    @Override
    public String toString() {
      return "P_TestingVersionedItem [" + m_version + "]";
    }
  }
}
