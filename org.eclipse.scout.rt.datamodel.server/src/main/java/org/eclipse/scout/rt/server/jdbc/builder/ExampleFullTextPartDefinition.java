/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.jdbc.builder;

import java.util.List;
import java.util.Map;

/**
 * Example handler that demonstrates custom part building for a full-text search field based on a oracle database with
 * full-text engine.
 * <p>
 * the part is registered with
 * <code>FormDataStatementBuilder.setBasicDefinition(Class, new PartDefinition("LAST_NAME"))</code> see
 * {@link FormDataStatementBuilder#setBasicDefinition(Class, String, int)}
 */
public class ExampleFullTextPartDefinition extends BasicPartDefinition {

  /**
   * @param fieldType
   * @param sqlAttribute
   */
  public ExampleFullTextPartDefinition(Class fieldType, String sqlAttribute) {
    super(fieldType, sqlAttribute, OPERATOR_NONE);
  }

  @Override
  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  protected String createInstanceImpl(FormDataStatementBuilder builder, List<Object> valueDatas, List<String> bindNames, List<Object> bindValues, Map<String, String> parentAliasMap) {
    String pattern = (String) bindValues.get(0);
    //generate a search patter from pattern, decorate and replace pattern
    //...
    String sqlAttribute = "CONTAINS(<attribute>" + this.getSqlAttribute() + "</attribute>,'" + pattern + "')>0";
    return builder.createSqlPart(AGGREGATION_NONE, sqlAttribute, getOperation(), null, null, isPlainBind(), parentAliasMap);
  }
}
