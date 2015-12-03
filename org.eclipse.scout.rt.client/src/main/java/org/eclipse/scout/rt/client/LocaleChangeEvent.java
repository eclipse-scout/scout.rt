/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client;

import java.util.EventObject;
import java.util.Locale;

/**
 * This event is fired when the locale has changed.
 */
public class LocaleChangeEvent extends EventObject {
  private static final long serialVersionUID = 1L;

  private Locale m_locale;

  public LocaleChangeEvent(Object source) {
    super(source);
  }

  public LocaleChangeEvent(Object source, Locale locale) {
    super(source);
    m_locale = locale;
  }

  public Locale getLocale() {
    return m_locale;
  }
}
