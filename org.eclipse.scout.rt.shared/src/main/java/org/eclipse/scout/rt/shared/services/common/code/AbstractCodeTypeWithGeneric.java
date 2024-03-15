/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
import java.util.Map.Entry;

import org.eclipse.scout.rt.api.data.code.CodeTypeDo;
import org.eclipse.scout.rt.dataobject.mapping.ToDoFunctionHelper;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodeChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericCreateCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericLoadCodesChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.CodeTypeWithGenericChains.CodeTypeWithGenericOverwriteCodeChain;
import org.eclipse.scout.rt.shared.extension.services.common.code.ICodeTypeExtension;
import org.eclipse.scout.rt.shared.extension.services.common.code.MoveCodesHandler;
import org.eclipse.scout.rt.shared.services.common.code.mapping.ICodeTypeToDoFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("119a9156-45e7-4f32-9b55-01aa6b5283d7")
public abstract class AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>> implements ICodeType<CODE_TYPE_ID, CODE_ID>, IContributionOwner, Serializable {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractCodeTypeWithGeneric.class);
  private static final long serialVersionUID = 1L;

  private boolean m_initialized;
  private String m_text;
  private String m_textPlural;
  private String m_iconId;
  private boolean m_hierarchy;
  private int m_maxLevel;
  private transient Map<CODE_ID, CODE> m_rootCodeMap = new HashMap<>();
  private List<CODE> m_rootCodeList = new ArrayList<>();
  private transient Map<CODE_ID, Integer> m_codeIndexMap = new HashMap<>();
  protected IContributionOwner m_contributionHolder;
  private final ObjectExtensions<AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>, ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> m_objectExtensions;

  public AbstractCodeTypeWithGeneric() {
    this(true);
  }

  public AbstractCodeTypeWithGeneric(boolean callInitializer) {
    m_objectExtensions = new ObjectExtensions<>(this, true);
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

  @Override
  public final <T> T optContribution(Class<T> contribution) {
    return m_contributionHolder.optContribution(contribution);
  }

  protected void callInitializer() {
    if (!m_initialized) {
      interceptInitConfig();
      m_initialized = true;
    }
  }

  protected final List<Class<? extends ICode>> getConfiguredCodes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(ConfigurationUtility.filterClasses(dca, ICode.class), ICode.class);
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

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(50)
  protected String getConfiguredTextPlural() {
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
    List<Class<? extends ICode>> configuredCodes = getConfiguredCodes();
    List<ICode> contributedCodes = m_contributionHolder.getContributionsByClass(ICode.class);
    OrderedCollection<CODE> codes = new OrderedCollection<>();
    for (Class<? extends ICode> codeClazz : configuredCodes) {
      CODE code = (CODE) ConfigurationUtility.newInnerInstance(this, codeClazz);
      if (codeClazz.isAnnotationPresent(Order.class)) {
        code.setOrder(codeClazz.getAnnotation(Order.class).value());
      }
      codes.addOrdered(code);
    }
    for (ICode c : contributedCodes) {
      Class<? extends ICode> codeClazz = c.getClass();
      if (codeClazz.isAnnotationPresent(Order.class)) {
        c.setOrder(codeClazz.getAnnotation(Order.class).value());
      }
      codes.addOrdered((CODE) c);
    }

    new MoveCodesHandler<>(codes).moveModelObjects();
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
        throw new ProcessingException("Could not create a new instance of code row! Override the execCreateCode method.", e);
      }
    }
    else {
      // try to create mutable code
      if (codeClazz.isAssignableFrom(MutableCode.class)) {
        return (CODE) new MutableCode<>(newRow);
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
    if (newCode.getCssClass() == null) {
      newCode.withCssClass(oldCode.getCssClass());
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
    m_objectExtensions.initConfig(createLocalExtension(), this::initConfig);
  }

  protected void initConfig() {
    m_text = getConfiguredText();
    m_textPlural = getConfiguredTextPlural();
    m_iconId = getConfiguredIconId();
    m_hierarchy = getConfiguredIsHierarchy();
    m_maxLevel = getConfiguredMaxLevel();
    m_contributionHolder = new ContributionComposite(this);
    loadCodes();
  }

  protected ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>> createLocalExtension() {
    return new LocalCodeTypeWithGenericExtension<>(this);
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
   */
  protected final List<? extends CODE> interceptCreateCodes() {
    List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> extensions = getAllExtensions();
    CodeTypeWithGenericCreateCodesChain<CODE_TYPE_ID, CODE_ID, CODE> chain = new CodeTypeWithGenericCreateCodesChain<>(extensions);
    return chain.execCreateCodes();
  }

  /**
   * The extension support for {@link AbstractCodeTypeWithGeneric#execCreateCode(ICodeRow)} method.
   */
  protected final CODE interceptCreateCode(ICodeRow<CODE_ID> newRow) {
    List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> extensions = getAllExtensions();
    CodeTypeWithGenericCreateCodeChain<CODE_TYPE_ID, CODE_ID, CODE> chain = new CodeTypeWithGenericCreateCodeChain<>(extensions);
    return chain.execCreateCode(newRow);
  }

  /**
   * The extension support for {@link AbstractCodeTypeWithGeneric#execLoadCodes(Class)} method.
   */
  protected List<? extends ICodeRow<CODE_ID>> interceptLoadCodes(Class<? extends ICodeRow<CODE_ID>> codeRowType) {
    List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> extensions = getAllExtensions();
    CodeTypeWithGenericLoadCodesChain<CODE_TYPE_ID, CODE_ID, CODE> chain = new CodeTypeWithGenericLoadCodesChain<>(extensions);
    return chain.execLoadCodes(codeRowType);
  }

  protected void interceptOverwriteCode(ICodeRow<CODE_ID> oldCode, ICodeRow<CODE_ID> newCode) {
    List<? extends ICodeTypeExtension<CODE_TYPE_ID, CODE_ID, ? extends AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE>>> extensions = getAllExtensions();
    CodeTypeWithGenericOverwriteCodeChain<CODE_TYPE_ID, CODE_ID, CODE> chain = new CodeTypeWithGenericOverwriteCodeChain<>(extensions);
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
  public String getTextPlural() {
    return m_textPlural;
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
      for (CODE childCode : m_rootCodeList) {
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
    if (extKey == null) {
      return null;
    }
    for (CODE childCode : m_rootCodeList) {
      if (extKey.equals(childCode.getExtKey())) {
        return childCode;
      }
      CODE c = (CODE) childCode.getChildCodeByExtKey(extKey);
      if (c != null) {
        return c;
      }
    }
    return null;
  }

  protected void rebuildCodeIndexMap() {
    m_codeIndexMap = new HashMap<>();
    ICodeVisitor<ICode<CODE_ID>> v = new ICodeVisitor<>() {
      private int m_index = 0;

      @Override
      public boolean visit(ICode<CODE_ID> code, int treeLevel) {
        m_codeIndexMap.put(code.getId(), m_index);
        m_index++;
        return true;
      }
    };
    visit(v, false);
  }

  @Override
  public int getCodeIndex(final CODE_ID id) {
    return ObjectUtility.nvl(m_codeIndexMap.get(id), -1);
  }

  @Override
  public int getCodeIndex(final ICode<CODE_ID> c) {
    if (c == null) {
      return -1;
    }
    return getCodeIndex(c.getId());
  }

  @Override
  public List<? extends CODE> getCodes() {
    return getCodes(true);
  }

  @Override
  public List<? extends CODE> getCodes(boolean activeOnly) {
    List<CODE> list = new ArrayList<>(m_rootCodeList);
    if (activeOnly) {
      list.removeIf(code -> !code.isActive());
    }
    return list;
  }

  protected void loadCodes() {
    m_rootCodeMap = new HashMap<>();
    m_rootCodeList = new ArrayList<>();
    //
    // 1a create unconnected codes and assign to type
    List<CODE> allCodesOrdered = new ArrayList<>();
    Map<CODE, CODE> codeToParentCodeMap = new HashMap<>();
    Map<CODE_ID, CODE> idToCodeMap = new HashMap<>();
    // 1a add configured codes
    List<? extends CODE> createdList = interceptCreateCodes();
    if (createdList != null) {
      visit(createdList, (CODE code, int treeLevel) -> {
        allCodesOrdered.add(code);
        idToCodeMap.put(code.getId(), code);
        codeToParentCodeMap.put(code, code.getParentCode() != null ? idToCodeMap.get(code.getParentCode().getId()) : null);
        return true;
      }, false);
    }
    // 1b add dynamic codes
    List<? extends ICodeRow<CODE_ID>> result = interceptLoadCodes(getConfiguredCodeRowType());
    if (result != null && !result.isEmpty()) {
      Map<CODE, CODE_ID> codeToParentIdMap = new HashMap<>();
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
          CODE_ID parentId = newRow.getParentKey();
          codeToParentIdMap.put(newCode, parentId);
        }
        else if (existingCode != null) {
          // remove old (and then re-add) to preserve dynamic ordering.
          allCodesOrdered.remove(existingCode);
          allCodesOrdered.add(existingCode);
        }
      }
      for (Entry<CODE, CODE_ID> e : codeToParentIdMap.entrySet()) {
        CODE code = e.getKey();
        CODE_ID parentId = e.getValue();
        CODE parentCode = null;
        if (parentId != null) {
          parentCode = idToCodeMap.get(parentId);
          if (parentCode == null) {
            LOG.warn("parent code for {} not found: id={}", code, parentId);
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
    // 3 mark all children of inactive codes also as inactive
    visit((code, treeLevel) -> {
      final ICode<CODE_ID> parentCode = code.getParentCode();
      if (parentCode != null && !parentCode.isActive() && code.isActive() && code instanceof AbstractCode<?>) {
        ((AbstractCode<?>) code).setActiveInternal(false);
      }
      return true;
    }, false);
    // 4 rebuild code indices
    rebuildCodeIndexMap();
  }

  /**
   * do not use this internal method unless the intention is in fact to change the structure of the possibly shared
   * {@link ICodeType}
   * <p>
   * Add a new root code, overwrite (drop) existing root code
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
    return "CodeType[id=" + getId() + ", text=" + getText() + "]";
  }

  @Override
  public <T extends ICode<CODE_ID>> boolean visit(ICodeVisitor<T> visitor) {
    return visit(visitor, true);
  }

  @Override
  public <T extends ICode<CODE_ID>> boolean visit(ICodeVisitor<T> visitor, boolean activeOnly) {
    return visit(getCodes(activeOnly), visitor, activeOnly);
  }

  @SuppressWarnings("unchecked")
  protected <T extends ICode<CODE_ID>> boolean visit(List<? extends CODE> codes, ICodeVisitor<T> visitor, boolean activeOnly) {
    for (CODE code : codes) {
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
    m_rootCodeMap = new HashMap<>();
    if (m_rootCodeList == null) {
      m_rootCodeList = new ArrayList<>();
    }
    else {
      for (CODE code : m_rootCodeList) {
        m_rootCodeMap.put(code.getId(), code);
        code.setParentCodeInternal(null);
        code.setCodeTypeInternal(this);
      }
    }
    rebuildCodeIndexMap();

    return this;
  }

  @Override
  public String classId() {
    return ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass());
  }

  @Override
  public CodeTypeDo toDo() {
    return BEANS.get(ToDoFunctionHelper.class).toDo(this, ICodeTypeToDoFunction.class);
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
