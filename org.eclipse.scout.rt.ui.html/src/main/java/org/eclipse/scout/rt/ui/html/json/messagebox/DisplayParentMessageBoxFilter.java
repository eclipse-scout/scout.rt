/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.messagebox;

import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.filter.IFilter;

/**
 * Filter to accept message-boxes attached to a specific {@link IDisplayParent}.
 */
public class DisplayParentMessageBoxFilter implements IFilter<IMessageBox> {

  private final IDisplayParent m_displayParent;

  public DisplayParentMessageBoxFilter(final IDisplayParent displayParent) {
    m_displayParent = displayParent;
  }

  @Override
  public boolean accept(final IMessageBox messageBox) {
    return m_displayParent == messageBox.getDisplayParent();
  }
}
