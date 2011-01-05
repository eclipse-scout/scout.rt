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

import java.util.WeakHashMap;

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

public class AbstractMemoryPolicy implements IMemoryPolicy {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractMemoryPolicy.class);

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
    if (!(p instanceof IPageWithTable<?>)) {
      return;
    }
    if (p.getOutline() instanceof AbstractPageField.SimpleOutline) {
      return;
    }
    IForm f = ((IPageWithTable<?>) p).getSearchFormInternal();
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
    String id = p.getClass().getName() + (p.getBookmarkIdentifier() != null ? "/" + p.getBookmarkIdentifier() : "") + "/" + f.getClass().getName();
    m_formToIdentifierMap.put(f, id);
    f.removeFormListener(m_formListener);
    f.addFormListener(m_formListener);
    return id;
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
