/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages.js;

import java.util.List;

import org.eclipse.scout.rt.api.data.page.IPageParamDo;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.dataobject.IDoEntity;

/**
 * Wraps a page implemented in Scout JS.
 * <p>
 * The jsPage will be created by the Outline. To determine which jsPage will be created, the property
 * {@code jsPageObjectType} is used.
 * <p>
 * No page properties will be sent to the browser, they need to be set using JavaScript. However, it is possible to set
 * some properties from Java code by passing an additional model using {@link IJsPage#setJsPageModel(IDoEntity)}.
 */
public interface IJsPage extends IPage<ITable> {

  String PROP_JS_PAGE_OBJECT_TYPE = "jsPageObjectType";
  String PROP_JS_PAGE_MODEL = "jsPageModel";

  String getJsPageObjectType();

  void setJsPageObjectType(String jsPageObjectType);

  IDoEntity getJsPageModel();

  void setJsPageModel(IDoEntity jsPageModel);

  void loadChildPages(List<IPageParamDo> pageParams, boolean replace);

  void changeNode(ICell cell);
}
