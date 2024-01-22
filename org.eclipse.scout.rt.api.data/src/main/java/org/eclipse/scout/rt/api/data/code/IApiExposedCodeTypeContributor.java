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

import org.eclipse.scout.rt.api.data.IApiExposedItemContributor;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Contributor for CodeTypes that should be exposed by the Scout REST api.
 */
@Bean
public interface IApiExposedCodeTypeContributor extends IApiExposedItemContributor<CodeTypeDo> {
  /**
   * @param codeTypes
   *          Live {@link Set} of {@link CodeTypeDo} which are published in the Scout REST resource. The {@link Set} may
   *          be directly modified.
   */
  @Override
  void contribute(Set<CodeTypeDo> codeTypes);
}
