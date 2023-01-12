/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.splitbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.AbstractSplitBox;

public abstract class AbstractSplitBoxExtension<OWNER extends AbstractSplitBox> extends AbstractCompositeFieldExtension<OWNER> implements ISplitBoxExtension<OWNER> {

  public AbstractSplitBoxExtension(OWNER owner) {
    super(owner);
  }
}
