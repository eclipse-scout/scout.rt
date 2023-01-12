/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
