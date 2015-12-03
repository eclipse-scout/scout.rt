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
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.IntegerHolder;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodeChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericLoadCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericOverwriteCodeChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.ICodeTypeExtension;
import org.eclipse.scout.rt.shared.extension.services.common.code.MoveCodesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>> implements ICodeType<CODE_TYPE_ID, CODE_ID>, IContributionOwner, IExtensibleObject, Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractCodeTypeWithGeneric.class);
  private static final long serialVersionUID = 1L;

  private boolean m_initialized;
  private String m_text;
  private String m_iconId;
  private boolean m_hierarchy;
  private int m_maxLevel;
  private transient Map<CODE_ID, CODE> m_rootCodeMap = new HashMap<CODE_ID, CODE>();
  private List<CODE> m_rootCodeList = new ArrayList<CODE>();
  protected IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>, ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> m_objectExtensions;

  public AbstractCodeTypeWithGeneric() {
    this(true);
  }

  public AbstractCodeTypeWithGeneric(boolean callInitializer) {
    m_objectExtensions = new ObjectExtensions<AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>, ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>>(this);
    if (callInitializer) {
      callInitializer();
    }
  }

  @Override
  public final List<Object> getAllContributions() {
    return m_contributionHolder.getAllContributions();
  }

  @Override
  public final <T> List<T> getContributionsByClass(Class<T> type) {
    return m_contributionHolder.getContributionsByClass(type);
  }

  @Override
  public final <T> T getContribution(Class<T> contribution) {
    return m_contributionHolder.getContribution(contribution);
  }

  protected void callInitializer() {
    if (!m_initialized) {
      interceptInitConfig();
      m_initialized = true;
    }
  }

  public AbstractCodeTypeWithGeneric(String label, boolean hierarchy) {
    m_objectExtensions = new ObjectExtensions<AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>, ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>>(this);
    m_text = label;
    m_hierarchy = hierarchy;
  }

  protected final List<Class<ICode>> getConfiguredCodes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, ICode.class);
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredIsHierarchy() {
    return false;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(30)
  protected int getConfiguredMaxLevel() {
    return Integer.MAX_VALUE;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(40)
  protected String getConfiguredText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(10)
  protected String getConfiguredIconId() {
    return null;
  }

  @SuppressWarnings("unchecked")
  @ConfigProperty(ConfigProperty.CODE_ROW)
  @Order(120)
  protected Class<? extends ICodeRow<CODE_ID>> getConfiguredCodeRowType() {
    return (Class<? extends ICodeRow<CODE_ID>>) CodeRow.class;
  }

  /**
   * This method is called on server side to create basic code set
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(1)
  protected List<? extends CODE> execCreateCodes() {
    List<Class<ICode>> configuredCodes = getConfiguredCodes();
    List<ICode> contributedCodes = m_contributionHolder.getContributionsByClass(ICode.class);
    OrderedCollection<CODE> codes = new OrderedCollection<CODE>();
    for (Class<? extends ICode> codeClazz : configuredCodes) {
      CODE code = (CODE) ConfigurationUtility.newInnerInstance(this, codeClazz);
      codes.addOrdered(code);
    }
    for (ICode c : contributedCodes) {
      codes.addOrdered((CODE) c);
    }

    new MoveCodesHandler<CODE_ID, CODE>(codes).moveModelObjects();
    return codes.getOrderedList();
  }

  /**
   * This method is called on server side to create a specific code for a code row. This method is called when loading
   * codes, in particular by
   *
   * @return a {@link ICode} to accept that row, return null to ignore that row
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(2)
  protected CODE execCreateCode(ICodeRow<CODE_ID> newRow) {
    Class<CODE> codeClazz = TypeCastUtility.getGenericsParameterClass(this.getClass(), AbstractCodeTypeWithGeneric.class, 2);
    if (!Modifier.isAbstract(codeClazz.getModifiers()) && !Modifier.isInterface(codeClazz.getModifiers())) {
      Class<? extends ICodeRow<CODE_ID>> codeRowClazz = getConfiguredCodeRowType();
      try {
        Constructor<CODE> ctor = codeClazz.getConstructor(codeRowClazz);
        return ctor.newInstance(newRow);
      }
      catch (Exception e) {
        throw new ProcessingException("Could not create a new instance of code row! Override the execCreateCode mehtod.", e);
      }
    }
    else {
      // try to create mutable code
      if (codeClazz.isAssignableFrom(MutableCode.class)) {
        return (CODE) new MutableCode<CODE_ID>(newRow);
      }
    }
    return null;
  }

  /**
   * This method is called on server side to load additional dynamic codes to the {@link #interceptCreateCodes()} list
   * <br>
   * Sample for sql call:
   *
   * <pre>
   * String sql =
   *     &quot;SELECT key,text,iconId,tooltipText,backgroundColor,foregroundColor,font,active,parentKey,extKey,calcValue,enabled,partitionId &quot; +
   *         &quot;FROM TABLE &quot; +
   *         &quot;WHERE ...&quot;;
   * Object[][] data = BEANS.get(ISqlService.class).select(sql, new Object[]{});
   * return createCodeRowArray(data);
   * </pre>
   */
  @ConfigOperation
  @Order(10)
  protected List<? extends ICodeRow<CODE_ID>> execLoadCodes(Class<? extends ICodeRow<CODE_ID>> codeRowType) {
    return null;
  }

  /**
   * When there are configured codes (inner classes) that are overwritten by {@link #execLoadCodes(Class)} then this
   * method is called to give a chance to merge attributes of the old configured code to the new dynamic code.
   * <p>
   * The default merges the following properties from the old code to the new iff they are null on the new code.
   * <ul>
   * <li>backgroundColor</li>
   * <li>font</li>
   * <li>foregroundColor</li>
   * <li>iconId</li>
   * <li>extKey</li>
   * <li>value</li>
   * </ul>
   * <p>
   *
   * @param oldCode
   *          is the old (configured) code that is dumped after this call
   * @param newCode
   *          is the new code that replaces the old code
   */
  @ConfigOperation
  @Order(20)
  protected void execOverwriteCode(ICodeRow<CODE_ID> oldCode, ICodeRow<CODE_ID> newCode) {
    if (newCode.getBackgroundColor() == null) {
      newCode.withBackgroundColor(oldCode.getBackgroundColor());
    }
    if (newCode.getFont() == null) {
      newCode.withFont(oldCode.getFont());
    }
    if (newCode.getForegroundColor() == null) {
      newCode.withForegroundColor(oldCode.getForegroundColor());
    }
    if (newCode.getIconId() == null) {
      newCode.withIconId(oldCode.getIconId());
    }
    if (newCode.getExtKey() == null) {
      newCode.withExtKey(oldCode.getExtKey());
    }
    if (newCode.getValue() == null) {
      newCode.withValue(oldCode.getValue());
    }
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
    m_text = getConfiguredText();
    m_iconId = getConfiguredIconId();
    m_hierarchy = getConfiguredIsHierarchy();
    m_maxLevel = getConfiguredMaxLevel();
    m_contributionHolder = new ContributionComposite(this);
    loadCodes();
  }

  protected ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>> createLocalExtension() {
    return new LocalCodeTypeWithGenericExtension<CODE_TYPE_ID, CODE_ID, CODE, AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>(this);
  }

  @Override
  public final List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  @Override
  public <E extends IExtension<?>> E getExtension(Class<E> c) {
    return m_objectExtensions.getExtension(c);
  }

  /**
   * The extension support for {@link AbstractCodeTypeWithGeneric#execCreateCodes()} method.
   *
   * @return
   */
  protected final List<? extends CODE> interceptCreateCodes() {
    List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> extensions = getAllExtensions();
    CodeTypeWithGenericCreateCodesChain<CODE_TYPE_ID, CODE_ID, CODE> chain = new CodeTypeWithGenericCreateCodesChain<CODE_TYPE_ID, CODE_ID, CODE>(extensions);
    return chain.execCreateCodes();
  }

  /**
   * The extension support for {@link AbstractCodeTypeWithGeneric#execCreateCode(ICodeRow)} method.
   *
   * @return
   */
  protected final CODE interceptCreateCode(ICodeRow<CODE_ID> newRow) {
    List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> extensions = getAllExtensions();
    CodeTypeWithGenericCreateCodeChain<CODE_TYPE_ID, CODE_ID, CODE> chain = new CodeTypeWithGenericCreateCodeChain<CODE_TYPE_ID, CODE_ID, CODE>(extensions);
    return chain.execCreateCode(newRow);
  }

  /**
   * The extension support for {@link AbstractCodeTypeWithGeneric#execLoadCodes(Class)} method.
   *
   * @param codeRowType
   * @return
   */
  protected List<? extends ICodeRow<CODE_ID>> interceptLoadCodes(Class<? extends ICodeRow<CODE_ID>> codeRowType) {
    List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> extensions = getAllExtensions();
    CodeTypeWithGenericLoadCodesChain<CODE_TYPE_ID, CODE_ID, CODE> chain = new CodeTypeWithGenericLoadCodesChain<CODE_TYPE_ID, CODE_ID, CODE>(extensions);
    return chain.execLoadCodes(codeRowType);
  }

  protected void interceptOverwriteCode(ICodeRow<CODE_ID> oldCode, ICodeRow<CODE_ID> newCode) {
    List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> extensions = getAllExtensions();
    CodeTypeWithGenericOverwriteCodeChain<CODE_TYPE_ID, CODE_ID, CODE> chain = new CodeTypeWithGenericOverwriteCodeChain<CODE_TYPE_ID, CODE_ID, CODE>(extensions);
    chain.execOverwriteCode(oldCode, newCode);
  }

  /**
   * default implementations add a field:
   *
   * <pre>
   * public static final long ID=123;
   * and create a getter:
   * public T getId(){ return ID; }
   * </pre>
   */
  @Override
  public abstract CODE_TYPE_ID getId();

  @Override
  public String getText() {
    return m_text;
  }

  @Override
  public String getIconId() {
    return m_iconId;
  }

  @Override
  public boolean isHierarchy() {
    return m_hierarchy;
  }

  @Override
  public int getMaxLevel() {
    return m_maxLevel;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CODE getCode(CODE_ID id) {
    CODE c = m_rootCodeMap.get(id);
    if (c == null) {
      for (Iterator<CODE> it = m_rootCodeList.iterator(); it.hasNext();) {
        CODE childCode = it.next();
        c = (CODE) childCode.getChildCode(id);
        if (c != null) {
          return c;
        }
      }
    }
    return c;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CODE getCodeByExtKey(Object extKey) {
    CODE c = null;
    for (Iterator<CODE> it = m_rootCodeList.iterator(); it.hasNext();) {
      CODE childCode = it.next();
      if (extKey.equals(childCode.getExtKey())) {
        c = childCode;
      }
      else {
        c = (CODE) childCode.getChildCodeByExtKey(extKey);
      }
      if (c != null) {
        return c;
      }
    }
    return c;
  }

  @Override
  public int getCodeIndex(final CODE_ID id) {
    final IntegerHolder result = new IntegerHolder(-1);
    ICodeVisitor<ICode<CODE_ID>> v = new ICodeVisitor<ICode<CODE_ID>>() {
      private int index = 0;

      @Override
      public boolean visit(ICode<CODE_ID> code, int treeLevel) {
        if (CompareUtility.equals(code.getId(), id)) {
          result.setValue(index);
        }
        else {
          index++;
        }
        return result.getValue() < 0;
      }
    };
    visit(v, false);
    return result.getValue();
  }

  @Override
  public int getCodeIndex(final ICode<CODE_ID> c) {
    final IntegerHolder result = new IntegerHolder(-1);
    ICodeVisitor<ICode<CODE_ID>> v = new ICodeVisitor<ICode<CODE_ID>>() {
      private int index = 0;

      @Override
      public boolean visit(ICode<CODE_ID> code, int treeLevel) {
        if (code == c) {
          result.setValue(index);
        }
        else {
          index++;
        }
        return result.getValue() < 0;
      }
    };
    visit(v, false);
    return result.getValue();
  }

  @Override
  public List<? extends CODE> getCodes() {
    return getCodes(true);
  }

  @Override
  public List<? extends CODE> getCodes(boolean activeOnly) {
    List<CODE> list = new ArrayList<CODE>(m_rootCodeList);
    if (activeOnly) {
      for (Iterator<CODE> it = list.iterator(); it.hasNext();) {
        CODE code = it.next();
        if (!code.isActive()) {
          it.remove();
        }
      }
    }
    return list;
  }

  protected void loadCodes() {
    m_rootCodeMap = new HashMap<CODE_ID, CODE>();
    m_rootCodeList = new ArrayList<CODE>();
    //
    // 1a create unconnected codes and assign to type
    List<CODE> allCodesOrdered = new ArrayList<CODE>();
    Map<CODE, CODE> codeToParentCodeMap = new HashMap<CODE, CODE>();
    Map<CODE_ID, CODE> idToCodeMap = new HashMap<CODE_ID, CODE>();
    // 1a add configured codes
    List<? extends CODE> createdList = interceptCreateCodes();
    if (createdList != null) {
      for (CODE code : createdList) {
        allCodesOrdered.add(code);
        idToCodeMap.put(code.getId(), code);
        codeToParentCodeMap.put(code, null);
      }
    }
    // 1b add dynamic codes
    List<? extends ICodeRow<CODE_ID>> result = interceptLoadCodes(getConfiguredCodeRowType());
    if (result != null && result.size() > 0) {
      HashMap<CODE, CODE_ID> codeToParentIdMap = new HashMap<CODE, CODE_ID>();
      // create unconnected codes and assign to type
      for (ICodeRow<CODE_ID> newRow : result) {
        CODE existingCode = idToCodeMap.get(newRow.getKey());
        if (existingCode != null) {
          // There is already a static code with same id.
          interceptOverwriteCode(existingCode.toCodeRow(), newRow);
        }
        CODE newCode = interceptCreateCode(newRow);
        if (newCode != null) {
          if (existingCode != null) {
            // remove old (and then re-add) to preserve dynamic ordering.
            allCodesOrdered.remove(existingCode);
            idToCodeMap.remove(existingCode.getId());
            codeToParentCodeMap.remove(existingCode);
          }
          //add new
          allCodesOrdered.add(newCode);
          idToCodeMap.put(newCode.getId(), newCode);
          CODE_ID parentId = (CODE_ID) newRow.getParentKey();
          codeToParentIdMap.put(newCode, parentId);
        }
        else if (existingCode != null) {
          // remove old (and then re-add) to preserve dynamic ordering.
          allCodesOrdered.remove(existingCode);
          allCodesOrdered.add(existingCode);
        }
      }
      for (Iterator<Map.Entry<CODE, CODE_ID>> it = codeToParentIdMap.entrySet().iterator(); it.hasNext();) {
        Map.Entry<CODE, CODE_ID> e = it.next();
        CODE code = e.getKey();
        CODE_ID parentId = e.getValue();
        CODE parentCode = null;
        if (parentId != null) {
          parentCode = idToCodeMap.get(parentId);
          if (parentCode == null) {
            LOG.warn("parent code for " + code + " not found: id=" + parentId);
          }
        }
        codeToParentCodeMap.put(code, parentCode);
      }
    }
    // 2 interconnect codes and types to structure
    for (CODE code : allCodesOrdered) {
      CODE parentCode = codeToParentCodeMap.get(code);
      if (parentCode != null) {
        parentCode.addChildCodeInternal(-1, code);
      }
      else {
        this.addRootCodeInternal(-1, code);
      }
    }
    // 3 mark all chidren of inactive codes also as inactive
    visit(new ICodeVisitor<ICode<CODE_ID>>() {
      @Override
      public boolean visit(ICode<CODE_ID> code, int treeLevel) {
        if (code.getParentCode() != null) {
          if (!code.getParentCode().isActive() && code.isActive()) {
            if (code instanceof AbstractCode<?>) {
              ((AbstractCode<?>) code).setActiveInternal(false);
            }
          }
        }
        return true;
      }
    }, false);
  }

  /**
   * do not use this internal method unless the intention is in fact to change the structure of the possibly shared
   * {@link ICodeType}
   * <p>
   * Add a new root code, owerwrite (drop) existing root code
   *
   * @since 4.0
   * @param index
   *          if index is -1 and the codeId existed before, then it is replaced at the same position. If index is -1 and
   *          the codeId did not exist, then the code is appended to the end.
   */
  protected void addRootCodeInternal(int index, CODE code) {
    if (code == null) {
      return;
    }
    int oldIndex = removeRootCodeInternal(code.getId());
    if (oldIndex >= 0 && index < 0) {
      index = oldIndex;
    }
    code.setCodeTypeInternal(this);
    code.setParentCodeInternal(null);
    m_rootCodeMap.put(code.getId(), code);
    if (index < 0) {
      m_rootCodeList.add(code);
    }
    else {
      m_rootCodeList.add(Math.min(index, m_rootCodeList.size()), code);
    }
  }

  /**
   * do not use this internal method unless the intention is in fact to change the structure of the possibly shared
   * {@link ICodeType}
   * <p>
   * Remove a root code
   *
   * @since 4.0
   * @return the index the code had in the list or -1
   */
  protected int removeRootCodeInternal(CODE_ID codeId) {
    CODE droppedCode = m_rootCodeMap.get(codeId);
    if (droppedCode == null) {
      return -1;
    }
    int index = -1;
    if (m_rootCodeList != null) {
      for (Iterator<CODE> it = m_rootCodeList.iterator(); it.hasNext();) {
        index++;
        CODE candidateCode = it.next();
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
  public String toString() {
    return "CodeType[id=" + getId() + ", label=" + getText() + "]";
  }

  @Override
  public <T extends ICode<CODE_ID>> boolean visit(ICodeVisitor<T> visitor) {
    return visit(visitor, true);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ICode<CODE_ID>> boolean visit(ICodeVisitor<T> visitor, boolean activeOnly) {
    for (CODE code : getCodes(activeOnly)) {
      if (!visitor.visit((T) code, 0)) {
        return false;
      }
      if (!code.visit(visitor, 1, activeOnly)) {
        return false;
      }
    }
    return true;
  }

  protected Object readResolve() throws ObjectStreamException {
    m_rootCodeMap = new HashMap<CODE_ID, CODE>();
    if (m_rootCodeList == null) {
      m_rootCodeList = new ArrayList<CODE>();
    }
    else {
      for (CODE code : m_rootCodeList) {
        m_rootCodeMap.put(code.getId(), code);
        code.setParentCodeInternal(null);
        code.setCodeTypeInternal(this);
      }
    }
    return this;
  }

  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalCodeTypeWithGenericExtension<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>, OWNER extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>> extends AbstractSerializableExtension<OWNER>
      implements ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, OWNER> {
    private static final long serialVersionUID = 1L;

    public LocalCodeTypeWithGenericExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public List<? extends CODE> execCreateCodes(CodeTypeWithGenericCreateCodesChain chain) {
      return getOwner().execCreateCodes();
    }

    @Override
    public ICode<CODE_ID> execCreateCode(CodeTypeWithGenericCreateCodeChain chain, ICodeRow<CODE_ID> newRow) {
      return getOwner().execCreateCode(newRow);
    }

    @Override
    public List<? extends ICodeRow<CODE_ID>> execLoadCodes(CodeTypeWithGenericLoadCodesChain chain, Class<? extends ICodeRow<CODE_ID>> codeRowType) {
      return getOwner().execLoadCodes(codeRowType);
    }

    @Override
    public void execOverwriteCode(CodeTypeWithGenericOverwriteCodeChain chain, ICodeRow<CODE_ID> oldCode, ICodeRow<CODE_ID> newCode) {
      getOwner().execOverwriteCode(oldCode, newCode);
    }

  }
}
