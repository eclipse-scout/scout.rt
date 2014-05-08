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
package org.eclipse.scout.rt.spec.client.config.entity;

import java.util.List;

import org.eclipse.scout.commons.ArrayComparator;
import org.eclipse.scout.rt.spec.client.filter.IDocFilter;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;

/**
 * Configuration for the documentation of scout entities in a table (e.g. a table containing all fields in a form)
 * 
 * @param <T>
 *          the type of the configuration (e.g. {@link org.eclipse.scout.rt.client.ui.form.fields.IFormField IFormField}
 */
public interface IDocEntityTableConfig<T> {

  /**
   * Configuration for documenting type <code>T</code>.
   * 
   * @return a list of properties that should be generated.
   */
  List<IDocTextExtractor<T>> getTextExtractors();

  /**
   * Configuration for filtering <code>T</code>. Only the objects accepted by all filters are generated.
   * 
   * @return a list of filters.
   */
  List<IDocFilter<T>> getFilters();

  /**
   * Title of the section
   */
  String getTitle();

  /**
   * {@link ArrayComparator.ColumnComparator ArrayComparator.ColumnComparators} for sorting the table
   * <p>
   * If null or empty, no sorting takes place.
   */
  ArrayComparator.ColumnComparator[] getSortColumns();

}
