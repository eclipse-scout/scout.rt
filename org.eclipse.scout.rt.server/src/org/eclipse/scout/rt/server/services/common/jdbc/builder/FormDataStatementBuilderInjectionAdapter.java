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
package org.eclipse.scout.rt.server.services.common.jdbc.builder;

import org.eclipse.scout.rt.server.services.common.jdbc.builder.FormDataStatementBuilder.AttributeStrategy;
import org.eclipse.scout.rt.server.services.common.jdbc.builder.FormDataStatementBuilder.EntityStrategy;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerAttributeNodeData;
import org.eclipse.scout.rt.shared.data.form.fields.composer.ComposerEntityNodeData;

/**
 * see {@link FormDataStatementBuilder#addFormDataStatementBuilderInjection(EntityContributionInjectionAdapter)}
 * 
 * @author imo
 * @since 3.8
 */
public class FormDataStatementBuilderInjectionAdapter implements IFormDataStatementBuilderInjection {

  @Override
  public void preBuildEntity(ComposerEntityNodeData node, EntityStrategy entityStrategy, EntityContribution childContributions) {
  }

  @Override
  public void postBuildEntity(ComposerEntityNodeData node, EntityStrategy entityStrategy, EntityContribution entityContribution) {
  }

  @Override
  public void postBuildAttribute(ComposerAttributeNodeData node, AttributeStrategy attributeStrategy, EntityContribution contrib) {
  }

}
