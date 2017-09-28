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
package org.eclipse.scout.rt.shared.services.common.code;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeChains.CodeCreateChildCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.ICodeExtension;

@ClassId("8a1ed7c8-14e0-42ba-a275-258a3c2c4a51")
public abstract class AbstractCode<T> implements ICode<T>, Serializable, IContributionOwner, IExtensibleObject {
  private static final long serialVersionUID = 1L;

  private transient ICodeType<?, T> m_codeType;
  private ICodeRow<T> m_row;
  private transient ICode<T> m_parentCode;
  private transient Map<T, ICode<T>> m_codeMap = null;
  private List<ICode<T>> m_codeList = null;
  protected IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractCode<T>, ICodeExtension<T, ? extends AbstractCode<T>>> m_objectExtensions;

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <TYPE> List<TYPE> getContributionsByClass(Class<TYPE> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <TYPE> TYPE getContribution(Class<TYPE> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  /**
   * Configured
   */
  public AbstractCode() {
    this(true);
  }

  public AbstractCode(boolean callInitializer) {
    this(null, callInitializer);
  }

  public AbstractCode(ICodeRow<T> row) {
    this(row, true);
  }

  /**
   * Dynamic
   */
  public AbstractCode(ICodeRow<T> row, boolean callInitializer) {
    m_row = row;
    m_objectExtensions = new ObjectExtensions<AbstractCode<T>, ICodeExtension<T, ? extends AbstractCode<T>>>(this, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  protected final void callInitializer() {
    interceptInitConfig();
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

  @ConfigProperty(ConfigProperty.STRING)
  @Order(40)
  protected String getConfiguredCssClass() {
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

  /**
   * Configures the view order of this code. The view order determines the order in which the codes appear. The order of
   * codes with no view order configured ({@code < 0}) is initialized based on the {@link Order} annotation of the code
   * class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this code.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(90)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  /**
   * Calculates the column's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0. If
   * no {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   *
   * @since 3.10.0-M4
   */
  @SuppressWarnings("squid:S1244")
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    Class<?> cls = getClass();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      while (cls != null && ICode.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = (Order) cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  protected final List<Class<ICode>> getConfiguredCodes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, ICode.class);
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    if (m_row == null) {
      m_row = interceptCodeRow(new CodeRow<T>(
          getId(),
          getConfiguredText(),
          getConfiguredIconId(),
          getConfiguredTooltipText() != null ? getConfiguredTooltipText() : null,
          (getConfiguredBackgroundColor()),
          (getConfiguredForegroundColor()),
          FontSpec.parse(getConfiguredFont()),
          getConfiguredCssClass(),
          getConfiguredEnabled(),
          null,
          getConfiguredActive(),
          getConfiguredExtKey(),
          getConfiguredValue(),
          0,
          calculateViewOrder()));
    }
    m_contributionHolder = new ContributionComposite(this);
    // add configured child codes
    for (ICode<T> childCode : interceptCreateChildCodes()) {
      addChildCodeInternal(-1, childCode);
    }
  }

  protected ICodeExtension<T, ? extends AbstractCode<T>> createLocalExtension() {
    return new LocalCodeExtension<T, AbstractCode<T>>(this);
  }

  @Override
  public final List<? extends ICodeExtension<T, ? extends AbstractCode<T>>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <E extends IExtension<?>> E getExtension(Class<E> c) {
    return m_objectExtensions.getExtension(c);
  }

  /**
   * @return Creates and returns child codes. Note: {@link #addChildCodeInternal(ICode)} must not be invoked.
   * @since 3.8.3
   */
  @ConfigOperation
  @SuppressWarnings("unchecked")
  protected List<? extends ICode<T>> execCreateChildCodes() {
    List<Class<ICode>> configuredCodes = getConfiguredCodes();
    List<ICode> contributedCodes = m_contributionHolder.getContributionsByClass(ICode.class);

    List<ICode<T>> codes = new ArrayList<ICode<T>>(configuredCodes.size() + contributedCodes.size());
    for (Class<? extends ICode> codeClazz : configuredCodes) {
      ICode<T> code = ConfigurationUtility.newInnerInstance(this, codeClazz);
      codes.add(code);
    }
    for (ICode<?> c : contributedCodes) {
      codes.add((ICode<T>) c);
    }
    Collections.sort(codes, new OrderedComparator());

    return codes;
  }

  /**
   * This interception method is called from the {@link #initConfig()} method just on construction of the code object.
   * <p>
   * Override this method to change that code row used by this code or to return a different code row than the default.
   * <p>
   * The default does nothing and returns the argument row.
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
    m_row.withActive(b);
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
    m_row.withEnabled(b);
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
  public String getCssClass() {
    return m_row.getCssClass();
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
  public double getOrder() {
    return m_row.getOrder();
  }

  @Override
  public void setOrder(double order) {
    m_row.withOrder(order);
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

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalCodeExtension<T, OWNER extends AbstractCode<T>> extends AbstractSerializableExtension<OWNER> implements ICodeExtension<T, OWNER> {
    private static final long serialVersionUID = 1L;

    public LocalCodeExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public List<? extends ICode<T>> execCreateChildCodes(CodeCreateChildCodesChain<T> chain) {
      return getOwner().execCreateChildCodes();
    }
  }

  protected final List<? extends ICode<T>> interceptCreateChildCodes() {
    List<? extends ICodeExtension<T, ? extends AbstractCode<T>>> extensions = getAllExtensions();
    CodeCreateChildCodesChain<T> chain = new CodeCreateChildCodesChain<T>(extensions);
    return chain.execCreateChildCodes();
  }
}
