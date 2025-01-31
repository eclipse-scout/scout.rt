/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.clipboardfield;

import java.util.Collection;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.clipboardfield.AbstractClipboardField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

public abstract class AbstractClipboardFieldExtension<OWNER extends AbstractClipboardField> extends AbstractValueFieldExtension<Collection<BinaryResource>, OWNER> implements IClipboardFieldExtension<OWNER> {

  /**
   * @param owner
   */
  public AbstractClipboardFieldExtension(OWNER owner) {
    super(owner);
  }
}
