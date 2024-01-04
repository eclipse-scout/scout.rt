/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.scout.rt.ui.html.IUiTextContributor;

public class UiTextContributionFilter implements Predicate<Entry<String, String>> {
  private Set<String> m_acceptedTexts = new HashSet<>();

  public UiTextContributionFilter(List<IUiTextContributor> contributors) {
    for (IUiTextContributor contributor : contributors) {
      contributor.contribute(m_acceptedTexts);
    }
  }

  @Override
  public boolean test(Entry<String, String> entry) {
    return m_acceptedTexts.contains(entry.getKey());
  }
}
