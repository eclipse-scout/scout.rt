/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.util.Set;

import org.eclipse.scout.rt.api.data.IApiExposedItemContributor;
import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Implementations of this interface contribute to the list of text keys that are sent as (translated) text sent to the
 * UI on session startup (i.e. static UI texts that are required by JavaScript classes). Dynamic texts are always sent
 * as regular (form-)data and must <i>not</i> be contributed here.
 */
@ApplicationScoped
@FunctionalInterface
public interface IUiTextContributor extends IApiExposedItemContributor<String> {

  /**
   * @param textKeys
   *          live set to modify
   */
  @Override
  void contribute(Set<String> textKeys);
}
