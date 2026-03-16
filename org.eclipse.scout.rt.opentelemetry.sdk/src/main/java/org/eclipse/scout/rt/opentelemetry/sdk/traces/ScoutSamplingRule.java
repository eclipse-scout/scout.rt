/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.opentelemetry.sdk.traces;

import java.util.List;
import java.util.Objects;

import org.eclipse.scout.rt.opentelemetry.sdk.traces.RuleBasedSamplerBuilder.ISamplingCondition;

import io.opentelemetry.sdk.trace.samplers.Sampler;

/***
 * Inspired by <a href="https://github.com/open-telemetry/opentelemetry-java-contrib/blob/main/samplers/src/main/java/io/opentelemetry/contrib/sampler/SamplingRule.java">SamplingRule</a>
 */
public final class ScoutSamplingRule {
  final Sampler delegate;

  final List<ISamplingCondition> conditions;

  ScoutSamplingRule(Sampler delegate, List<ISamplingCondition> conditions) {
    this.delegate = delegate;
    this.conditions = conditions;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ScoutSamplingRule that)) {
      return false;
    }
    return Objects.equals(delegate, that.delegate) && Objects.equals(conditions, that.conditions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(delegate, conditions);
  }
}
