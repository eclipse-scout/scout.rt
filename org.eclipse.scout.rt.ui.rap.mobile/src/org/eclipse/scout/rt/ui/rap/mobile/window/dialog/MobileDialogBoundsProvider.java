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

import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.window.IFormBoundsProvider;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Positions the form in the middle of the screen. As default the width and height are set to width and height of the
 * display which means full screen. If a form provides a width this will be considered as max width. This means if the
 * screen is smaller than the form the form will be as width as the screen. If the screen is bigger than the form the
 * form will be as width as specified with max width.
 * 
 * @since 3.8.0
 */
public class MobileDialogBoundsProvider implements IFormBoundsProvider {
  private final IForm m_form;
  private IRwtEnvironment m_uiEnvironment;

  public MobileDialogBoundsProvider(IForm form, IRwtEnvironment uiEnvironment) {
    m_form = form;
    m_uiEnvironment = uiEnvironment;
  }

  @Override
  public Rectangle getBounds() {
    int maxWidth = Integer.MAX_VALUE;
    java.awt.Rectangle formBounds = ClientUIPreferences.getInstance(m_uiEnvironment.getClientSession()).getFormBounds(m_form);
    if (formBounds != null) {
      maxWidth = formBounds.width;
    }

    Rectangle displayBounds = Display.getCurrent().getBounds();

    int height = displayBounds.height;
    int width = Math.min(maxWidth, displayBounds.width);
    int x = (displayBounds.width / 2) - (width / 2);
    int y = 0;

    return new Rectangle(x, y, width, height);
  }

  @Override
  public void storeBounds(Rectangle bounds) {

  }

}
