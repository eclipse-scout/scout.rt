/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.inventory.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.internal.BeanFilter;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.internal.fixture.TestingBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.jboss.jandex.Indexer;
import org.junit.Assert;
import org.junit.Test;

public class JandexClassInventoryTest {

  private static void assertEqualClassInfos(Set<Class> expected, Set<IClassInfo> actual) {
    assertEqualClasses(expected, actual
        .stream()
        .map(ci -> ci.resolveClass())
        .collect(Collectors.toSet()));
  }

  private static void assertEqualClasses(Set<Class> expected, Set<Class> actual) {
    if (!expected.equals(actual)) {
      System.out.println("# Expected: " + expected.stream().map(c -> c.getName()).sorted().collect(Collectors.joining("\n")));
      System.out.println("# Actual: " + actual.stream().map(c -> c.getName()).sorted().collect(Collectors.joining("\n")));
    }
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testFindInnerClasses() throws IOException {
    Indexer indexer = new Indexer();
    String basePath = TestingBean.class.getName().replace('.', '/');
    for (String path : new String[]{
        Bean.class.getName().replace('.', '/'),
        ApplicationScoped.class.getName().replace('.', '/'),
        basePath,
        basePath + "2",
        basePath + "$S1",
        basePath + "$S2",
        basePath + "$S3",
        basePath + "$S4",
        basePath + "$S5",
        basePath + "$S6",
        basePath + "$S6Sub1",
        basePath + "$S6Sub2",
        basePath + "$S1Sub1",
        basePath + "$M1",
        basePath + "$I1",
        basePath + "$I2",
        basePath + "$E1",
        basePath + "$A1",
    }) {
      indexer.index(TestingBean.class.getClassLoader().getResource(path + ".class").openStream());
    }
    JandexClassInventory classInventory = new JandexClassInventory(indexer.complete());
    final Set<Class> actual = new BeanFilter().collect(classInventory);

    Set<Class> expected = CollectionUtility.hashSet(
        org.eclipse.scout.rt.platform.inventory.internal.fixture.TestingBean.class,
        org.eclipse.scout.rt.platform.inventory.internal.fixture.TestingBean.S1.class,
        org.eclipse.scout.rt.platform.inventory.internal.fixture.TestingBean.S1Sub1.class,
        org.eclipse.scout.rt.platform.inventory.internal.fixture.TestingBean.S5.class);

    assertEqualClasses(expected, actual);
  }

  /**
   * <pre>
    IFixtureInterface1
      FixtureImpl1
        FixtureImpl1Sub
      IFixtureInterface2
        IFixtureInterface2Sub1
        IFixtureInterface2Sub2
          FixtureImpl2
   * </pre>
   */
  @Test
  public void testFindSubinterfacesAndSubclasses() throws IOException {
    Indexer indexer = new Indexer();
    for (Class clazz : Arrays.asList(
        IFixtureInterface1.class,
        FixtureImpl1.class,
        FixtureImpl1Sub.class,
        IFixtureInterface2.class,
        IFixtureInterface2Sub1.class,
        IFixtureInterface2Sub2.class,
        FixtureImpl2.class)) {
      String path = clazz.getName().replace('.', '/');
      indexer.index(clazz.getClassLoader().getResource(path + ".class").openStream());
    }
    JandexClassInventory classInventory = new JandexClassInventory(indexer.complete());

    assertEqualClassInfos(
        CollectionUtility.hashSet(
            FixtureImpl1.class,
            FixtureImpl1Sub.class,
            IFixtureInterface2.class,
            IFixtureInterface2Sub1.class,
            IFixtureInterface2Sub2.class,
            FixtureImpl2.class),
        classInventory.getAllKnownSubClasses(IFixtureInterface1.class));

    assertEqualClassInfos(
        CollectionUtility.hashSet(
            FixtureImpl1Sub.class),
        classInventory.getAllKnownSubClasses(FixtureImpl1.class));

    assertEqualClassInfos(
        CollectionUtility.hashSet(),
        classInventory.getAllKnownSubClasses(FixtureImpl1Sub.class));

    assertEqualClassInfos(
        CollectionUtility.hashSet(
            IFixtureInterface2Sub1.class,
            IFixtureInterface2Sub2.class,
            FixtureImpl2.class),
        classInventory.getAllKnownSubClasses(IFixtureInterface2.class));

    assertEqualClassInfos(
        CollectionUtility.hashSet(),
        classInventory.getAllKnownSubClasses(IFixtureInterface2Sub1.class));

    assertEqualClassInfos(
        CollectionUtility.hashSet(
            FixtureImpl2.class),
        classInventory.getAllKnownSubClasses(IFixtureInterface2Sub2.class));

    assertEqualClassInfos(
        CollectionUtility.hashSet(),
        classInventory.getAllKnownSubClasses(FixtureImpl2.class));
  }

  /**
   * interface that has implementor classes and some subinterfaces
   *
   * <pre>
    IFixtureInterface1
      FixtureImpl1
        FixtureImpl1Sub
      IFixtureInterface2
        IFixtureInterface2Sub1
        IFixtureInterface2Sub2
          FixtureImpl2
   * </pre>
   */
  public interface IFixtureInterface1 {
  }

  public static class FixtureImpl1 implements IFixtureInterface1 {
  }

  public static class FixtureImpl1Sub extends FixtureImpl1 {
  }

  /**
   * subinterface that has subinterfaces and on one of them an implementor class
   */
  public interface IFixtureInterface2 extends IFixtureInterface1 {
  }

  public interface IFixtureInterface2Sub1 extends IFixtureInterface2 {
  }

  public interface IFixtureInterface2Sub2 extends IFixtureInterface2 {
  }

  public static class FixtureImpl2 implements IFixtureInterface2Sub2 {
  }
}
