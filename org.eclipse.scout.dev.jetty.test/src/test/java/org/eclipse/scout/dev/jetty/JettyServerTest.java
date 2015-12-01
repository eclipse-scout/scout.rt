/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.dev.jetty;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

public class JettyServerTest {

  /**
   * Test Use Case: see JavaDoc of javax.servlet.ServletContext.getResourcePaths(String)
   */
  @Test
  public void testFindResourcesFromDependentJars1() {
    Set<String> resources = JettyServer.getResourcePathsFromDependentJars(getClass().getClassLoader(), "/");
    assertEquals(CollectionUtility.hashSet(
        "/welcome.html",
        "/catalog/",
        "/customer/",
        "/WEB-INF/"), resources);
  }

  /**
   * Test Use Case: see JavaDoc of javax.servlet.ServletContext.getResourcePaths(String)
   */
  @Test
  public void testFindResourcesFromDependentJars2() {
    Set<String> resources = JettyServer.getResourcePathsFromDependentJars(getClass().getClassLoader(), "/catalog/");
    assertEquals(CollectionUtility.hashSet(
        "/catalog/index.html",
        "/catalog/products.html",
        "/catalog/offers/",
        "/catalog/moreOffers/"), resources); // located in dependent JAR 'org.eclipse.scout.dev.jetty.test.affix'.
  }

  /**
   * Test Use Case: see JavaDoc of javax.servlet.ServletContext.getResourcePaths(String)
   */
  @Test
  public void testFindResourcesFromDependentJars3() {
    Set<String> resources = JettyServer.getResourcePathsFromDependentJars(getClass().getClassLoader(), "/catalog/moreOffers/");
    assertEquals(CollectionUtility.hashSet(
        "/catalog/moreOffers/books.html"), resources); // located in dependent JAR 'org.eclipse.scout.dev.jetty.test.affix'.
  }
}
