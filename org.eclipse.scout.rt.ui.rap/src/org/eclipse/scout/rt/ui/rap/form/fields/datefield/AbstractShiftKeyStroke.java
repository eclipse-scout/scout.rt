/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.datefield;

import org.eclipse.scout.rt.ui.rap.keystroke.RwtKeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

/**
 * @since 3.8.3
 */
abstract class AbstractShiftKeyStroke extends RwtKeyStroke {
  private final int m_level;
  private final int m_value;

  public AbstractShiftKeyStroke(int keyCode, int level, int value) {
    this(keyCode, SWT.NONE, level, value);
  }

  public AbstractShiftKeyStroke(int keyCode, int stateMask, int level, int value) {
    super(keyCode, stateMask);
    m_level = level;
    m_value = value;
  }

  protected abstract void shift(final int level, final int value);

  @Override
  public void handleUiAction(Event e) {
    shift(getLevel(), getValue());
    e.doit = false;
  }

  protected int getLevel() {
    return m_level;
  }

  protected int getValue() {
    return m_value;
  }
}
