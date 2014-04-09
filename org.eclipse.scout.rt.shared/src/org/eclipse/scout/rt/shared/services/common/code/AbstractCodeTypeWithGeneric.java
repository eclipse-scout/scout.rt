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
package org.eclipse.scout.rt.shared.services.common.code;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.MatrixUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.IntegerHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractCodeTypeWithGeneric<CODE_TYPE_ID, CODE_ID, CODE extends ICode<CODE_ID>> implements ICodeType<CODE_TYPE_ID, CODE_ID>, Serializable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCodeTypeWithGeneric.class);
  private static final long serialVersionUID = 1L;

  private boolean m_initialized;
  private String m_text;
  private String m_iconId;
  private boolean m_hierarchy;
  private int m_maxLevel;
  private transient Map<CODE_ID, CODE> m_rootCodeMap = new HashMap<CODE_ID, CODE>();
  private List<CODE> m_rootCodeList = new ArrayList<CODE>();

  public AbstractCodeTypeWithGeneric() {
    this(true);
  }

  public AbstractCodeTypeWithGeneric(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      initConfig();
      m_initialized = true;
    }
  }

  public AbstractCodeTypeWithGeneric(String label, boolean hierarchy) {
    m_text = label;
    m_hierarchy = hierarchy;
  }

  @SuppressWarnings("unchecked")
  protected final List<Class<? extends CODE>> getConfiguredCodes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ICode>> filtered = ConfigurationUtility.filterClasses(dca, ICode.class);
    List<Class<? extends ICode>> sortFilteredClassesByOrderAnnotation = ConfigurationUtility.sortFilteredClassesByOrderAnnotation(filtered, ICode.class);
    List<Class<? extends CODE>> result = new ArrayList<Class<? extends CODE>>();
    for (Class<? extends ICode> codeClazz : sortFilteredClassesByOrderAnnotation) {
      result.add((Class<? extends CODE>) codeClazz);
    }
    return result;
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

  /**
   * @Deprecated: Use a {@link ClassId} annotation as key for Doc-Text. Will be removed in the 5.0 Release.
   */
  @Deprecated
  @Order(110)
  protected String getConfiguredDoc() {
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
  @ConfigOperation
  @Order(1)
  protected List<? extends CODE> execCreateCodes() throws ProcessingException {
    List<CODE> list = new ArrayList<CODE>();
    for (Class<? extends ICode> codeClazz : getConfiguredCodes()) {
      try {
        @SuppressWarnings("unchecked")
        CODE code = (CODE) ConfigurationUtility.newInnerInstance(this, codeClazz);
        list.add(code);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    return list;
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
  protected CODE execCreateCode(ICodeRow<CODE_ID> newRow) throws ProcessingException {
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
   * This method is called on server side to load additional dynamic codes to the {@link #execCreateCodes()} list<br>
   * Sample for sql call:
   * 
   * <pre>
   * String sql =
   *     &quot;SELECT key,text,iconId,tooltipText,backgroundColor,foregroundColor,font,active,parentKey,extKey,calcValue,enabled,partitionId &quot; +
   *         &quot;FROM TABLE &quot; +
   *         &quot;WHERE ...&quot;;
   * Object[][] data = SERVICES.getService(ISqlService.class).select(sql, new Object[]{});
   * return createCodeRowArray(data);
   * </pre>
   */
  @ConfigOperation
  @Order(10)
  protected List<? extends ICodeRow<CODE_ID>> execLoadCodes(Class<? extends ICodeRow<CODE_ID>> codeRowType) throws ProcessingException {
    return null;
  }

  /**
   * When there are configured codes (inner classes) that are overwritten by {@link #execLoadCodes()} then this method
   * is
   * called to give a chance to merge attributes of the old configured code to the new dynamic code.
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
  protected void execOverwriteCode(ICodeRow<CODE_ID> oldCode, ICodeRow<CODE_ID> newCode) throws ProcessingException {
    if (newCode instanceof CodeRow) {
      CodeRow editableRow = (CodeRow) newCode;
      if (editableRow.getBackgroundColor() == null) {
        editableRow.setBackgroundColor(oldCode.getBackgroundColor());
      }
      if (editableRow.getFont() == null) {
        editableRow.setFont(oldCode.getFont());
      }
      if (editableRow.getForegroundColor() == null) {
        editableRow.setForegroundColor(oldCode.getForegroundColor());
      }
      if (editableRow.getIconId() == null) {
        editableRow.setIconId(oldCode.getIconId());
      }
      if (editableRow.getExtKey() == null) {
        editableRow.setExtKey(oldCode.getExtKey());
      }
      if (editableRow.getValue() == null) {
        editableRow.setValue(oldCode.getValue());
      }
    }
  }

  protected void initConfig() {
    m_text = getConfiguredText();
    m_iconId = getConfiguredIconId();
    m_hierarchy = getConfiguredIsHierarchy();
    m_maxLevel = getConfiguredMaxLevel();
    try {
      loadCodes();
    }
    catch (ProcessingException e) {
      e.addContextMessage(ScoutTexts.get("CodeTypeInit") + " " + m_text);
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  /**
   * Convenience function to sort data for later call to {@link #createCodeRowArray(Object[][])} <br>
   * The sort indices are 0-based.
   * 
   * @deprecated use {@link MatrixUtility} directly. Will be removed with M-Release.
   */
  @Deprecated
  public static void sortData(Object[][] data, int... sortColumns) {
    MatrixUtility.sort(data, sortColumns);
  }

  /**
   * see {@link #createCodeRowArray(Object[][], int)}
   * 
   * @deprecated use {@link CodeRow#CodeRow(Object[], int)} instead. Will be removed with the M-Release.
   */
  @Deprecated
  public static CodeRow[] createCodeRowArray(Object[][] data) {
    return createCodeRowArray(data, data != null && data.length > 0 ? data[0].length : 0);
  }

  /**
   * Convenience function to transform Object[][] data into CodeRow[]
   * 
   * @param data
   *          The Object[][] must contain rows with the elements in the
   *          following order: <br>
   *          Object key <br>
   *          String text <br>
   *          String iconId <br>
   *          String tooltipText <br>
   *          String backgroundColor <br>
   *          String foregroundColor <br>
   *          String font <br>
   *          Long active (0 or 1) <br>
   *          Object parentKey <br>
   *          String extKey <br>
   *          Double calcValue <br>
   *          Long enabled (0 or 1) <br>
   *          Long partitionId
   * @param maxColumnIndex
   *          the maximum column index to be used to create the code rows, all
   *          column indexes >= columnCount are ignored
   * @deprecated use {@link CodeRow#CodeRow(Object[], int)} instead. Will be removed with the M-Release.
   */
  @Deprecated
  public static CodeRow[] createCodeRowArray(Object[][] data, int maxColumnIndex) {
    if (data == null || data.length == 0) {
      return new CodeRow[0];
    }
    else {
      CodeRow[] a = new CodeRow[data.length];
      for (int i = 0; i < data.length; i++) {
        a[i] = new CodeRow(data[i], maxColumnIndex);
      }
      return a;
    }
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
    return Collections.unmodifiableList(list);
  }

  private void loadCodes() throws ProcessingException {
    m_rootCodeMap = new HashMap<CODE_ID, CODE>();
    m_rootCodeList = new ArrayList<CODE>();
    //
    // 1a create unconnected codes and assign to type
    List<CODE> allCodesOrdered = new ArrayList<CODE>();
    Map<CODE, CODE> codeToParentCodeMap = new HashMap<CODE, CODE>();
    Map<CODE_ID, CODE> idToCodeMap = new HashMap<CODE_ID, CODE>();
    // 1a add configured codes
    List<? extends CODE> createdList = execCreateCodes();
    if (createdList != null) {
      for (CODE code : createdList) {
        allCodesOrdered.add(code);
        idToCodeMap.put(code.getId(), code);
        codeToParentCodeMap.put(code, null);
      }
    }
    // 1b add dynamic codes
    List<? extends ICodeRow<CODE_ID>> result = execLoadCodes(getConfiguredCodeRowType());
    if (result != null && result.size() > 0) {
      HashMap<CODE, CODE_ID> codeToParentIdMap = new HashMap<CODE, CODE_ID>();
      // create unconnected codes and assign to type
      for (ICodeRow<CODE_ID> newRow : result) {
        CODE existingCode = idToCodeMap.get(newRow.getKey());
        if (existingCode != null) {
          // There is already a static code with same id.
          execOverwriteCode(existingCode.toCodeRow(), newRow);
        }
        CODE newCode = execCreateCode(newRow);
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
        parentCode.addChildCodeInternal(code);
      }
      else {
        this.addChildCodeInternal(code);
      }
    }
    //3 mark all chidren of inactive codes also as inactive
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

  private void addChildCodeInternal(CODE code) {
    code.setCodeTypeInternal(this);
    code.setParentCodeInternal(null);
    m_rootCodeMap.put(code.getId(), code);
    m_rootCodeList.add(code);
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
}
