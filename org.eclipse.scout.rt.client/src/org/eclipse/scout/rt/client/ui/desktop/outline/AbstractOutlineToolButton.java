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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;

/**
 * @deprecated use {@link AbstractOutlineViewButton}
 */
@Deprecated
public abstract class AbstractOutlineToolButton extends AbstractOutlineViewButton {

  /**
   * @param desktop
   * @param outlineType
   */
  public AbstractOutlineToolButton(IDesktop desktop, Class<? extends IOutline> outlineType) {
    super(desktop, outlineType);
  }
}
