/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.platform;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.internal.BeanManagerImplementor;
import org.eclipse.scout.rt.platform.internal.PlatformImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Platform used by tests that require a global available platform.
 * <p>
 * Such test classes are annotated with <code>@RunWith(PlatformTestRunner.class)</code> or have a before statement with
 *
 * <pre>
 * &#64;Before
 * public void before() {
 *   Platform.get().awaitPlatformStarted();
 * }
 * </pre>
 * <p>
 * It is important that tests using {@link Platform#get()} or {@link BEANS#get(Class)} kind of logic make sure that the
 * test waits until the platform is really started. Just calling {@link Platform#get()} only ensures that the platform
 * is in <i>Stating</i> state. Only calling {@link IPlatform#awaitPlatformStarted()} ensures that it is in
 * <i>Started</i> state.
 * <p>
 * This implementation of IPlatform that is used in testing environments and blocks the initializing thread until
 * {@link IPlatform#awaitPlatformStarted()}.
 * <p>
 * The initializing thread is the thread that first calls - directly or indirectly with BEANS.get - Platform.get and
 * that way triggered the platform startup.
 *
 * @since 9.0
 */
public class GlobalTestingPlatform extends PlatformImplementor {
  private static final Logger LOG = LoggerFactory.getLogger(GlobalTestingPlatform.class);

  private Thread m_initializingThread;
  private boolean m_started;

  public GlobalTestingPlatform() {
    m_initializingThread = Thread.currentThread();
  }

  @Override
  public void start() {
    try {
      super.start();
    }
    finally {
      m_initializingThread = null;
      m_started = true;
    }
  }

  @Override
  protected BeanManagerImplementor newBeanManagerImplementor() {
    return new BeanManagerImplementor() {
      @Override
      protected void checkAccess() {
        if (m_started || m_initializingThread == null || Thread.currentThread() != m_initializingThread) {
          return;
        }
        // Accessing the BeanManager before the testing platform is fully started
        LOG.info("Accessing the BeanManager before the testing platform is fully started; blocking until Platform.awaitPlatformStarted");
        awaitPlatformStarted();
      }
    };
  }
}
