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
package org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractCompositeFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;

public abstract class AbstractGroupBoxExtension<OWNER extends AbstractGroupBox> extends AbstractCompositeFieldExtension<OWNER> implements IGroupBoxExtension<OWNER> {

  /**
   * @param owner
   */
  public AbstractGroupBoxExtension(OWNER owner) {
    super(owner);
  }

}
