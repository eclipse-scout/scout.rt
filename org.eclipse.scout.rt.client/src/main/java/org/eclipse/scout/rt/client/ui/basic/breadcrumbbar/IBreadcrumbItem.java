/*******************************************************************************
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.breadcrumbbar;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;

public interface IBreadcrumbItem extends IAction {
  String PROP_REF = "ref";

  String getRef();

  void setRef(String value);

  public FastListenerList<BreadcrumbItemListener> breadcrumbItemListeners();

  default void addBreadcrumbItemListener(BreadcrumbItemListener listener) {
    breadcrumbItemListeners().add(listener);
  }

  default void removeBreadcrumbItemListener(BreadcrumbItemListener listener) {
    breadcrumbItemListeners().remove(listener);
  }
}
