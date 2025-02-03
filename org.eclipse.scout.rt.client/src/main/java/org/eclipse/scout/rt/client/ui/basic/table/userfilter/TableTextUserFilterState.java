/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.userfilter.AbstractUserFilterState;

/**
 * @since 5.1
 */
public class TableTextUserFilterState extends AbstractUserFilterState {
  private static final long serialVersionUID = 1L;
  public static final String TYPE = "text";
  private String m_text;

  public TableTextUserFilterState(String text) {
    m_text = text;
    setType(TYPE);
  }

  public String getText() {
    return m_text;
  }

  public void setText(String text) {
    m_text = text;
  }

  @Override
  public String getDisplayText() {
    return m_text;
  }
}
