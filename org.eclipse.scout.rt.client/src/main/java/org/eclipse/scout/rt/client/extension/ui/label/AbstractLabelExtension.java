/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
