/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.util.Date;
import java.util.jar.Attributes.Name;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformVersionProperty;
import org.eclipse.scout.rt.platform.util.JarManifestHelperTest.Fixture.FixtureEnclosing;
import org.eclipse.scout.rt.platform.util.JarManifestHelperTest.Fixture.FixtureEnclosing.FixtureEnclosingEnclosing;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link JarManifestHelper}
 */
public class JarManifestHelperTest {

  protected JarManifestHelper m_helper;

  @Before
  public void before() {
    m_helper = BEANS.get(JarManifestHelper.class);
  }

  @Test
  public void testGetAttributes() {
    assumeModuleCompiledToJar();

    assertEquals("org.eclipse.scout.rt.platform", m_helper.getAttribute(Platform.class, Name.IMPLEMENTATION_TITLE.toString()));
    assertEquals("org.eclipse.scout.rt.platform", m_helper.getAttribute(Platform.class, Name.SPECIFICATION_TITLE.toString()));

    assertEquals("org.eclipse.scout.rt.platform", m_helper.getAttribute(PlatformVersionProperty.class, Name.IMPLEMENTATION_TITLE.toString()));
    assertEquals("org.eclipse.scout.rt.platform", m_helper.getAttribute(PlatformConfigProperties.PlatformVersionProperty.class, Name.SPECIFICATION_TITLE.toString()));
  }

  @Test
  public void testGetBuildDate() {
    assumeModuleCompiledToJar();
    assertTrue(new Date().after(m_helper.getBuildDateAttribute(Platform.class)));
  }

  @Test
  public void testGetNonExistingAttribute() {
    assumeModuleCompiledToJar();
    assertNull(m_helper.getAttribute(Platform.class, "fooBarBaz"));
  }

  @Test
  public void testToClassName() {
    assertEquals("PlatformConfigProperties$PlatformVersionProperty.class", m_helper.toClassName(PlatformVersionProperty.class));
    assertEquals("Platform.class", m_helper.toClassName(Platform.class));
    assertEquals("JarManifestHelperTest$Fixture.class", m_helper.toClassName(Fixture.class));
    assertEquals("JarManifestHelperTest$Fixture$FixtureEnclosing.class", m_helper.toClassName(FixtureEnclosing.class));
    assertEquals("JarManifestHelperTest$Fixture$FixtureEnclosing$FixtureEnclosingEnclosing.class", m_helper.toClassName(FixtureEnclosingEnclosing.class));
  }

  /**
   * manifest file is not available if the module is not compiled to jar file, skip tests when executed locally
   */
  protected void assumeModuleCompiledToJar() {
    assumeTrue(m_helper.toJarClasspath(Platform.class) != null);
  }

  /**
   * Fixture test classes
   */
  protected static class Fixture {
    // nested static class
    protected static class FixtureEnclosing {
      // next nested static class
      protected static class FixtureEnclosingEnclosing {
      }
    }
  }
}
