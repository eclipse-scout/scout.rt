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

import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.shared.data.model.DataModelConstants;

/**
 * Example handler that demonstrates custom part building for a full-text search field based on a oracle database with
 * full-text engine.
 * <p>
 * the part is registered with {@link FormDataStatementBuilder#setPartDefinition(Class, new
 * PartDefinition("LAST_NAME"))}
 */
public class ExampleFullTextPartDefinition extends BasicPartDefinition {

  /**
   * @param attribute
   */
  public ExampleFullTextPartDefinition(Class fieldType, String sqlAttribute) {
    super(fieldType, sqlAttribute, DataModelConstants.OPERATOR_NONE);
  }

  @Override
  protected String createInstanceImpl(FormDataStatementBuilder builder, List<Object> valueDatas, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) {
    String pattern = (String) bindValues.get(0);
    //generate a search patter from pattern, decorate and replace pattern
    //...
    String sqlAttribute = "CONTAINS(<attribute>" + this.getSqlAttribute() + "</attribute>,'" + pattern + "')>0";
    return builder.createSqlPart(DataModelConstants.AGGREGATION_NONE, sqlAttribute, getOperation(), null, null, isPlainBind(), parentAliasMap);
  }

}
