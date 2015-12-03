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
package org.eclipse.scout.rt.client.extension.ui.form.fields.splitbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.AbstractSplitBox;

public abstract class AbstractSplitBoxExtension<OWNER extends AbstractSplitBox> extends AbstractCompositeFieldExtension<OWNER> implements ISplitBoxExtension<OWNER> {

  public AbstractSplitBoxExtension(OWNER owner) {
    super(owner);
  }
}
