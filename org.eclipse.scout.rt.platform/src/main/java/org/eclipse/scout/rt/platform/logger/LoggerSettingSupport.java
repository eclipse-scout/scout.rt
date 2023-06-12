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

import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.logger.ILoggerSupport.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper bean for parameters or settings which may change numerous log levels.
 */
@ApplicationScoped
public class LoggerSettingSupport {

  private static final Logger LOG = LoggerFactory.getLogger(LoggerSettingSupport.class);

  /**
   * Apply a specific {@link ILoggerSetting} to set/change log-levels. Additionally initial state tracking is enabled
   * and log-levels are reset to initial states (before any log-levels are set), e.g. if a previous setting included a
   * customized log-level which a later one did not include anymore, in this case the initial state should be restored.
   * Therefore, it is not possible to call this method multiple times to change different log-levels w/o resetting the
   * previous changes.
   */
  public void initLogger(ILoggerSetting setting) {
    final ILoggerSupport loggerSupport = BEANS.get(ILoggerSupport.class);
    loggerSupport.resetToInitialStates();

    if (setting == null) {
      return;
    }

    // track changes to restore them later
    loggerSupport.trackInitialStates();

    if (setting.getRootLevel() != null) {
      loggerSupport.setLogLevel(Logger.ROOT_LOGGER_NAME, setting.getRootLevel());
    }

    if (setting.getCustomLevels() != null) {
      setting.getCustomLevels().entrySet()
          .stream()
          .filter(e -> e.getKey() != null)
          .forEach(e -> loggerSupport.setLogLevel(e.getKey(), e.getValue()));
    }
    LOG.info("Initialized logger using setting={}", setting);
  }

  /**
   * The setting providing at most one root-level and possibly multiple custom levels (for specific loggers).
   */
  public interface ILoggerSetting {

    /**
     * @return new root-level (or null to keep it unchanged)
     */
    LogLevel getRootLevel();

    /**
     * @return additional custom levels (or null not to change any); however map itself should not contain null values.
     */
    Map<String, LogLevel> getCustomLevels();
  }
}
