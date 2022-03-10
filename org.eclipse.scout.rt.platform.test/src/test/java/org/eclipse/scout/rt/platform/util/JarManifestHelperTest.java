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

  /**
   * manifest file is not available if the module is not compiled to jar file, skip tests when executed locally
   */
  protected void assumeModuleCompiledToJar() {
    assumeTrue(m_helper.toJarClasspath(Platform.class) != null);
  }
}
