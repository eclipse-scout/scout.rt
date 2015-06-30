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
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTableForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTreeForm;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Filter for Forms to be rendered by UI.
 */
public class FormFilter implements IFilter<IForm> {

  private final boolean m_formBased;

  public FormFilter(boolean formBased) {
    m_formBased = formBased;
  }

  @Override
  public boolean accept(final IForm form) {
    if (m_formBased) {
      return true;
    }
    if (form instanceof IOutlineTableForm) {
      return false;
    }
    if (form instanceof IOutlineTreeForm) {
      return false;
    }
    return true;
  }
}
