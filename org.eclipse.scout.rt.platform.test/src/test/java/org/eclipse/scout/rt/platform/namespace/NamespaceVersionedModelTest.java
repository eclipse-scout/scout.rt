/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
  public void testTransitivDependenciesSparse() {
    NamespaceVersionedModel<INamespaceVersioned> inventory = createInventory(
        item("alfa-3"),
        item("alfa-4"),
        item("alfa-5"),
        item("bravo-18", dep("alfa-5")),
        item("charlie-8", dep("alfa-4")),
        item("charlie-10", dep("bravo-18")),
        item("foxtrot-21", dep("alfa-3")),
        item("foxtrot-22", dep("alfa-5")));
    assertItems(
        Arrays.asList(item("foxtrot-21"), item("alfa-4"), item("charlie-8"), item("alfa-5"), item("charlie-10"), item("foxtrot-22")),
        inventory.getItems(versions(version("alfa-3"), version("charlie-7"), version("foxtrot-20")), versions(version("alfa-5"), version("bravo-18"), version("charlie-10"), version("foxtrot-22"))));

    assertItems(
        Arrays.asList(item("alfa-3"), item("foxtrot-21"), item("alfa-4"), item("charlie-8"), item("alfa-5"), item("bravo-18"), item("charlie-10"), item("foxtrot-22")),
        inventory.getItems());
  }

  @Test
  public void testTransitivDependenciesSparse2() {
    // alfa sees no other
    // bravo sees alfa
    // charlie sees bravo & alfa
    // foxtrot sees alfa
    NamespaceVersionedModel<INamespaceVersioned> inventory = createInventory(
        item("alfa-12"),
        item("alfa-14"),
        item("alfa-20"),
        item("bravo-18", dep("alfa-14")),
        item("bravo-20", dep("alfa-20")),
        item("charlie-12"),
        item("charlie-13"),
        item("charlie-20", dep("alfa-20")),
        item("foxtrot-11"),
        item("foxtrot-12"),
        item("foxtrot-13", dep("charlie-12")),
        item("foxtrot-14"),
        item("foxtrot-15"),
        item("foxtrot-20", dep("charlie-20")),
        item("foxtrot-21"),
        item("foxtrot-23"));

    assertItems(
        Arrays.asList(
            item("alfa-12"), item("alfa-14"), item("bravo-18"), item("foxtrot-11"), item("foxtrot-12"), item("charlie-12"),
            item("foxtrot-13"), item("foxtrot-14"), item("charlie-13"), item("foxtrot-15"), item("alfa-20"), item("bravo-20"),
            item("charlie-20"), item("foxtrot-20"), item("foxtrot-21"), item("foxtrot-23")),
        inventory.getItems());
  }

  @Test
  public void testTransitivDependenciesSparse3() {
    NamespaceVersionedModel<INamespaceVersioned> inventory = createInventory(
        item("alfa-27"),
        item("alfa-34"),
        item("bravo-33", dep("alfa-27")),
        item("bravo-34", dep("alfa-34")),
        item("bravo-40"),
        item("charlie-2", dep("alfa-27")),
        item("charlie-11", dep("bravo-33")),
        item("charlie-21", dep("bravo-34")),
        item("charlie-21.1"),
        item("charlie-22"),
        item("charlie-22.1"),
        item("charlie-23", dep("bravo-40")),
        item("foxtrot-1", dep("charlie-22")),
        item("foxtrot-21", dep("charlie-23")),
        item("foxtrot-22", dep("charlie-23")));

    assertItems(
        Arrays.asList(
            item("alfa-27"), item("bravo-33"), item("charlie-2"), item("charlie-11"), item("alfa-34"), item("bravo-34"),
            item("charlie-21"), item("charlie-21.1"), item("bravo-40"), item("charlie-22"), item("foxtrot-1"),
            item("charlie-22.1"), item("charlie-23"), item("foxtrot-21"), item("foxtrot-22")),
        inventory.getItems());
  }

  @Test
  public void testMissingNamespaceInToVersions() {
    NamespaceVersionedModel<INamespaceVersioned> inventory = createInventory(Arrays.asList("alfa", "bravo", "charlie"), Arrays.asList(
        item("alfa-4"),
        item("alfa-5"), // scenario 'migration' 'alfa-5' migrates to namespace alfa to charlie
        item("bravo-15", dep("alfa-4"))));

    assertItems(
        Arrays.asList(item("alfa-4"), item("bravo-15"), item("alfa-5")),
        inventory.getItems(versions(version("alfa-3"), version("bravo-14")), versions(version("bravo-18"), version("charlie-1"))));
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
        item("foxtrot-1"),
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
