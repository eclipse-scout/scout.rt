/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

@SuppressWarnings({"serial", "squid:S2057"})
public class UiSessionEvent extends EventObject implements IModelEvent {

  public static final int TYPE_ADAPTER_REGISTERED = 100;

  private final int m_type;
  private IJsonAdapter<?> m_jsonAdapter;

  public UiSessionEvent(UiSession source, int type, IJsonAdapter<?> adapter) {
    super(source);
    m_type = type;
    m_jsonAdapter = adapter;
  }

  public UiSession getUisession() {
    return (UiSession) getSource();
  }

  public IJsonAdapter<?> getJsonAdapter() {
    return m_jsonAdapter;
  }

  @Override
  public int getType() {
    return m_type;
  }
}
