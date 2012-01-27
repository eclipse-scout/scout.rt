/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.keystroke;

import org.eclipse.rwt.lifecycle.PhaseListener;
import org.eclipse.swt.widgets.Event;

public interface IRwtKeyStroke {

  /**
   * set e.doit false to avoid
   * 
   * @param e
   */
  void handleUiAction(Event e);

  int getStateMask();

  int getKeyCode();

  void setPhaseListener(PhaseListener phaseListener);

  PhaseListener getPhaseListener();
}
