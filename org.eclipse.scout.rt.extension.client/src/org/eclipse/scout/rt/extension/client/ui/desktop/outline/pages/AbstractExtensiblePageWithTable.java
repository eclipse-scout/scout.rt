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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.extension.client.IExtensibleScoutObject;
import org.eclipse.scout.rt.shared.ContextMap;

/**
 * Page with table implementation supporting the following Scout extension features:
 * <ul>
 * <li>adding, removing and modifying statically configured pages</li>
 * </ul>
 * 
 * @since 3.9.0
 */
public abstract class AbstractExtensiblePageWithTable<T extends ITable> extends AbstractPageWithTable<T> implements IExtensibleScoutObject {

  public AbstractExtensiblePageWithTable() {
    super();
  }

  public AbstractExtensiblePageWithTable(boolean callInitializer) {
    super(callInitializer);
  }

  public AbstractExtensiblePageWithTable(ContextMap contextMap) {
    super(contextMap);
  }

  public AbstractExtensiblePageWithTable(String userPreferenceContext) {
    super(userPreferenceContext);
  }

  public AbstractExtensiblePageWithTable(boolean callInitializer, ContextMap contextMap) {
    super(callInitializer, contextMap);
  }

  public AbstractExtensiblePageWithTable(boolean callInitializer, String userPreferenceContext) {
    super(callInitializer, userPreferenceContext);
  }

  public AbstractExtensiblePageWithTable(boolean callInitializer, ContextMap contextMap, String userPreferenceContext) {
    super(callInitializer, contextMap, userPreferenceContext);
  }

  @Override
  protected IPage createChildPageInternal(ITableRow row) throws ProcessingException {
    IPage childPage = super.createChildPageInternal(row);
    PageExtensionUtility.adaptPage(getOutline(), this, childPage);
    return childPage;
  }
}
