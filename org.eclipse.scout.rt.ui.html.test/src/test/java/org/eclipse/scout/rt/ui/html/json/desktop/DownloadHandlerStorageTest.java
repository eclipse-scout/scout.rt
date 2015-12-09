/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class DownloadHandlerStorageTest {

  private static final String KEY = "foo";

  @Test
  public void testRemove() {
    DownloadHandlerStorage storage = new DownloadHandlerStorage();
    BinaryResource res = new BinaryResource("bar.txt", null);
    storage.put(KEY, res);
    assertEquals(1, storage.futureMap().size());
    assertEquals(res, storage.remove(KEY));
    assertEquals("futureMap must be cleared when element is removed", 0, storage.futureMap().size());
    assertNull(storage.remove(KEY));
  }

  @Test
  public void testRemove_AfterTimeout() {
    DownloadHandlerStorage storage = new DownloadHandlerStorage() {
      @Override
      protected long getTTLForResource(BinaryResource res) {
        return 10L;
      }
    };
    BinaryResource res = new BinaryResource("bar.txt", null);
    storage.put(KEY, res);
    sleepSafe(100);
    // FIXME awe: Improve this test (it fails sometimes because of timing issues)
    assertNull(storage.remove(KEY));
    assertEquals("futureMap must be cleared after timeout", 0, storage.futureMap().size());
  }

  private static void sleepSafe(long sleepTime) {
    try {
      Thread.sleep(sleepTime);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
