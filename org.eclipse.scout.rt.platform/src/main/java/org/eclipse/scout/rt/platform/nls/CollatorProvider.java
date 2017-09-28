/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
 * A Provider for {@link Collator}s to simplify replacements.
 * <h3>{@link CollatorProvider}</h3>
 *
 * @author jgu
 */
@ApplicationScoped
public class CollatorProvider {

  public Collator getInstance() {
    return getInstance(NlsLocale.get());
  }

  /**
   * @param desiredLocale
   * @return a collator for the desired locale
   */
  public Collator getInstance(Locale desiredLocale) {
    return Collator.getInstance(desiredLocale);
  }

}
