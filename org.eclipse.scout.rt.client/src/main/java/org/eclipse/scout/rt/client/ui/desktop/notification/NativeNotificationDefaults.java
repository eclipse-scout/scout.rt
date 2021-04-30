/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.desktop.notification;

public class NativeNotificationDefaults {
  private String m_title;
  private String m_iconId;
  private String m_visibility;

  public String getTitle() {
    return m_title;
  }

  public NativeNotificationDefaults withTitle(String title) {
    m_title = title;
    return this;
  }

  public String getIconId() {
    return m_iconId;
  }

  public NativeNotificationDefaults withIconId(String iconId) {
    m_iconId = iconId;
    return this;
  }

  public String getVisibility() {
    return m_visibility;
  }

  public NativeNotificationDefaults withVisibility(String visibility) {
    m_visibility = visibility;
    return this;
  }
}
