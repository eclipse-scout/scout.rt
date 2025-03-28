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

import org.eclipse.scout.rt.opentelemetry.sdk.traces.RuleBasedSamplerBuilder.ISamplingCondition;

import io.opentelemetry.api.common.AttributeKey;

/**
 * Used to define a condition where the span attribute value is equals to the target value
 */
public final class EqualityCondition implements ISamplingCondition {
  AttributeKey<String> attributeKey;
  String targetValue;

  private EqualityCondition(AttributeKey<String> attributeKey, String targetValue) {
    this.attributeKey = requireNonNull(attributeKey);
    this.targetValue = requireNonNull(targetValue);
  }

  public static EqualityCondition of(AttributeKey<String> attributeKey, String targetValue) {
    return new EqualityCondition(attributeKey, targetValue);
  }

  @Override
  public AttributeKey<String> getAttributeKey() {
    return attributeKey;
  }

  @Override
  public boolean matches(String value) {
    return targetValue.equals(value);
  }
}

