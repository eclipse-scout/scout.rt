/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.filechooserbutton;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton.AbstractFileChooserButton;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

public abstract class AbstractFileChooserButtonExtension<OWNER extends AbstractFileChooserButton> extends AbstractValueFieldExtension<BinaryResource, OWNER> implements IFileChooserButtonExtension<OWNER> {

  public AbstractFileChooserButtonExtension(OWNER owner) {
    super(owner);
  }
}
