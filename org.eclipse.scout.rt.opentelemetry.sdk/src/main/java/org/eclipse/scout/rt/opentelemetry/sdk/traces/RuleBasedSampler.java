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

import java.util.List;

import org.eclipse.scout.rt.opentelemetry.sdk.traces.RuleBasedSamplerBuilder.ISamplingCondition;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

/***
 * Makes a sample decision based on the {@link ScoutSamplingRule}
 *
 * Inspired by <a href="https://github.com/open-telemetry/opentelemetry-java-contrib/blob/main/samplers/src/main/java/io/opentelemetry/contrib/sampler/RuleBasedRoutingSampler.java">RuleBasedRoutingSampler</a>
 */
public final class RuleBasedSampler implements Sampler {

  private final List<ScoutSamplingRule> rules;
  private final SpanKind kind;
  private final Sampler fallback;

  RuleBasedSampler(List<ScoutSamplingRule> rules, SpanKind kind, Sampler fallback) {
    this.kind = requireNonNull(kind);
    this.fallback = requireNonNull(fallback);
    this.rules = requireNonNull(rules);
  }

  public static RuleBasedSamplerBuilder builder(SpanKind kind, Sampler fallback) {
    return new RuleBasedSamplerBuilder(
        requireNonNull(kind, "span kind must not be null"),
        requireNonNull(fallback, "fallback sampler must not be null"));
  }

  /***
   * The first matching sampling rule decides if the trace is sampled or not (OR).
   * However, each sampling rule may contain several conditions which all must be fulfilled for a sampling rule to match (AND).
   */
  @Override
  public SamplingResult shouldSample(
      Context parentContext,
      String traceId,
      String name,
      SpanKind spanKind,
      Attributes attributes,
      List<LinkData> parentLinks) {
    if (kind != spanKind) {
      return fallback.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
    }
    for (ScoutSamplingRule samplingRule : rules) {
      boolean match = true;

      for (ISamplingCondition condition : samplingRule.conditions) {
        if (attributes.get(condition.getAttributeKey()) == null) {
          match = false;
          break;
        }
        String s = attributes.get(condition.getAttributeKey());
        if (!condition.matches(s)) {
          match = false;
          break;
        }
      }
      if (match) {
        return samplingRule.delegate.shouldSample(
            parentContext, traceId, name, spanKind, attributes, parentLinks);
      }
    }

    return fallback.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
  }

  @Override
  public String getDescription() {
    return "RegexBasedSampler{"
        + "rules="
        + rules
        + ", kind="
        + kind
        + ", fallback="
        + fallback
        + '}';
  }

  @Override
  public String toString() {
    return getDescription();
  }
}
