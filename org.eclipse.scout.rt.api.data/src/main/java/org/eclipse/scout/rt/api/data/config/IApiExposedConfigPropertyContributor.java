/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.config;

import java.util.Set;

import org.eclipse.scout.rt.api.data.IApiExposedItemContributor;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Contributor that modifies the {@link Set} of {@link ConfigPropertyDo} instances that should be exposed in the api.
 */
@Bean
public interface IApiExposedConfigPropertyContributor extends IApiExposedItemContributor<ConfigPropertyDo> {
  /**
   * @param configPropertyDos
   *     Live {@link Set} of {@link ConfigPropertyDo} instances which are published in the api. The {@link Set} may
   *     be directly modified.
   */
  @Override
  void contribute(Set<ConfigPropertyDo> configPropertyDos);
}
