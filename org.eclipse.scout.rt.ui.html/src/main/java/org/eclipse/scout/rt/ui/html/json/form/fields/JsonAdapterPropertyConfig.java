/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.util.function.Predicate;

public class JsonAdapterPropertyConfig {

  private final boolean m_global;
  private final boolean m_disposeOnChange;
  private final Predicate<?> m_filter;

  JsonAdapterPropertyConfig() {
    m_global = false;
    m_disposeOnChange = true;
    m_filter = null;
  }

  JsonAdapterPropertyConfig(boolean global, boolean disposeOnChange, Predicate<?> filter) {
    m_global = global;
    m_disposeOnChange = disposeOnChange;
    m_filter = filter;
  }

  public boolean isGlobal() {
    return m_global;
  }

  public boolean isDisposeOnChange() {
    return m_disposeOnChange;
  }

  @SuppressWarnings("unchecked")
  public Predicate<Object> getFilter() {
    return (Predicate<Object>) m_filter;
  }

}
