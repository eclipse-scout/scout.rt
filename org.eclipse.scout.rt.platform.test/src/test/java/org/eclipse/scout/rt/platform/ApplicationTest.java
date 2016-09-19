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

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.fixture.TestApplication;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationTest {

  @Test
  public void testApplicationLifecycle() {
    // backup current platform
    IPlatform backup = Platform.peek();
    try {
      Platform.set(new DefaultPlatform());
      for (int k = 0; k < 3; k++) {
        try {
          Platform.get().start();
          Assert.assertEquals(TestApplication.getInstance().getClass(), TestApplication.class);
          Platform.get().awaitPlatformStarted();
          Assert.assertEquals(State.PlatformStarted, Platform.get().getState());
        }
        finally {
          Platform.get().stop();
        }
        // Platform.get() is not null after stopping it
        Assert.assertEquals(State.PlatformStopped, Platform.get().getState());
      }
    }
    finally {
      // restore previous platform
      Platform.set(backup);
    }
  }
}
