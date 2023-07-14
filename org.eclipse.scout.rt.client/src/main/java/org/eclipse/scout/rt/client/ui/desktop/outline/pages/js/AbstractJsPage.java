/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages.js;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("6e594e35-850b-4620-b295-dbfc82ca8e1a")
public abstract class AbstractJsPage extends AbstractPage<ITable> implements IJsPage {

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
   * @return the objectType of the jsPage to be opened
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(4200)
  protected String getConfiguredJsPageObjectType() {
    return null;
  }

  /**
   * @return additional model for the jsPage
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(4242)
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
  protected ITable createTable() {
    return null;
  }
}
