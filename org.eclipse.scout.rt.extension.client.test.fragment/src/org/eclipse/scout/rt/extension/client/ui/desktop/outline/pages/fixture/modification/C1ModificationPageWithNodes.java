/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification;

/**
 * @since 3.9.0
 */
public class C1ModificationPageWithNodes extends AbstractCModificationPageWithNodes implements IModifiablePage {

  private boolean m_modified;

  @Override
  public void markModified() {
    m_modified = true;
  }

  @Override
  public boolean isModified() {
    return m_modified;
  }
}
