/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.basic.breadcrumbbar;

import java.util.List;

import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

@ClassId("9a3ef53f-94a0-4750-b182-5e912ebebc94")
public class AbstractBreadcrumbBar extends AbstractWidget implements IBreadcrumbBar {

  protected final BreadcrumbItemListener m_breadcrumbListener = this::onBreadcrumbItemAction;

  @ConfigOperation
  protected void execBreadcrumbItemAction(IBreadcrumbItem item) {
    // noop
  }

  @Override
  public void setBreadcrumbItems(List<IBreadcrumbItem> breadcrumbItems) {
    setBreadcrumbItemsInternal(breadcrumbItems);
  }

  protected void setBreadcrumbItemsInternal(List<IBreadcrumbItem> breadcrumbItems) {
    for (IBreadcrumbItem oldItem : getBreadcrumbItems()) {
      oldItem.removeBreadcrumbItemListener(m_breadcrumbListener);
    }

    propertySupport.setPropertyAlwaysFire(PROP_BREADCRUMBS, breadcrumbItems);

    for (IBreadcrumbItem newItem : breadcrumbItems) {
      newItem.addBreadcrumbItemListener(m_breadcrumbListener);
    }
  }

  protected void onBreadcrumbItemAction(BreadcrumbItemEvent event) {
    execBreadcrumbItemAction((IBreadcrumbItem) event.getSource());
  }

  public List<IBreadcrumbItem> getBreadcrumbItemsInternal() {
    return propertySupport.getPropertyList(PROP_BREADCRUMBS);
  }

  @Override
  public List<IBreadcrumbItem> getBreadcrumbItems() {
    return CollectionUtility.arrayList(getBreadcrumbItemsInternal());
  }

  @Override
  public IBreadcrumbItem getBreadcrumbItemFor(String ref) {
    for (IBreadcrumbItem b : getBreadcrumbItems()) {
      String refValue = b.getRef();
      if (ObjectUtility.equals(refValue, ref)) {
        return b;
      }
    }
    return null;
  }
}
