/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
   *     true means init, dispose, load and store are not handled by the tree field
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
