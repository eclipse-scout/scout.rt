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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.code.CODES;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeVisitor;
import org.eclipse.scout.service.SERVICES;

/**
 * @see LookupCall
 */
@ClassId("bf3702b8-ee95-4c7b-870d-105b9d0deec2")
public class CodeLookupCall extends LocalLookupCall implements Serializable {
  private static final long serialVersionUID = 0L;

  /**
   * Helper method to create a lookup call from a codetype using the {@link ICodeLookupCallFactoryService}.
   */
  public static CodeLookupCall newInstanceByService(Class<? extends ICodeType> codeTypeClass) {
    return SERVICES.getService(ICodeLookupCallFactoryService.class).newInstance(codeTypeClass);
  }

  private Class<? extends ICodeType> m_codeTypeClass;
  private ICodeLookupCallVisitor m_filter;
  private Comparator<LookupRow> m_sortComparator;

  public CodeLookupCall(Class<? extends ICodeType> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
  }

  public Comparator<LookupRow> getSortComparator() {
    return m_sortComparator;
  }

  public void setSortComparator(Comparator<LookupRow> comp) {
    m_sortComparator = comp;
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
    CodeLookupCall other = (CodeLookupCall) obj;
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

  public void setFilter(ICodeLookupCallVisitor filter) {
    m_filter = filter;
  }

  public ICodeLookupCallVisitor getFilter() {
    return m_filter;
  }

  /**
   * Implementation that creates lookup rows from codes.
   * <p>
   * By default calls {@link #execCreateLookupRowFromCode(ICode)}
   */
  protected LookupRow[] execCreateLookupRowsFromCodes(List<? extends ICode<?>> codes) {
    LookupRow[] a = new LookupRow[codes.size()];
    for (int i = 0; i < a.length; i++) {
      a[i] = execCreateLookupRowFromCode(codes.get(i));
    }
    return a;
  }

  /**
   * Implementation that creates lookup rows from codes.
   * <p>
   * By default calls {@link #createLookupRow(ICode)}
   */
  protected LookupRow execCreateLookupRowFromCode(ICode<?> code) {
    return createLookupRow(code);
  }

  /**
   * Default implementation to create lookup rows from codes.
   * <p>
   * Called by {@link #execCreateLookupRowsFromCodes(List)}.
   */
  public static LookupRow[] createLookupRowArray(List<? extends ICode> codes) {
    LookupRow[] a = new LookupRow[codes.size()];
    for (int i = 0; i < a.length; i++) {
      a[i] = createLookupRow(codes.get(i));
    }
    return a;
  }

  /**
   * Default implementation to create a lookup row from a code.
   * <p>
   * Called by {@link #createLookupRowArray(List)}.
   */
  public static LookupRow createLookupRow(ICode<?> c) {
    Object parentId = null;
    if (c.getParentCode() != null) {
      parentId = c.getParentCode().getId();
    }
    return new LookupRow(c.getId(), c.getText(), c.getIconId(), c.getTooltipText(), c.getBackgroundColor(), c.getForegroundColor(), c.getFont(), c.isEnabled(), parentId, c.isActive());
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
    ArrayList<ICode<?>> list = new ArrayList<ICode<?>>(1);
    ICode c = resolveCodeByKey();
    if (c != null) {
      list.add(c);
    }
    return execCreateLookupRowsFromCodes(list);
  }

  /**
   * Complete override using code data
   */
  @Override
  public LookupRow[] getDataByText() throws ProcessingException {
    final Pattern pat = getSearchPattern(getText());
    AbstractLookupRowCollector v = new AbstractLookupRowCollector() {
      @Override
      public boolean visit(ICode code, int treeLevel) {
        if (m_filter != null && !m_filter.visit(CodeLookupCall.this, code, treeLevel)) {
          return true;
        }
        if (getActive().isUndefined() || getActive().getBooleanValue() == code.isActive()) {
          LookupRow row = execCreateLookupRowFromCode(code);
          if (row != null && row.getText() != null && pat.matcher(row.getText().toLowerCase()).matches()) {
            add(row);
          }
        }
        return true;
      }
    };
    resolveCodes(v);
    List<LookupRow> result = v.getLookupRows();
    if (result.size() > 1) {
      Comparator<LookupRow> comparator = getSortComparator();
      if (comparator != null) {
        Collections.sort(result, comparator);
      }
    }
    return result.toArray(new LookupRow[result.size()]);
  }

  /**
   * Complete override using code data
   */
  @Override
  public LookupRow[] getDataByAll() throws ProcessingException {
    final Pattern pat = getSearchPattern(getAll());
    AbstractLookupRowCollector v = new AbstractLookupRowCollector() {
      @Override
      public boolean visit(ICode code, int treeLevel) {
        if (m_filter != null && !m_filter.visit(CodeLookupCall.this, code, treeLevel)) {
          return true;
        }
        if (getActive().isUndefined() || getActive().getBooleanValue() == code.isActive()) {
          LookupRow row = execCreateLookupRowFromCode(code);
          if (row != null && row.getText() != null && pat.matcher(row.getText().toLowerCase()).matches()) {
            add(row);
          }
        }
        return true;
      }
    };
    resolveCodes(v);
    List<LookupRow> result = v.getLookupRows();
    if (result.size() > 1) {
      Comparator<LookupRow> comparator = getSortComparator();
      if (comparator != null) {
        Collections.sort(result, comparator);
      }
    }
    return result.toArray(new LookupRow[result.size()]);
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
    AbstractLookupRowCollector v = new AbstractLookupRowCollector() {
      @Override
      public boolean visit(ICode code, int treeLevel) {
        if (m_filter != null && !m_filter.visit(CodeLookupCall.this, code, treeLevel)) {
          return true;
        }
        ICode parentCode = code.getParentCode();
        if (getActive().isUndefined() || getActive().getBooleanValue() == code.isActive()) {
          if (((parentCode == null && key == null) || (parentCode != null && parentCode.getId() != null && parentCode.getId().equals(key)))) {
            LookupRow row = execCreateLookupRowFromCode(code);
            if (row != null) {
              add(row);
            }
          }
        }
        return true;
      }
    };
    resolveCodes(v);
    List<LookupRow> result = v.getLookupRows();
    if (result.size() > 1) {
      Comparator<LookupRow> comparator = getSortComparator();
      if (comparator != null) {
        Collections.sort(result, comparator);
      }
    }
    return result.toArray(new LookupRow[result.size()]);
  }

  /**
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388242
   * <p>
   * 
   * @return the result of this lookup call into a single code or null.
   * @since 3.8.1
   */
  protected ICode<?> resolveCodeByKey() throws ProcessingException {
    Object key = getKey();
    ICodeType t = CODES.getCodeType(m_codeTypeClass);
    if (t == null) {
      return null;
    }
    return t.getCode(key);
  }

  /**
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388242
   * <p>
   * 
   * @return the result of this lookup call into multiple codes matching the filter.
   * @since 3.8.1
   */
  protected void resolveCodes(ICodeVisitor v) throws ProcessingException {
    ICodeType t = CODES.getCodeType(m_codeTypeClass);
    if (t != null) {
      t.visit(v, false);
    }
  }

  private static abstract class AbstractLookupRowCollector implements ICodeVisitor {
    private ArrayList<LookupRow> m_list = new ArrayList<LookupRow>();

    public AbstractLookupRowCollector() {
    }

    public void add(LookupRow row) {
      m_list.add(row);
    }

    public List<LookupRow> getLookupRows() {
      return m_list;
    }
  }
}
