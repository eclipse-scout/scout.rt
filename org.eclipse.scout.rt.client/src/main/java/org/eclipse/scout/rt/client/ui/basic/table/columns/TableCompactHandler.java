/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.eclipse.scout.rt.platform.html.HTML.*;
import static org.eclipse.scout.rt.platform.util.StringUtility.isNullOrEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.CompactBeanBuilder;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableCompactHandler;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableCompactHandler implements ITableCompactHandler {
  private static final Logger LOG = LoggerFactory.getLogger(TableCompactHandler.class);
  private ITable m_table;
  private TableListener m_tableListener;
  private Map<String, Object> m_oldStates;

  private boolean m_useOnlyVisibleColumns;
  private int m_maxContentLines;
  private boolean m_moreLinkAvailable;

  private CompactBeanBuilder m_beanBuilder;
  private CompactLineBuilder m_titleBuilder;
  private CompactLineBuilder m_subtitleBuilder;
  private CompactLineBuilder m_titleSuffixBuilder;
  private List<CompactLineBuilder> m_contentBuilder;
  private CompactLineBuilder m_defaultContentLineBuilder;
  private Consumer<CompactLine> m_lineCustomizer;
  private Predicate<IColumn<?>> m_columnFilter;

  public TableCompactHandler(ITable table) {
    m_table = table;
    m_oldStates = new HashMap<>();
    withUseOnlyVisibleColumns(true);
    withMaxContentLines(3);
    withMoreLinkAvailable(true);
    m_beanBuilder = new BeanBuilder();
    m_titleBuilder = new TitleBuilder(this::getDefaultTitleColumn);
    m_subtitleBuilder = new SubtitleBuilder(this::getDefaultSubtitleColumn);
    m_titleSuffixBuilder = new TitleSuffixBuilder(null);
    m_defaultContentLineBuilder = new ContentLineBuilder();
    m_contentBuilder = new ArrayList<>();
  }

  public ITable getTable() {
    return m_table;
  }

  @Override
  public boolean isUseOnlyVisibleColumns() {
    return m_useOnlyVisibleColumns;
  }

  @Override
  public TableCompactHandler withUseOnlyVisibleColumns(boolean useOnlyVisibleColumns) {
    m_useOnlyVisibleColumns = useOnlyVisibleColumns;
    return this;
  }

  @Override
  public int getMaxContentLines() {
    return m_maxContentLines;
  }

  @Override
  public TableCompactHandler withMaxContentLines(int maxContentLines) {
    m_maxContentLines = maxContentLines;
    return this;
  }

  @Override
  public boolean isMoreLinkAvailable() {
    return m_moreLinkAvailable;
  }

  @Override
  public TableCompactHandler withMoreLinkAvailable(boolean moreLinkAvailable) {
    m_moreLinkAvailable = moreLinkAvailable;
    return this;
  }

  @Override
  public void handle(boolean compact) {
    if (compact) {
      compactColumns(true);
      attachTableListener();
    }
    else {
      detachTableListener();
      compactColumns(false);
    }
    adjustTable(compact);
    if (compact) {
      updateValues(getTable().getRows());
    }
  }

  protected void adjustTable(boolean compact) {
    if (compact) {
      cacheAndSetProperty(ITable.PROP_HEADER_VISIBLE, () -> getTable().isHeaderVisible(), () -> getTable().setHeaderVisible(false));
      cacheAndSetProperty(ITable.PROP_AUTO_RESIZE_COLUMNS, () -> getTable().isAutoResizeColumns(), () -> getTable().setAutoResizeColumns(true));
    }
    else {
      resetProperty(ITable.PROP_HEADER_VISIBLE, (value) -> getTable().setHeaderVisible(value), Boolean.class);
      resetProperty(ITable.PROP_AUTO_RESIZE_COLUMNS, (value) -> getTable().setAutoResizeColumns(value), Boolean.class);
    }
  }

  protected void cacheAndSetProperty(String propertyName, Supplier getter, Runnable setter) {
    m_oldStates.putIfAbsent(propertyName, getter.get());
    setter.run();
  }

  protected <T> void resetProperty(String propertyName, Consumer<T> setter, Class<T> type) {
    if (m_oldStates.containsKey(propertyName)) {
      setter.accept(type.cast(m_oldStates.get(propertyName)));
      m_oldStates.remove(propertyName);
    }
  }

  protected void compactColumns(boolean compact) {
    for (IColumn<?> column : getTable().getColumnSet().getDisplayableColumns()) {
      column.setCompacted(compact);
    }
    TableEvent e = new TableEvent(m_table, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
    if (m_table instanceof AbstractTable) {
      // Necessary to inform the ui if compact state changes while table is already displayed
      ((AbstractTable) m_table).fireTableEventInternal(e);
    }
  }

  private void attachTableListener() {
    if (m_tableListener == null) {
      m_tableListener = new P_TableListener();
      getTable().addTableListener(m_tableListener, TableEvent.TYPE_ROWS_INSERTED, TableEvent.TYPE_ROWS_UPDATED, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED, TableEvent.TYPE_COLUMN_HEADERS_UPDATED);
    }
  }

  private void detachTableListener() {
    if (m_tableListener != null) {
      getTable().removeTableListener(m_tableListener, TableEvent.TYPE_ROWS_INSERTED, TableEvent.TYPE_ROWS_UPDATED, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED, TableEvent.TYPE_COLUMN_HEADERS_UPDATED);
      m_tableListener = null;
    }
  }

  public void updateValues(List<ITableRow> rows) {
    if (rows.size() == 0) {
      return;
    }
    List<IColumn<?>> columns = getColumns();
    rows.forEach(row -> updateValue(columns, row));
  }

  protected void updateValue(List<IColumn<?>> columns, ITableRow row) {
    row.setCompactValue(buildValue(columns, row));
  }

  @Override
  public String buildValue(ITableRow row) {
    return buildValue(getColumns(), row);
  }

  @Override
  public CompactBean buildBean(ITableRow row) {
    return buildBean(getColumns(), row);
  }

  protected String buildValue(List<IColumn<?>> columns, ITableRow row) {
    return buildValue(buildBean(columns, row));
  }

  protected CompactBean buildBean(List<IColumn<?>> columns, ITableRow row) {
    CompactBean bean = m_beanBuilder.apply(columns, row);
    postProcessBean(bean);
    return bean;
  }

  protected void processColumns(List<IColumn<?>> columns, ITableRow row, CompactBean bean) {
    for (IColumn<?> column : columns) {
      processColumn(column, row, bean);
    }
  }

  /**
   * @return the columns based on {@link #isUseOnlyVisibleColumns()} sorted by the provided title and content columns
   * (title columns come first, then content columns, then the remaining columns)
   */
  @Override
  public List<IColumn<?>> getColumns() {
    m_beanBuilder.prepare();
    for (CompactLineBuilder builder : getBuilders()) {
      builder.prepare();
    }

    List<IColumn<?>> columns = getTable().getColumns().stream()
        .filter(this::acceptColumn)
        .collect(Collectors.toList());

    // Provided title columns
    List<IColumn<?>> titleColumns = columns.stream()
        .filter(column -> getTitleBuilder().accept(column) || getTitleSuffixBuilder().accept(column) || getSubtitleBuilder().accept(column))
        .collect(Collectors.toList());

    // Provided content columns
    List<IColumn<?>> contentBuilderColumns = getLinkedAndAcceptedCompactBuilderColumns(getContentBuilders());

    // Concat title and content columns
    List<IColumn<?>> providedColumns = Stream.concat(
        titleColumns.stream(),
        contentBuilderColumns.stream()).collect(Collectors.toList());

    // Add remaining columns after title and content columns
    columns.removeAll(providedColumns);
    providedColumns.addAll(columns);

    return providedColumns;
  }

  /**
   * @return those columns that are linked with a builder and accepted by the builder
   */
  protected List<IColumn<?>> getLinkedAndAcceptedCompactBuilderColumns(List<CompactLineBuilder> builders) {
    return builders.stream()
        .map(builder -> {
          IColumn<?> column = builder.getColumn();
          if (column != null && builder.accept(column)) {
            return column;
          }
          return null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public CompactBeanBuilder getBeanBuilder() {
    return m_beanBuilder;
  }

  @Override
  public TableCompactHandler withBeanBuilder(CompactBeanBuilder builder) {
    m_beanBuilder = builder;
    return this;
  }

  @Override
  public CompactLineBuilder getTitleBuilder() {
    return m_titleBuilder;
  }

  @Override
  public TableCompactHandler withTitleBuilder(CompactLineBuilder builder) {
    m_titleBuilder = builder;
    return this;
  }

  @Override
  public TableCompactHandler withTitleColumnSupplier(Supplier<IColumn<?>> titleColumnSupplier) {
    return withTitleBuilder(new TitleBuilder(titleColumnSupplier));
  }

  @Override
  public CompactLineBuilder getTitleSuffixBuilder() {
    return m_titleSuffixBuilder;
  }

  @Override
  public TableCompactHandler withTitleSuffixBuilder(CompactLineBuilder builder) {
    m_titleSuffixBuilder = builder;
    return this;
  }

  @Override
  public TableCompactHandler withTitleSuffixColumnSupplier(Supplier<IColumn<?>> titleSuffixColumnSupplier) {
    return withTitleSuffixBuilder(new TitleSuffixBuilder(titleSuffixColumnSupplier));
  }

  @Override
  public CompactLineBuilder getSubtitleBuilder() {
    return m_subtitleBuilder;
  }

  @Override
  public TableCompactHandler withSubtitleBuilder(CompactLineBuilder builder) {
    m_subtitleBuilder = builder;
    return this;
  }

  @Override
  public TableCompactHandler withSubtitleColumnSupplier(Supplier<IColumn<?>> subtitleColumnSupplier) {
    return withSubtitleBuilder(new SubtitleBuilder(subtitleColumnSupplier));
  }

  @Override
  public List<CompactLineBuilder> getContentBuilders() {
    return m_contentBuilder;
  }

  @Override
  public TableCompactHandler addContentLineBuilder(CompactLineBuilder builder) {
    m_contentBuilder.add(builder);
    return this;
  }

  @Override
  public TableCompactHandler withContentColumnSuppliers(List<Supplier<IColumn<?>>> contentColumnSuppliers) {
    for (Supplier<IColumn<?>> supplier : contentColumnSuppliers) {
      addContentLineBuilder(new CompactLineBuilder(supplier));
    }
    return this;
  }

  @Override
  public TableCompactHandler addContentColumnSupplier(Supplier<IColumn<?>> contentColumnSupplier) {
    return addContentLineBuilder(new CompactLineBuilder(contentColumnSupplier));
  }

  /**
   * The default content line builder is used when no other content builder accepts the column.
   */
  @Override
  public CompactLineBuilder getDefaultContentLineBuilder() {
    return m_defaultContentLineBuilder;
  }

  @Override
  public TableCompactHandler withDefaultContentLineBuilder(CompactLineBuilder compactLineBuilder) {
    m_defaultContentLineBuilder = compactLineBuilder;
    return this;
  }

  @Override
  public TableCompactHandler withColumnFilter(Predicate<IColumn<?>> filter) {
    m_columnFilter = filter;
    return this;
  }

  @Override
  public TableCompactHandler addColumnFilter(Predicate<IColumn<?>> filter) {
    if (m_columnFilter != null) {
      filter = m_columnFilter.and(filter);
    }
    return withColumnFilter(filter);
  }

  @Override
  public Predicate<IColumn<?>> getColumnFilter() {
    return m_columnFilter;
  }

  protected List<CompactLineBuilder> getBuilders() {
    ArrayList<CompactLineBuilder> builders = CollectionUtility.arrayList(getTitleBuilder(), getTitleSuffixBuilder(), getSubtitleBuilder());
    builders.addAll(getContentBuilders());
    return builders;
  }

  /**
   * Is called for each supplied column.
   */
  protected boolean acceptColumn(IColumn<?> column) {
    if (m_columnFilter != null && !m_columnFilter.test(column)) {
      return false;
    }
    return !isUseOnlyVisibleColumns() || column.isVisible();
  }

  protected boolean acceptedByBuilder(IColumn<?> column) {
    return getBuilders().stream().anyMatch(builder -> builder.accept(column));
  }

  protected void processColumn(IColumn<?> column, ITableRow row, CompactBean bean) {
    updateBean(bean, column, row);
  }

  /**
   * @param bean
   *     the bean for the current row
   * @param column
   *     the currently processed column
   * @param row
   *     the current row
   */
  protected void updateBean(CompactBean bean, IColumn<?> column, ITableRow row) {
    if (getTitleBuilder().accept(column)) {
      bean.setTitleLine(buildCompactLine(getTitleBuilder(), column, row));
    }
    else if (getTitleSuffixBuilder().accept(column)) {
      bean.setTitleSuffixLine(buildCompactLine(getTitleSuffixBuilder(), column, row));
    }
    else if (getSubtitleBuilder().accept(column)) {
      bean.setSubtitleLine(buildCompactLine(getSubtitleBuilder(), column, row));
    }
    else {
      for (CompactLineBuilder contentBuilder : getContentBuilders()) {
        if (contentBuilder.accept(column)) {
          bean.addContentLine(buildCompactLine(contentBuilder, column, row));
          return;
        }
      }
      if (getDefaultContentLineBuilder().accept(column)) {
        // If no provided content line builder accepts the column, pass it to the default content builder to add the remaining columns
        bean.addContentLine(buildCompactLine(getDefaultContentLineBuilder(), column, row));
      }
    }
  }

  protected CompactLine buildCompactLine(CompactLineBuilder builder, IColumn<?> column, ITableRow row) {
    CompactLine line = builder.build(column, row);
    adaptCompactLine(line, column, row);
    return line;
  }

  protected void adaptCompactLine(CompactLine line, IColumn<?> column, ITableRow row) {
    if (getLineCustomizer() != null) {
      getLineCustomizer().accept(line);
    }
  }

  @Override
  public Consumer<CompactLine> getLineCustomizer() {
    return m_lineCustomizer;
  }

  @Override
  public TableCompactHandler withLineCustomizer(Consumer<CompactLine> customizer) {
    m_lineCustomizer = customizer;
    return this;
  }

  protected void postProcessBean(CompactBean bean) {
    bean.transform(true, getMaxContentLines(), isMoreLinkAvailable());

    // If only title is set move it to content. A title without content does not look good.
    if (!isNullOrEmpty(bean.getTitle()) && isNullOrEmpty(bean.getSubtitle()) && isNullOrEmpty(bean.getTitleSuffix()) && isNullOrEmpty(bean.getContent())) {
      bean.setContent(bean.getTitle());
      bean.setTitle("");
    }
  }

  @Override
  public String buildValue(CompactBean bean) {
    String hasHeader = StringUtility.hasText(bean.getTitle() + bean.getTitleSuffix() + bean.getSubtitle()) ? "has-header" : "";
    IHtmlElement moreLink = isMoreLinkAvailable() && StringUtility.hasText(bean.getMoreContent()) ? div(span(TEXTS.get("More")).addCssClass("more-link link")).cssClass("compact-cell-more") : null;
    String value = HTML.fragment(
        div(
            div(
                span(raw(bean.getTitle())).cssClass("left"),
                span(raw(bean.getTitleSuffix())).cssClass("right")).cssClass("compact-cell-title"),
            div(raw(bean.getSubtitle())).cssClass("compact-cell-subtitle")).cssClass("compact-cell-header"),
        div(raw(bean.getContent())).cssClass("compact-cell-content " + hasHeader),
        div(raw(bean.getMoreContent())).cssClass("compact-cell-more-content hidden " + hasHeader),
        moreLink).toHtml();
    return value;
  }

  protected IColumn<?> getDefaultTitleColumn() {
    return getColumnAt(0);
  }

  protected IColumn<?> getDefaultSubtitleColumn() {
    return getColumnAt(1);
  }

  protected IColumn<?> getColumnAt(int index) {
    List<IColumn<?>> columns = getTable().getColumnSet().getColumns();
    // Stream columns rather than using columnSet.getVisibleColumn(index) because we want the original order (defined visible) and not the order configured by the user
    columns = columns.stream().filter(this::acceptColumn).collect(Collectors.toList());
    if (columns.size() < index + 1) {
      return null;
    }
    return columns.get(index);
  }

  protected class P_TableListener extends TableAdapter {
    @Override
    public void tableChanged(TableEvent e) {
      List<ITableRow> rows = e.getRows();
      if (ObjectUtility.isOneOf(e.getType(), TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED, TableEvent.TYPE_COLUMN_HEADERS_UPDATED)) {
        rows = getTable().getRows();
      }
      long time = System.currentTimeMillis();
      updateValues(rows);
      LOG.debug("Event {} handled. Updating values took {}ms for {} rows.", e.getType(), System.currentTimeMillis() - time, rows.size());
    }
  }

  public static class TitleBuilder extends CompactLineBuilder {

    public TitleBuilder(Supplier<IColumn<?>> columnSupplier) {
      super(columnSupplier);
      withShowLabel(false);
    }
  }

  public static class SubtitleBuilder extends CompactLineBuilder {

    public SubtitleBuilder(Supplier<IColumn<?>> columnSupplier) {
      super(columnSupplier);
      withShowLabel(false);
    }
  }

  public static class TitleSuffixBuilder extends CompactLineBuilder {

    public TitleSuffixBuilder(Supplier<IColumn<?>> columnSupplier) {
      super(columnSupplier);
      withShowLabel(false);
    }
  }

  public static class ContentLineBuilder extends CompactLineBuilder {
    @Override
    public boolean accept(IColumn<?> column) {
      return true; // Accept any column
    }
  }

  protected class BeanBuilder implements CompactBeanBuilder {

    @Override
    public CompactBean apply(List<IColumn<?>> columns, ITableRow row) {
      CompactBean bean = new CompactBean();
      processColumns(columns, row, bean);
      return bean;
    }
  }
}
