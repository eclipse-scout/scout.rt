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
package org.eclipse.scout.rt.client.ui.form.fields.browserfield;

import java.net.URL;

import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractBrowserField extends AbstractValueField<RemoteFile> implements IBrowserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBrowserField.class);

  private IBrowserFieldUIFacade m_uiFacade;
  private boolean m_scrollBarEnabled;

  public AbstractBrowserField() {
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredScrollBarEnabled() {
    return false;
  }

  /**
   * @param url
   * @param path
   *          {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected void execHyperlinkAction(URL url, String path, boolean local) throws ProcessingException {
    LOG.info("execHyperlinkAction " + url + " (in " + getClass().getName() + ")");
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    m_scrollBarEnabled = getConfiguredScrollBarEnabled();
  }

  @Override
  protected void applySearchInternal(SearchFilter search) {
    //nop
  }

  public void doHyperlinkAction(URL url) throws ProcessingException {
    execHyperlinkAction(url, url.getPath(), url != null && url.getHost().equals("local"));
  }

  public void setExternalURL(URL url) {
    propertySupport.setProperty(PROP_EXTERNAL_URL, url);
  }

  public URL getExternalURL() {
    return (URL) propertySupport.getProperty(PROP_EXTERNAL_URL);
  }

  public IBrowserFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  public boolean isScrollBarEnabled() {
    return m_scrollBarEnabled;
  }

  private class P_UIFacade implements IBrowserFieldUIFacade {

    public void fireHyperlinkActionFromUI(URL url) {
      try {
        doHyperlinkAction(url);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

}
