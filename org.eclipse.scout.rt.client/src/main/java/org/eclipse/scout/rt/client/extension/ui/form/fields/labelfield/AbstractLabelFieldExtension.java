/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.form.fields.labelfield;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.AbstractLabelField;

public abstract class AbstractLabelFieldExtension<OWNER extends AbstractLabelField> extends AbstractValueFieldExtension<String, OWNER> implements ILabelFieldExtension<OWNER> {

  public AbstractLabelFieldExtension(OWNER owner) {
    super(owner);
  }
}
