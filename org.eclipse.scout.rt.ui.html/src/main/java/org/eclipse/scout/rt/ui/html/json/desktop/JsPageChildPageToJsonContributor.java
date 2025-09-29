/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.api.data.page.IPageParamDo;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.js.IJsPage;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonDataObjectHelper;
import org.json.JSONObject;

/**
 * The creation of child pages of an {@link IJsPage} is triggered by the JsPageHelper.
 * The helper sends an {@link IPageParamDo} that is used to create the page.
 * The {@link IPageParamDo} is set on the child page as {@link IPage#getPrimaryKey()}.
 * This {@link IPageToJsonContributor} adds the {@link IPageParamDo} to the {@link JSONObject} so the JsPageHelper can identify the created child pages.
 */
public class JsPageChildPageToJsonContributor implements IPageToJsonContributor {

  public static final String PROP_JS_PAGE_CHILD_PAGE_PARAM = "__jsPageChildPageParam";

  private final JsonDataObjectHelper m_jsonDoHelper = BEANS.get(JsonDataObjectHelper.class); // cached instance

  protected JsonDataObjectHelper jsonDoHelper() {
    return m_jsonDoHelper;
  }

  @Override
  public void contribute(JSONObject json, IPage<?> page) {
    // check if parent is an IJsPage
    if (page == null || !(page.getParentPage() instanceof IJsPage)) {
      return;
    }

    // check if primaryKey is an IPageParamDo
    if (!(page.getPrimaryKey() instanceof IPageParamDo pageParam)) {
      return;
    }

    JSONObject jsPageChildPageParam = jsonDoHelper().dataObjectToJson(pageParam);

    // simply add the pageParam to the JSONObject if the page is NOT an IJsPage
    if (!(page instanceof IJsPage)) {
      json.put(PROP_JS_PAGE_CHILD_PAGE_PARAM, jsPageChildPageParam);
      return;
    }

    // add the pageParam to the jsPageModel if the page is an IJsPage to ensure the property is set on the created Scout JS page
    JSONObject jsPageModel = ObjectUtility.nvl(json.optJSONObject(IJsPage.PROP_JS_PAGE_MODEL), new JSONObject());
    jsPageModel.put(PROP_JS_PAGE_CHILD_PAGE_PARAM, jsPageChildPageParam);

    json.put(IJsPage.PROP_JS_PAGE_MODEL, jsPageModel);
  }
}
