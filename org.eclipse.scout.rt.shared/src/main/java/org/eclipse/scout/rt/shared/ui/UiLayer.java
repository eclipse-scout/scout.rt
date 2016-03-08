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
package org.eclipse.scout.rt.shared.ui;

/**
 * @since 3.8.0
 */
public enum UiLayer implements IUiLayer {

  HTML(true),
  UNKNOWN;

  final boolean m_webUi;

  private UiLayer(boolean webUi) {
    m_webUi = webUi;
  }

  private UiLayer() {
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
