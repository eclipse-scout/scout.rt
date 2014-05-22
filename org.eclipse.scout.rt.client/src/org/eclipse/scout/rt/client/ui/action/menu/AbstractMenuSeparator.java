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
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;

/**
 *
 */
public abstract class AbstractMenuSeparator extends AbstractMenu {

  @Override
  protected final boolean getConfiguredSeparator() {
    return true;
  }

  @Override
  protected final Set<? extends IMenuType> getConfiguredMenuTypes() {
    return CollectionUtility.emptyHashSet();
  }

  @Override
  protected final String getConfiguredKeyStroke() {
    return null;
  }

  @Override
  protected final void execAction() throws ProcessingException {
    // void
  }

  @Override
  protected final void execToggleAction(boolean selected) throws ProcessingException {
    // void
  }
}
