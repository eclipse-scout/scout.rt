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
package org.eclipse.scout.rt.ui.rap.window;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @since 3.8.0
 */
public class DefaultFormBoundsProvider implements IFormBoundsProvider {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultFormBoundsProvider.class);

  private final IForm m_form;
  private IRwtEnvironment m_uiEnvironment;

  public DefaultFormBoundsProvider(IForm form, IRwtEnvironment uiEnvironment) {
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
    java.awt.Rectangle awtBounds = null;
    if (bounds != null) {
      awtBounds = new java.awt.Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    ClientUIPreferences.getInstance(m_uiEnvironment.getClientSession()).setFormBounds(m_form, awtBounds);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Bounds stored for " + m_form.getClass() + ". Bounds: " + bounds);
    }
  }
}
