/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.services.lookup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.shared.services.common.code.ICode;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.code.ICodeVisitor;

/**
 * @see LookupCall
 */
@IgnoreBean
@ClassId("bf3702b8-ee95-4c7b-870d-105b9d0deec2")
public class CodeLookupCall<CODE_ID> extends LocalLookupCall<CODE_ID> {
  private static final long serialVersionUID = 0L;

  /**
   * Helper method to create a lookup call from a codetype using the {@link ICodeLookupCallFactoryService}.
   */
  public static <T> CodeLookupCall<T> newInstanceByService(Class<? extends ICodeType<?, T>> codeTypeClass) {
    return BEANS.get(ICodeLookupCallFactoryService.class).newInstance(codeTypeClass);
  }

  private final Class<? extends ICodeType<?, CODE_ID>> m_codeTypeClass;
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
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((m_codeTypeClass == null) ? 0 : m_codeTypeClass.hashCode());
    result = prime * result + ((m_filter == null) ? 0 : m_filter.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CodeLookupCall other = (CodeLookupCall) obj;
    if (m_codeTypeClass == null) {
      if (other.m_codeTypeClass != null) {
        return false;
      }
    }
    else if (!m_codeTypeClass.equals(other.m_codeTypeClass)) {
      return false;
    }
    if (m_filter == null) {
      if (other.m_filter != null) {
        return false;
      }
    }
    else if (!m_filter.equals(other.m_filter)) {
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
    List<ILookupRow<CODE_ID>> a = new ArrayList<>(codes.size());
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
    List<ILookupRow<T>> rows = new ArrayList<>(codes.size());
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
    return new LookupRow<>(c.getId(), c.getText())
        .withIconId(c.getIconId())
        .withTooltipText(c.getTooltipText())
        .withBackgroundColor(c.getBackgroundColor())
        .withForegroundColor(c.getForegroundColor())
        .withFont(c.getFont())
        .withCssClass(c.getCssClass())
        .withEnabled(c.isEnabled())
        .withParentKey(c.getParentCode() != null ? c.getParentCode().getId() : null)
        .withActive(c.isActive());
  }

  /**
   * Complete override using code data
   */
  @Override
  public List<ILookupRow<CODE_ID>> getDataByKey() {
    List<ICode<CODE_ID>> list = new ArrayList<>(1);
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
      protected ILookupRow<CODE_ID> toLookupRow(ICode<CODE_ID> code) {
        ILookupRow<CODE_ID> row = execCreateLookupRowFromCode(code);
        if (row != null && row.getText() != null && (pat.matcher(row.getText().toLowerCase()).matches() || (isHierarchicalLookup() && isParentInResultList(m_list, row)))) {
          return row;
        }
        return null;
      }
    };
    resolveCodes(v);
    return v.getLookupRows();
  }

  /**
   * Complete override using code data
   */
  @Override
  public List<ILookupRow<CODE_ID>> getDataByAll() {
    final Pattern pat = createSearchPattern(getAll());
    AbstractLookupRowCollector v = new AbstractLookupRowCollector() {
      @Override
      protected ILookupRow<CODE_ID> toLookupRow(ICode<CODE_ID> code) {
        ILookupRow<CODE_ID> row = execCreateLookupRowFromCode(code);
        if (row != null && row.getText() != null && pat.matcher(row.getText().toLowerCase()).matches()) {
          return row;
        }
        return null;
      }
    };
    resolveCodes(v);
    return v.getLookupRows();
  }

  /**
   * Complete override using code data
   */
  @Override
  public List<ILookupRow<CODE_ID>> getDataByRec() {
    Object recValue = getRec();
    if (recValue instanceof Number && ((Number) recValue).longValue() == 0) {
      recValue = null;
    }
    final Object key = recValue;
    AbstractLookupRowCollector v = new AbstractLookupRowCollector() {
      @Override
      protected ILookupRow<CODE_ID> toLookupRow(ICode<CODE_ID> code) {
        ICode parentCode = code.getParentCode();
        if ((parentCode == null && key == null) || (parentCode != null && parentCode.getId() != null && parentCode.getId().equals(key))) {
          return execCreateLookupRowFromCode(code);
        }
        return null;
      }
    };
    resolveCodes(v);
    return v.getLookupRows();
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

  protected abstract class AbstractLookupRowCollector implements ICodeVisitor<ICode<CODE_ID>> {

    protected final List<ILookupRow<CODE_ID>> m_list = new ArrayList<>();

    @Override
    public boolean visit(ICode<CODE_ID> code, int treeLevel) {
      if (!acceptedByRowCount()) {
        return false;
      }
      if (!acceptedByFilter(code, treeLevel)) {
        return true;
      }
      if (acceptedByActive(code, treeLevel)) {
        ILookupRow<CODE_ID> row = toLookupRow(code);
        if (row != null) {
          m_list.add(row);
        }
      }
      return true;
    }

    protected boolean acceptedByActive(ICode<CODE_ID> code, int treeLevel) {
      return getActive().isUndefined() || getActive().getBooleanValue() == code.isActive();
    }

    protected boolean acceptedByRowCount() {
      // if max row count=n, actually return n+1 rows so that the UI recognizes that there are more rows
      return getMaxRowCount() < 1 || m_list.size() <= getMaxRowCount();
    }

    protected boolean acceptedByFilter(ICode<CODE_ID> code, int treeLevel) {
      return m_filter == null || m_filter.visit(CodeLookupCall.this, code, treeLevel);
    }

    protected abstract ILookupRow<CODE_ID> toLookupRow(ICode<CODE_ID> code);

    /**
     * @return A live list holding all {@link ILookupRow LookupRows} that fulfill all filters sorted by
     *         {@link CodeLookupCall#getSortComparator()}.
     */
    public List<ILookupRow<CODE_ID>> getLookupRows() {
      if (m_list.size() > 1) {
        Comparator<ILookupRow<CODE_ID>> comparator = getSortComparator();
        if (comparator != null) {
          m_list.sort(comparator);
        }
      }
      return m_list;
    }
  }
}
