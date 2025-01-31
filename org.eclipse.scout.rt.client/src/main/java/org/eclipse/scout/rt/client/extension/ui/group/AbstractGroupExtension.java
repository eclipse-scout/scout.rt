/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.group;

import org.eclipse.scout.rt.client.extension.ui.group.GroupChains.GroupDisposeGroupChain;
import org.eclipse.scout.rt.client.extension.ui.group.GroupChains.GroupInitGroupChain;
import org.eclipse.scout.rt.client.ui.group.AbstractGroup;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractGroupExtension<OWNER_FIELD extends AbstractGroup> extends AbstractExtension<OWNER_FIELD>
    implements IGroupExtension<OWNER_FIELD> {

  public AbstractGroupExtension(OWNER_FIELD owner) {
    super(owner);
  }

  @Override
  public void execDisposeGroup(GroupDisposeGroupChain chain) {
    chain.execDisposeGroup();
  }

  @Override
  public void execInitGroup(GroupInitGroupChain chain) {
    chain.execInitGroup();
  }
}
