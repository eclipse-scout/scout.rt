/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.nls;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.scout.rt.platform.ApplicationScoped;

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
    return Collator.getInstance(locale);
  }
}
