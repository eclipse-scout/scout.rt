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
package org.eclipse.scout.rt.ui.rap.mobile.window.dialog;

import org.eclipse.scout.rt.ui.rap.window.IFormBoundsProvider;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Positions the form in the middle of the screen and makes it as height as the screen.
 * <p>
 * As default the height is set to the height of the display which means the dialog is as height as the screen.<br>
 * The width is set to a fix value which is {@link #DIALOG_WIDTH} but only if it does not exceed the width of the
 * screen.
 * <p>
 * The bounds are not cached.
 * 
 * @since 3.9.0
 */
public class FixedSizeDialogBoundsProvider implements IFormBoundsProvider {
  public static final int DIALOG_WIDTH = 700;

  private int m_maxWidth = DIALOG_WIDTH;
  private int m_maxHeight = Integer.MAX_VALUE;

  public FixedSizeDialogBoundsProvider() {
  }

  @Override
  public Rectangle getBounds() {
    int maxWidth = m_maxWidth;
    int maxHeight = m_maxHeight;

    Rectangle displayBounds = Display.getCurrent().getBounds();

    int height = Math.min(maxHeight, displayBounds.height);
    int width = Math.min(maxWidth, displayBounds.width);
    int x = (displayBounds.width / 2) - (width / 2);
    int y = 0;

    return new Rectangle(x, y, width, height);
  }

  @Override
  public void storeBounds(Rectangle bounds) {
    // nop
  }

}
