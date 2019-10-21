/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.tile.BeanTileChains.BeanTileAppLinkActionChain;
import org.eclipse.scout.rt.client.extension.ui.tile.IBeanTileExtension;
import org.eclipse.scout.rt.client.extension.ui.tile.ITileExtension;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("f731542c-43bb-4747-b907-71e4e2ae3dcf")
public abstract class AbstractBeanTile<BEAN> extends AbstractTile implements IBeanTile<BEAN> {

  private IBeanTileUIFacade m_uiFacade;

  public AbstractBeanTile() {
    super();
  }

  public AbstractBeanTile(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
  }

  protected IBeanTileUIFacade createUIFacade() {
    return new P_UIFacade();
  }

  @Override
  public IBeanTileUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  @SuppressWarnings("unchecked")
  public BEAN getBean() {
    return (BEAN) propertySupport.getProperty(PROP_BEAN);
  }

  @Override
  public void setBean(BEAN bean) {
    propertySupport.setProperty(PROP_BEAN, bean);
  }

  /**
   * Called when an app link has been clicked.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(10)
  protected void execAppLinkAction(String ref) {
  }

  protected final void interceptAppLinkAction(String ref) {
    List<? extends ITileExtension<? extends AbstractTile>> extensions = getAllExtensions();
    BeanTileAppLinkActionChain chain = new BeanTileAppLinkActionChain<BEAN>(extensions);
    chain.execAppLinkAction(ref);
  }

  protected static class LocalBeanTileExtension<BEAN, OWNER extends AbstractBeanTile<BEAN>> extends LocalTileExtension<OWNER> implements IBeanTileExtension<BEAN, OWNER> {

    public LocalBeanTileExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execAppLinkAction(BeanTileAppLinkActionChain<BEAN> chain, String ref) {
      getOwner().execAppLinkAction(ref);
    }
  }

  @Override
  protected IBeanTileExtension<BEAN, ? extends AbstractBeanTile<BEAN>> createLocalExtension() {
    return new LocalBeanTileExtension<>(this);
  }

  @Override
  public void doAppLinkAction(String ref) {
    interceptAppLinkAction(ref);
  }

  protected class P_UIFacade implements IBeanTileUIFacade {

    @Override
    public void fireAppLinkActionFromUI(String ref) {
      doAppLinkAction(ref);
    }
  }
}
