/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithNewPlatform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

@RunWith(PlatformTestRunner.class)
@RunWithNewPlatform
public class UiThreadInterruptionTest {
  private final LogAppender m_appender = new LogAppender();

  @Before
  public void before() {
    m_appender.start();
    Logger log = LoggerFactory.getLogger(UiThreadInterruption.class);
    ((ch.qos.logback.classic.Logger) log).addAppender(m_appender);
  }

  @After
  public void after() {
    m_appender.stop();
    Logger log = LoggerFactory.getLogger(UiThreadInterruption.class);
    ((ch.qos.logback.classic.Logger) log).detachAppender(m_appender);
  }

  @Test
  public void testDetectAndClearWithNoInterrupt() {
    BEANS.get(UiThreadInterruption.class).detectAndClear(this, "test");
    assertEquals(0, m_appender.logEvents.size());
  }

  @Test
  public void testDetectAndClearWithInterrupt() {
    Thread.currentThread().interrupt();
    assertTrue(Thread.currentThread().isInterrupted());
    BEANS.get(UiThreadInterruption.class).detectAndClear(this, "test");
    assertFalse(Thread.currentThread().isInterrupted());
    assertEquals(1, m_appender.logEvents.size());
    String msg = m_appender.logEvents.get(0).getFormattedMessage();
    assertTrue(msg, msg.startsWith("DETECTED_THREAD_INTERRUPTION") && msg.endsWith("clearing interrupt status successful"));
  }

  private static class LogAppender extends AppenderBase<ILoggingEvent> {
    protected final List<ILoggingEvent> logEvents = new ArrayList<>();

    @Override
    protected void append(ILoggingEvent e) {
      logEvents.add(e);
    }
  }
}
