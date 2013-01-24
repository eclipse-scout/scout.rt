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
package org.eclipse.scout.rt.shared.services.lookup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;

public class AbstractCodeTypeLookupCall extends LocalLookupCall {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCodeTypeLookupCall.class);
  private static final long serialVersionUID = 1L;

  private String m_bundlePrefix;

  /**
   * @return the bundlePrefix
   */
  public String getBundlePrefix() {
    return m_bundlePrefix;
  }

  /**
   * @param bundlePrefix
   *          the bundlePrefix to set
   */
  public void setBundlePrefix(String bundlePrefix) {
    m_bundlePrefix = bundlePrefix;
  }

  @Override
  protected List<LookupRow> execCreateLookupRows() throws ProcessingException {
    ICodeType[] codeTypes = CODES.getAllCodeTypes(m_bundlePrefix);
    List<LookupRow> result = new ArrayList<LookupRow>();
    for (ICodeType type : codeTypes) {
      result.add(new LookupRow(type.getId(), type.getText()));
    }

    return result;
  }

  @Override
  public LookupRow[] getDataByKey() throws ProcessingException {
    ICodeType codeType = CODES.findCodeTypeById(getKey());
    if (codeType != null) {
      LookupRow[] result = new LookupRow[1];
      result[0] = new LookupRow(codeType.getId(), codeType.getText());
      return result;

    }
    return null;
  }
}
