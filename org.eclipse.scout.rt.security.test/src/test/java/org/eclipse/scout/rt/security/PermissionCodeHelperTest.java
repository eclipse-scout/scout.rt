/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security;

import static org.junit.Assert.*;

import java.io.Serial;
import java.security.BasicPermission;
import java.security.Permission;

import org.eclipse.scout.rt.dataobject.exception.IPermissionCodeHelper;
import org.eclipse.scout.rt.security.fixture.AFixturePermission;
import org.eclipse.scout.rt.security.fixture.DFixturePermission;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link PermissionCodeHelper}
 */
@RunWith(PlatformTestRunner.class)
public class PermissionCodeHelperTest {

  private final IPermissionCodeHelper m_helper = new PermissionCodeHelper();

  @Test
  public void testGetPermissionCode_nullPermission() {
    assertNull(m_helper.getPermissionCode(null));
  }

  @Test
  public void testGetPermissionCode_regularPermission() {
    Permission perm = new BasicPermission("myPermission") {
      @Serial
      private static final long serialVersionUID = 1L;
    };
    assertEquals("acf16cd7c4", m_helper.getPermissionCode(perm));
  }

  @Test
  public void testGetPermissionCode_iPermission() {
    assertEquals("21b4f4bd9e", m_helper.getPermissionCode(new AFixturePermission()));
    assertEquals("2ac968752f", m_helper.getPermissionCode(new DFixturePermission()));
  }
}
