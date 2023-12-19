/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.app;

import java.io.File;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Assert;
import org.junit.Test;

public class DevelopmentWebAppContextTest {

  protected DevelopmentWebAppContext createContext() {
    return new DevelopmentWebAppContext(new File(".").getAbsolutePath(), "/");
  }

  /**
   * Test Use Case: see JavaDoc of jakarta.servlet.ServletContext.getResourcePaths(String)
   */
  @Test
  public void testFindResourcesFromDependentJars1() {
    Set<String> resources = createContext().getResourcePathsFromDependentJars(getClass().getClassLoader(), "/");
    Assert.assertEquals(CollectionUtility.hashSet(
        "/welcome.html",
        "/catalog/",
        "/customer/",
        "/WEB-INF/"), resources);
  }

  /**
   * Test Use Case: see JavaDoc of jakarta.servlet.ServletContext.getResourcePaths(String)
   */
  @Test
  public void testFindResourcesFromDependentJars2() {
    Set<String> resources = createContext().getResourcePathsFromDependentJars(getClass().getClassLoader(), "/catalog/");
    Assert.assertEquals(CollectionUtility.hashSet(
        "/catalog/index.html",
        "/catalog/products.html",
        "/catalog/offers/",
        "/catalog/moreOffers/"), resources); // located in dependent JAR 'org.eclipse.scout.rt.app.test.affix'.
  }

  /**
   * Test Use Case: see JavaDoc of jakarta.servlet.ServletContext.getResourcePaths(String)
   */
  @Test
  public void testFindResourcesFromDependentJars3() {
    Set<String> resources = createContext().getResourcePathsFromDependentJars(getClass().getClassLoader(), "/catalog/moreOffers/");
    Assert.assertEquals(CollectionUtility.hashSet(
        "/catalog/moreOffers/books.html"), resources); // located in dependent JAR 'org.eclipse.scout.rt.app.test.affix'.
  }
}
