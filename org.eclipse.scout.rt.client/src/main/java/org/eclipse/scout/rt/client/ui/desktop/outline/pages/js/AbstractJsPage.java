/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages.js;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.api.data.page.IPageParamDo;
import org.eclipse.scout.rt.api.data.page.IdPageParamDo;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithNodes;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

@ClassId("3ad642d8-4858-43af-bf4c-f5aa7fab4ba1")
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

  @Override
  public String classId() {
    // If there is no classId annotation, null is returned
    // If there is a classId annotation, it must match the uuid of the JavaScript page
    return ConfigurationUtility.getAnnotatedClass(getClass());
  }

  @Override
  public void loadChildPages(List<IPageParamDo> pageParams, boolean replace) {
    IOutline outline = getOutline();
    if (outline == null) {
      return;
    }
    try {
      outline.setTreeChanging(true);
      setChildrenLoaded(false);
      fireBeforeDataLoaded();
      try {
        if (replace) {
          outline.removeAllChildNodes(this);
          outline.addChildNodes(this, createChildPages(pageParams));
        }
        else {
          List<IPage<?>> childPages = createChildPages(pageParams);

          List<IPage<?>> childPagesToRemove = getChildPages();
          childPagesToRemove.removeAll(childPages);
          outline.removeChildNodes(this, childPagesToRemove);

          List<IPage<?>> childPagesToAdd = CollectionUtility.arrayList(childPages);
          childPagesToAdd.removeAll(getChildPages());
          outline.addChildNodes(this, childPagesToAdd);

          outline.updateChildNodeOrder(this, childPages);
        }
      }
      finally {
        fireAfterDataLoaded();
      }
      setChildrenLoaded(true);
      setChildrenDirty(false);
    }
    finally {
      outline.setTreeChanging(false);
    }
  }

  protected List<IPage<?>> createChildPages(List<IPageParamDo> pageParams) {
    if (!CollectionUtility.hasElements(pageParams)) {
      return emptyList();
    }
    return pageParams.stream()
        .map(this::getOrCreateChildPage)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  protected IPage<?> getOrCreateChildPage(IPageParamDo pageParam) {
    IPage<?> existingPage = findExistingPage(pageParam);
    if (existingPage != null) {
      return existingPage;
    }
    IPage<?> childPage = createChildPage(pageParam);
    if (childPage != null) {
      childPage.setPrimaryKey(pageParam);
    }
    return childPage;
  }

  protected IPage<?> findExistingPage(IPageParamDo pageParam) {
    return getChildPages().stream()
        .filter(p -> ObjectUtility.equals(pageParam, p.getPrimaryKey()))
        .findAny()
        .orElse(null);
  }

  /**
   * Override this method to create a child page using an {@link IPageParamDo} given by the Scout JS equivalent of this page.
   */
  protected IPage<?> createChildPage(IPageParamDo pageParam) {
    if (pageParam instanceof IdPageParamDo idPageParam) {
      return createChildPage(idPageParam.getId());
    }
    return null;
  }

  /**
   * Override this method to create a child page using an {@link IId} given by the Scout JS equivalent of this page.
   */
  protected IPage<?> createChildPage(IId id) {
    return null;
  }

  @Override
  public void changeNode(ICell cell) {
    if (cell == null) {
      return;
    }

    IDoEntity jsPageModel = getJsPageModel();
    if (jsPageModel == null) {
      jsPageModel = BEANS.get(DoEntity.class);
    }

    // properties come from a summary column from a parent PageWithTable, transfer them to jsPageModel so that they are still available after a browser reload
    // keep in sync with token: [5vv7MGGQ5BQY5NXX7CwJ9tmL4]
    jsPageModel.put("text", cell.getText());
    jsPageModel.put("htmlEnabled", cell.isHtmlEnabled());
    jsPageModel.put("cssClass", cell.getCssClass());
    jsPageModel.put("iconId", cell.getIconId());

    setJsPageModel(jsPageModel);

    if (getParentPage() instanceof IPageWithNodes pageWithNodes) {
      pageWithNodes.updateTableRowFromPage(this);
    }
  }
}
