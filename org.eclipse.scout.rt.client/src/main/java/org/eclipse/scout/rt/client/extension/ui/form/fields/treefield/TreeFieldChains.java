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
package org.eclipse.scout.rt.client.extension.ui.form.fields.treefield;

import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.AbstractTreeField;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class TreeFieldChains {

  private TreeFieldChains() {
  }

  protected abstract static class AbstractTreeFieldChain extends AbstractExtensionChain<ITreeFieldExtension<? extends AbstractTreeField>> {

    public AbstractTreeFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, ITreeFieldExtension.class);
    }
  }

  public static class TreeFieldSaveChain extends AbstractTreeFieldChain {

    public TreeFieldSaveChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSave(final Collection<? extends ITreeNode> insertedNodes, final Collection<? extends ITreeNode> updatedNodes, final Collection<? extends ITreeNode> deletedNodes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) {
          next.execSave(TreeFieldSaveChain.this, insertedNodes, updatedNodes, deletedNodes);
        }
      };
      callChain(methodInvocation, insertedNodes, updatedNodes, deletedNodes);
    }
  }

  public static class TreeFieldSaveDeletedNodeChain extends AbstractTreeFieldChain {

    public TreeFieldSaveDeletedNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveDeletedNode(final ITreeNode row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) {
          next.execSaveDeletedNode(TreeFieldSaveDeletedNodeChain.this, row);
        }
      };
      callChain(methodInvocation, row);
    }
  }

  public static class TreeFieldSaveUpdatedNodeChain extends AbstractTreeFieldChain {

    public TreeFieldSaveUpdatedNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveUpdatedNode(final ITreeNode row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) {
          next.execSaveUpdatedNode(TreeFieldSaveUpdatedNodeChain.this, row);
        }
      };
      callChain(methodInvocation, row);
    }
  }

  public static class TreeFieldLoadChildNodesChain extends AbstractTreeFieldChain {

    public TreeFieldLoadChildNodesChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execLoadChildNodes(final ITreeNode parentNode) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) {
          next.execLoadChildNodes(TreeFieldLoadChildNodesChain.this, parentNode);
        }
      };
      callChain(methodInvocation, parentNode);
    }
  }

  public static class TreeFieldSaveInsertedNodeChain extends AbstractTreeFieldChain {

    public TreeFieldSaveInsertedNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execSaveInsertedNode(final ITreeNode row) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ITreeFieldExtension<? extends AbstractTreeField> next) {
          next.execSaveInsertedNode(TreeFieldSaveInsertedNodeChain.this, row);
        }
      };
      callChain(methodInvocation, row);
    }
  }
}
