/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
  public void testFragmentRegisteredService() throws Exception {
    ITestService service = BEANS.get(ITestService.class);
    Assert.assertNotNull(service);
  }
}
