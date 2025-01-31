/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.scout.rt.client.ui.basic.table.columns.CompactBean;
import org.eclipse.scout.rt.client.ui.basic.table.columns.CompactLine;
import org.eclipse.scout.rt.client.ui.basic.table.columns.CompactLineBuilder;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

public interface ITableCompactHandler {

  /**
   * Transforms the table into compact state:
   * <ul>
   * <li>Adjusts some properties of the table itself (e.g. header visible = false)</li>
   * <li>Marks all columns as compacted so they won't be sent to ui.</li>
   * <li>Computes the compact value for each row and and updates the property {@link ITableRow#getCompactValue()}.</li>
   * <li>Adds listeners to the table to update the computed value on relevant table changes.</li>
   * </ul>
   * The ui itself will add a separate compact column that displays the compacted values.
   */
  void handle(boolean compact);

  /**
   * The method will only build the compacted value without touching the table or its columns.
   *
   * @return the final string ready to be displayed. It contains html, so make sure html is enabled for the target.
   */
  String buildValue(ITableRow row);

  /**
   * Processes the html template and creates a string based on the values of the bean.
   *
   * @param bean
   *     the bean that contains the data for a specific row
   * @return the final string ready to be displayed. It contains html, so make sure html is enabled for the target.
   */
  String buildValue(CompactBean bean);

  /**
   * @return the bean used to build the final html string for a given row.
   */
  CompactBean buildBean(ITableRow row);

  List<IColumn<?>> getColumns();

  boolean isUseOnlyVisibleColumns();

  /**
   * By default, invisible columns are not processed. This can be changed by setting the flag to true.<br>
   */
  ITableCompactHandler withUseOnlyVisibleColumns(boolean useOnlyVisibleColumns);

  int getMaxContentLines();

  /**
   * @param maxContentLines
   *     the maximum number of lines in the content block. If {@link #isMoreLinkAvailable()} is true and the more
   *     link would only reveal one line, the number is automatically increased by 1.
   */
  ITableCompactHandler withMaxContentLines(int maxContentLines);

  boolean isMoreLinkAvailable();

  /**
   * The more link is shown if there are more content lines than {@link #getMaxContentLines()}. Clicking on the more
   * link will reveal the other lines.<br>
   * Set this flag to false to never show a more link.
   */
  ITableCompactHandler withMoreLinkAvailable(boolean moreLinkAvailable);

  CompactBeanBuilder getBeanBuilder();

  /**
   * The default bean builder will process every column and fill the bean based on the given line builders (e.g.
   * {@link #getTitleBuilder()}. The creation of the bean can be influenced by using custom line builders. But since
   * each line builder processes only one column they may not be sufficient. To have more control a custom bean builder
   * may be used instead which will turn off the line builders and you need to create the whole bean by yourself.
   */
  ITableCompactHandler withBeanBuilder(CompactBeanBuilder builder);

  CompactLineBuilder getTitleBuilder();

  /**
   * The title builder creates a line for {@link CompactBean#setTitleLine(CompactLine)}. The default will create a line
   * for the first column and won't add a label.
   * <p>
   * <b>Note</b>: if you add a builder for an invisible column, the column will only be considered if
   * {@link #isUseOnlyVisibleColumns()} is false.<br>
   */
  ITableCompactHandler withTitleBuilder(CompactLineBuilder builder);

  /**
   * Shortcut for {@link #withTitleBuilder(CompactLineBuilder)}.
   */
  ITableCompactHandler withTitleColumnSupplier(Supplier<IColumn<?>> titleColumnSupplier);

  CompactLineBuilder getSubtitleBuilder();

  /**
   * The subtitle builder creates a line for {@link CompactBean#setSubtitleLine(CompactLine)}. The default will create a
   * line for the second column and won't add a label.
   * <p>
   * <b>Note</b>: if you add a builder for an invisible column, the column will only be considered if
   * {@link #isUseOnlyVisibleColumns()} is false.<br>
   */
  ITableCompactHandler withSubtitleBuilder(CompactLineBuilder builder);

  /**
   * Shortcut for {@link #withSubtitleBuilder(CompactLineBuilder)}.
   */
  ITableCompactHandler withSubtitleColumnSupplier(Supplier<IColumn<?>> subtitleColumnSupplier);

  CompactLineBuilder getTitleSuffixBuilder();

  /**
   * The title suffix builder creates a line for {@link CompactBean#setTitleSuffixLine(CompactLine)}. The default will
   * never create a line. Set a custom builder to fill the title suffix.
   * <p>
   * <b>Note</b>: if you add a builder for an invisible column, the column will only be considered if
   * {@link #isUseOnlyVisibleColumns()} is false.<br>
   */
  ITableCompactHandler withTitleSuffixBuilder(CompactLineBuilder builder);

  /**
   * Shortcut for {@link #withTitleSuffixBuilder(CompactLineBuilder)}.
   */
  ITableCompactHandler withTitleSuffixColumnSupplier(Supplier<IColumn<?>> titleSuffixColumnSupplier);

  List<CompactLineBuilder> getContentBuilders();

  /**
   * The content line builder creates a line for {@link CompactBean#addContentLine(CompactLine)}. By default a line will
   * be created for every column not processed by title, subtitle or title suffix builder. A label will always be
   * added.<br>
   * By adding a content line builder you can control the order and generation of content lines. Columns not explicitly
   * handled by a provided content line builder will be be appended to the end and handled by
   * {@link #getDefaultContentLineBuilder()}.<br>
   * Depending on the number of content line builders you may have to increase the max content lines using
   * {@link #withMaxContentLines(int)}
   * <p>
   * <b>Note</b>: if you add a builder for an invisible column, the column will only be considered if
   * {@link #isUseOnlyVisibleColumns()} is false.<br>
   * <b>Note</b>: if you want to exclude columns, you need to use {@link #withColumnFilter(Predicate)} rather than add a
   * builder that not accepts a column.
   */
  ITableCompactHandler addContentLineBuilder(CompactLineBuilder builder);

  /**
   * Shortcut for {@link #addContentLineBuilder(CompactLineBuilder)}.
   */
  ITableCompactHandler addContentColumnSupplier(Supplier<IColumn<?>> contentColumnSupplier);

  /**
   * @see #addContentColumnSupplier(Supplier)
   */
  ITableCompactHandler withContentColumnSuppliers(List<Supplier<IColumn<?>>> contentColumnSuppliers);

  CompactLineBuilder getDefaultContentLineBuilder();

  ITableCompactHandler withDefaultContentLineBuilder(CompactLineBuilder compactLineBuilder);

  /**
   * Sets a filter to accept columns for compact value generation. To exclude a column, the predicate needs to return
   * false. <br>
   * If no filter is set, all columns are accepted. By default, no filter is set.
   */
  ITableCompactHandler withColumnFilter(Predicate<IColumn<?>> filter);

  /**
   * Compared to {@link #withColumnFilter(Predicate)}, the existing filter, if there is one, won't be replaced but
   * concatenated using {@link Predicate#and(Predicate)}.
   */
  ITableCompactHandler addColumnFilter(Predicate<IColumn<?>> filter);

  Predicate<IColumn<?>> getColumnFilter();

  Consumer<CompactLine> getLineCustomizer();

  /**
   * The customizer makes it possible to adjust every created compact line. There is no customizer added by default.
   */
  ITableCompactHandler withLineCustomizer(Consumer<CompactLine> customizer);
}
