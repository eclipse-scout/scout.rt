/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.junit.Test;

/**
 * JUnit tests for {@link LockFile}
 *
 * @since 9.0
 */
public class LockFileTest {

  @Test
  public void testLock() throws IOException {
    File f = File.createTempFile(getClass().getName() + "-testWithLockCall", ".lock");

    LockFile a = new LockFile(f);
    LockFile b = new LockFile(f);

    //unlock without lock must not fail
    a.unlock();

    a.lock(10, TimeUnit.SECONDS);

    try {
      b.lock(1, TimeUnit.SECONDS);
      fail("b must not get the lock");
    }
    catch (TimedOutError e) {
      //expected
    }

    a.unlock();
    //multiple unlock must not fail
    a.unlock();
    a.unlock();

    b.lock(10, TimeUnit.SECONDS);

    b.unlock();
    b.unlock();
    b.unlock();
  }
}
