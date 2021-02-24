/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
      contributor.contributeUiTextKeys(m_acceptedTexts);
    }
  }

  @Override
  public boolean test(Entry<String, String> entry) {
    return m_acceptedTexts.contains(entry.getKey());
  }
}
