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

import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.composer.attribute.IComposerAttribute;
import org.eclipse.scout.rt.client.ui.form.fields.composer.entity.IComposerEntity;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.AttributeNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EitherOrNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.node.EntityNode;
import org.eclipse.scout.rt.client.ui.form.fields.composer.operator.IComposerOp;
import org.eclipse.scout.rt.shared.data.form.fields.composer.AbstractComposerAttributeData;

public interface IComposerField extends IFormField {

  IComposerFieldUIFacade getUIFacade();

  ITree getTree();

  IComposerAttribute[] getComposerAttributes();

  IComposerEntity[] getComposerEntities();

  EntityNode addEntityNode(ITreeNode parentNode, IComposerEntity e, boolean negated, Object[] values, String[] texts);

  AttributeNode addAttributeNode(ITreeNode parentNode, IComposerAttribute a, Integer aggregationType, IComposerOp op, Object[] values, String[] texts);

  EitherOrNode addEitherNode(ITreeNode parentNode, boolean negated);

  EitherOrNode addAdditionalOrNode(ITreeNode eitherOrNode, boolean negated);

  /**
   * @return meta data for the attribute, default returns null
   *         <p>
   *         see {@link ComposerFieldUtility}
   */
  Map<String, String> getMetaDataOfAttribute(IComposerAttribute a);

  /**
   * @return meta data for the attribute, default returns null
   *         <p>
   *         see {@link ComposerFieldUtility}
   * @param values
   *          of the node containing the attribute; this may contain some meta data relevant for dynamic attributes
   */
  Map<String, String> getMetaDataOfAttributeData(AbstractComposerAttributeData a, Object[] values);

  /**
   * set field value to initValue and clear all error flags
   */
  void resetValue();
}
