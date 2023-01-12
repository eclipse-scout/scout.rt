/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;

public abstract class AbstractLabelFieldExtension<OWNER extends AbstractLabelField> extends AbstractValueFieldExtension<String, OWNER> implements ILabelFieldExtension<OWNER> {

  public AbstractLabelFieldExtension(OWNER owner) {
    super(owner);
  }
}
