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
package org.eclipse.scout.rt.client.services.common.perf;

import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.service.IService;

public interface IPerformanceAnalyzerService extends IService, IPropertyObserver {
  String PROP_NETWORK_LATENCY = "networkLatency";
  String PROP_SERVER_EXECUTION_TIME = "serverExecutionTime";

  void addNetworkLatencySample(long millis);

  long getNetworkLatency();

  void addServerExecutionTimeSample(long millis);

  long getServerExecutionTime();
}
