/*******************************************************************************
 * Copyright (c) 2010,2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.action.keystroke;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.IAction;

public class KeyStroke extends AbstractKeyStroke {
  private IAction m_delegate;
  private String m_id;

  public KeyStroke(String keyStroke) {
    this(keyStroke, null);
  }

  public KeyStroke(String keyStroke, IAction delegate) {
    super(false);
    m_delegate = delegate;
    // create special id with key as part of name
    if (delegate != null) {
      m_id = delegate.getActionId() + ".keyStroke";
    }
    else {
      m_id = keyStroke;
    }
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

  @Override
  protected void execAction() throws ProcessingException {
    if (m_delegate != null) {
      m_delegate.prepareAction();
      if (m_delegate.isThisAndParentsVisible() && m_delegate.isThisAndParentsEnabled()) {
        m_delegate.doAction();
      }
    }
    else {
      super.execAction();
    }
  }

}
