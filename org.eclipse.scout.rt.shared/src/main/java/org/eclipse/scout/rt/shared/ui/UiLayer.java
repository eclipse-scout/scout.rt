/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui;

/**
 * @since 3.8.0
 */
public enum UiLayer implements IUiLayer {

  HTML(true),
  UNKNOWN;

  final boolean m_webUi;

  UiLayer(boolean webUi) {
    m_webUi = webUi;
  }

  UiLayer() {
    this(false);
  }

  @Override
  public boolean isWebUi() {
    return m_webUi;
  }

  @Override
  public String getIdentifier() {
    return name();
  }

  public static IUiLayer createByIdentifier(String identifier) {
    return valueOf(identifier);
  }
}
