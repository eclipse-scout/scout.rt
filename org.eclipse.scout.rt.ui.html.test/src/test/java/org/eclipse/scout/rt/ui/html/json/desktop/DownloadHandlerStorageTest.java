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

  DownloadHandlerStorage storage = new DownloadHandlerStorage();

  class TestHandler extends DownloadHandler {

    public TestHandler(long ttl) {
      super(new BinaryResource("bar.txt", null), ttl);
    }

  }

  @Test
  public void testRemove() {
    IDownloadHandler handler = new TestHandler(100);
    storage.put(KEY, handler);
    assertEquals(1, storage.getFutureMapSize());
    assertEquals(handler, storage.remove(KEY));
    assertEquals("futureMap must be cleared when element is removed", 0, storage.getFutureMapSize());
    assertNull(storage.remove(KEY));
  }

  @Test
  public void testRemove_AfterTimeout() {
    TestHandler handler = new TestHandler(1);
    storage.put(KEY, handler);
    sleepSafe(20);
    assertNull(storage.remove(KEY));
    assertEquals("futureMap must be cleared after timeout", 0, storage.getFutureMapSize());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPut_InvalidTTL() {
    TestHandler handler = new TestHandler(0);
    storage.put(KEY, handler);
  }

  private void sleepSafe(long sleepTime) {
    try {
      Thread.sleep(sleepTime);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
