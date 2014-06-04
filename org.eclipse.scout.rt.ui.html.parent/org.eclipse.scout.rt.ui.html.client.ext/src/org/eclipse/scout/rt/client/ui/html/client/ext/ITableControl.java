/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.html.client.ext;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.rt.client.ui.form.IForm;

public interface ITableControl extends IPropertyObserver {
  String PROP_LABEL = "label";
  String PROP_SELECTED = "selected";
  String PROP_FORM = "form";
  String PROP_ENABLED = "enabled";

  String getLabel();

  boolean isSelected();

  IForm getForm();

  //FIXME CGU better use icon instead?
  String getCssClass();

  String getGroup();

  boolean isEnabled();

  void fireActivatedFromUI();
}
