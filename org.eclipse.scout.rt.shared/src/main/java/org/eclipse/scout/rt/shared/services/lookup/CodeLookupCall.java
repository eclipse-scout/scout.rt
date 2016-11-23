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
package org.eclipse.scout.rt.shared.services.lookup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeVisitor;

/**
 * @see LookupCall
 */
@ClassId("bf3702b8-ee95-4c7b-870d-105b9d0deec2")
public class CodeLookupCall<CODE_ID> extends LocalLookupCall<CODE_ID> implements Serializable {
  private static final long serialVersionUID = 0L;

  /**
   * Helper method to create a lookup call from a codetype using the {@link ICodeLookupCallFactoryService}.
   */
  public static <T> CodeLookupCall<T> newInstanceByService(Class<? extends ICodeType<?, T>> codeTypeClass) {
    return BEANS.get(ICodeLookupCallFactoryService.class).newInstance(codeTypeClass);
  }

  private Class<? extends ICodeType<?, CODE_ID>> m_codeTypeClass;
  private ICodeLookupCallVisitor<CODE_ID> m_filter;
  private Comparator<ILookupRow<CODE_ID>> m_sortComparator;

  public CodeLookupCall(Class<? extends ICodeType<?, CODE_ID>> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
  }

  public Comparator<ILookupRow<CODE_ID>> getSortComparator() {
    return m_sortComparator;
  }

  public void setSortComparator(Comparator<ILookupRow<CODE_ID>> comp) {
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

  public void setFilter(ICodeLookupCallVisitor<CODE_ID> filter) {
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
  protected List<ILookupRow<CODE_ID>> execCreateLookupRowsFromCodes(List<? extends ICode<CODE_ID>> codes) {
    List<ILookupRow<CODE_ID>> a = new ArrayList<ILookupRow<CODE_ID>>(codes.size());
    for (ICode<CODE_ID> c : codes) {
      a.add(execCreateLookupRowFromCode(c));
    }
    return a;
  }

  /**
   * Implementation that creates lookup rows from codes.
   * <p>
   */
  protected ILookupRow<CODE_ID> execCreateLookupRowFromCode(ICode<CODE_ID> c) {
    return createCodeLookupRow(c);
  }

  /**
   * Default implementation to create lookup rows from codes.
   * <p>
   * Called by {@link #execCreateLookupRowsFromCodes(List)}.
   */
  protected <T> List<ILookupRow<T>> createCodeLookupRowList(List<? extends ICode<T>> codes) {
    List<ILookupRow<T>> rows = new ArrayList<ILookupRow<T>>(codes.size());
    for (ICode<T> code : codes) {
      rows.add(createCodeLookupRow(code));
    }
    return rows;
  }

  /**
   * Default implementation to create a lookup row from a code.
   * <p>
   * Called by {@link #createCodeLookupRowList(List)}.
   */
  protected <T> ILookupRow<T> createCodeLookupRow(ICode<T> c) {
    return new LookupRow<T>(c.getId(), c.getText())
        .withIconId(c.getIconId())
        .withTooltipText(c.getTooltipText())
        .withBackgroundColor(c.getBackgroundColor())
        .withForegroundColor(c.getForegroundColor())
        .withFont(c.getFont())
        .withEnabled(c.isEnabled())
        .withParentKey(c.getParentCode() != null ? c.getParentCode().getId() : null)
        .withActive(c.isActive());
  }

  @Override
  protected Pattern createSearchPattern(String s) {
    if (s == null) {
      s = "";
    }
    s = s.replace(getWildcard(), "@wildcard@");
    s = s.toLowerCase();
    s = StringUtility.escapeRegexMetachars(s);
    s = s.replace("@wildcard@", ".*");
    if (!s.endsWith(".*")) {
      s = s + ".*";
    }
    return Pattern.compile(s, Pattern.DOTALL);
  }

  /**
   * Complete override using code data
   */
  @Override
  public List<ILookupRow<CODE_ID>> getDataByKey() {
    ArrayList<ICode<CODE_ID>> list = new ArrayList<ICode<CODE_ID>>(1);
    ICode<CODE_ID> c = resolveCodeByKey();
    if (c != null) {
      list.add(c);
    }
    return execCreateLookupRowsFromCodes(list);
  }

  /**
   * Complete override using code data
   */
  @Override
  public List<ILookupRow<CODE_ID>> getDataByText() {
    final Pattern pat = createSearchPattern(getText());
    AbstractLookupRowCollector v = new AbstractLookupRowCollector() {
      @Override
      public boolean visit(ICode<CODE_ID> code, int treeLevel) {
        if (m_filter != null && !m_filter.visit(CodeLookupCall.this, code, treeLevel)) {
          return true;
        }
        if (getActive().isUndefined() || getActive().getBooleanValue() == code.isActive()) {
          ILookupRow<CODE_ID> row = execCreateLookupRowFromCode(code);
          if (row != null && row.getText() != null && (pat.matcher(row.getText().toLowerCase()).matches() || (isHierarchicLookup() && isParentInResultList(getLookupRows(), row)))) {
            add(row);
          }
        }
        return true;
      }
    };
    resolveCodes(v);
    List<ILookupRow<CODE_ID>> result = v.getLookupRows();
    if (result.size() > 1) {
      Comparator<ILookupRow<CODE_ID>> comparator = getSortComparator();
      if (comparator != null) {
        Collections.sort(result, comparator);
      }
    }
    return result;
  }

  /**
   * Complete override using code data
   */
  @Override
  public List<ILookupRow<CODE_ID>> getDataByAll() {
    final Pattern pat = createSearchPattern(getAll());
    AbstractLookupRowCollector v = new AbstractLookupRowCollector() {
      @Override
      public boolean visit(ICode<CODE_ID> code, int treeLevel) {
        if (m_filter != null && !m_filter.visit(CodeLookupCall.this, code, treeLevel)) {
          return true;
        }
        if (getActive().isUndefined() || getActive().getBooleanValue() == code.isActive()) {
          ILookupRow<CODE_ID> row = execCreateLookupRowFromCode(code);
          if (row != null && row.getText() != null && pat.matcher(row.getText().toLowerCase()).matches()) {
            add(row);
          }
        }
        return true;
      }
    };
    resolveCodes(v);
    List<ILookupRow<CODE_ID>> result = v.getLookupRows();
    if (result.size() > 1) {
      Comparator<ILookupRow<CODE_ID>> comparator = getSortComparator();
      if (comparator != null) {
        Collections.sort(result, comparator);
      }
    }
    return result;
  }

  /**
   * Complete override using code data
   */
  @Override
  public List<ILookupRow<CODE_ID>> getDataByRec() {
    Object recValue = getRec();
    if ((recValue instanceof Number) && ((Number) recValue).longValue() == 0) {
      recValue = null;
    }
    final Object key = recValue;
    AbstractLookupRowCollector v = new AbstractLookupRowCollector() {
      @Override
      public boolean visit(ICode<CODE_ID> code, int treeLevel) {
        if (m_filter != null && !m_filter.visit(CodeLookupCall.this, code, treeLevel)) {
          return true;
        }
        ICode parentCode = code.getParentCode();
        if (getActive().isUndefined() || getActive().getBooleanValue() == code.isActive()) {
          if (((parentCode == null && key == null) || (parentCode != null && parentCode.getId() != null && parentCode.getId().equals(key)))) {
            ILookupRow<CODE_ID> row = execCreateLookupRowFromCode(code);
            if (row != null) {
              add(row);
            }
          }
        }
        return true;
      }
    };
    resolveCodes(v);
    List<ILookupRow<CODE_ID>> result = v.getLookupRows();
    if (result.size() > 1) {
      Comparator<ILookupRow<CODE_ID>> comparator = getSortComparator();
      if (comparator != null) {
        Collections.sort(result, comparator);
      }
    }
    return result;
  }

  /**
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=388242
   * <p>
   *
   * @return the result of this lookup call into a single code or null.
   * @since 3.8.1
   */
  protected ICode<CODE_ID> resolveCodeByKey() {
    CODE_ID key = getKey();
    ICodeType<?, CODE_ID> t = BEANS.opt(m_codeTypeClass);
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
  protected void resolveCodes(ICodeVisitor<ICode<CODE_ID>> v) {
    ICodeType<?, CODE_ID> t = BEANS.opt(m_codeTypeClass);
    if (t != null) {
      t.visit(v, false);
    }
  }

  private abstract class AbstractLookupRowCollector implements ICodeVisitor<ICode<CODE_ID>> {

    private List<ILookupRow<CODE_ID>> m_list = new ArrayList<ILookupRow<CODE_ID>>();

    public AbstractLookupRowCollector() {
    }

    public void add(ILookupRow<CODE_ID> row) {
      m_list.add(row);
    }

    public List<ILookupRow<CODE_ID>> getLookupRows() {
      return m_list;
    }
  }

}
