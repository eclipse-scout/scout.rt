/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.util.EventObject;

import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

@SuppressWarnings({"serial", "squid:S2057"})
public class UiSessionEvent extends EventObject {

  public static final int TYPE_ADAPTER_CREATED = 100;
  public static final int TYPE_ADAPTER_DISPOSED = 200;

  private final int m_type;
  private IJsonAdapter<?> m_jsonAdapter;

  public UiSessionEvent(UiSession source, int type, IJsonAdapter<?> adapter) {
    super(source);
    m_type = type;
    m_jsonAdapter = adapter;
  }

  @Override
  public UiSession getSource() {
    return (UiSession) super.getSource();
  }

  public IJsonAdapter<?> getJsonAdapter() {
    return m_jsonAdapter;
  }

  public int getType() {
    return m_type;
  }
}
