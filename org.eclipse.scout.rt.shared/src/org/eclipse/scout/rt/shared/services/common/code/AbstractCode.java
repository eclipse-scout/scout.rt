/*******************************************************************************
 * Copyright (c) 2010,2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.code;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public abstract class AbstractCode<T> implements ICode<T>, Serializable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCode.class);
  private static final long serialVersionUID = 1L;

  private transient ICodeType<?, T> m_codeType;
  private ICodeRow<T> m_row;
  private transient ICode<T> m_parentCode;
  private transient Map<T, ICode<T>> m_codeMap = null;
  private List<ICode<T>> m_codeList = null;

  /**
   * Dynamic
   */
  public AbstractCode(ICodeRow<T> row) {
    m_row = row;
  }

  /**
   * Configured
   */
  public AbstractCode() {
    initConfig();
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(50)
  protected String getConfiguredText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  protected boolean getConfiguredActive() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(65)
  protected boolean getConfiguredEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(40)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(70)
  protected String getConfiguredTooltipText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(20)
  protected String getConfiguredBackgroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(10)
  protected String getConfiguredForegroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  protected String getConfiguredFont() {
    return null;
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(80)
  protected Double getConfiguredValue() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(80)
  protected String getConfiguredExtKey() {
    return null;
  }

  protected final List<Class<? extends ICode>> getConfiguredCodes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ICode>> filtered = ConfigurationUtility.filterClasses(dca, ICode.class);
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, ICode.class);
  }

  protected void initConfig() {
    m_row = interceptCodeRow(new CodeRow<T>(
        getId(),
        getConfiguredText(),
        getConfiguredIconId(),
        getConfiguredTooltipText() != null ? getConfiguredTooltipText() : null,
        (getConfiguredBackgroundColor()),
        (getConfiguredForegroundColor()),
        FontSpec.parse(getConfiguredFont()),
        getConfiguredEnabled(),
        null,
        getConfiguredActive(),
        getConfiguredExtKey(),
        getConfiguredValue(),
        0
        ));
    // add configured child codes
    for (ICode<T> childCode : execCreateChildCodes()) {
      addChildCodeInternal(-1, childCode);
    }
  }

  /**
   * @return Creates and returns child codes. Note: {@link #addChildCodeInternal(ICode)} must not be invoked.
   * @since 3.8.3
   */
  protected List<? extends ICode<T>> execCreateChildCodes() {
    List<ICode<T>> codes = new ArrayList<ICode<T>>();
    for (Class<? extends ICode> codeClazz : getConfiguredCodes()) {
      try {
        @SuppressWarnings("unchecked")
        ICode<T> code = ConfigurationUtility.newInnerInstance(this, codeClazz);
        codes.add(code);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    return codes;
  }

  /**
   * This interception method is called from the {@link #initConfig()} method just on construction of the code object.
   * <p>
   * Override this method to change that code row used by this code or to return a different code row than the default.
   * <p>
   * The defaukt does nothing and returns the argument row.
   */
  protected ICodeRow<T> interceptCodeRow(ICodeRow<T> row) {
    return row;
  }

  @Override
  public T getId() {
    return (T) m_row.getKey();
  }

  @Override
  public String getText() {
    return m_row.getText();
  }

  @Override
  public boolean isActive() {
    return m_row.isActive();
  }

  /**
   * Do only call this method during creation and setup of a code / code type.
   * <p>
   * Never call it afterwards, this may lead to conflicting situations when {@link ICode}s are in use and references.
   */
  public void setActiveInternal(boolean b) {
    m_row.setActive(b);
  }

  @Override
  public boolean isEnabled() {
    return m_row.isEnabled();
  }

  /**
   * Do only call this method during creation and setup of a code / code type.
   * <p>
   * Never call it afterwards, this may lead to conflicting situations when {@link ICode}s are in use and references.
   */
  public void setEnabledInternal(boolean b) {
    m_row.setEnabled(b);
  }

  @Override
  public String getIconId() {
    String id = m_row.getIconId();
    if (id == null && m_codeType != null) {
      id = m_codeType.getIconId();
    }
    return id;
  }

  @Override
  public String getTooltipText() {
    return m_row.getTooltipText();
  }

  @Override
  public String getBackgroundColor() {
    return m_row.getBackgroundColor();
  }

  @Override
  public String getForegroundColor() {
    return m_row.getForegroundColor();
  }

  @Override
  public FontSpec getFont() {
    return m_row.getFont();
  }

  @Override
  public ICode<T> getParentCode() {
    return m_parentCode;
  }

  @Override
  public long getPartitionId() {
    return m_row.getPartitionId();
  }

  @Override
  public String getExtKey() {
    return m_row.getExtKey();
  }

  @Override
  public Number getValue() {
    return m_row.getValue();
  }

  @Override
  public List<? extends ICode<T>> getChildCodes() {
    return getChildCodes(true);
  }

  @Override
  public List<? extends ICode<T>> getChildCodes(boolean activeOnly) {
    if (m_codeList == null) {
      return new ArrayList<ICode<T>>(0);
    }
    List<ICode<T>> result = new ArrayList<ICode<T>>(m_codeList);
    if (activeOnly) {
      for (Iterator<ICode<T>> it = result.iterator(); it.hasNext();) {
        if (!it.next().isActive()) {
          it.remove();
        }
      }
    }
    return result;
  }

  @Override
  public ICode<T> getChildCode(T id) {
    ICode<T> c = null;
    if (m_codeMap != null) {
      c = m_codeMap.get(id);
    }
    if (c == null) {
      if (m_codeList == null) {
        return null;
      }
      for (Iterator<ICode<T>> it = m_codeList.iterator(); it.hasNext();) {
        ICode<T> childCode = it.next();
        c = childCode.getChildCode(id);
        if (c != null) {
          return c;
        }
      }
    }
    return c;
  }

  @Override
  public ICode<T> getChildCodeByExtKey(Object extKey) {
    if (m_codeList == null) {
      return null;
    }
    ICode<T> c = null;
    for (Iterator<ICode<T>> it = m_codeList.iterator(); it.hasNext();) {
      ICode<T> childCode = it.next();
      if (extKey.equals(childCode.getExtKey())) {
        return childCode;
      }
      else {
        c = childCode.getChildCodeByExtKey(extKey);
        if (c != null) {
          return c;
        }
      }
    }
    return c;
  }

  @Override
  public ICodeType<?, T> getCodeType() {
    return m_codeType;
  }

  @Override
  public void addChildCodeInternal(int index, ICode<T> code) {
    if (code == null) {
      return;
    }
    int oldIndex = removeChildCodeInternal(code.getId());
    if (oldIndex >= 0 && index < 0) {
      index = oldIndex;
    }
    if (m_codeMap == null) {
      m_codeMap = new HashMap<T, ICode<T>>();
    }
    if (m_codeList == null) {
      m_codeList = new ArrayList<ICode<T>>();
    }
    code.setCodeTypeInternal(m_codeType);
    code.setParentCodeInternal(this);
    m_codeMap.put(code.getId(), code);
    if (index < 0) {
      m_codeList.add(code);
    }
    else {
      m_codeList.add(Math.min(index, m_codeList.size()), code);
    }
  }

  @Override
  public int removeChildCodeInternal(T codeId) {
    if (m_codeMap == null) {
      return -1;
    }
    ICode<T> droppedCode = m_codeMap.remove(codeId);
    if (droppedCode == null) {
      return -1;
    }
    int index = -1;
    if (m_codeList != null) {
      for (Iterator<ICode<T>> it = m_codeList.iterator(); it.hasNext();) {
        index++;
        ICode<T> candidateCode = it.next();
        if (candidateCode == droppedCode) {
          it.remove();
          break;
        }
      }
    }
    droppedCode.setCodeTypeInternal(null);
    droppedCode.setParentCodeInternal(null);
    return index;
  }

  @Override
  public void setParentCodeInternal(ICode<T> c) {
    m_parentCode = c;
  }

  @Override
  public void setCodeTypeInternal(ICodeType<?, T> type) {
    m_codeType = type;
    if (m_codeList != null) {
      for (ICode<T> c : m_codeList) {
        c.setCodeTypeInternal(type);
      }
    }
  }

  @Override
  public String toString() {
    return "Code[id=" + getId() + ", text='" + getText() + "' " + (isActive() ? "active" : "inactive") + "]";
  }

  @SuppressWarnings("unchecked")
  @Override
  public <CODE extends ICode<T>> boolean visit(ICodeVisitor<CODE> visitor, int level, boolean activeOnly) {
    List<? extends ICode<T>> a = getChildCodes(activeOnly);
    for (ICode<T> childCode : a) {
      if (!visitor.visit((CODE) childCode, level)) {
        return false;
      }
      if (!childCode.visit(visitor, level + 1, activeOnly)) {
        return false;
      }
    }
    return true;
  }

  protected Object readResolve() throws ObjectStreamException {
    m_codeMap = new HashMap<T, ICode<T>>();
    if (m_codeList == null) {
      m_codeList = new ArrayList<ICode<T>>();
    }
    else {
      for (ICode<T> c : m_codeList) {
        m_codeMap.put(c.getId(), c);
        c.setParentCodeInternal(this);
      }
    }
    return this;
  }

  @Override
  public ICodeRow<T> toCodeRow() {
    return new CodeRow<T>(m_row);
  }

  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
  }
}
