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

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.ui.rap.window.IFormBoundsProvider;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Reads the width and height specified in the {@link GridData} of the main group box ({@link IForm#getRootGroupBox()}).
 * If nothing is specified null will be returned.
 * <p>
 * The bounds are not cached.
 * 
 * @since 3.9.0
 */
public class FormBasedDesktopFormBoundsProvider implements IFormBoundsProvider {
  private final IForm m_form;

  public FormBasedDesktopFormBoundsProvider(IForm form) {
    m_form = form;
  }

  @Override
  public Rectangle getBounds() {
    int maxWidth = -1;
    int maxHeight = -1;

    GridData gridData = m_form.getRootGroupBox().getGridData();
    if (gridData.widthInPixel > 0) {
      maxWidth = gridData.widthInPixel;
    }
    if (gridData.heightInPixel > 0) {
      maxHeight = gridData.heightInPixel;
    }

    if (maxWidth == -1 && maxHeight == -1) {
      return null;
    }
    else {
      return new Rectangle(-1, -1, maxWidth, maxHeight);
    }
  }

  @Override
  public void storeBounds(Rectangle bounds) {
    // nop
  }

}
