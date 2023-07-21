/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import org.eclipse.scout.rt.platform.ApplicationScoped;

import io.opentelemetry.api.OpenTelemetry;

/**
 * A provider of one or more application metrics based on OpenTelemetry.
 * <p>
 * A metric provider is usually used for more generally or feature-independent metrics such as JVM/cpu metrics. It can
 * also be used for metrics whose source code is not under your control, e.g. external libraries.
 * </p>
 * <p>
 * Such metric providers are responsible to set up the corresponding metric(s) and also to properly close/free used
 * resources on Scout platform shutdown.
 * <p>
 * A metric provider usually interacts with a {@link OpenTelemetry} instance.
 *
 * @see OpenTelemetry
 */
@ApplicationScoped
public interface IMetricProvider {

  /**
   * Register the metrics to the given {@link OpenTelemetry}.
   * <p>
   * This method is usually called once at Scout platform startup.
   *
   * @see OpenTelemetry#getMeter(String)
   */
  void register(OpenTelemetry openTelemetry);

  /**
   * Close/free used resources.
   */
  void close();
}
