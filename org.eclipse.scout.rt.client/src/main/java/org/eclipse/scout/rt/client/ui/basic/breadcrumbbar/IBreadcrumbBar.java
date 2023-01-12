/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.breadcrumbbar;

import java.util.List;

import org.eclipse.scout.rt.client.ui.IWidget;

public interface IBreadcrumbBar extends IWidget {
  String PROP_BREADCRUMBS = "breadcrumbItems";

  List<IBreadcrumbItem> getBreadcrumbItems();

  void setBreadcrumbItems(List<IBreadcrumbItem> breadcrumbItems);

  IBreadcrumbItem getBreadcrumbItemFor(String value);
}
