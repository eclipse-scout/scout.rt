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
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages;

import java.util.ArrayList;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.ContextMap;

/**
 * Page with nodes implementation using Eclipse extension point mechanisms for adding new pages as well as modifying and
 * removing statically configured ones.
 * 
 * @since 3.9.0
 */
public abstract class AbstractExtensiblePageWithNodes extends AbstractPageWithNodes {

  public AbstractExtensiblePageWithNodes() {
    super();
  }

  public AbstractExtensiblePageWithNodes(boolean callInitializer) {
    super(callInitializer);
  }

  public AbstractExtensiblePageWithNodes(ContextMap contextMap) {
    super(contextMap);
  }

  public AbstractExtensiblePageWithNodes(String userPreferenceContext) {
    super(userPreferenceContext);
  }

  public AbstractExtensiblePageWithNodes(boolean callInitializer, String userPreferenceContext) {
    super(callInitializer, userPreferenceContext);
  }

  public AbstractExtensiblePageWithNodes(boolean callInitializer, ContextMap contextMap, String userPreferenceContext) {
    super(callInitializer, contextMap, userPreferenceContext);
  }

  @Override
  protected void createChildPagesInternal(ArrayList<IPage> pageList) throws ProcessingException {
    super.createChildPagesInternal(pageList);
    PageExtensionUtility.adaptPageWithNodes(this, pageList);
  }
}
