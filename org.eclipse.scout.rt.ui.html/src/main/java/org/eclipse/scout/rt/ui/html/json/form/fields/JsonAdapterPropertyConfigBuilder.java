/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.util.function.Predicate;

public class JsonAdapterPropertyConfigBuilder {

  private static final JsonAdapterPropertyConfig DEFAULT = new JsonAdapterPropertyConfig();

  private static final JsonAdapterPropertyConfig GLOBAL = new JsonAdapterPropertyConfigBuilder().global().build();

  private Predicate<Object> m_global;
  private boolean m_disposeOnChange;
  private Predicate<?> m_filter;

  public JsonAdapterPropertyConfigBuilder() {
    m_disposeOnChange = true;
  }

  public JsonAdapterPropertyConfigBuilder global() {
    m_global = model -> true;
    m_disposeOnChange = false;
    return this;
  }

  public JsonAdapterPropertyConfigBuilder global(Predicate<Object> global) {
    m_global = global;
    return this;
  }

  public JsonAdapterPropertyConfigBuilder disposeOnChange(boolean disposeOnChange) {
    m_disposeOnChange = disposeOnChange;
    return this;
  }

  public JsonAdapterPropertyConfigBuilder filter(Predicate<?> filter) {
    m_filter = filter;
    return this;
  }

  public JsonAdapterPropertyConfig build() {
    return new JsonAdapterPropertyConfig(m_global, m_disposeOnChange, m_filter);
  }

  public static JsonAdapterPropertyConfig defaultConfig() {
    return DEFAULT;
  }

  public static JsonAdapterPropertyConfig globalConfig() {
    return GLOBAL;
  }
}
