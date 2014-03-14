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
package org.eclipse.scout.rt.spec.client.gen.extract;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupCall;

/**
 * Extractor for the entity's documented type (name with link to the doc section where the type is explained).
 * <p>
 * The entity's class hierarchy will be searched bottom up for the first documented type. A documented type is a a class
 * with a {@link ClassId} annotation for which a doc-text with key <code>[classid]_name</code> is available.
 * 
 * @param <T>
 */
public class ColumnTypeExtractor extends LinkableTypeExtractor<IColumn<?>> {
  public static final String LINKS_TAG_NAME = "links";

  @Override
  public String getText(IColumn<?> column) {
    StringBuilder sb = new StringBuilder();
    sb.append(super.getText(column));
    if (column instanceof ISmartColumn) {
      sb.append(" (").append(getDetailText((ISmartColumn<?>) column)).append(")");
    }
    return sb.toString();
  }

  public String getDetailText(ISmartColumn<?> smartcolumn) {
    StringBuilder text = new StringBuilder();
    Class codeTypeClass = getCodeTypeClass(smartcolumn);
    if (codeTypeClass != null) {
      text.append(TEXTS.get("org.eclipse.scout.rt.spec.codetype")).append(": ");
      text.append(new LinkableTypeExtractor<ICodeType>(ICodeType.class, true).getText(getCodeTypeClass(smartcolumn)));
    }
    else if (smartcolumn.getLookupCall() != null) {
      text.append(TEXTS.get("org.eclipse.scout.rt.spec.lookupcall")).append(": ");
      text.append(new LinkableTypeExtractor<LookupCall>(LookupCall.class, true).getText(smartcolumn.getLookupCall()));
    }
    else {
      text.append(TEXTS.get("org.eclipse.scout.rt.spec.na"));
    }
    return text.toString();
  }

  protected Class getCodeTypeClass(ISmartColumn<?> smartcolumn) {
    Class codeTypeClass = smartcolumn.getCodeTypeClass();
    if (codeTypeClass == null) {
      LookupCall lookupCall = smartcolumn.getLookupCall();
      if (lookupCall instanceof CodeLookupCall) {
        codeTypeClass = ((CodeLookupCall) lookupCall).getCodeTypeClass();
      }
    }
    return codeTypeClass;
  }

}
