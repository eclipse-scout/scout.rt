/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swing.spellchecker;

import java.io.Serializable;

import javax.swing.text.JTextComponent;

import org.eclipse.scout.rt.ui.swing.basic.ISwingScoutComposite;

/**
 * Scout Swing textfield holder
 */
public class SwingFieldHolder implements Serializable {
  private static final long serialVersionUID = 1L;

  private ISwingScoutComposite m_uiModelComposite;
  private JTextComponent m_textComponent;

  public SwingFieldHolder(ISwingScoutComposite uiModelComposite, JTextComponent textComponent) {
    m_uiModelComposite = uiModelComposite;
    m_textComponent = textComponent;
  }

  public ISwingScoutComposite getUiModelComposite() {
    return m_uiModelComposite;
  }

  public JTextComponent getTextComponent() {
    return m_textComponent;
  }
}
