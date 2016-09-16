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
package org.eclipse.scout.rt.client.ui.action.keystroke;

import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("e3167cec-e0d2-4f89-abc9-62696c7e250e")
@SuppressWarnings("bsiRulesDefinition:orderMissing")
public class KeyStroke extends AbstractKeyStroke {
  private String m_id;

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
