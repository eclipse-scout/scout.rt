/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.logger;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.logger.ILoggerSupport.LogLevel;
import org.eclipse.scout.rt.platform.logger.LoggerSettingSupport.ILoggerSetting;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.junit.Test;

public abstract class AbstractLoggerSupportTest {

  @Test
  public void testInitialStateTracking() {
    IBean<Object> bean = null;
    try {
      bean = BeanTestingHelper.get().registerBean(
          new BeanMetaData(ILoggerSupport.class)
              .withApplicationScoped(true)
              .withInitialInstance(getLoggerSupport()));
      // actual test
      testInitialStateTrackingInternal(null);
      testInitialStateTrackingInternal(new ILoggerSetting() {
        @Override
        public LogLevel getRootLevel() {
          return null;
        }

        @Override
        public Map<String, LogLevel> getCustomLevels() {
          return Collections.emptyMap();
        }
      });
    }
    finally {
      if (bean != null) {
        BeanTestingHelper.get().unregisterBean(bean);
      }
    }
  }

  protected void testInitialStateTrackingInternal(ILoggerSetting resetSetting) {
    assertNull(getTestLoggerLevel()); // pre-condition (level is initially not set)
    BEANS.get(LoggerSettingSupport.class).initLogger(new ILoggerSetting() {
      @Override
      public LogLevel getRootLevel() {
        return null; // no root level change
      }

      @Override
      public Map<String, LogLevel> getCustomLevels() {
        return Collections.singletonMap(getTestLoggerName(), LogLevel.INFO);
      }
    }); // initialized setting
    assertEquals(getTestLoggerLevel(), LogLevel.INFO); // level is now set to INFO

    BEANS.get(LoggerSettingSupport.class).initLogger(resetSetting); // init again
    assertNull(getTestLoggerLevel()); // post-condition: level is again not set anymore
  }

  protected abstract ILoggerSupport getLoggerSupport();

  protected abstract String getTestLoggerName();

  protected abstract LogLevel getTestLoggerLevel();
}
