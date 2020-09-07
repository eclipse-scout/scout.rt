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
package org.eclipse.scout.rt.client.ui.form.fields.breadcrumbbarfield;

import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbBar;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public interface IBreadcrumbBarField extends IFormField {

  public static final String PROP_BREADCRUMB_BAR = "breadcrumbBar";

  IBreadcrumbBar getBreadcrumbBar();

  void setBreadcrumbBar(IBreadcrumbBar bar);
}
