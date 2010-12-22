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

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * No specific restrictions, cache all table page search form contents and check memory limits after page reload.
 */
public class LargeMemoryPolicy extends AbstractMemoryPolicy {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LargeMemoryPolicy.class);

  //cache all search form contents
  private final HashMap<String/*pageFormIdentifier*/, String/*formXml*/> m_searchFormCache;

  public LargeMemoryPolicy() {
    m_searchFormCache = new HashMap<String, String>();
  }

  @Override
  protected void loadSearchFormState(IForm f, String pageFormIdentifier) throws ProcessingException {
    //check if there is stored search form data
    String xml = m_searchFormCache.get(pageFormIdentifier);
    if (xml != null) {
      f.setXML(xml);
    }
  }

  @Override
  protected void storeSearchFormState(IForm f, String pageFormIdentifier) throws ProcessingException {
    //cache search form data
    if (f.isEmpty()) {
      m_searchFormCache.remove(pageFormIdentifier);
    }
    else {
      String xml = f.getXML("UTF-8");
      m_searchFormCache.put(pageFormIdentifier, xml);
    }
  }

  @Override
  public void afterOutlineSelectionChanged(final IDesktop desktop) {
    long memTotal = Runtime.getRuntime().totalMemory();
    long memUsed = (memTotal - Runtime.getRuntime().freeMemory());
    long memMax = Runtime.getRuntime().maxMemory();
    if (memUsed > memMax * 80L / 100L) {
      new ClientSyncJob("Check memory", ClientSyncJob.getCurrentSession()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          desktop.releaseUnusedPages();
          System.gc();
        }
      }.schedule();
    }
  }

  @Override
  public String toString() {
    return "Large";
  }
}
