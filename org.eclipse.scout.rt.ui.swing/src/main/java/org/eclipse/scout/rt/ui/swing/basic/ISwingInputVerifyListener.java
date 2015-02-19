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
package org.eclipse.scout.rt.ui.swing.basic;

import javax.swing.InputVerifier;
import javax.swing.JComponent;

import org.eclipse.scout.commons.WeakEventListener;

/**
 * Listener to be notified about input verify events
 * 
 * @see ISwingScoutComposite#addInputVerifyListener(ISwingInputVerifyListener)
 * @see JComponent#setInputVerifier(javax.swing.InputVerifier)
 * @see InputVerifier
 */
public interface ISwingInputVerifyListener extends WeakEventListener {

  /**
   * Called when the {@link ISwingScoutComposite} handles the {@link InputVerifier#verify(JComponent)} event.
   * 
   * @param input
   * @see InputVerifier#verify(JComponent)
   */
  void verify(JComponent input);
}
