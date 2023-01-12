/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.common.search;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

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
