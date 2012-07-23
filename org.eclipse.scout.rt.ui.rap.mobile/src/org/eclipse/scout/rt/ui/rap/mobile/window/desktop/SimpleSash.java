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
package org.eclipse.scout.rt.ui.rap.mobile.window.desktop;

import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Sash;

/**
 * A sash with no functionality.
 * 
 * @since 3.9.0
 */
public class SimpleSash extends Sash {
  private static final long serialVersionUID = 1L;

  public SimpleSash(Composite parent, int style) {
    super(parent, style);

    setEnabled(false);
  }

  @Override
  public void addSelectionListener(SelectionListener listener) {
    // nop
  }

  @Override
  public void removeSelectionListener(SelectionListener listener) {
    // nop
  }

}
