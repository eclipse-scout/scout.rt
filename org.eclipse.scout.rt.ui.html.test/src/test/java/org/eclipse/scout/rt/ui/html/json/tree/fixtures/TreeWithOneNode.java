/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
