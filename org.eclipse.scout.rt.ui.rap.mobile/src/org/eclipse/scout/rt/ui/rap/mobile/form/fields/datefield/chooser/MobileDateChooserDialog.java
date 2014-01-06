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

import java.util.Date;

import org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser.DateChooserDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.9.0
 */
public class MobileDateChooserDialog extends DateChooserDialog {
  private static final int DATE_CELL_WIDTH = 38;
  private static final int DATE_CELL_HEIGHT = 33;
  private static final int CONTROL_BUTTON_WIDTH = 30;
  private static final int CONTROL_BUTTON_HEIGHT = 30;

  private static final long serialVersionUID = 1L;

  public MobileDateChooserDialog(Shell parentShell, Control field, Date date) {
    super(parentShell, field, date);
  }

  @Override
  protected int getShellStyle() {
    return SWT.APPLICATION_MODAL | SWT.CLOSE;
  }

  /**
   * Position the popup in the center of the parent shell
   */
  @Override
  protected Point getInitialLocation(Point initialSize, Control field) {
    Rectangle parentShellBounds = getParentShell().getBounds();

    int x = parentShellBounds.x + (parentShellBounds.width - initialSize.x) / 2;
    int y = parentShellBounds.y + (parentShellBounds.height - initialSize.y) / 2;

    return new Point(x, y);
  }

  @Override
  protected int getDateCellHeight() {
    return DATE_CELL_HEIGHT;
  }

  @Override
  protected int getDateCellWidth() {
    return DATE_CELL_WIDTH;
  }

  @Override
  protected int getControlButtonHeight() {
    return CONTROL_BUTTON_HEIGHT;
  }

  @Override
  protected int getControlButtonWidth() {
    return CONTROL_BUTTON_WIDTH;
  }

}
