/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.lookup.fixture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeVisitor;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCallTest;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * This is an old version of the {@code CodeLookupCall} class required only for unit tests.
 * 
 * @see CodeLookupCallTest
 */
public class LegacyCodeLookupCall extends LocalLookupCall implements Serializable {
  private static final long serialVersionUID = 0L;

  private Class<? extends ICodeType> m_codeTypeClass;
  private ILegacyCodeLookupCallVisitor m_filter;

  public LegacyCodeLookupCall(Class<? extends ICodeType> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    LegacyCodeLookupCall other = (LegacyCodeLookupCall) obj;
    if (this.m_codeTypeClass != other.m_codeTypeClass) {
      return false;
    }
    if (this.m_filter != other.m_filter) {
      return false;
    }
    return true;
  }

  public Class<? extends ICodeType> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  public void setFilter(ILegacyCodeLookupCallVisitor filter) {
    m_filter = filter;
  }

  public ILegacyCodeLookupCallVisitor getFilter() {
    return m_filter;
  }

  /**
   * Implementation that creates lookup rows from codes.
   * <p>
   * By default calls {@link #createLookupRowArray(List)}
   */
  protected LookupRow[] execCreateLookupRowsFromCodes(List<? extends ICode<?>> codes) {
    return createLookupRowArray(codes);
  }

  /**
   * Default implementation to create lookup rows from codes.
   * <p>
   * Called by {@link #execCreateLookupRowsFromCodes(List)}.
   */
  public static LookupRow[] createLookupRowArray(List<? extends ICode> codes) {
    LookupRow[] a = new LookupRow[codes.size()];
    for (int i = 0; i < a.length; i++) {
      ICode c = codes.get(i);
      Object parentId = null;
      if (c.getParentCode() != null) {
        parentId = c.getParentCode().getId();
      }
      a[i] = new LookupRow(c.getId(), c.getText(), c.getIconId(), c.getTooltipText(), c.getBackgroundColor(), c.getForegroundColor(), c.getFont(), c.isEnabled(), parentId, c.isActive());
    }
    return a;
  }

  public static Pattern getSearchPattern(String s) {
    if (s == null) {
      s = "";
    }
    s = s.toLowerCase();
    if (!s.endsWith("*")) {
      s = s + "*";
    }
    return Pattern.compile(StringUtility.toRegExPattern(s), Pattern.DOTALL);
  }

  /**
   * Complete override using code data
   */
  @Override
  public LookupRow[] getDataByKey() throws ProcessingException {
    Object key = getKey();
    ArrayList<ICode<?>> list = new ArrayList<ICode<?>>(1);
    ICodeType t = CODES.getCodeType(m_codeTypeClass);
    if (t != null) {
      ICode c = t.getCode(key);
      if (c != null) {
        list.add(c);
      }
    }
    return execCreateLookupRowsFromCodes(list);
  }

  /**
   * Complete override using code data
   */
  @Override
  public LookupRow[] getDataByText() throws ProcessingException {
    final Pattern pat = getSearchPattern(getText());
    P_AbstractCollectingCodeVisitor v = new P_AbstractCollectingCodeVisitor() {
      @Override
      public boolean visit(ICode code, int treeLevel) {
        if (m_filter != null && !m_filter.visit(LegacyCodeLookupCall.this, code, treeLevel)) {
          return true;
        }
        if (getActive().isUndefined() || getActive().getBooleanValue() == code.isActive()) {
          if (code.getText() != null) {
            if (pat.matcher(code.getText().toLowerCase()).matches()) {
              add(code);
            }
          }
        }
        return true;
      }
    };
    ICodeType t = CODES.getCodeType(m_codeTypeClass);
    if (t != null) {
      t.visit(v, false);
    }
    return execCreateLookupRowsFromCodes(v.getCodes());
  }

  /**
   * Complete override using code data
   */
  @Override
  public LookupRow[] getDataByAll() throws ProcessingException {
    final Pattern pat = getSearchPattern(getAll());
    P_AbstractCollectingCodeVisitor v = new P_AbstractCollectingCodeVisitor() {
      @Override
      public boolean visit(ICode code, int treeLevel) {
        if (m_filter != null && !m_filter.visit(LegacyCodeLookupCall.this, code, treeLevel)) {
          return true;
        }
        if (getActive().isUndefined() || getActive().getBooleanValue() == code.isActive()) {
          if (code.getText() != null) {
            if (pat.matcher(code.getText().toLowerCase()).matches()) {
              add(code);
            }
          }
        }
        return true;
      }
    };
    ICodeType t = CODES.getCodeType(m_codeTypeClass);
    if (t != null) {
      t.visit(v, false);
    }
    return execCreateLookupRowsFromCodes(v.getCodes());
  }

  /**
   * Complete override using code data
   */
  @Override
  public LookupRow[] getDataByRec() throws ProcessingException {
    Object recValue = getRec();
    if ((recValue instanceof Number) && ((Number) recValue).longValue() == 0) {
      recValue = null;
    }
    final Object key = recValue;
    P_AbstractCollectingCodeVisitor v = new P_AbstractCollectingCodeVisitor() {
      @Override
      public boolean visit(ICode code, int treeLevel) {
        if (m_filter != null && !m_filter.visit(LegacyCodeLookupCall.this, code, treeLevel)) {
          return true;
        }
        ICode parentCode = code.getParentCode();
        if (getActive().isUndefined() || getActive().getBooleanValue() == code.isActive()) {
          if (((parentCode == null && key == null) || (parentCode != null && parentCode.getId() != null && parentCode.getId().equals(key)))) {
            add(code);
          }
        }
        return true;
      }
    };
    ICodeType t = CODES.getCodeType(m_codeTypeClass);
    if (t != null) {
      t.visit(v, false);
    }
    return execCreateLookupRowsFromCodes(v.getCodes());
  }

  private static abstract class P_AbstractCollectingCodeVisitor implements ICodeVisitor {
    private ArrayList<ICode<?>> m_list = new ArrayList<ICode<?>>();

    public P_AbstractCollectingCodeVisitor() {
    }

    public void add(ICode code) {
      m_list.add(code);
    }

    public ICode getFirstCode() {
      if (m_list.size() > 0) {
        return m_list.get(0);
      }
      else {
        return null;
      }
    }

    public List<ICode<?>> getCodes() {
      return m_list;
    }
  }
}
