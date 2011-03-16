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
  private final FormListener m_formListener = new FormListener() {
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

  public AbstractMemoryPolicy() {
    m_formToIdentifierMap = new WeakHashMap<IForm, String>();
  }

  public void addNotify() {
    m_active = true;
  }

  public void removeNotify() {
    m_active = false;
  }

  /**
   * Attaches listener on table page search forms
   */
  public void pageCreated(IPage p) throws ProcessingException {
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

  protected String createUniqueIdForPage(IPage p, IForm f) {
    if (p == null) {
      return null;
    }
    StringBuilder builder = new StringBuilder();
    createIdForPage(builder, p, f);
    IPage page = p.getParentPage();
    while (page != null) {
      createIdForPage(builder, page, null);
      page = page.getParentPage();
    }
    CRC32 crc = new CRC32();
    crc.update(builder.toString().getBytes());
    return "" + crc.getValue();
  }

  private void createIdForPage(StringBuilder b, IPage page, IForm form) {
    b.append("/");
    b.append(page.getClass().getName());
    if (page.getBookmarkIdentifier() != null) {
      b.append("/");
      b.append(page.getBookmarkIdentifier());
    }
    if (form != null) {
      b.append("/");
      b.append(form.getClass().getName());
    }
    FastBeanInfo pi = new FastBeanInfo(page.getClass(), page.getClass().getSuperclass());
    for (FastPropertyDescriptor prop : pi.getPropertyDescriptors()) {
      if (prop.getReadMethod() != null &&
          (Date.class.isAssignableFrom(prop.getPropertyType()) ||
              Number.class.isAssignableFrom(prop.getPropertyType()) ||
              String.class.isAssignableFrom(prop.getPropertyType()) ||
              long.class.isAssignableFrom(prop.getPropertyType()))) {
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

  public void afterOutlineSelectionChanged(final IDesktop desktop) {
  }

  public void beforeTablePageLoadData(IPageWithTable<?> page) {
  }

  public void afterTablePageLoadData(IPageWithTable<?> page) {
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
