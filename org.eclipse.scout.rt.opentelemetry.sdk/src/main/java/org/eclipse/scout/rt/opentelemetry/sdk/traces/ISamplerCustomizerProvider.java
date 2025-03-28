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

import org.eclipse.scout.rt.platform.ApplicationScoped;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.samplers.Sampler;

/**
 * A provider of samplers based on OpenTelemetry
 * <p>
 * A sampler provider can be used to register samplers for your application.
 * You can use samplers to make decisions on which traces to collect and
 * which to drop. They can be used for reducing the number of recorded traces.
 * </p>
 * <p>
 * Example usage:
 *
 * <pre>
 *   public class MySamplerProvider implements ISamplerCustomizerProvider{
 *     &#064;Override
 *     public Sampler createSamplerCustomizer(Sampler fallback, ConfigProperties config) {
 *      return new RuleBasedSamplerBuilder(SpanKind.SERVER, fallback)
 *          .drop(EqualityCondition.of(UrlAttributes.URL_QUERY, "poll"),
 *            RegexCondition.of(UrlAttributes.URL_PATH, "^/json"))
 *          .drop(RegexCondition.of(UrlAttributes.URL_PATH, "^/status"))
 *     }
 *   }
 * </pre>
 *
 * @see RuleBasedSampler
 */
@ApplicationScoped
public interface ISamplerCustomizerProvider {

  Sampler createSamplerCustomizer(Sampler fallback, ConfigProperties config);
}
