/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.form.fields.tabbox;

import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.ui.rap.form.fields.IRwtScoutFormField;

public interface IRwtScoutTabBox extends IRwtScoutFormField<ITabBox> {

  String VARIANT_TABBOX_CONTAINER = "tabboxContainer";
  String VARIANT_TABBOX_BUTTON_ACTIVE = "tabboxButton-active";
  String VARIANT_TABBOX_BUTTON = "tabboxButton";
  String VARIANT_TABBOX_BUTTON_ACTIVE_MARKED = "tabboxButton-active-marked";
  String VARIANT_TABBOX_BUTTON_MARKED = "tabboxButton-marked";

}
