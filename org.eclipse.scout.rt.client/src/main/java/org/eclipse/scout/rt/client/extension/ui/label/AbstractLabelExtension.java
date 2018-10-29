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
package org.eclipse.scout.rt.client.extension.ui.label;

import org.eclipse.scout.rt.client.extension.ui.label.LabelChains.LabelAppLinkActionChain;
import org.eclipse.scout.rt.client.ui.label.AbstractLabel;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractLabelExtension<OWNER extends AbstractLabel> extends AbstractExtension<OWNER> implements ILabelExtension<OWNER> {

  public AbstractLabelExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execAppLinkAction(LabelAppLinkActionChain chain, String ref) {
    chain.execAppLinkAction(ref);
  }
}
