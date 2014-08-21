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
package org.eclipse.scout.rt.client;

import java.util.Date;
import java.util.WeakHashMap;
import java.util.zip.CRC32;

import org.eclipse.scout.commons.beans.FastBeanInfo;
import org.eclipse.scout.commons.beans.FastPropertyDescriptor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.TableColumnFilterEvent;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.TableColumnFilterListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.pagefield.AbstractPageField;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

public class AbstractMemoryPolicy implements IMemoryPolicy {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractMemoryPolicy.class);

  public static class SearchFormState {
    final String formContentXml;
    final SearchFilter searchFilter;

    public SearchFormState(String xml, SearchFilter filter) {
      formContentXml = xml;
      searchFilter = filter;
    }
  }

  private boolean m_active;
  private final WeakHashMap<IForm, String> m_formToIdentifierMap;
  private final WeakHashMap<ITable, String> m_tableToIdentifierMap;

  private final FormListener m_formListener = new FormListener() {
    @Override
    public void formChanged(FormEvent e) throws ProcessingException {
      //auto-detach
      if (!m_active) {
        e.getForm().removeFormListener(m_formListener);
        return;
      }
      String id = m_formToIdentifierMap.get(e.getForm());
      if (id != null) {
        try {
          handlePageFormEvent(e, id);
        }
        catch (Throwable t) {
          LOG.warn("page form event " + e, t);
        }
      }
    }
  };

  private final TableColumnFilterListener m_tableColumnFilterListener = new TableColumnFilterListener() {
    @Override
    public void tableColumnFilterChanged(TableColumnFilterEvent e) throws ProcessingException {
      if (!m_active) {
        e.getColumnFilterManager().removeListener(m_tableColumnFilterListener);
        return;
      }
      String id = m_tableToIdentifierMap.get(e.getTable());
      if (id != null) {
        try {
          handleTableFilterEvent(e, id);
        }
        catch (Throwable t) {
          LOG.warn("table filter event " + e, t);
        }
      }
    }
  };

  public AbstractMemoryPolicy() {
    m_formToIdentifierMap = new WeakHashMap<IForm, String>();
    m_tableToIdentifierMap = new WeakHashMap<ITable, String>();
  }

  @Override
  public void addNotify() {
    m_active = true;
  }

  @Override
  public void removeNotify() {
    m_active = false;
  }

  /**
   * Attaches listener on page contents
   */
  @Override
  public void pageCreated(IPage p) throws ProcessingException {
    if (p.getOutline() instanceof AbstractPageField.SimpleOutline) {
      return;
    }
    if (p instanceof IPageWithTable) {
      IPageWithTable<? extends ITable> pt = (IPageWithTable<?>) p;
      ITable table = pt.getTable();
      if (table != null) {
        String pageTableIdentifier = registerPageTable(pt, table);
        if (pageTableIdentifier != null) {
          loadColumnFilterState(table, pageTableIdentifier);
        }
      }
    }
  }

  @Override
  public void pageSearchFormStarted(IPageWithTable<?> p) throws ProcessingException {
    if (p.getOutline() instanceof AbstractPageField.SimpleOutline) {
      return;
    }
    IForm f = p.getSearchFormInternal();
    if (f != null) {
      String pageFormIdentifier = registerPageForm(p, f);
      if (f.isFormOpen()) {
        loadSearchFormState(f, pageFormIdentifier);
      }
    }
  }

  /**
   * @return the identifier for the page form
   */
  protected String registerPageForm(IPage p, IForm f) {
    String id = createUniqueIdForPage(p, f);
    m_formToIdentifierMap.put(f, id);
    f.removeFormListener(m_formListener);
    f.addFormListener(m_formListener);
    return id;
  }

  /**
   * @return the identifier for the page table or <code>null</code> if the table does not have a column filter manager.
   */
  protected String registerPageTable(IPage p, ITable t) {
    if (t.getColumnFilterManager() == null) {
      return null;
    }
    String id = createUniqueIdForPage(p, t);
    m_tableToIdentifierMap.put(t, id);
    t.getColumnFilterManager().removeListener(m_tableColumnFilterListener);
    t.getColumnFilterManager().addListener(m_tableColumnFilterListener);
    return id;
  }

  protected String createUniqueIdForPage(IPage p, Object o) {
    if (p == null) {
      return null;
    }
    StringBuilder builder = new StringBuilder();
    createIdForPage(builder, p, o);
    IPage page = p.getParentPage();
    while (page != null) {
      createIdForPage(builder, page, null);
      page = page.getParentPage();
    }
    CRC32 crc = new CRC32();
    crc.update(builder.toString().getBytes());
    return "" + crc.getValue();
  }

  private void createIdForPage(StringBuilder b, IPage page, Object o) {
    b.append("/");
    b.append(page.getClass().getName());
    if (page.getUserPreferenceContext() != null) {
      b.append("/");
      b.append(page.getUserPreferenceContext());
    }
    if (o != null) {
      b.append("/");
      b.append(o.getClass().getName());
    }
    FastBeanInfo pi = new FastBeanInfo(page.getClass(), page.getClass().getSuperclass());
    for (FastPropertyDescriptor prop : pi.getPropertyDescriptors()) {
      if (prop.getReadMethod() != null
          && (Date.class.isAssignableFrom(prop.getPropertyType())
              || Number.class.isAssignableFrom(prop.getPropertyType())
              || String.class.isAssignableFrom(prop.getPropertyType())
              || long.class.isAssignableFrom(prop.getPropertyType()))) {
        // only accept Numbers, Strings or Dates
        try {
          b.append("/");
          b.append(prop.getName());
          b.append("=");
          b.append(prop.getReadMethod().invoke(page, new Object[0]));
        }
        catch (Exception e) {
          e.printStackTrace();
          // nop - ignore this property
        }
      }
    }
  }

  protected void handlePageFormEvent(FormEvent e, String pageFormIdentifier) throws ProcessingException {
    switch (e.getType()) {
      case FormEvent.TYPE_LOAD_COMPLETE: {
        //store form state since it was probably reset
        storeSearchFormState(e.getForm(), pageFormIdentifier);
        break;
      }
      case FormEvent.TYPE_STORE_AFTER: {
        storeSearchFormState(e.getForm(), pageFormIdentifier);
        break;
      }
    }
  }

  protected void loadSearchFormState(IForm f, String pageFormIdentifier) throws ProcessingException {
    //nop
  }

  protected void storeSearchFormState(IForm f, String pageFormIdentifier) throws ProcessingException {
    //nop
  }

  protected void handleTableFilterEvent(TableColumnFilterEvent e, String id) throws ProcessingException {
    switch (e.getType()) {
      case TableColumnFilterEvent.TYPE_FILTER_ADDED:
      case TableColumnFilterEvent.TYPE_FILTER_CHANGED:
      case TableColumnFilterEvent.TYPE_FILTER_REMOVED:
      case TableColumnFilterEvent.TYPE_FILTERS_RESET:
        storeColumnFilterState(e.getTable(), id);
        break;
    }

  }

  protected void storeColumnFilterState(ITable t, String pageTableIdentifier) throws ProcessingException {
    // nop
  }

  protected void loadColumnFilterState(ITable t, String pageTableIdentifier) throws ProcessingException {
    // nop
  }

  @Override
  public void afterOutlineSelectionChanged(final IDesktop desktop) {
  }

  @Override
  public void beforeTablePageLoadData(IPageWithTable<?> page) {
  }

  @Override
  public void afterTablePageLoadData(IPageWithTable<?> page) {
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
