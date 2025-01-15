/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.userfilter;

import org.eclipse.scout.rt.platform.text.TEXTS;

/**
 * @since 5.1
 */
public abstract class AbstractUserFilterState implements IUserFilterState {
  private static final long serialVersionUID = 1L;
  private String m_type;

  @Override
  public String getType() {
    return m_type;
  }

  @Override
  public void setType(String type) {
    m_type = type;
  }

  @Override
  public Object createKey() {
    return m_type;
  }

  @Override
  public String getDisplayText() {
    return TEXTS.get("Filter");
  }

  @Override
  public boolean notifyDeserialized(Object obj) {
    return true;
  }
}
