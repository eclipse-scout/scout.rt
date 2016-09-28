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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.OpenUriAction;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.server.commons.servlet.cache.DownloadHttpResponseInterceptor;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.desktop.DownloadHandlerStorage.BinaryResourceHolderWithAction;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
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
    BinaryResourceHolder res = new BinaryResourceHolder(new BinaryResource("bar.txt", null));
    storage.put(KEY, res, OpenUriAction.DOWNLOAD);
    assertEquals(1, storage.futureMap().size());
    BinaryResourceHolderWithAction holderWithAction = storage.get(KEY);
    assertEquals(OpenUriAction.DOWNLOAD, holderWithAction.getOpenUriAction());
    assertEquals(res, holderWithAction.getHolder().get());
    assertEquals("futureMap must be cleared when element is removed", 0, storage.futureMap().size());
    assertNull(storage.get(KEY));
  }

  @Test
  public void testGet() {
    DownloadHandlerStorage storage = new DownloadHandlerStorage();
    BinaryResourceHolder res = new BinaryResourceHolder(new BinaryResource("bar.txt", null));
    storage.put(KEY, res, OpenUriAction.NEW_WINDOW);
    assertEquals(1, storage.futureMap().size());
    BinaryResourceHolderWithAction holderWithAction = storage.get(KEY);
    assertEquals(OpenUriAction.NEW_WINDOW, holderWithAction.getOpenUriAction());
    assertEquals(res, holderWithAction.getHolder().get());
    assertFalse("Must not contain download interceptor.", containsDownloadInterceptor(holderWithAction.getHolder()));
    holderWithAction = storage.get(KEY);
    assertEquals(res, holderWithAction.getHolder().get());
    assertTrue("Must contain download interceptor.", containsDownloadInterceptor(holderWithAction.getHolder()));
  }

  private boolean containsDownloadInterceptor(BinaryResourceHolder holder) {
    for (IHttpResponseInterceptor interceptor : holder.getHttpResponseInterceptors()) {
      if (interceptor instanceof DownloadHttpResponseInterceptor) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testRemove_AfterTimeout() {
    DownloadHandlerStorage storage = new DownloadHandlerStorage() {
      @Override
      protected long getTTLForResource(BinaryResource res) {
        return 10L;
      }
    };
    BinaryResourceHolder res = new BinaryResourceHolder(new BinaryResource("bar.txt", null));
    storage.put(KEY, res, OpenUriAction.NEW_WINDOW);

    SleepUtil.sleepElseLog(100, TimeUnit.MILLISECONDS);

    assertNull(storage.get(KEY));
    assertEquals("futureMap must be cleared after timeout", 0, storage.futureMap().size());
  }
}
