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

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield.LabelFieldChains.LabelFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;

public interface ILabelFieldExtension<OWNER extends AbstractLabelField> extends IValueFieldExtension<String, OWNER> {

  void execAppLinkAction(LabelFieldAppLinkActionChain chain, String ref);
}
