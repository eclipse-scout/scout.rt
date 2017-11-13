/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
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
  String PROP_TITLE = "title";
  String PROP_BODY = "body";

  void init();

  void postInitConfig();

  void dispose();

  ITypeWithClassId getContainer();

  void setContainer(ITypeWithClassId container);

  boolean isCollapsed();

  void setCollapsed(boolean collapsed);

  void toggleCollapse();

  String getTitle();

  void setTitle(String body);

  IWidget getBody();

  void setBody(IWidget widget);

  IGroupUIFacade getUIFacade();
}
