/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.api.client.http.HttpRequestInitializer;

@RunWith(PlatformTestRunner.class)
public class AbstractHttpTransportManagerTest {

  /**
   * Verify actual initialization of {@link AbstractHttpTransportManager#initSynchronized()} is run only once. If this
   * test flip/flops this is not the case.
   */
  @Test
  public void testInitializeOnlyOnce() throws InterruptedException {
    for (int i = 0; i < 50; i++) { // repeat test several times
      // count how many times createHttpRequestInitializer has been called during initialization
      AtomicInteger initCount = new AtomicInteger();

      // dummy transport manager
      AbstractHttpTransportManager abstractHttpTransportManager = new AbstractHttpTransportManager() {

        @Override
        public String getName() {
          return AbstractHttpTransportManagerTest.class.getName();
        }

        @Override
        protected HttpRequestInitializer createHttpRequestInitializer() {
          initCount.incrementAndGet();
          return super.createHttpRequestInitializer();
        }
      };

      // try calling init parallel multiple times
      ExecutorService executorService = Executors.newFixedThreadPool(8);
      executorService.invokeAll(IntStream.range(0, 8).mapToObj(j -> (Callable<Object>) () -> {
        abstractHttpTransportManager.init();
        return null;
      }).collect(Collectors.toSet()));
      executorService.shutdown();

      // wait for all tasks to be finished
      assertTrue(executorService.awaitTermination(1, TimeUnit.MINUTES));

      // createHttpRequestInitializer called by initSynchronized may be called only once
      assertEquals(1, initCount.get());
    }
  }
}
