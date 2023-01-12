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
import org.eclipse.scout.rt.shared.extension.IExtension;

public interface IGroupExtension<OWNER extends AbstractGroup> extends IExtension<OWNER> {

  void execInitGroup(GroupInitGroupChain chain);

  void execDisposeGroup(GroupDisposeGroupChain chain);
}
