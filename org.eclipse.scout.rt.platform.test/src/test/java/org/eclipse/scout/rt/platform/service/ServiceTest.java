/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.service;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.service.internal.ITestService;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ServiceTest {

  @Test
  public void testFragmentRegisteredService() {
    ITestService service = BEANS.get(ITestService.class);
    Assert.assertNotNull(service);
  }
}
