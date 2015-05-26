/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Events with a level below the specified min level or above the specified max level will be denied. Events with a
 * within the specified interval will trigger a FilterReply.NEUTRAL result, to allow the rest of the filter chain
 * process the event
 *
 * @since 5.1
 */
public class LevelRangeFilter extends Filter<ILoggingEvent> {

  private Level m_levelMin;
  private Level m_levelMax;

  @Override
  public FilterReply decide(ILoggingEvent event) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }

    if (event.getLevel().isGreaterOrEqual(m_levelMin) && m_levelMax.isGreaterOrEqual(event.getLevel())) {
      return FilterReply.NEUTRAL;
    }
    else {
      return FilterReply.DENY;
    }
  }

  public void setLevelMin(String levelMin) {
    m_levelMin = Level.toLevel(levelMin);
  }

  public void setLevelMax(String levelMax) {
    m_levelMax = Level.toLevel(levelMax);
  }

  @Override
  public void start() {
    if (m_levelMin != null || m_levelMax != null) {
      if (m_levelMin == null) {
        m_levelMin = Level.TRACE;
      }
      if (m_levelMax == null) {
        m_levelMax = Level.ERROR;
      }
      super.start();
    }
  }
}
