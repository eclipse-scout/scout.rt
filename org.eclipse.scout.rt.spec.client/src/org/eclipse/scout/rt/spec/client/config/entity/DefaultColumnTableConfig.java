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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.spec.client.filter.DefaultDocFilter;
import org.eclipse.scout.rt.spec.client.filter.IDocFilter;
import org.eclipse.scout.rt.spec.client.filter.column.DisplayableColumnFilter;
import org.eclipse.scout.rt.spec.client.gen.extract.ColumnTypeExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.DescriptionExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.IDocTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.column.ColumnHeaderTextExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.column.ColumnHeaderTooltipExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.column.ColumnSortIndexExtractor;
import org.eclipse.scout.rt.spec.client.gen.extract.column.ColumnWidthExtractor;

/**
 * The default configuration for {@link IColumn}
 */
public class DefaultColumnTableConfig extends AbstractEntityTableConfig<IColumn<?>> {

  /**
   * Default properties for {@link IColumn} with
   * <p>
   * Sort,Label,Type,Width,Tooltip,Description
   * </p>
   */
  @Override
  public List<IDocTextExtractor<IColumn<?>>> getTextExtractors() {
    List<IDocTextExtractor<IColumn<?>>> propertyTemplate = new ArrayList<IDocTextExtractor<IColumn<?>>>();
    propertyTemplate.add(new ColumnHeaderTextExtractor());
    propertyTemplate.add(new DescriptionExtractor<IColumn<?>>());
    propertyTemplate.add(new ColumnHeaderTooltipExtractor());
    propertyTemplate.add(new ColumnSortIndexExtractor());
    propertyTemplate.add(new ColumnWidthExtractor());
    propertyTemplate.add(new ColumnTypeExtractor());
    return propertyTemplate;
  }

  /**
   * Default filters for {@link IColumn}: Ignores Types annotated with {@link org.eclipse.scout.commons.annotations.Doc
   * Doc#ignore()}==false and columns that are not displayable
   */
  @Override
  public List<IDocFilter<IColumn<?>>> getFilters() {
    List<IDocFilter<IColumn<?>>> columnFilters = new ArrayList<IDocFilter<IColumn<?>>>();
    columnFilters.add(new DefaultDocFilter<IColumn<?>>());
    columnFilters.add(new DisplayableColumnFilter());
    return columnFilters;
  }

  @Override
  public String getTitle() {
    return TEXTS.get("org.eclipse.scout.rt.spec.columns");
  }
}
