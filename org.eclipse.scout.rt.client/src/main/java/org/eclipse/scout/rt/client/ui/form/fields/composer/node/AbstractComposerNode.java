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
package org.eclipse.scout.rt.client.ui.form.fields.composer.node;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * Control Structure node for NOT, OR, AND, etc.
 * <p>
 * Example of an extensive company search formula:
 *
 * <pre>
 * EITHER
 *   name starts with "abb"
 *   AND name ends with "ch"
 *   AND has Region with
 *     country is Switzerland
 * OR
 *   name starts with "bsi"
 *   AND name ends with "us"
 * </pre>
 */
public abstract class AbstractComposerNode extends AbstractTreeNode {
  private final IComposerField m_composerField;

  public AbstractComposerNode(IComposerField composerField, boolean callInitializer) {
    super(false);
    m_composerField = composerField;
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public boolean isLeaf() {
    return getChildNodeCount() == 0;
  }

  protected ITreeNode getSiblingBefore() {
    if (getTree() != null && getParentNode() != null) {
      int index = getChildNodeIndex();
      if (index - 1 >= 0) {
        return getParentNode().getChildNode(index - 1);
      }
    }
    return null;
  }

  protected ITreeNode getSiblingAfter() {
    if (getTree() != null && getParentNode() != null) {
      int index = getChildNodeIndex();
      int count = getParentNode().getChildNodeCount();
      if (index + 1 < count) {
        return getParentNode().getChildNode(index + 1);
      }
    }
    return null;
  }

  public IComposerField getComposerField() {
    return m_composerField;
  }

  protected void attachAddEntityMenus(Collection<IMenu> menus) {
    EntityNode eNode = null;
    ITreeNode n = this;
    while (n != null) {
      if (n instanceof EntityNode) {
        eNode = (EntityNode) n;
        break;
      }
      n = n.getParentNode();
    }

    List<IDataModelEntity> childEntitites;
    if (eNode != null) {
      childEntitites = eNode.getEntity().getEntities();
    }
    else {
      childEntitites = getComposerField().getEntities();
    }
    for (IDataModelEntity e : childEntitites) {
      menus.add(new AddEntityMenu(getComposerField(), this, e));
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getCell().getText() + "]";
  }

}
