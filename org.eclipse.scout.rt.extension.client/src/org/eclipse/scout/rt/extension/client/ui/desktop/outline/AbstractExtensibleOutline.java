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
package org.eclipse.scout.rt.extension.client.ui.desktop.outline;

import java.util.Collection;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.IExtensibleScoutObject;
import org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.PageExtensionUtility;

/**
 * Outline supporting the following Scout extension features:
 * <ul>
 * <li>adding, removing and modifying statically configured pages</li>
 * </ul>
 * 
 * @since 3.9.0
 */
public abstract class AbstractExtensibleOutline extends AbstractOutline implements IExtensibleScoutObject {

  public AbstractExtensibleOutline() {
    super();
  }

  public AbstractExtensibleOutline(boolean callInitialzier) {
    super(callInitialzier);
  }

  @Override
  protected void createChildPagesInternal(Collection<IPage> pageList) throws ProcessingException {
    super.createChildPagesInternal(pageList);
    PageExtensionUtility.adaptOutline(this, pageList);
  }
}
