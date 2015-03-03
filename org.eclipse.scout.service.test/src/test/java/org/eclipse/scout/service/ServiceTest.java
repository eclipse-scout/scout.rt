/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.service;

import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.service.internal.ITestService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServiceTest {

  @BeforeClass
  public static void setup() {
    ((Platform) Platform.get()).ensureStarted();
  }

  @Test
  public void testFragmentRegisteredService() throws Exception {
    ITestService service = SERVICES.getService(ITestService.class);
    Assert.assertNotNull(service);
  }
}
