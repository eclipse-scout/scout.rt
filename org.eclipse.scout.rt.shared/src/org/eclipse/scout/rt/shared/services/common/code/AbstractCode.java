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

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public abstract class AbstractCode<T> implements ICode<T>, Serializable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCode.class);
  private static final long serialVersionUID = 1L;

  private transient ICodeType m_codeType;
  private CodeRow m_row;
  private transient ICode m_parentCode;
  private transient HashMap<Object, ICode> m_codeMap = null;
  private ArrayList<ICode> m_codeList = null;

  /**
   * Dynamic
   */
  public AbstractCode(CodeRow row) {
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
  @ConfigPropertyValue("null")
  protected String getConfiguredText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(60)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredActive() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(65)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredEnabled() {
    return true;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(40)
  @ConfigPropertyValue("null")
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(70)
  @ConfigPropertyValue("null")
  protected String getConfiguredTooltipText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(20)
  @ConfigPropertyValue("null")
  protected String getConfiguredBackgroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(10)
  @ConfigPropertyValue("null")
  protected String getConfiguredForegroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  @ConfigPropertyValue("null")
  protected String getConfiguredFont() {
    return null;
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(80)
  @ConfigPropertyValue("null")
  protected Double getConfiguredValue() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(80)
  @ConfigPropertyValue("null")
  protected String getConfiguredExtKey() {
    return null;
  }

  private Class<? extends ICode>[] getConfiguredCodes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(dca, ICode.class);
  }

  protected void initConfig() {
    m_row = interceptCodeRow(new CodeRow(
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
    for (ICode childCode : execCreateChildCodes()) {
      addChildCodeInternal(childCode);
    }
  }

  /**
   * @return Creates and returns child codes. Note: {@link #addChildCodeInternal(ICode)} must not be invoked.
   * @since 3.8.3
   */
  protected List<ICode<?>> execCreateChildCodes() {
    List<ICode<?>> codes = new ArrayList<ICode<?>>();
    Class<? extends ICode>[] a = getConfiguredCodes();
    if (a != null) {
      for (int i = 0; i < a.length; i++) {
        try {
          ICode code = ConfigurationUtility.newInnerInstance(this, a[i]);
          codes.add(code);
        }
        catch (Exception e) {
          LOG.warn(null, e);
        }
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
  protected CodeRow interceptCodeRow(CodeRow row) {
    return row;
  }

  @Override
  @SuppressWarnings("unchecked")
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
    return m_row.getTooltip();
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
  public ICode getParentCode() {
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
  public ICode[] getChildCodes() {
    return getChildCodes(true);
  }

  @Override
  public ICode[] getChildCodes(boolean activeOnly) {
    if (m_codeList == null) {
      return new ICode[0];
    }
    ArrayList<ICode> list = new ArrayList<ICode>(m_codeList);
    if (activeOnly) {
      for (Iterator it = list.iterator(); it.hasNext();) {
        ICode code = (ICode) it.next();
        if (!code.isActive()) {
          it.remove();
        }
      }
    }
    return list.toArray(new ICode[0]);
  }

  @Override
  public ICode getChildCode(Object id) {
    ICode c = null;
    if (m_codeMap != null) {
      c = m_codeMap.get(id);
    }
    if (c == null) {
      if (m_codeList == null) {
        return null;
      }
      for (Iterator it = m_codeList.iterator(); it.hasNext();) {
        ICode childCode = (ICode) it.next();
        c = childCode.getChildCode(id);
        if (c != null) {
          return c;
        }
      }
    }
    return c;
  }

  @Override
  public ICode getChildCodeByExtKey(Object extKey) {
    if (m_codeList == null) {
      return null;
    }
    ICode c = null;
    for (Iterator<ICode> it = m_codeList.iterator(); it.hasNext();) {
      ICode childCode = it.next();
      if (extKey.equals(childCode.getExtKey())) {
        c = childCode;
      }
      else {
        c = childCode.getChildCodeByExtKey(extKey);
      }
      if (c != null) {
        return c;
      }
    }
    return c;
  }

  @Override
  public ICodeType getCodeType() {
    return m_codeType;
  }

  @Override
  public void addChildCodeInternal(ICode code) {
    code.setCodeTypeInternal(m_codeType);
    code.setParentCodeInternal(this);
    if (m_codeMap == null) {
      m_codeMap = new HashMap<Object, ICode>();
    }
    m_codeMap.put(code.getId(), code);
    if (m_codeList == null) {
      m_codeList = new ArrayList<ICode>();
    }
    m_codeList.add(code);
  }

  @Override
  public void setParentCodeInternal(ICode c) {
    m_parentCode = c;
  }

  @Override
  public void setCodeTypeInternal(ICodeType type) {
    m_codeType = type;
    if (m_codeList != null) {
      for (ICode c : m_codeList) {
        c.setCodeTypeInternal(type);
      }
    }
  }

  @Override
  public String toString() {
    return "Code[id=" + getId() + ", text='" + getText() + "' " + (isActive() ? "active" : "inactive") + "]";
  }

  @Override
  public boolean visit(ICodeVisitor visitor, int level, boolean activeOnly) {
    ICode[] a = getChildCodes(activeOnly);
    for (int i = 0; i < a.length; i++) {
      ICode code = a[i];
      if (!visitor.visit(code, level)) {
        return false;
      }
      if (!code.visit(visitor, level + 1, activeOnly)) {
        return false;
      }
    }
    return true;
  }

  protected Object readResolve() throws ObjectStreamException {
    m_codeMap = new HashMap<Object, ICode>();
    if (m_codeList == null) {
      m_codeList = new ArrayList<ICode>();
    }
    else {
      for (ICode<?> c : m_codeList) {
        m_codeMap.put(c.getId(), c);
        c.setParentCodeInternal(this);
      }
    }
    return this;
  }

  @Override
  public CodeRow toCodeRow() {
    return new CodeRow(m_row);
  }
}
