/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
