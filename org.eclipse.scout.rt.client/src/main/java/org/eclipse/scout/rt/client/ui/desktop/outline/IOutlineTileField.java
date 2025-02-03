/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public interface IOutlineTileField extends IFormField {

  String PROP_OUTLINE = "outline";

  void setOutline(IOutline outline);

  IOutline getOutline();
}
