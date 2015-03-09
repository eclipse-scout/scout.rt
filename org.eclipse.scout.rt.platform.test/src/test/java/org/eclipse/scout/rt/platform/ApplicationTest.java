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
package org.eclipse.scout.rt.platform;

import org.eclipse.scout.rt.platform.fixture.TestApplication;
import org.eclipse.scout.rt.testing.platform.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(PlatformTestRunner.class)
public class ApplicationTest {

  @Test
  public void test() {
    Assert.assertNotNull(TestApplication.getInstance());
  }

}
