/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * A provider for {@link Collator}s to simplify replacements.
 *
 * @author jgu
 */
@ApplicationScoped
public class CollatorProvider {

  /**
   * Delegates to {@link #getInstance(Locale)} with {@link NlsLocale#get()} as argument.
   *
   * @return a collator for the current locale
   */
  public Collator getInstance() {
    return getInstance(NlsLocale.get());
  }

  /**
   * Subclasses can override this method to change the default behavior.
   *
   * @return a collator for the given locale
   */
  public Collator getInstance(Locale locale) {
    return BEANS.get(NaturalCollatorProvider.class).getInstance(locale);
  }
}
