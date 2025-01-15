/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.tree.fixtures;

import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("2811b9e4-b6ca-485a-b1a7-6c9d93fd5a68")
public class TreeWithOneNode extends AbstractTree {

  @Override
  protected void execInitTree() {
    super.execInitTree();
    addChildNode(getRootNode(), new TreeNode());
  }
}
