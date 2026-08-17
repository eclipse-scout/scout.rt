/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey.server;

import static org.junit.Assert.assertEquals;

import jakarta.ws.rs.client.WebTarget;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.rest.jersey.JerseyTestApplication;
import org.eclipse.scout.rt.rest.jersey.JerseyTestRestClientHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class CountResourceTest {

  @BeforeClass
  public static void beforeClass() {
    BEANS.get(JerseyTestApplication.class).ensureStarted();
  }

  @Test
  public void testCount() {
    JerseyTestRestClientHelper helper = BEANS.get(JerseyTestRestClientHelper.class);
    WebTarget target = helper.target("api/count");
    // assert incrementing counter (resource is application scoped and is able to retain internal state)
    for (int i = 0; i < 3; i++) {
      Integer count = target.request().get(Integer.class);
      assertEquals(Integer.valueOf(i), count);
    }
  }
}
