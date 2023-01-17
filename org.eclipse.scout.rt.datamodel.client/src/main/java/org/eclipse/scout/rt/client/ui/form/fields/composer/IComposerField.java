/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IResettableFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

/**
 * remove {@link IResettableFormField} when removing this class
 */
public interface IComposerField extends IFormField, IResettableFormField {

  IDataModel getDataModel();

  void setDataModel(IDataModel dm);

  IComposerFieldUIFacade getUIFacade();

  ITree getTree();

  /**
   * convenience for getDataModel().getAttributes()
   */
  List<IDataModelAttribute> getAttributes();

  /**
   * convenience for getDataModel().getEntities()
   */
  List<IDataModelEntity> getEntities();

  EntityNode addEntityNode(ITreeNode parentNode, IDataModelEntity e, boolean negated, List<?> values, List<String> texts);

  AttributeNode addAttributeNode(ITreeNode parentNode, IDataModelAttribute a, Integer aggregationType, IDataModelAttributeOp op, List<?> values, List<String> texts);

  EitherOrNode addEitherNode(ITreeNode parentNode, boolean negated);

  EitherOrNode addAdditionalOrNode(ITreeNode eitherOrNode, boolean negated);

  /**
   * set field value to initValue and clear all error flags
   */
  @Override
  void resetValue();
}
