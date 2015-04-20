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
package org.eclipse.scout.rt.ui.html.json.desktop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.DownloadHandler;
import org.eclipse.scout.rt.client.ui.desktop.IDownloadHandler;
import org.eclipse.scout.rt.shared.data.basic.BinaryResource;
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

  private DownloadHandlerStorage storage = new DownloadHandlerStorage();

  @Test
  public void testRemove() {
    IDownloadHandler handler = new P_TestHandler(100);
    storage.put(KEY, handler);
    assertEquals(1, storage.futureMap().size());
    assertEquals(handler, storage.remove(KEY));
    assertEquals("futureMap must be cleared when element is removed", 0, storage.futureMap().size());
    assertNull(storage.remove(KEY));
  }

  @Test
  public void testRemove_AfterTimeout() {
    P_TestHandler handler = new P_TestHandler(1);
    storage.put(KEY, handler);
    sleepSafe(20);
    assertNull(storage.remove(KEY));
    assertEquals("futureMap must be cleared after timeout", 0, storage.futureMap().size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPut_InvalidTTL() {
    P_TestHandler handler = new P_TestHandler(0);
    storage.put(KEY, handler);
  }

  private static void sleepSafe(long sleepTime) {
    try {
      Thread.sleep(sleepTime);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private class P_TestHandler extends DownloadHandler {

    public P_TestHandler(long ttl) {
      super(new BinaryResource("bar.txt", null), ttl);
    }
  }
}
