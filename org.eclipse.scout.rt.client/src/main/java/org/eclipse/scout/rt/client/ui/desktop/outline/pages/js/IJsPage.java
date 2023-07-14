/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages.js;

import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.dataobject.IDoEntity;

public interface IJsPage extends IPage<ITable> {
  String PROP_JS_PAGE_MODEL = "jsPageModel";

  String getJsPageObjectType();

  void setJsPageObjectType(String jsPageObjectType);

  IDoEntity getJsPageModel();

  void setJsPageModel(IDoEntity jsPageModel);
}
