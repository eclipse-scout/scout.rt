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

import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.window.IFormBoundsProvider;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @since 3.9.0
 */
public class MobileDesktopFormBoundsProvider implements IFormBoundsProvider {
  private final IForm m_form;
  private IRwtEnvironment m_uiEnvironment;

  public MobileDesktopFormBoundsProvider(IForm form, IRwtEnvironment uiEnvironment) {
    m_form = form;
    m_uiEnvironment = uiEnvironment;
  }

  @Override
  public Rectangle getBounds() {
    java.awt.Rectangle awtBounds = ClientUIPreferences.getInstance(m_uiEnvironment.getClientSession()).getFormBounds(m_form);
    if (awtBounds != null) {
      return new Rectangle(awtBounds.x, awtBounds.y, awtBounds.width, awtBounds.height);
    }

    return null;
  }

  @Override
  public void storeBounds(Rectangle bounds) {
    // Does not store any bounds to enable the possibility to have different width for different forms on the same view stack.
  }

}
