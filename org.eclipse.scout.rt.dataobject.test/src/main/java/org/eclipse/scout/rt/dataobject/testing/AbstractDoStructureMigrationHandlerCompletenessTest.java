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
package org.eclipse.scout.rt.dataobject.testing;

import static org.junit.Assert.fail;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataobject.migration.AbstractDoStructureMigrationHandlerTest;
import org.eclipse.scout.rt.dataobject.migration.IDoStructureMigrationHandler;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.Test;

/**
 * Completeness test to check that for all {@link IDoStructureMigrationHandler} a corresponding
 * {@link AbstractDoStructureMigrationHandlerTest} exists.
 */
public abstract class AbstractDoStructureMigrationHandlerCompletenessTest {

  /**
   * @return the package name prefix within this completeness tests looks for {@link IDoStructureMigrationHandler}
   */
  protected abstract String getPackageNamePrefix();

  /**
   * @return a list of classes/test classes that will be excluded from completeness checking. This may be used if there
   *         is a very special case where a successful standard test cannot be implemented.
   */
  protected Set<Class<?>> getExclusionList() {
    return Collections.emptySet();
  }

  @Test
  public void testCompleteness() {
    // Convention: migration handler naming contains version number: LoremMigrationHandler_1_2_0
    // Naming of test class: LoremMigrationHandler_1_2_0_Test
    Set<Class<?>> excludedMigrationHandlers = getExclusionList();
    Set<String> expectedTestClassNames = findClasses(IDoStructureMigrationHandler.class)
        .filter(clazz -> !excludedMigrationHandlers.contains(clazz))
        .map(Class::getName)
        .map(className -> className + "_Test")
        .collect(Collectors.toSet());

    Set<String> actualTestClassNames = findClasses(AbstractDoStructureMigrationHandlerTest.class)
        .map(Class::getName)
        .collect(Collectors.toSet());

    Set<String> missingTestClassNames = new HashSet<>(expectedTestClassNames);
    missingTestClassNames.removeAll(actualTestClassNames);

    Set<String> unknownTestClassNames = new HashSet<>(actualTestClassNames);
    unknownTestClassNames.removeAll(expectedTestClassNames);

    if (!missingTestClassNames.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      builder.append("There are missing test classes:\n");
      builder.append(StringUtility.join("\n", missingTestClassNames));
      if (!unknownTestClassNames.isEmpty()) {
        builder.append("\n");
        builder.append("Maybe the name of one of these is wrong :\n");
        builder.append(StringUtility.join("\n", unknownTestClassNames));
      }

      fail(builder.toString());
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> Stream<Class<T>> findClasses(Class<T> clazz) {
    return ClassInventory.get().getAllKnownSubClasses(clazz).stream()
        .filter(this::acceptClass)
        .map(classInfo -> (Class<T>) classInfo.resolveClass())
        .filter(resolvedClass -> !Modifier.isStatic(resolvedClass.getModifiers()));
  }

  protected boolean acceptClass(IClassInfo ci) {
    if (!ci.isInstanciable()) {
      return false;
    }

    if (ci.hasAnnotation(IgnoreBean.class)) {
      return false; // e.g. test migration handlers
    }

    String className = ci.name();
    return className.startsWith(getPackageNamePrefix());
  }
}
