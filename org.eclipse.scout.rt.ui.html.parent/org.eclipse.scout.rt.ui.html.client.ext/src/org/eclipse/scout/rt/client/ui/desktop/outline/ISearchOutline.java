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

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Outline with search capabilities.
 */
public interface ISearchOutline extends IOutline {

  String PROP_SEARCH_STATUS = "searchStatus";

  String PROP_SEARCH_QUERY = "searchQuery";

  void search(String query) throws ProcessingException;

  String getSearchQuery();

  String getSearchStatus();

}
