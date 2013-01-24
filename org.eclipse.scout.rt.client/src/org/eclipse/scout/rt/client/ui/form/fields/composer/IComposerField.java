/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.composer;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttributeOp;
import org.eclipse.scout.rt.shared.data.model.IDataModelEntity;

public interface IComposerField extends IFormField {

  IDataModel getDataModel();

  void setDataModel(IDataModel dm);

  IComposerFieldUIFacade getUIFacade();

  ITree getTree();

  /**
   * convenience for getDataModel().getAttributes()
   */
  IDataModelAttribute[] getAttributes();

  /**
   * convenience for getDataModel().getEntities()
   */
  IDataModelEntity[] getEntities();

  EntityNode addEntityNode(ITreeNode parentNode, IDataModelEntity e, boolean negated, Object[] values, String[] texts);

  AttributeNode addAttributeNode(ITreeNode parentNode, IDataModelAttribute a, Integer aggregationType, IDataModelAttributeOp op, Object[] values, String[] texts);

  EitherOrNode addEitherNode(ITreeNode parentNode, boolean negated);

  EitherOrNode addAdditionalOrNode(ITreeNode eitherOrNode, boolean negated);

  /**
   * set field value to initValue and clear all error flags
   */
  void resetValue();
}
