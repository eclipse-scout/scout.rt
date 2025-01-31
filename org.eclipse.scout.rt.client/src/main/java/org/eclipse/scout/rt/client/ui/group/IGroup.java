/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.group;

import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.shared.dimension.IVisibleDimension;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

public interface IGroup extends IWidget, IOrdered, IStyleable, IExtensibleObject, IContributionOwner, IVisibleDimension {

  String PROP_ORDER = "order";
  String PROP_COLLAPSED = "collapsed";
  String PROP_COLLAPSIBLE = "collapsible";
  String PROP_COLLAPSE_STYLE = "collapseStyle";
  String PROP_TITLE = "title";
  String PROP_VISIBLE = "visible";
  String PROP_HEADER = "header";
  String PROP_HEADER_FOCUSABLE = "headerFocusable";
  String PROP_HEADER_VISIBLE = "headerVisible";
  String PROP_BODY = "body";
  String PROP_GROUP_ID = "groupId";
  String PROP_TITLE_SUFFIX = "titleSuffix";
  String PROP_ICON_ID = "iconId";

  String COLLAPSE_STYLE_LEFT = "left";
  String COLLAPSE_STYLE_RIGHT = "right";
  String COLLAPSE_STYLE_BOTTOM = "bottom";

  boolean isCollapsed();

  void setCollapsed(boolean collapsed);

  void toggleCollapse();

  boolean isCollapsible();

  void setCollapsible(boolean collapsible);

  String getCollapseStyle();

  void setCollapseStyle(String collapseStyle);

  String getTitle();

  void setTitle(String title);

  String getTitleSuffix();

  void setTitleSuffix(String suffix);

  /**
   * @return If this {@link IFormField} is visible. It is visible if all visibility-dimensions are <code>true</code>.
   */
  boolean isVisible();

  /**
   * Changes the visible property of this {@link IFormField} to the given value.
   *
   * @param visible
   *     The new visible value.
   */
  void setVisible(boolean b);

  /**
   * @return The visible-granted property of this {@link IFormField}.
   */
  boolean isVisibleGranted();

  /**
   * Changes the visible-granted property of this {@link IFormField} to the given value.
   *
   * @param visible
   *     The new visible-granted value.
   */
  void setVisibleGranted(boolean b);

  boolean isHeaderVisible();

  void setHeaderVisible(boolean headerVisible);

  boolean isHeaderFocusable();

  void setHeaderFocusable(boolean headerFocusable);

  IWidget getHeader();

  void setHeader(IWidget widget);

  IWidget getBody();

  void setBody(IWidget widget);

  IGroupUIFacade getUIFacade();

  Object getGroupId();

  void setGroupId(Object groupId);

  String getIconId();

  void setIconId(String iconId);
}
