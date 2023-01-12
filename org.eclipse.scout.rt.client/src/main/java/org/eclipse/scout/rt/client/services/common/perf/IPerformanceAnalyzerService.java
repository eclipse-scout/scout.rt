/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
