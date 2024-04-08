/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.opentelemetry;

import org.eclipse.scout.rt.platform.Bean;

import io.opentelemetry.api.trace.Span;

@Bean
public interface ISpanAttributeMapper<T> {

  /**
   * Adds attributes from source object to the span using the correct semantic conventions.
   *
   * @param span
   *     span to add attributes to
   * @param source
   *     source object
   */
  void addAttribute(Span span, T source);
}
