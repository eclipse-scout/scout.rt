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
package org.eclipse.scout.rt.server.jdbc.builder;

import org.eclipse.scout.rt.server.jdbc.builder.FormDataStatementBuilder.AttributeStrategy;
import org.eclipse.scout.rt.server.jdbc.builder.FormDataStatementBuilder.EntityStrategy;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;

/**
 * see {@link FormDataStatementBuilder#addFormDataStatementBuilderInjection(IFormDataStatementBuilderInjection)}
 *
 * @since 3.8
 */
public interface IFormDataStatementBuilderInjection {

  /**
   * This method is called in
   * {@link FormDataStatementBuilder#buildComposerEntityNodeContribution(ComposerEntityNodeData, EntityStrategy)}
   * <p>
   * The {@link EntityContribution} is the life object with all <em>child</em> parts.
   * <p>
   * The contents can be manipulated, be careful what to change.
   */
  void preBuildEntity(ComposerEntityNodeData node, EntityStrategy entityStrategy, EntityContribution childContributions);

  /**
   * This method is called in
   * {@link FormDataStatementBuilder#buildComposerEntityNodeContribution(ComposerEntityNodeData, EntityStrategy)}
   * <p>
   * The {@link EntityContribution} is the life object with all <em>entity</em> parts after the child parts have been
   * merged with the entity itself.
   * <p>
   * The contents can be manipulated, be careful what to change.
   */
  void postBuildEntity(ComposerEntityNodeData node, EntityStrategy entityStrategy, EntityContribution entityContribution);

  /**
   * This method is called in
   * {@link FormDataStatementBuilder#buildComposerAttributeNode(ComposerAttributeNodeData, AttributeStrategy)}
   * <p>
   * The {@link EntityContribution} is the life object.
   * <p>
   * The contents can be manipulated, be careful what to change.
   */
  void postBuildAttribute(ComposerAttributeNodeData node, AttributeStrategy attributeStrategy, EntityContribution contrib);

}
