/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client.menu;


public class MenuItem {

  private final String m_name;
  private final boolean m_enabled;
  private final boolean m_visible;

  public MenuItem(String name, boolean enabled, boolean visible) {
    m_name = name;
    m_enabled = enabled;
    m_visible = visible;
  }

  public String getName() {
    return m_name;
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public boolean isVisible() {
    return m_visible;
  }

}
