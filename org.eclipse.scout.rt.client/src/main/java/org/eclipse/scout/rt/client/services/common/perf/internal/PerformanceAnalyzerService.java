/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.perf.internal;

import java.beans.PropertyChangeListener;

import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceAnalyzerService implements IPerformanceAnalyzerService {
  private static final Logger LOG = LoggerFactory.getLogger(PerformanceAnalyzerService.class);

  private final PerformanceSampleSet m_networkLatency;
  private final PerformanceSampleSet m_serverExecutionTime;
  private final BasicPropertySupport m_propertySupport;

  public PerformanceAnalyzerService() {
    m_propertySupport = new BasicPropertySupport(this);
    m_networkLatency = new PerformanceSampleSet(10, 70);
    m_serverExecutionTime = new PerformanceSampleSet(10, 100);
  }

  @Override
  public void addNetworkLatencySample(long millis) {
    long oldValue = m_networkLatency.getValue();
    m_networkLatency.addSample(millis);
    long newValue = m_networkLatency.getValue();
    try {
      m_propertySupport.firePropertyChange(PROP_NETWORK_LATENCY, oldValue, newValue);
    }
    catch (Exception t) {
      LOG.warn("Unexpected exception", t);
    }
  }

  @Override
  public long getNetworkLatency() {
    return m_networkLatency.getValue();
  }

  @Override
  public void addServerExecutionTimeSample(long millis) {
    long oldValue = m_serverExecutionTime.getValue();
    m_serverExecutionTime.addSample(millis);
    long newValue = m_serverExecutionTime.getValue();
    try {
      m_propertySupport.firePropertyChange(PROP_SERVER_EXECUTION_TIME, oldValue, newValue);
    }
    catch (Exception t) {
      LOG.warn("Unexpected Exception", t);
    }
  }

  @Override
  public long getServerExecutionTime() {
    return m_serverExecutionTime.getValue();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertySupport.addPropertyChangeListener(propertyName, listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    m_propertySupport.removePropertyChangeListener(propertyName, listener);
  }
}
