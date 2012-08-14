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

import org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser.TimeChooser;
import org.eclipse.scout.rt.ui.rap.form.fields.datefield.chooser.TimeChooserDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.9.0
 */
public class MobileTimeChooserDialog extends TimeChooserDialog {

  private static final long serialVersionUID = 1L;

  public MobileTimeChooserDialog(Shell parentShell, Date date) {
    super(parentShell, date);
  }

  @Override
  protected int getShellStyle() {
    return SWT.APPLICATION_MODAL | SWT.CLOSE;
  }

  /**
   * Position the popup in the center of the parent shell
   */
  @Override
  protected Point getLocation(Control field) {
    Rectangle parentShellBounds = getParentShell().getBounds();
    Point dialogSize = getShell().getSize();

    int x = parentShellBounds.x + (parentShellBounds.width - dialogSize.x) / 2;
    int y = parentShellBounds.y + (parentShellBounds.height - dialogSize.y) / 2;

    return new Point(x, y);
  }

  @Override
  protected TimeChooser createTimeChooser(Composite parent) {
    return new MobileTimeChooser(parent);
  }

}
