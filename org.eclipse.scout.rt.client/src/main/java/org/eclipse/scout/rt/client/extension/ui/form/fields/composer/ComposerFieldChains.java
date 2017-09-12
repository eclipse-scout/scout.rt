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
package org.eclipse.scout.rt.client.extension.ui.form.fields.composer;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.AbstractComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.RootNode;
import org.eclipse.scout.rt.shared.data.model.AttributePath;
import org.eclipse.scout.rt.shared.data.model.EntityPath;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class ComposerFieldChains {

  private ComposerFieldChains() {
  }

  protected abstract static class AbstractComposerFieldChain extends AbstractExtensionChain<IComposerFieldExtension<? extends AbstractComposerField>> {

    public AbstractComposerFieldChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions, IComposerFieldExtension.class);
    }
  }

  public static class ComposerFieldResolveEntityPathChain extends AbstractComposerFieldChain {

    public ComposerFieldResolveEntityPathChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public EntityPath execResolveEntityPath(final EntityNode node) {
      MethodInvocation<EntityPath> methodInvocation = new MethodInvocation<EntityPath>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          setReturnValue(next.execResolveEntityPath(ComposerFieldResolveEntityPathChain.this, node));
        }
      };
      callChain(methodInvocation, node);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ComposerFieldResolveRootPathForTopLevelEntityChain extends AbstractComposerFieldChain {

    public ComposerFieldResolveRootPathForTopLevelEntityChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execResolveRootPathForTopLevelEntity(final IDataModelEntity e, final List<IDataModelEntity> lifeList) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          next.execResolveRootPathForTopLevelEntity(ComposerFieldResolveRootPathForTopLevelEntityChain.this, e, lifeList);
        }
      };
      callChain(methodInvocation, e, lifeList);
    }
  }

  public static class ComposerFieldCreateRootNodeChain extends AbstractComposerFieldChain {

    public ComposerFieldCreateRootNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public RootNode execCreateRootNode() {
      MethodInvocation<RootNode> methodInvocation = new MethodInvocation<RootNode>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          setReturnValue(next.execCreateRootNode(ComposerFieldCreateRootNodeChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ComposerFieldResolveAttributePathChain extends AbstractComposerFieldChain {

    public ComposerFieldResolveAttributePathChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public AttributePath execResolveAttributePath(final AttributeNode node) {
      MethodInvocation<AttributePath> methodInvocation = new MethodInvocation<AttributePath>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          setReturnValue(next.execResolveAttributePath(ComposerFieldResolveAttributePathChain.this, node));
        }
      };
      callChain(methodInvocation, node);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ComposerFieldCreateAttributeNodeChain extends AbstractComposerFieldChain {

    public ComposerFieldCreateAttributeNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public AttributeNode execCreateAttributeNode(final ITreeNode parentNode, final IDataModelAttribute a, final Integer aggregationType, final IDataModelAttributeOp op, final List<?> values, final List<String> texts) {
      MethodInvocation<AttributeNode> methodInvocation = new MethodInvocation<AttributeNode>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          setReturnValue(next.execCreateAttributeNode(ComposerFieldCreateAttributeNodeChain.this, parentNode, a, aggregationType, op, values, texts));
        }
      };
      callChain(methodInvocation, parentNode, a, aggregationType, op, values, texts);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ComposerFieldCreateDataModelChain extends AbstractComposerFieldChain {

    public ComposerFieldCreateDataModelChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public IDataModel execCreateDataModel() {
      MethodInvocation<IDataModel> methodInvocation = new MethodInvocation<IDataModel>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          setReturnValue(next.execCreateDataModel(ComposerFieldCreateDataModelChain.this));
        }
      };
      callChain(methodInvocation);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ComposerFieldCreateEitherNodeChain extends AbstractComposerFieldChain {

    public ComposerFieldCreateEitherNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public EitherOrNode execCreateEitherNode(final ITreeNode parentNode, final boolean negated) {
      MethodInvocation<EitherOrNode> methodInvocation = new MethodInvocation<EitherOrNode>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          setReturnValue(next.execCreateEitherNode(ComposerFieldCreateEitherNodeChain.this, parentNode, negated));
        }
      };
      callChain(methodInvocation, parentNode, negated);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ComposerFieldResolveRootPathForTopLevelAttributeChain extends AbstractComposerFieldChain {

    public ComposerFieldResolveRootPathForTopLevelAttributeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public void execResolveRootPathForTopLevelAttribute(final IDataModelAttribute a, final List<IDataModelEntity> lifeList) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          next.execResolveRootPathForTopLevelAttribute(ComposerFieldResolveRootPathForTopLevelAttributeChain.this, a, lifeList);
        }
      };
      callChain(methodInvocation, a, lifeList);
    }
  }

  public static class ComposerFieldCreateAdditionalOrNodeChain extends AbstractComposerFieldChain {

    public ComposerFieldCreateAdditionalOrNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public EitherOrNode execCreateAdditionalOrNode(final ITreeNode eitherOrNode, final boolean negated) {
      MethodInvocation<EitherOrNode> methodInvocation = new MethodInvocation<EitherOrNode>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          setReturnValue(next.execCreateAdditionalOrNode(ComposerFieldCreateAdditionalOrNodeChain.this, eitherOrNode, negated));
        }
      };
      callChain(methodInvocation, eitherOrNode, negated);
      return methodInvocation.getReturnValue();
    }
  }

  public static class ComposerFieldCreateEntityNodeChain extends AbstractComposerFieldChain {

    public ComposerFieldCreateEntityNodeChain(List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions) {
      super(extensions);
    }

    public EntityNode execCreateEntityNode(final ITreeNode parentNode, final IDataModelEntity e, final boolean negated, final List<?> values, final List<String> texts) {
      MethodInvocation<EntityNode> methodInvocation = new MethodInvocation<EntityNode>() {
        @Override
        protected void callMethod(IComposerFieldExtension<? extends AbstractComposerField> next) {
          setReturnValue(next.execCreateEntityNode(ComposerFieldCreateEntityNodeChain.this, parentNode, e, negated, values, texts));
        }
      };
      callChain(methodInvocation, parentNode, e, negated, values, texts);
      return methodInvocation.getReturnValue();
    }
  }
}
