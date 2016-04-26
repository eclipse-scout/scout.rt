/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.bookmark.internal;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.CRC32;

import org.eclipse.scout.rt.client.services.common.bookmark.DefaultBookmarkAdapter;
import org.eclipse.scout.rt.client.services.common.bookmark.IBookmarkAdapter;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.bookmark.AbstractPageState;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.bookmark.NodePageState;
import org.eclipse.scout.rt.shared.services.common.bookmark.TableColumnState;
import org.eclipse.scout.rt.shared.services.common.bookmark.TablePageState;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BookmarkUtility {

  private static final Logger LOG = LoggerFactory.getLogger(BookmarkUtility.class);

  private BookmarkUtility() {
  }

  public static IOutline resolveOutline(List<? extends IOutline> outlines, String className) {
    if (className == null) {
      return null;
    }
    // pass 1: fully qualified name
    for (IOutline o : outlines) {
      if (o.getClass().getName().equals(className)) {
        return o;
      }
    }
    // pass 2: simple name, not case sensitive
    String simpleClassName = className.replaceAll("^.*\\.", "");
    for (IOutline o : outlines) {
      if (o.getClass().getSimpleName().equalsIgnoreCase(simpleClassName)) {
        return o;
      }
    }
    return null;
  }

  /**
   * @param columns
   *          is the set of available columns to search in
   * @param identifier
   *          is the columnId, simple class name or class name of the columns to find
   */
  public static IColumn resolveColumn(List<? extends IColumn> columns, String identifier) {
    if (identifier == null) {
      return null;
    }
    // pass 1: fully qualified name
    for (IColumn c : columns) {
      if (identifier.equals(c.getClass().getName())) {
        return c;
      }
    }
    // pass 2: columnId
    for (IColumn c : columns) {
      if (identifier.equals(c.getColumnId())) {
        return c;
      }
    }
    // pass 3: simple name, not case sensitive
    String simpleClassName = identifier.replaceAll("^.*\\.", "");
    for (IColumn c : columns) {
      if (simpleClassName.equalsIgnoreCase(c.getClass().getSimpleName())) {
        return c;
      }
    }
    return null;
  }

  public static IPage<?> resolvePage(List<? extends IPage> pages, String className, String bookmarkIdentifier) {
    if (className == null) {
      return null;
    }
    TreeMap<CompositeObject, IPage> sortMap = new TreeMap<CompositeObject, IPage>();
    String simpleClassName = className.replaceAll("^.*\\.", "");
    int index = 0;
    for (IPage<?> p : pages) {
      int classNameScore = 0;
      int userPreferenceContextScore = 0;
      if (p.getClass().getName().equals(className)) {
        classNameScore = -2;
      }
      else if (p.getClass().getSimpleName().equalsIgnoreCase(simpleClassName)) {
        classNameScore = -1;
      }
      IBookmarkAdapter bookmarkAdapter = getBookmarkAdapter(p);
      if (bookmarkIdentifier == null || bookmarkIdentifier.equalsIgnoreCase(bookmarkAdapter.getIdentifier())) {
        userPreferenceContextScore = -1;
      }
      if (classNameScore != 0 && userPreferenceContextScore != 0) {
        sortMap.put(new CompositeObject(classNameScore, userPreferenceContextScore, index), p);
      }
      index++;
    }
    if (sortMap.isEmpty()) {
      return null;
    }
    CompositeObject bestMatchingKey = sortMap.firstKey();
    IPage<?> bestMatchingPage = sortMap.remove(bestMatchingKey);
    if (!sortMap.isEmpty()) {
      // check ambiguity
      CompositeObject nextKey = sortMap.firstKey();
      if (CompareUtility.equals(bestMatchingKey.getComponent(0), nextKey.getComponent(0)) && CompareUtility.equals(bestMatchingKey.getComponent(1), nextKey.getComponent(1))) {
        LOG.warn("More than one pages found for page class [{}] and bookmark Identifier [{}]", className, bookmarkIdentifier);
      }
    }
    return bestMatchingPage;
  }

  /**
   * intercept objects that are not remoting-capable or not serializable and replace by strings
   */
  @SuppressWarnings("unchecked")
  public static List<Object> makeSerializableKeys(List<?> a, boolean useLegacySupport) {
    return (List<Object>) makeSerializableKey(a, useLegacySupport);
  }

  public static Object makeSerializableKey(Object o, boolean useLegacySupport) {
    if (o == null) {
      return o;
    }
    else if (o instanceof Number) {
      return o;
    }
    else if (o instanceof String) {
      return o;
    }
    else if (o instanceof Boolean) {
      return o;
    }
    else if (o instanceof Date) {
      return o;
    }
    else if (o instanceof Collection) {
      List<Object> result = new ArrayList<Object>();
      for (Object oi : (Collection) o) {
        result.add(makeSerializableKey(oi, useLegacySupport));
      }
      return result;
    }
    else if (o.getClass().isArray()) {
      ArrayList<Integer> dimList = new ArrayList<Integer>();
      Class xc = o.getClass();
      Object xo = o;
      while (xc.isArray()) {
        int len = xo != null ? Array.getLength(xo) : 0;
        dimList.add(len);
        xc = xc.getComponentType();
        if (xo != null && len > 0) {
          xo = Array.get(xo, 0);
        }
      }
      int[] dim = new int[dimList.size()];
      for (int i = 0; i < dim.length; i++) {
        dim[i] = dimList.get(i);
      }
      Object b = Array.newInstance(makeSerializableClass(xc, useLegacySupport), dim);
      for (int i = 0; i < dim[0]; i++) {
        Array.set(b, i, makeSerializableKey(Array.get(o, i), useLegacySupport));
      }
      return b;
    }
    else if (!useLegacySupport && o instanceof Serializable) {
      return o;
    }
    else {
      // check if key object overrides toString()
      if (!useLegacySupport && !(o instanceof String)) {
        if (ConfigurationUtility.isMethodOverwrite(Object.class, "toString", new Class[0], o.getClass())) {
          LOG.warn("Bookmark key is not serializable. Falling back to toString(). Note: keys may not be stable [class={}, string representation:{}]", o.getClass(), o);
        }
        else {
          LOG.error("Bookmark key is not serializable. Falling back to toString() which is not overriden by the given class [{}]", o.getClass());
        }
      }
      return o.toString();
    }
  }

  /**
   * return String.class for classes that are not remoting-capable or not serializable
   */
  public static Class makeSerializableClass(Class c, boolean useLegacySupport) {
    if (c == null) {
      throw new IllegalArgumentException("class must not be null");
    }
    if (c.isArray()) {
      throw new IllegalArgumentException("class must not be an array class");
    }
    if (c.isPrimitive()) {
      return c;
    }
    else if (Number.class.isAssignableFrom(c)) {
      return c;
    }
    else if (String.class.isAssignableFrom(c)) {
      return c;
    }
    else if (Boolean.class.isAssignableFrom(c)) {
      return c;
    }
    else if (Date.class.isAssignableFrom(c)) {
      return c;
    }
    else if (Object.class == c) {
      return c;
    }
    else if (!useLegacySupport && Serializable.class.isAssignableFrom(c)) {
      return c;
    }
    else {
      return String.class;
    }
  }

  /**
   * Load a {@link Bookmark} on the specified {@link IDesktop} model.
   * <p>
   * First the specific {@link Bookmark#getOutlineClassName()} is evaluated and activated, afterwards every page from
   * the {@link Bookmark#getPath()} will be selected (respecting the {@link AbstractPageState}).
   * </p>
   * Finally the path will be expanded. Possible exceptions might occur if no outline is set in the {@link Bookmark} or
   * the outline is not available.
   */
  public static void activateBookmark(IDesktop desktop, Bookmark bm) {
    activateBookmark(desktop, bm, true);
  }

  /**
   * Load a {@link Bookmark} on the specified {@link IDesktop} model.
   * <p>
   * First the specific {@link Bookmark#getOutlineClassName()} is evaluated and, if activateOutline is true, activated.
   * Afterwards every page from the {@link Bookmark#getPath()} will be selected (respecting the
   * {@link AbstractPageState}).
   * </p>
   * Finally the path will be expanded. Possible exceptions might occur if no outline is set in the {@link Bookmark} or
   * the outline is not available.
   */
  public static void activateBookmark(IDesktop desktop, Bookmark bm, boolean activateOutline) {
    if (bm.getOutlineClassName() == null) {
      return;
    }
    IOutline outline = BookmarkUtility.resolveOutline(desktop.getAvailableOutlines(), bm.getOutlineClassName());
    if (outline == null) {
      throw new ProcessingException("outline '" + bm.getOutlineClassName() + "' was not found");
    }
    if (!(outline.isVisible() && outline.isEnabled())) {
      throw new VetoException(TEXTS.get("BookmarkActivationFailedOutlineNotAvailable", outline.getTitle()));
    }
    if (activateOutline) {
      desktop.activateOutline(outline);
    }
    try {
      outline.setTreeChanging(true);
      //
      IPage<?> parentPage = outline.getRootPage();
      boolean pathFullyRestored = true;
      List<AbstractPageState> path = bm.getPath();
      AbstractPageState parentPageState = path.get(0);
      boolean resetViewAndWarnOnFail = bm.getId() != 0;
      for (int i = 1; i < path.size(); i++) {
        // try to find correct child page (next parentPage)
        IPage<?> childPage = null;
        AbstractPageState childState = path.get(i);
        if (parentPageState instanceof TablePageState) {
          TablePageState tablePageState = (TablePageState) parentPageState;
          if (parentPage instanceof IPageWithTable) {
            IPageWithTable tablePage = (IPageWithTable) parentPage;
            childPage = bmLoadTablePage(tablePage, tablePageState, false, resetViewAndWarnOnFail);
          }
        }
        else if (parentPageState instanceof NodePageState) {
          NodePageState nodePageState = (NodePageState) parentPageState;
          if (parentPage instanceof IPageWithNodes) {
            IPageWithNodes nodePage = (IPageWithNodes) parentPage;
            childPage = bmLoadNodePage(nodePage, nodePageState, childState, resetViewAndWarnOnFail);
          }
        }
        // next
        if (childPage != null) {
          parentPage = childPage;
          parentPageState = childState;
        }
        else if (i < path.size()) {
          pathFullyRestored = false;
          break;
        }
      }
      if (pathFullyRestored) {
        if (parentPageState instanceof TablePageState && parentPage instanceof IPageWithTable) {
          bmLoadTablePage((IPageWithTable) parentPage, (TablePageState) parentPageState, true, resetViewAndWarnOnFail);
        }
        else if (parentPage instanceof IPageWithNodes) {
          bmLoadNodePage((IPageWithNodes) parentPage, (NodePageState) parentPageState, null, resetViewAndWarnOnFail);
        }
      }
      /*
       * Expansions of the restored tree path
       */
      IPage<?> p = parentPage;
      // last element
      if (pathFullyRestored && parentPageState.isExpanded() != null) {
        p.setExpanded(parentPageState.isExpanded());
      }
      else {
        if (!(p instanceof IPageWithTable)) {
          p.setExpanded(true);
        }
      }
      // ancestor elements
      p = p.getParentPage();
      while (p != null) {
        p.setExpanded(true);
        p = p.getParentPage();
      }
      outline.selectNode(parentPage, false);
    }
    finally {
      outline.setTreeChanging(false);
    }
  }

  /**
   * Constructs a list of {@link TableColumnState} objects which describe the set of columns of the given {@link ITable}
   * .
   *
   * @param table
   *          The table with the columns to back-up.
   * @return A {@link List} of {@link TableColumnState} objects that can be restored via
   *         {@link #restoreTableColumns(ITable, List)}
   */
  public static List<TableColumnState> backupTableColumns(ITable table) {
    ArrayList<TableColumnState> allColumns = new ArrayList<TableColumnState>();
    ColumnSet columnSet = table.getColumnSet();
    //add all columns but in user order
    for (IColumn<?> c : columnSet.getAllColumnsInUserOrder()) {
      TableColumnState colState = new TableColumnState();
      colState.setColumnClassName(c.getColumnId());
      colState.setDisplayable(c.isDisplayable());
      colState.setVisible(c.isDisplayable() && c.isVisible());
      colState.setWidth(c.getWidth());

      if (c instanceof INumberColumn) {
        colState.setAggregationFunction(((INumberColumn) c).getAggregationFunction());
        colState.setBackgroundEffect(((INumberColumn) c).getBackgroundEffect());
      }

      if (columnSet.isUserSortColumn(c)) {
        int sortOrder = columnSet.getSortColumnIndex(c);
        if (sortOrder >= 0) {
          colState.setSortOrder(sortOrder);
          colState.setSortAscending(c.isSortAscending());
          colState.setGroupingActive(c.isGroupingActive());
        }
        else {
          colState.setSortOrder(-1);
        }
      }
      allColumns.add(colState);
    }
    return allColumns;
  }

  /**
   * Restores a tables columns from the given list of {@link TableColumnState} objects.
   *
   * @param table
   *          The table to be restored.
   * @param oldColumns
   *          A {@link List} of {@link TableColumnState} objects to restore. Such can be retrieved by the
   *          {@link #backupTableColumns(ITable)} method.
   */
  public static void restoreTableColumns(ITable table, List<TableColumnState> oldColumns) {
    if (oldColumns != null && oldColumns.size() > 0 && table != null) {
      ColumnSet columnSet = table.getColumnSet();
      // visible columns and width
      ArrayList<IColumn> visibleColumns = new ArrayList<IColumn>();
      for (TableColumnState colState : oldColumns) {
        //legacy support: null=true
        if (colState.getVisible() == null || colState.getVisible()) {
          IColumn col = resolveColumn(columnSet.getDisplayableColumns(), colState.getClassName());
          if (col != null && col.isDisplayable()) {
            if (colState.getWidth() > 0) {
              col.setWidth(colState.getWidth());
            }
            visibleColumns.add(col);
          }
        }
      }
      List<IColumn<?>> existingVisibleCols = columnSet.getVisibleColumns();
      if (!existingVisibleCols.equals(visibleColumns)) {
        columnSet.setVisibleColumns(visibleColumns);
      }

      //aggregation functions and background effect:
      for (TableColumnState colState : oldColumns) {

        IColumn<?> col = BookmarkUtility.resolveColumn(columnSet.getColumns(), colState.getClassName());
        if (col instanceof INumberColumn) {
          if (colState.getAggregationFunction() != null) {
            ((INumberColumn<?>) col).setAggregationFunction(colState.getAggregationFunction());
          }
          ((INumberColumn<?>) col).setBackgroundEffect(colState.getBackgroundEffect());
        }
      }

      //sort order (only respect visible and user-sort columns)
      boolean userSortValid = true;
      TreeMap<Integer, IColumn> sortColMap = new TreeMap<Integer, IColumn>();
      HashMap<IColumn<?>, TableColumnState> sortColToColumnState = new HashMap<>();
      HashSet<IColumn<?>> groupedCols = new HashSet<>();
      for (TableColumnState colState : oldColumns) {
        if (colState.getSortOrder() >= 0) {
          IColumn col = BookmarkUtility.resolveColumn(columnSet.getColumns(), colState.getClassName());
          if (col != null) {
            sortColMap.put(colState.getSortOrder(), col);
            sortColToColumnState.put(col, colState);
            if (colState.isGroupingActive()) {
              groupedCols.add(col);
            }
            if (col.getSortIndex() != colState.getSortOrder()) {
              userSortValid = false;
            }
            if (col.isSortAscending() != colState.isSortAscending()) {
              userSortValid = false;
            }
            if (col.isGroupingActive() != colState.isGroupingActive()) {
              userSortValid = false;
            }
          }
        }
      }
      if (!sortColMap.values().containsAll(columnSet.getUserSortColumns())) {
        userSortValid = false;
      }

      if (userSortValid) {
        HashSet<IColumn<?>> existingGroupedUserSortCols = new HashSet<>();
        //check if grouping is valid also:
        for (IColumn<?> c : columnSet.getUserSortColumns()) {
          if (c.isGroupingActive()) {
            existingGroupedUserSortCols.add(c);
          }
        }
        if (!groupedCols.containsAll(existingGroupedUserSortCols)) {
          userSortValid = false;
        }
      }

      if (!userSortValid) {
        columnSet.clearSortColumns();
        boolean groupingPossible = true;
        for (IColumn<?> headSortColumn : columnSet.getPermanentHeadSortColumns()) {
          if (!headSortColumn.isVisible() || !headSortColumn.isGroupingActive()) {
            TableColumnState state = sortColToColumnState.get(headSortColumn);
            if (state != null) {
              if (!state.isGroupingActive()) {
                groupingPossible = false;
                break;
              }
            }
          }
        }
        for (IColumn<?> col : sortColMap.values()) {
          TableColumnState state = sortColToColumnState.get(col);
          if (groupingPossible) {
            if (state.isGroupingActive()) {
              columnSet.addGroupingColumn(col, state.isSortAscending());
            }
            else {
              columnSet.addSortColumn(col, state.isSortAscending());
              groupingPossible = false;
            }
          }
          else {
            columnSet.addSortColumn(col, state.isSortAscending());
          }
        }
        table.sort();
      }
      ClientUIPreferences.getInstance().setAllTableColumnPreferences(table);
    }
  }

  public static IBookmarkAdapter getBookmarkAdapter(IPage<?> page) {
    IBookmarkAdapter bookmarkAdapter = page.getAdapter(IBookmarkAdapter.class);
    if (bookmarkAdapter != null) {
      return bookmarkAdapter;
    }
    return new DefaultBookmarkAdapter(page);
  }

  public static Bookmark createBookmark(IDesktop desktop) {
    IOutline outline = desktop.getOutline();
    if (outline == null) {
      return null;
    }

    IPage<?> activePage = outline.getActivePage();
    return createBookmark(activePage);
  }

  public static Bookmark createBookmark(IPage<?> page) {
    if (page == null || page.getOutline() == null) {
      return null;
    }
    IBookmarkAdapter bookmarkAdapter = getBookmarkAdapter(page);

    IOutline outline = page.getOutline();
    Bookmark b = new Bookmark();
    b.setIconId(bookmarkAdapter.getIconId());
    // outline
    b.setOutlineClassName(bookmarkAdapter.getOutlineClassName());
    ArrayList<IPage<?>> path = new ArrayList<IPage<?>>();
    ArrayList<String> titleSegments = new ArrayList<String>();
    while (page != null) {
      IBookmarkAdapter currentBookmarkAdapter = getBookmarkAdapter(page);
      path.add(0, page);
      String s = currentBookmarkAdapter.getTitle();
      if (s != null) {
        titleSegments.add(0, s);
      }
      // next
      page = (IPage) page.getParentNode();
    }
    if (bookmarkAdapter.getOutlineTitle() != null) {
      titleSegments.add(0, bookmarkAdapter.getOutlineTitle());
    }
    // title
    int len = 0;
    if (titleSegments.size() > 0) {
      len += titleSegments.get(0).length();
    }
    if (titleSegments.size() > 1) {
      len += titleSegments.get(titleSegments.size() - 1).length();
    }
    for (int i = titleSegments.size() - 1; i > 0; i--) {
      if (len > 200) {
        titleSegments.remove(i);
      }
      else if (len + titleSegments.get(i).length() <= 200) {
        len += titleSegments.get(i).length();
      }
      else {
        titleSegments.set(i, "...");
        len = 201;
      }
    }
    StringBuilder buf = new StringBuilder();
    for (String s : titleSegments) {
      if (buf.length() > 0) {
        buf.append(" - ");
      }
      buf.append(s);
    }
    b.setTitle(buf.toString());
    // text
    StringBuilder text = new StringBuilder();
    // add constraints texts
    String prefix = "";
    for (int i = 0; i < path.size(); i++) {
      page = path.get(i);
      IBookmarkAdapter currentBookmarkAdapter = getBookmarkAdapter(page);
      if (i > 0 || outline.isRootNodeVisible()) {
        text.append(prefix + currentBookmarkAdapter.getText());
        text.append("\n");
        if (page instanceof IPageWithTable) {
          IPageWithTable tablePage = (IPageWithTable) page;
          SearchFilter search = tablePage.getSearchFilter();
          if (search != null) {
            for (String s : search.getDisplayTexts()) {
              if (s != null) {
                String indent = prefix + "  ";
                s = s.trim().replaceAll("[\\n]", "\n" + indent);
                if (s.length() > 0) {
                  text.append(indent + s);
                  text.append("\n");
                }
              }
            }
          }
        }
        prefix += "  ";
      }
    }
    b.setText(text.toString().trim());
    // path
    for (int i = 0; i < path.size(); i++) {
      page = path.get(i);
      if (page instanceof IPageWithTable) {
        IPageWithTable tablePage = (IPageWithTable) page;
        IPage<?> childPage = null;
        if (i + 1 < path.size()) {
          childPage = path.get(i + 1);
        }
        b.addPathElement(bmStoreTablePage(tablePage, childPage));
      }
      else if (page instanceof IPageWithNodes) {
        IPageWithNodes nodePage = (IPageWithNodes) page;
        b.addPathElement(bmStoreNodePage(nodePage));
      }
    }
    return b;
  }

  private static IPage<?> bmLoadTablePage(IPageWithTable tablePage, TablePageState tablePageState, boolean leafState, boolean resetViewAndWarnOnFail) {
    ITable table = tablePage.getTable();
    if (tablePageState.getTableCustomizerData() != null && tablePage.getTable().getTableCustomizer() != null) {
      byte[] newData = tablePageState.getTableCustomizerData();
      ITableCustomizer tc = tablePage.getTable().getTableCustomizer();
      byte[] curData = tc.getSerializedData();
      if (!CompareUtility.equals(curData, newData)) {
        tc.removeAllColumns();
        tc.setSerializedData(newData);
        table.resetColumnConfiguration();
        tablePage.setChildrenLoaded(false);
      }
    }
    // starts search form
    tablePage.getSearchFilter();
    // setup table
    try {
      table.setTableChanging(true);
      restoreTableColumns(tablePage.getTable(), tablePageState.getAvailableColumns());
    }
    finally {
      table.setTableChanging(false);
    }
    // setup user filter
    if (tablePageState.getUserFilterData() != null && tablePage.getTable().getUserFilterManager() != null) {
      byte[] newData = tablePageState.getUserFilterData();
      TableUserFilterManager ufm = tablePage.getTable().getUserFilterManager();
      byte[] curData = ufm.getSerializedData();
      if (!CompareUtility.equals(curData, newData)) {
        try {
          ufm.setSerializedData(newData);
        }
        catch (RuntimeException e) {
          LOG.error("User filters could not be restored. ", e);
        }
      }
    }
    // setup search
    if (tablePageState.getSearchFormState() != null) {
      ISearchForm searchForm = tablePage.getSearchFormInternal();
      if (searchForm != null) {
        boolean doSearch = true;
        String newSearchFilterState = tablePageState.getSearchFilterState();
        String oldSearchFilterState = "" + createSearchFilterCRC(searchForm.getSearchFilter());
        if (CompareUtility.equals(oldSearchFilterState, newSearchFilterState)) {
          String newSearchFormState = tablePageState.getSearchFormState();
          String oldSearchFormState = searchForm.storeToXmlString();
          if (CompareUtility.equals(oldSearchFormState, newSearchFormState)) {
            doSearch = false;
          }
        }
        // in case search form is in correct state, but no search has been executed, force search
        if (tablePage.getTable().getRowCount() == 0) {
          doSearch = true;
        }
        if (doSearch) {
          searchForm.loadFromXmlString(tablePageState.getSearchFormState());
          if (tablePageState.isSearchFilterComplete()) {
            searchForm.doSaveWithoutMarkerChange();
          }
        }
      }
    }
    IPage<?> childPage = null;
    boolean loadChildren = !leafState;
    if (tablePage.isChildrenDirty() || tablePage.isChildrenVolatile()) {
      loadChildren = true;
      tablePage.setChildrenLoaded(false);
    }
    if (loadChildren) {
      tablePage.ensureChildrenLoaded();
      tablePage.setChildrenDirty(false);
      CompositeObject childPk = tablePageState.getExpandedChildPrimaryKey();
      if (childPk != null) {
        for (int r = 0; r < table.getRowCount(); r++) {
          CompositeObject testPkLegacy = new CompositeObject(BookmarkUtility.makeSerializableKeys(table.getRowKeys(r), true));
          CompositeObject testPk = new CompositeObject(BookmarkUtility.makeSerializableKeys(table.getRowKeys(r), false));
          if (testPk.equals(childPk) || testPkLegacy.equals(childPk)) {
            if (r < tablePage.getChildNodeCount()) {
              childPage = tablePage.getChildPage(r);
            }
            break;
          }
        }
      }
      else {
        List<ITreeNode> filteredChildNodes = tablePage.getFilteredChildNodes();
        if (filteredChildNodes.size() > 0) {
          childPage = (IPage) CollectionUtility.firstElement(filteredChildNodes);
        }
        else if (tablePage.getChildNodeCount() > 0) {
          childPage = tablePage.getChildPage(0);
        }
      }
    }
    // load selections
    if (leafState) {
      if (tablePageState.getSelectedChildrenPrimaryKeys().size() > 0) {
        tablePage.ensureChildrenLoaded();
        HashSet<CompositeObject> selectionSet = new HashSet<CompositeObject>(tablePageState.getSelectedChildrenPrimaryKeys());
        ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
        for (ITableRow row : table.getRows()) {
          CompositeObject testPkLegacy = new CompositeObject(BookmarkUtility.makeSerializableKeys(row.getKeyValues(), true));
          CompositeObject testPk = new CompositeObject(BookmarkUtility.makeSerializableKeys(row.getKeyValues(), false));
          if (selectionSet.contains(testPk) || selectionSet.contains(testPkLegacy)) {
            //row must not be filtered out
            if (row.isFilterAccepted()) {
              rowList.add(row);
            }
          }
        }
        if (rowList.size() > 0) {
          table.selectRows(rowList);
        }
      }

      return childPage;
    }

    // check, whether table column filter must be reset
    if (resetViewAndWarnOnFail) {
      if (childPage == null || (!childPage.isFilterAccepted() && table.getUserFilterManager() != null)) {
        table.getUserFilterManager().reset();
        tablePage.setTableStatus(new Status(ScoutTexts.get("BookmarkResetColumnFilters"), IStatus.WARNING));
      }
    }

    // child page is not available or filtered out
    if (childPage == null || !childPage.isFilterAccepted()) {
      if (resetViewAndWarnOnFail) {
        // set appropriate warning
        if (tablePage.isSearchActive() && tablePage.getSearchFormInternal() != null) {
          tablePage.setTableStatus(new Status(ScoutTexts.get("BookmarkResolutionCanceledCheckSearchCriteria"), IStatus.ERROR));
        }
        else {
          tablePage.setTableStatus(new Status(ScoutTexts.get("BookmarkResolutionCanceled"), IStatus.ERROR));
        }
      }
      childPage = null;
    }

    return childPage;
  }

  private static IPage<?> bmLoadNodePage(IPageWithNodes nodePage, NodePageState nodePageState, AbstractPageState childState, boolean resetViewAndWarnOnFail) {
    IPage<?> childPage = null;
    if (childState != null) {
      nodePage.ensureChildrenLoaded();
      IPage<?> p = BookmarkUtility.resolvePage(nodePage.getChildPages(), childState.getPageClassName(), childState.getBookmarkIdentifier());
      if (p != null) {
        ITable table = nodePage.getTable();
        // reset table column filter if requested
        if (resetViewAndWarnOnFail && !p.isFilterAccepted() && table.getUserFilterManager() != null) {
          table.getUserFilterManager().reset();
        }

        // check table column filter
        if (p.isFilterAccepted()) {
          childPage = p;
        }
      }
      // set appropriate warning if child page is not available or filtered out
      if (childPage == null && resetViewAndWarnOnFail) {
        nodePage.setTableStatus(new Status(ScoutTexts.get("BookmarkResolutionCanceled"), IStatus.ERROR));
      }
    }
    return childPage;
  }

  private static TablePageState bmStoreTablePage(IPageWithTable page, IPage<?> childPage) {
    ITable table = page.getTable();
    TablePageState state = new TablePageState();
    state.setPageClassName(page.getClass().getName());
    IBookmarkAdapter bookmarkAdapter = getBookmarkAdapter(page);
    state.setBookmarkIdentifier(bookmarkAdapter.getIdentifier());
    state.setLabel(bookmarkAdapter.getText());
    state.setExpanded(page.isExpanded());
    IForm searchForm = page.getSearchFormInternal();
    if (searchForm != null) {
      state.setSearchFormState(searchForm.storeToXmlString());
      state.setSearchFilterState(searchForm.getSearchFilter().isCompleted(), "" + createSearchFilterCRC(searchForm.getSearchFilter()));
    }
    if (page.getTable().getTableCustomizer() != null) {
      state.setTableCustomizerData(page.getTable().getTableCustomizer().getSerializedData());
    }
    if (page.getTable().getUserFilterManager() != null) {
      state.setUserFilterData(page.getTable().getUserFilterManager().getSerializedData());
    }
    List<TableColumnState> allColumns = backupTableColumns(page.getTable());
    state.setAvailableColumns(allColumns);
    //
    ArrayList<CompositeObject> pkList = new ArrayList<CompositeObject>();
    for (ITableRow row : table.getSelectedRows()) {
      pkList.add(new CompositeObject(BookmarkUtility.makeSerializableKeys(row.getKeyValues(), false)));
    }
    state.setSelectedChildrenPrimaryKeys(pkList);
    //
    if (childPage != null) {
      for (int j = 0; j < table.getRowCount(); j++) {
        if (page.getChildNode(j) == childPage) {
          ITableRow childRow = table.getRow(j);
          state.setExpandedChildPrimaryKey(new CompositeObject(BookmarkUtility.makeSerializableKeys(childRow.getKeyValues(), false)));
          break;
        }
      }
    }
    return state;
  }

  private static NodePageState bmStoreNodePage(IPageWithNodes page) {
    NodePageState state = new NodePageState();
    state.setPageClassName(page.getClass().getName());
    IBookmarkAdapter bookmarkAdapter = getBookmarkAdapter(page);
    state.setBookmarkIdentifier(bookmarkAdapter.getIdentifier());
    state.setLabel(bookmarkAdapter.getText());
    state.setExpanded(page.isExpanded());
    return state;
  }

  private static long createSearchFilterCRC(SearchFilter filter) {
    if (filter == null) {
      return 0L;
    }
    try {
      CRC32 crc = new CRC32();
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      ObjectOutputStream oo = new ObjectOutputStream(bo);
      oo.writeObject(filter);
      oo.close();
      crc.update(bo.toByteArray());
      return crc.getValue();
    }
    catch (Exception t) {
      // nop
      return -1L;
    }
  }

}
