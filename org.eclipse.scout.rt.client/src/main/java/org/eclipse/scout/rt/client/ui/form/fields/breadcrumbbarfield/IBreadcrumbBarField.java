/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.form.fields.breadcrumbbarfield;

import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbBar;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public interface IBreadcrumbBarField extends IFormField {

  public static final String PROP_BREADCRUMB_BAR = "breadcrumbBar";

  IBreadcrumbBar getBreadcrumbBar();

  void setBreadcrumbBar(IBreadcrumbBar bar);
}
