/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.keystroke;

import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("e3167cec-e0d2-4f89-abc9-62696c7e250e")
public class KeyStroke extends AbstractKeyStroke {
  private final String m_id;

  public KeyStroke(String keyStroke) {
    m_id = keyStroke;
    callInitializer();
    setKeyStroke(keyStroke);
  }

  /*
   * ticket 76552 multiple KeyStrokes map to same action
   */
  @Override
  public String getActionId() {
    return m_id;
  }
}
