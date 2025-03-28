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

import java.util.regex.Pattern;

import org.eclipse.scout.rt.opentelemetry.sdk.traces.RuleBasedSamplerBuilder.ISamplingCondition;

import io.opentelemetry.api.common.AttributeKey;

/**
 * Used to define a condition where the span attribute value matches the given pattern
 */
public final class RegexCondition implements ISamplingCondition {
  AttributeKey<String> attributeKey;
  Pattern pattern;

  private RegexCondition(AttributeKey<String> attributeKey, String pattern) {
    this.attributeKey = requireNonNull(attributeKey);
    this.pattern = Pattern.compile(pattern);
  }

  public static RegexCondition of(AttributeKey<String> attributeKey, String pattern) {
    return new RegexCondition(attributeKey, pattern);
  }

  @Override
  public AttributeKey<String> getAttributeKey() {
    return attributeKey;
  }

  @Override
  public boolean matches(String value) {
    return pattern.matcher(value).matches();
  }
}

