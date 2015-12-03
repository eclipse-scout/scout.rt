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
package org.eclipse.scout.rt.platform;

import org.eclipse.scout.rt.platform.fixture.TestApplication;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationTest {

  @Test
  public void testStartApplication() {
    // backup current platform
    IPlatform backup = Platform.get();
    try {
      try {
        Platform.set(new DefaultPlatform());
        Platform.get().start();

        Assert.assertEquals(TestApplication.getInstance().getClass(), TestApplication.class);
      }
      finally {
        Platform.get().stop();
      }
      // Platform.get() is null after stopping it
      Assert.assertNull(Platform.get());
    }
    finally {
      // restore previous platform
      Platform.set(backup);
    }
  }
}
