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
package org.eclipse.scout.rt.client.ui.form.fields.treefield;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public interface ITreeField extends IFormField {

  /**
   * {@link ITree}
   */
  String PROP_TREE = "tree";

  /**
   * Tree contained in tree field. If the tree is an inner type, then init, dispose, load and store is handled by the
   * tree field.
   */
  ITree getTree();

  /**
   * Install a (new) tree into the tree field.
   * 
   * @param externallyManaged
   *          true means init, dispose, load and store are not handled by the tree field
   */
  void setTree(ITree newTree, boolean externallyManaged);

  /**
   * Populate tree with data from service all existing data in the tree is discarded
   * 
   * @see execFilterTreeNode
   */
  void loadRootNode();

  void loadChildNodes(ITreeNode parentNode);

  void doSave();

}
