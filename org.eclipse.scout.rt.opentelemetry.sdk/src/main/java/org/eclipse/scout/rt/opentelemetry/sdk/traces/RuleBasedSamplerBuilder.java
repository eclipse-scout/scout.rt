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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.trace.samplers.Sampler;

/***
 * Inspired by <a href="https://github.com/open-telemetry/opentelemetry-java-contrib/blob/main/samplers/src/main/java/io/opentelemetry/contrib/sampler/RuleBasedRoutingSamplerBuilder.java">RuleBasedRoutingSamplerBuilder</a>
 */
public final class RuleBasedSamplerBuilder {
  private final List<ScoutSamplingRule> rules = new ArrayList<>();
  private final SpanKind kind;
  private final Sampler defaultDelegate;

  public RuleBasedSamplerBuilder(SpanKind kind, Sampler defaultDelegate) {
    this.kind = kind;
    this.defaultDelegate = defaultDelegate;
  }

  public RuleBasedSamplerBuilder customize(Sampler sampler, ISamplingCondition... conditions) {
    requireNonNull(conditions, "conditions may not be null");
    rules.add(new ScoutSamplingRule(sampler, CollectionUtility.arrayList(conditions)));

    return this;
  }

  public RuleBasedSamplerBuilder drop(ISamplingCondition... conditions) {
    return customize(Sampler.alwaysOff(), conditions);
  }

  public RuleBasedSamplerBuilder recordAndSample(ISamplingCondition... conditions) {
    return customize(Sampler.alwaysOn(), conditions);
  }

  public RuleBasedSampler build() {
    return new RuleBasedSampler(rules, kind, defaultDelegate);
  }

  public interface ISamplingCondition {

    AttributeKey<String> getAttributeKey();

    boolean matches(String value);
  }
}
