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
package org.eclipse.scout.rt.client.services.common.search;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.service.IService;

/**
 * Provides an implementation for new search filters
 */
public interface ISearchFilterService extends IService {

  SearchFilter createNewSearchFilter();

  /**
   * whenever applySearch is called on any field, this methods is called
   */
  void applySearchDelegate(IFormField field, SearchFilter filter, boolean includeChildren);
}
