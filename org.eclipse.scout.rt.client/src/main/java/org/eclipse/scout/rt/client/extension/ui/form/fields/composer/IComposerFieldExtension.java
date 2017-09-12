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
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateAdditionalOrNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateAttributeNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateDataModelChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateEitherNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateEntityNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldCreateRootNodeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldResolveAttributePathChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldResolveEntityPathChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldResolveRootPathForTopLevelAttributeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.composer.ComposerFieldChains.ComposerFieldResolveRootPathForTopLevelEntityChain;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
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

public interface IComposerFieldExtension<OWNER extends AbstractComposerField> extends IFormFieldExtension<OWNER> {

  EntityPath execResolveEntityPath(ComposerFieldResolveEntityPathChain chain, EntityNode node);

  void execResolveRootPathForTopLevelEntity(ComposerFieldResolveRootPathForTopLevelEntityChain chain, IDataModelEntity e, List<IDataModelEntity> lifeList);

  RootNode execCreateRootNode(ComposerFieldCreateRootNodeChain chain);

  AttributePath execResolveAttributePath(ComposerFieldResolveAttributePathChain chain, AttributeNode node);

  AttributeNode execCreateAttributeNode(ComposerFieldCreateAttributeNodeChain chain, ITreeNode parentNode, IDataModelAttribute a, Integer aggregationType, IDataModelAttributeOp op, List<?> values, List<String> texts);

  IDataModel execCreateDataModel(ComposerFieldCreateDataModelChain chain);

  EitherOrNode execCreateEitherNode(ComposerFieldCreateEitherNodeChain chain, ITreeNode parentNode, boolean negated);

  void execResolveRootPathForTopLevelAttribute(ComposerFieldResolveRootPathForTopLevelAttributeChain chain, IDataModelAttribute a, List<IDataModelEntity> lifeList);

  EitherOrNode execCreateAdditionalOrNode(ComposerFieldCreateAdditionalOrNodeChain chain, ITreeNode eitherOrNode, boolean negated);

  EntityNode execCreateEntityNode(ComposerFieldCreateEntityNodeChain chain, ITreeNode parentNode, IDataModelEntity e, boolean negated, List<?> values, List<String> texts);
}
