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
import org.eclipse.scout.rt.client.ui.basic.filechooser.IFileChooser;
import org.eclipse.scout.rt.platform.filter.IFilter;

/**
 * Filter to accept file choosers attached to a specific {@link IDisplayParent}.
 */
public class DisplayParentFileChooserFilter implements IFilter<IFileChooser> {

  private final IDisplayParent m_displayParent;

  public DisplayParentFileChooserFilter(final IDisplayParent displayParent) {
    m_displayParent = displayParent;
  }

  @Override
  public boolean accept(final IFileChooser fileChooser) {
    return m_displayParent == fileChooser.getDisplayParent();
  }
}
