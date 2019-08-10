/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
