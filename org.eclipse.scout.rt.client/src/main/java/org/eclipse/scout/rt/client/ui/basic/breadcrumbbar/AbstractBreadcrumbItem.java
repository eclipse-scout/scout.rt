/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.breadcrumbbar;

import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;

@ClassId("259a8161-3a96-446e-b34e-e956acfbf61e")
public abstract class AbstractBreadcrumbItem extends AbstractAction implements IBreadcrumbItem {

  private FastListenerList<BreadcrumbItemListener> m_listenerList;

  public AbstractBreadcrumbItem() {
    this(true);
  }

  public AbstractBreadcrumbItem(boolean callInitializer) {
    super(false);
    m_listenerList = new FastListenerList<>();
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setRef(getConfiguredRef());
  }

  @Override
  protected void doActionInternal() {
    super.doActionInternal();
    fireBreadcrumbAction();
  }

  @Override
  public FastListenerList<BreadcrumbItemListener> breadcrumbItemListeners() {
    return m_listenerList;
  }

  protected void fireBreadcrumbAction() {
    fireBreadcrumbEvent(new BreadcrumbItemEvent(this, BreadcrumbItemEvent.TYPE_BREADCRUMB_ITEM_ACTION));
  }

  protected void fireBreadcrumbEvent(BreadcrumbItemEvent e) {
    breadcrumbItemListeners().list().forEach(listener -> listener.breadcrumbItemChanged(e));
  }

  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(1000)
  protected String getConfiguredRef() {
    return null;
  }

  @Override
  public String getRef() {
    return propertySupport.getPropertyString(PROP_REF);
  }

  @Override
  public void setRef(String value) {
    propertySupport.setProperty(PROP_REF, value);
  }
}
