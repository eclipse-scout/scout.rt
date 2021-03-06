/*
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield.LabelFieldChains.LabelFieldAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;

public interface ILabelFieldExtension<OWNER extends AbstractLabelField> extends IValueFieldExtension<String, OWNER> {

  void execAppLinkAction(LabelFieldAppLinkActionChain chain, String ref);

}
