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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeVisitor;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCallTest;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LocalLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;

/**
 * This is an old version of the {@code CodeLookupCall} class required only for unit tests.
 *
 * @see CodeLookupCallTest
 */
public class LegacyCodeLookupCall<CODE_ID_TYPE> extends LocalLookupCall<CODE_ID_TYPE> implements Serializable {
  private static final long serialVersionUID = 0L;

  private Class<? extends ICodeType<?, CODE_ID_TYPE>> m_codeTypeClass;
  private ILegacyCodeLookupCallVisitor<CODE_ID_TYPE> m_filter;

  public LegacyCodeLookupCall(Class<? extends ICodeType<?, CODE_ID_TYPE>> codeTypeClass) {
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

  public Class<? extends ICodeType<?, CODE_ID_TYPE>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  public void setFilter(ILegacyCodeLookupCallVisitor<CODE_ID_TYPE> filter) {
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
  protected List<ILookupRow<CODE_ID_TYPE>> execCreateLookupRowsFromCodes(List<? extends ICode<CODE_ID_TYPE>> codes) {
    return createLookupRowArray(codes);
  }

  /**
   * Default implementation to create lookup rows from codes.
   * <p>
   * Called by {@link #execCreateLookupRowsFromCodes(List)}.
   */
  protected List<ILookupRow<CODE_ID_TYPE>> createLookupRowArray(List<? extends ICode<CODE_ID_TYPE>> codes) {
    List<ILookupRow<CODE_ID_TYPE>> rows = new ArrayList<ILookupRow<CODE_ID_TYPE>>();
    for (ICode<CODE_ID_TYPE> code : codes) {
      CODE_ID_TYPE parentId = null;
      if (code.getParentCode() != null) {
        parentId = code.getParentCode().getId();
      }
      rows.add(new LookupRow<CODE_ID_TYPE>(code.getId(), code.getText())
          .withIconId(code.getIconId())
          .withTooltipText(code.getTooltipText())
          .withBackgroundColor(code.getBackgroundColor())
          .withForegroundColor(code.getForegroundColor())
          .withFont(code.getFont())
          .withEnabled(code.isEnabled())
          .withParentKey(parentId)
          .withActive(code.isActive()));
    }
    return rows;
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
  public List<ILookupRow<CODE_ID_TYPE>> getDataByKey() {
    CODE_ID_TYPE key = getKey();
    List<ICode<CODE_ID_TYPE>> list = new ArrayList<ICode<CODE_ID_TYPE>>(1);
    ICodeType<?, CODE_ID_TYPE> t = BEANS.opt(m_codeTypeClass);
    if (t != null) {
      ICode<CODE_ID_TYPE> c = t.getCode(key);
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
  public List<ILookupRow<CODE_ID_TYPE>> getDataByText() {
    final Pattern pat = getSearchPattern(getText());
    P_AbstractCollectingCodeVisitor v = new P_AbstractCollectingCodeVisitor() {
      @Override
      public boolean visit(ICode<CODE_ID_TYPE> code, int treeLevel) {
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
    ICodeType<?, CODE_ID_TYPE> t = BEANS.opt(m_codeTypeClass);
    if (t != null) {
      t.visit(v, false);
    }
    return execCreateLookupRowsFromCodes(v.getCodes());
  }

  /**
   * Complete override using code data
   */
  @Override
  public List<ILookupRow<CODE_ID_TYPE>> getDataByAll() {
    final Pattern pat = getSearchPattern(getAll());
    P_AbstractCollectingCodeVisitor v = new P_AbstractCollectingCodeVisitor() {
      @Override
      public boolean visit(ICode<CODE_ID_TYPE> code, int treeLevel) {
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
    ICodeType<?, CODE_ID_TYPE> t = BEANS.opt(m_codeTypeClass);
    if (t != null) {
      t.visit(v, false);
    }
    return execCreateLookupRowsFromCodes(v.getCodes());
  }

  /**
   * Complete override using code data
   */
  @Override
  public List<ILookupRow<CODE_ID_TYPE>> getDataByRec() {
    Object recValue = getRec();
    if ((recValue instanceof Number) && ((Number) recValue).longValue() == 0) {
      recValue = null;
    }
    final Object key = recValue;
    P_AbstractCollectingCodeVisitor v = new P_AbstractCollectingCodeVisitor() {
      @Override
      public boolean visit(ICode<CODE_ID_TYPE> code, int treeLevel) {
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
    ICodeType<?, CODE_ID_TYPE> t = BEANS.opt(m_codeTypeClass);
    if (t != null) {
      t.visit(v, false);
    }
    return execCreateLookupRowsFromCodes(v.getCodes());
  }

  private abstract class P_AbstractCollectingCodeVisitor implements ICodeVisitor<ICode<CODE_ID_TYPE>> {
    private final ArrayList<ICode<CODE_ID_TYPE>> m_list = new ArrayList<ICode<CODE_ID_TYPE>>();

    public P_AbstractCollectingCodeVisitor() {
    }

    public void add(ICode<CODE_ID_TYPE> code) {
      m_list.add(code);
    }

    public List<ICode<CODE_ID_TYPE>> getCodes() {
      return CollectionUtility.arrayList(m_list);
    }
  }
}
