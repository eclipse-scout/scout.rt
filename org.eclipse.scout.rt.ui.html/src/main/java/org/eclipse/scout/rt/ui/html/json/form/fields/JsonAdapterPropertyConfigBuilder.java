/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields;

import org.eclipse.scout.rt.platform.filter.IFilter;

public class JsonAdapterPropertyConfigBuilder {

  private static final JsonAdapterPropertyConfig DEFAULT = new JsonAdapterPropertyConfig();

  private static final JsonAdapterPropertyConfig GLOBAL = new JsonAdapterPropertyConfigBuilder().global().build();

  private boolean m_global;
  private boolean m_disposeOnChange;
  private IFilter<?> m_filter;

  public JsonAdapterPropertyConfigBuilder() {
    m_disposeOnChange = true;
  }

  public JsonAdapterPropertyConfigBuilder global() {
    m_global = true;
    m_disposeOnChange = false;
    return this;
  }

  public JsonAdapterPropertyConfigBuilder disposeOnChange(boolean disposeOnChange) {
    m_disposeOnChange = disposeOnChange;
    return this;
  }

  public JsonAdapterPropertyConfigBuilder filter(IFilter<?> filter) {
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
