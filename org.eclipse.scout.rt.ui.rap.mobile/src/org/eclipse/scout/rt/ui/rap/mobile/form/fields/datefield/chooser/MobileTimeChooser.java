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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.datefield.chooser;

import org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser.TimeChooser;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public class MobileTimeChooser extends TimeChooser {
  private static final int TABLE_CELL_HEIGHT = 30;

  public MobileTimeChooser(Composite parent) {
    super(parent);
  }

  @Override
  protected int getTableCellHeight() {
    return TABLE_CELL_HEIGHT;
  }

}
