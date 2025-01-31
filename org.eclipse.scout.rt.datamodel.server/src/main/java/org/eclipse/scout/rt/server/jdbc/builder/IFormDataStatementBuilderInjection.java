/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
