/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.group;

import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

public interface IGroup extends IWidget, IOrdered, IStyleable, IExtensibleObject, IContributionOwner, ITypeWithClassId {
  String PROP_ORDER = "order";
  String PROP_COLLAPSED = "collapsed";
  String PROP_COLLAPSE_STYLE = "collapseStyle";
  String PROP_TITLE = "title";
  String PROP_HEADER_VISIBLE = "headerVisible";
  String PROP_BODY = "body";
  String PROP_GROUP_ID = "groupId";
  String PROP_TITLE_SUFFIX = "titleSuffix";
  String PROP_ICON_ID = "iconId";

  String COLLAPSE_STYLE_LEFT = "left";
  String COLLAPSE_STYLE_RIGHT = "right";

  ITypeWithClassId getContainer();

  void setContainer(ITypeWithClassId container);

  boolean isCollapsed();

  void setCollapsed(boolean collapsed);

  void toggleCollapse();

  String getCollapseStyle();

  void setCollapseStyle(String collapseStyle);

  String getTitle();

  void setTitle(String title);

  String getTitleSuffix();

  void setTitleSuffix(String suffix);

  boolean isHeaderVisible();

  void setHeaderVisible(boolean headerVisible);

  IWidget getBody();

  void setBody(IWidget widget);

  IGroupUIFacade getUIFacade();

  Object getGroupId();

  void setGroupId(Object groupId);

  String getIconId();

  void setIconId(String iconId);
}
