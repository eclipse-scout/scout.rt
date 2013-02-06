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
package org.eclipse.scout.rt.ui.swing.simulator;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class SimulatorAction extends AbstractAction {
  private static final long serialVersionUID = 1L;

  private char m_cmd;

  public SimulatorAction(char cmd) {
    super();
    m_cmd = cmd;
  }

  @Override
  public void actionPerformed(ActionEvent a) {
    switch (m_cmd) {
      case 'R': {
        SwingScoutSimulator.getInstance().record();
        break;
      }
      case 'S': {
        SwingScoutSimulator.getInstance().stop();
        break;
      }
      case 'P': {
        SwingScoutSimulator.getInstance().play();
        break;
      }
      case 'M': {
        SwingScoutSimulator.getInstance().play(1000);
        break;
      }
    }
  }
}// end class
