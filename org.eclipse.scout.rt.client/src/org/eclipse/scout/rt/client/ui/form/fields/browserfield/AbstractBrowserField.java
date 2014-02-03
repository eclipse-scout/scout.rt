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

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;

@ClassId("6402e68c-abd1-42b8-8da2-b4a12f910c98")
public abstract class AbstractBrowserField extends AbstractValueField<RemoteFile> implements IBrowserField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractBrowserField.class);

  private IBrowserFieldUIFacade m_uiFacade;
  private boolean m_scrollBarEnabled;

  public AbstractBrowserField() {
    this(true);
  }

  public AbstractBrowserField(boolean callInitializer) {
    super(callInitializer);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(250)
  protected boolean getConfiguredScrollBarEnabled() {
    return false;
  }

  /**
   * This callback is invoked before the link is followed, it can be used as handler and vetoer. The default returns
   * true.<br>
   * If there is more to do then simply return true/false, put the
   * code in a {@link ClientSyncJob} to prevent deadlocks and other problems
   * 
   * @return true to accept this location, false to prevent the browser from going to that location (equal to browser
   *         esc/stop button)
   * @param location
   * @param path
   *          may be null for locations like about:blank or javascript:... {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected boolean execAcceptLocationChange(String location, String path, boolean local) throws ProcessingException {
    return true;
  }

  /**
   * This callback is invoked after the link was followed, thus it is already at that location
   * <p>
   * The default does noting.
   * 
   * @param location
   * @param path
   *          may be null for locations like about:blank or javascript:... {@link URL#getPath()}
   * @param local
   *          true if the url is not a valid external url but a local model url
   *          (http://local/...)
   */
  @ConfigOperation
  @Order(230)
  protected void execLocationChanged(String location, String path, boolean local) throws ProcessingException {
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

  @Override
  public void doLocationChange(String location) throws ProcessingException {
    if (getUIFacade().fireBeforeLocationChangedFromUI(location)) {
      getUIFacade().fireAfterLocationChangedFromUI(location);
    }
  }

  @Override
  public void setLocation(String location) {
    propertySupport.setProperty(PROP_LOCATION, location);
  }

  @Override
  public String getLocation() {
    return (String) propertySupport.getProperty(PROP_LOCATION);
  }

  @Override
  public IBrowserFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public boolean isScrollBarEnabled() {
    return m_scrollBarEnabled;
  }

  private class P_UIFacade implements IBrowserFieldUIFacade {

    @Override
    public boolean fireBeforeLocationChangedFromUI(String location) {
      try {
        URL url = null;
        try {
          url = new URL(location);
        }
        catch (Throwable t) {
          //nop
        }
        return execAcceptLocationChange(location, url != null ? url.getPath() : null, url != null && url.getHost().equals("local"));
      }
      catch (Throwable t) {
        LOG.error("location: " + location, t);
      }
      return false;
    }

    @Override
    public void fireAfterLocationChangedFromUI(String location) {
      try {
        URL url = null;
        try {
          url = new URL(location);
        }
        catch (Throwable t) {
          //nop
        }
        execLocationChanged(location, url != null ? url.getPath() : null, url != null && url.getHost().equals("local"));
      }
      catch (Throwable t) {
        LOG.error("location: " + location, t);
      }
    }
  }

}
