/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
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
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(createUIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    setBean(getConfiguredBean());
  }

  protected IBeanTileUIFacade createUIFacade() {
    return new P_UIFacade();
  }

  @Override
  public IBeanTileUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * @return the initial bean value
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  protected BEAN getConfiguredBean() {
    return null;
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
