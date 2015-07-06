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
package org.eclipse.scout.rt.ui.html.json.form;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Filter to accept {@link IForm}s attached to a specific {@link IDisplayParent}.
 */
public class DisplayParentFormFilter implements IFilter<IForm> {

  private final IDisplayParent m_displayParent;

  public DisplayParentFormFilter(final IDisplayParent displayParent) {
    m_displayParent = displayParent;
  }

  @Override
  public boolean accept(final IForm form) {
    return m_displayParent == form.getDisplayParent();
  }
}
