/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages.js;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("3ad642d8-4858-43af-bf4c-f5aa7fab4ba1")
public class AbstractJsPage extends AbstractPage<ITable> implements IJsPage {

  private String m_jsPageObjectType;
  private IDoEntity m_jsPageModel;

  public AbstractJsPage() {
    this(true);
  }

  public AbstractJsPage(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setJsPageObjectType(getConfiguredJsPageObjectType());
    setJsPageModel(getConfiguredJsPageModel());
  }

  /**
   * @return the objectType of the jsPage to be created
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(200)
  protected String getConfiguredJsPageObjectType() {
    return null;
  }

  /**
   * @return additional model for the jsPage
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(210)
  protected IDoEntity getConfiguredJsPageModel() {
    return null;
  }

  @Override
  public String getJsPageObjectType() {
    return m_jsPageObjectType;
  }

  @Override
  public void setJsPageObjectType(String jsPageObjectType) {
    m_jsPageObjectType = jsPageObjectType;
  }

  @Override
  public IDoEntity getJsPageModel() {
    return m_jsPageModel;
  }

  @Override
  public void setJsPageModel(IDoEntity jsPageModel) {
    m_jsPageModel = jsPageModel;
  }

  @Override
  protected final ITable createTable() {
    return null;
  }
}
