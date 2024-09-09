/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.code;

import java.util.Set;

import org.eclipse.scout.rt.api.data.ApiExposed;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Interface to get {@link CodeTypeDo} instances which are {@link ApiExposed}.
 */
@ApplicationScoped
public interface IApiExposedCodeTypeDoProvider {

  /**
   * @return The {@link CodeTypeDo} instances for all {@link ApiExposed} CodeTypes.
   */
  Set<CodeTypeDo> provide();

  /**
   * @param ids
   *     An optional filter to only return the {@link CodeTypeDo} instances having the given IDs (see
   *     {@link CodeTypeDo#getId()}). May be {@code null} then all are returned (no filtering).
   * @return The {@link CodeTypeDo} instances for the {@link ApiExposed} CodeTypes having the given IDs.
   */
  Set<CodeTypeDo> provide(Set<String> ids);
}
