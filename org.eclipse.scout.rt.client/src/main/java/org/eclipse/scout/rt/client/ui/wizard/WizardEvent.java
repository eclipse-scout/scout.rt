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
package org.eclipse.scout.rt.client.ui.wizard;

import java.util.EventObject;

import org.eclipse.scout.rt.client.ui.IModelEvent;

public class WizardEvent extends EventObject implements IModelEvent {
  private static final long serialVersionUID = 1L;

  /**
   * One or more of the following changes occured (state machine) State transition has occured, next step was activated
   * (but not run) History has changed Expected future has changed
   */
  public static final int TYPE_STATE_CHANGED = 10;

  /**
   * Wizard was closed after either finish, cancel, suspend or just by calling {@link IWizard#close()}.
   * <p>
   * Check the {@link IWizard#getCloseType()} for details.
   */
  public static final int TYPE_CLOSED = 50;

  private final int m_type;

  public WizardEvent(IWizard wizard, int type) {
    super(wizard);
    m_type = type;
  }

  public IWizard getWizard() {
    return (IWizard) getSource();
  }

  @Override
  public int getType() {
    return m_type;
  }
}
