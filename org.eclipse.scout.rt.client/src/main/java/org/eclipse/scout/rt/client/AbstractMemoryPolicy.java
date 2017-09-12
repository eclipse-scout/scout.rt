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
package org.eclipse.scout.rt.client;

import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.CRC32;

import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopEvent;
import org.eclipse.scout.rt.client.ui.desktop.DesktopListener;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.FormEvent;
import org.eclipse.scout.rt.client.ui.form.FormListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.pagefield.AbstractPageField.SimpleOutline;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.reflect.FastBeanInfo;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMemoryPolicy implements IMemoryPolicy {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractMemoryPolicy.class);

  public static class SearchFormState {
    final String m_formContentXml;
    final SearchFilter m_searchFilter;

    public SearchFormState(String xml, SearchFilter filter) {
      m_formContentXml = xml;
      m_searchFilter = filter;
    }
  }

  private boolean m_active;
  private final Map<IForm, String> m_formToIdentifierMap;
  private final Map<ITable, String> m_tableToIdentifierMap;
  private final MemoryPolicyListener m_memoryPolicyListener;

  private final FormListener m_formListener = new FormListener() {
    @Override
    public void formChanged(FormEvent e) {
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
        catch (Exception t) {
          LOG.warn("could not handle page form event{}", e, t);
        }
      }
    }
  };

  private final TableListener m_tableListener = new TableAdapter() {
    @Override
    public void tableChanged(TableEvent e) {
      if (!m_active) {
        e.getTable().removeTableListener(m_tableListener);
        return;
      }
      String id = m_tableToIdentifierMap.get(e.getTable());
      if (id != null) {
        try {
          handleTableFilterEvent(e, id);
        }
        catch (Exception t) {
          LOG.warn("could not handle table filter event {}", e, t);
        }
      }
    }
  };

  public AbstractMemoryPolicy() {
    m_formToIdentifierMap = new WeakHashMap<>();
    m_tableToIdentifierMap = new WeakHashMap<>();
    m_memoryPolicyListener = new MemoryPolicyListener();
  }

  @Override
  public void addNotify() {
    m_active = true;
  }

  @Override
  public void removeNotify() {
    m_active = false;
  }

  @Override
  public void pageCreated(IPage<?> p) {
    //nop
  }

  @Override
  public void pageTableCreated(IPage<?> p) {
    if (p instanceof IPageWithTable) {
      IPageWithTable<? extends ITable> pt = (IPageWithTable<?>) p;
      ITable table = pt.getTable(false);
      if (table != null) {
        String pageTableIdentifier = registerPageTable(pt, table);
        if (pageTableIdentifier != null) {
          loadUserFilterState(table, pageTableIdentifier);
        }
      }
    }
  }

  @Override
  public void pageSearchFormStarted(IPageWithTable<?> p) {
    if (p.getOutline() instanceof SimpleOutline) {
      return;
    }
    IForm f = p.getSearchFormInternal();
    if (f != null) {
      String pageFormIdentifier = registerPageForm(p, f);
      if (f.isFormStarted()) {
        loadSearchFormState(f, pageFormIdentifier);
      }
    }
  }

  /**
   * @return the identifier for the page form
   */
  protected String registerPageForm(IPage<?> p, IForm f) {
    String id = createUniqueIdForPage(p, f);
    m_formToIdentifierMap.put(f, id);
    f.removeFormListener(m_formListener);
    f.addFormListener(m_formListener);
    return id;
  }

  /**
   * @return the identifier for the page table or <code>null</code> if the table does not have a column filter manager.
   */
  protected String registerPageTable(IPage<?> p, ITable t) {
    if (t.getUserFilterManager() == null) {
      return null;
    }
    String id = createUniqueIdForPage(p, t);
    m_tableToIdentifierMap.put(t, id);
    t.removeTableListener(m_tableListener);
    t.addTableListener(m_tableListener);
    return id;
  }

  protected String createUniqueIdForPage(IPage<?> p, Object o) {
    if (p == null) {
      return null;
    }
    StringBuilder builder = new StringBuilder();
    createIdForPage(builder, p, o);
    IPage<?> page = p.getParentPage();
    while (page != null) {
      createIdForPage(builder, page, null);
      page = page.getParentPage();
    }
    CRC32 crc = new CRC32();
    crc.update(builder.toString().getBytes());
    return "" + crc.getValue();
  }

  private void createIdForPage(StringBuilder b, IPage<?> page, Object o) {
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
          LOG.error("Error reading property {}", prop, e);
          // nop - ignore this property
        }
      }
    }
  }

  protected void handlePageFormEvent(FormEvent e, String pageFormIdentifier) {
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

  protected void loadSearchFormState(IForm f, String pageFormIdentifier) {
    //nop
  }

  protected void storeSearchFormState(IForm f, String pageFormIdentifier) {
    //nop
  }

  protected void handleTableFilterEvent(TableEvent e, String id) {
    switch (e.getType()) {
      case TableEvent.TYPE_USER_FILTER_ADDED:
      case TableEvent.TYPE_USER_FILTER_REMOVED:
        storeUserFilterState(e.getTable(), id);
        break;
    }

  }

  protected void storeUserFilterState(ITable t, String pageTableIdentifier) {
    // nop
  }

  protected void loadUserFilterState(ITable t, String pageTableIdentifier) {
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

  @Override
  public void registerOutline(IOutline outline) {
    outline.addTreeListener(m_memoryPolicyListener);
  }

  @Override
  public void deregisterOutline(IOutline outline) {
    outline.removeTreeListener(m_memoryPolicyListener);
  }

  @Override
  public void registerDesktop(IDesktop desktop) {
    desktop.addDesktopListener(m_memoryPolicyListener);
  }

  @Override
  public void deregisterDesktop(IDesktop desktop) {
    desktop.removeDesktopListener(m_memoryPolicyListener);
  }

  protected class MemoryPolicyListener extends TreeAdapter implements DesktopListener {

    IOutline m_activeOutline;

    @Override
    public void desktopChanged(DesktopEvent e) {
      switch (e.getType()) {
        case DesktopEvent.TYPE_OUTLINE_CHANGED: {
          m_activeOutline = e.getOutline();
          break;
        }
        case DesktopEvent.TYPE_DESKTOP_CLOSED: {
          if (e.getSource() instanceof IDesktop) {
            deregisterDesktop((IDesktop) e.getSource());
          }
        }
      }
    }

    @Override
    public void treeChanged(TreeEvent e) {
      try {
        if (e.getType() == TreeEvent.TYPE_NODES_SELECTED && e.getSource() == m_activeOutline) {
          afterOutlineSelectionChanged(ClientSessionProvider.currentSession().getDesktop());
        }
        if (e.getNode() instanceof IPage) {
          IPage<?> p = (IPage<?>) e.getNode();
          switch (e.getType()) {
            case OutlineEvent.TYPE_PAGE_AFTER_PAGE_INIT: {
              pageCreated(p);
              break;
            }
            case OutlineEvent.TYPE_PAGE_AFTER_TABLE_INIT: {
              pageTableCreated(p);
              break;
            }
          }
        }
        if (e.getNode() instanceof IPageWithTable) {
          IPageWithTable<?> p = (IPageWithTable<?>) e.getNode();
          switch (e.getType()) {
            case OutlineEvent.TYPE_PAGE_BEFORE_DATA_LOADED: {
              beforeTablePageLoadData(p);
              break;
            }
            case OutlineEvent.TYPE_PAGE_AFTER_DATA_LOADED: {
              afterTablePageLoadData(p);
              break;
            }
            case OutlineEvent.TYPE_PAGE_AFTER_SEARCH_FORM_START: {
              pageSearchFormStarted(p);
              break;
            }
          }
        }
      }
      catch (RuntimeException | PlatformError t) {
        BEANS.get(ExceptionHandler.class).handle(t);
      }
    }
  }
}
