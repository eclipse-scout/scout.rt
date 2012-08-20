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
package org.eclipse.scout.rt.client.ui.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.StringWriter;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.FormData;
import org.eclipse.scout.commons.annotations.FormData.SdkCommand;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.beans.BasicPropertySupport;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.ProcessingStatus;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.xmlparser.SimpleXmlElement;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.services.common.search.ISearchFilterService;
import org.eclipse.scout.rt.client.ui.DataChangeListener;
import org.eclipse.scout.rt.client.ui.WeakDataChangeListener;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.PrintDevice;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder;
import org.eclipse.scout.rt.client.ui.profiler.DesktopProfiler;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

@FormData(value = AbstractFormFieldData.class, sdkCommand = SdkCommand.USE)
public abstract class AbstractFormField extends AbstractPropertyObserver implements IFormField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractFormField.class);

  private IForm m_form;
  private boolean m_initialized;
  // special property/members
  // enabled is defined as: enabledGranted && enabledProperty && enabledSlave && enabledProcessing
  private Permission m_enabledPermission;
  private boolean m_enabledGranted;
  private boolean m_enabledProperty;
  private boolean m_enabledSlave;
  private boolean m_enabledProcessingButton;
  // visible is defined as: visibleGranted && visibleProperty
  private Permission m_visiblePermission;
  private boolean m_visibleGranted;
  private boolean m_visibleProperty;
  private int m_valueChangeTriggerEnabled = 1;// >=1 is true
  // master/slave
  private IValueField<?> m_masterField;
  private boolean m_masterRequired;
  // auto layout
  private GridData m_gridData;
  private GridData m_gridDataHints;
  // label visibility
  private boolean m_labelVisible;
  private boolean m_labelSuppressed;
  private int m_labelPosition;
  private int m_labelWidthInPixel;
  private int m_labelHorizontalAlignment;
  // force save needed
  private boolean m_touched;
  //
  private BasicPropertySupport m_subtreePropertyChangeSupport;
  private P_MasterListener m_currentMasterListener;// my master
  private DataChangeListener m_internalDataChangeListener;
  //
  private String m_initialLabel;

  public AbstractFormField() {
    this(true);
  }

  public AbstractFormField(boolean callInitializer) {
    if (DesktopProfiler.getInstance().isEnabled()) {
      DesktopProfiler.getInstance().registerFormField(this);
    }
    m_enabledGranted = true;
    m_enabledSlave = true;
    m_enabledProcessingButton = true;
    m_visibleGranted = true;
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    if (!m_initialized) {
      try {
        setValueChangeTriggerEnabled(false);
        //
        initConfig();
      }
      finally {
        setValueChangeTriggerEnabled(true);
      }
      m_initialized = true;
    }
  }

  public static String parseFormFieldId(String className) {
    String s = className;
    int i = Math.max(s.lastIndexOf('$'), s.lastIndexOf('.'));
    s = s.substring(i + 1);
    return s;
  }

  /**
   * @deprecated processing logic belongs to server
   */
  @Deprecated
  protected String getConfiguredSearchTerm() {
    return null;
  }

  @Deprecated
  public final String getLegacySearchTerm() {
    return getConfiguredSearchTerm();
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  @ConfigPropertyValue("null")
  protected String getConfiguredLabel() {
    return null;
  }

  /**
   * One of the LABEL_POSITION_* constants or a custom constants interpreted by
   * the ui.
   * 
   * @since 17.11.2009
   */
  @ConfigProperty(ConfigProperty.LABEL_POSITION)
  @Order(15)
  @ConfigPropertyValue("LABEL_POSITION_DEFAULT")
  protected int getConfiguredLabelPosition() {
    return LABEL_POSITION_DEFAULT;
  }

  /**
   * @since 19.11.2009
   * @return the fixed label witdh &gt;0 or LABEL_WIDTH_DEFAULT or
   *         LABEL_WIDTH_UI for ui-dependent label width
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(16)
  @ConfigPropertyValue("LABEL_WIDTH_DEFAULT")
  protected int getConfiguredLabelWidthInPixel() {
    return LABEL_WIDTH_DEFAULT;
  }

  /**
   * @since 19.11.2009
   * @return negative for left, 0 for center and positive for right,
   *         LABEL_HORIZONTAL_ALIGNMENT_DEFAULT for default of ui
   */
  @ConfigProperty(ConfigProperty.LABEL_HORIZONTAL_ALIGNMENT)
  @Order(17)
  @ConfigPropertyValue("LABEL_HORIZONTAL_ALIGNMENT_DEFAULT")
  protected int getConfiguredLabelHorizontalAlignment() {
    return LABEL_HORIZONTAL_ALIGNMENT_DEFAULT;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredLabelVisible() {
    return true;
  }

  /**
   * affects only the filed itself. in case of a composite field initially the property
   * does not gets broadcasted.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(30)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredEnabled() {
    return true;
  }

  /**
   * affects only the filed itself. in case of a composite field initially the property
   * does not gets broadcasted.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredVisible() {
    return true;
  }

  /**
   * affects only the filed itself. in case of a composite field initially the property
   * does not gets broadcasted.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(45)
  @ConfigPropertyValue("false")
  @ValidationRule(ValidationRule.MANDATORY)
  protected boolean getConfiguredMandatory() {
    return false;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(50)
  @ConfigPropertyValue("null")
  protected String getConfiguredTooltipText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.COLOR)
  @Order(60)
  @ConfigPropertyValue("null")
  protected String getConfiguredForegroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.COLOR)
  @Order(70)
  @ConfigPropertyValue("null")
  protected String getConfiguredBackgroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.FONT)
  @Order(80)
  @ConfigPropertyValue("null")
  protected String getConfiguredFont() {
    return null;
  }

  @ConfigProperty(ConfigProperty.HORIZONTAL_ALIGNMENT)
  @Order(85)
  @ConfigPropertyValue("-1")
  protected int getConfiguredHorizontalAlignment() {
    return -1;
  }

  @ConfigProperty(ConfigProperty.VERTICAL_ALIGNMENT)
  @Order(86)
  @ConfigPropertyValue("-1")
  protected int getConfiguredVerticalAlignment() {
    return -1;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(87)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredFillHorizontal() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(88)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredFillVertical() {
    return true;
  }

  /**
   * This sets the logical layout property hint for X. This is a hint only and
   * can be accessed using {@link #getGridDataHints()}. The resulting
   * (validated) grid properties that are also used in the final gui layouting
   * are accessed using {@link #getGridData()} and are validated by {@link GridDataBuilder}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(90)
  @ConfigPropertyValue("-1")
  protected int getConfiguredGridX() {
    return -1;
  }

  /**
   * This sets the logical layout property hint for Y. This is a hint only and
   * can be accessed using {@link #getGridDataHints()}. The resulting
   * (validated) grid properties that are also used in the final gui layouting
   * are accessed using {@link #getGridData()} and are validated by {@link GridDataBuilder}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(95)
  @ConfigPropertyValue("-1")
  protected int getConfiguredGridY() {
    return -1;
  }

  /**
   * This sets the logical layout property hint for W. This is a hint only and
   * can be accessed using {@link #getGridDataHints()}. The resulting
   * (validated) grid properties that are also used in the final gui layouting
   * are accessed using {@link #getGridData()} and are validated by {@link GridDataBuilder}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(100)
  @ConfigPropertyValue("1")
  protected int getConfiguredGridW() {
    return 1;
  }

  /**
   * This sets the logical layout property hint for H. This is a hint only and
   * can be accessed using {@link #getGridDataHints()}. The resulting
   * (validated) grid properties that are also used in the final gui layouting
   * are accessed using {@link #getGridData()} and are validated by {@link GridDataBuilder}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(105)
  @ConfigPropertyValue("1")
  protected int getConfiguredGridH() {
    return 1;
  }

  /**
   * This sets the logical layout property hint for weightX. This is a hint only
   * and can be accessed using {@link #getGridDataHints()}. The resulting
   * (validated) grid properties that are also used in the final gui layouting
   * are accessed using {@link #getGridData()} and are validated by {@link GridDataBuilder}
   * <p>
   * weightX is by default 1.0
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(130)
  @ConfigPropertyValue("-1")
  protected double getConfiguredGridWeightX() {
    return -1;
  }

  /**
   * This sets the logical layout property hint for weightY. This is a hint only
   * and can be accessed using {@link #getGridDataHints()}. The resulting
   * (validated) grid properties that are also used in the final gui layouting
   * are accessed using {@link #getGridData()} and are validated by {@link GridDataBuilder}
   * <p>
   * weightY is by default 0.0 when the field has H=1, and H-1 when the field H is larger than 1
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(140)
  @ConfigPropertyValue("-1")
  protected double getConfiguredGridWeightY() {
    return -1;
  }

  /**
   * This sets the logical layout property hint for useUiWidth. This is a hint
   * only and can be accessed using {@link #getGridDataHints()}. The resulting
   * (validated) grid properties that are also used in the final gui layouting
   * are accessed using {@link #getGridData()} and are validated by {@link GridDataBuilder}
   * <p>
   * useUiWidth is by default false. true makes the layout manager to use the ui original preferred width to layout the
   * field in the group box
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(142)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredGridUseUiWidth() {
    return false;
  }

  /**
   * This sets the logical layout property hint for useUiHeight. This is a hint
   * only and can be accessed using {@link #getGridDataHints()}. The resulting
   * (validated) grid properties that are also used in the final gui layouting
   * are accessed using {@link #getGridData()} and are validated by {@link GridDataBuilder}
   * <p>
   * useUiHeight is by default false. true makes the layout manager to use the ui original preferred height to layout
   * the field in the group box
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(142)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredGridUseUiHeight() {
    return false;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(150)
  @ConfigPropertyValue("0")
  protected int getConfiguredWidthInPixel() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(160)
  @ConfigPropertyValue("0")
  protected int getConfiguredHeightInPixel() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.MASTER_FIELD)
  @Order(170)
  @ConfigPropertyValue("null")
  @ValidationRule(ValidationRule.MASTER_VALUE_FIELD)
  protected Class<? extends IValueField> getConfiguredMasterField() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(180)
  @ConfigPropertyValue("false")
  @ValidationRule(ValidationRule.MASTER_VALUE_REQUIRED)
  protected boolean getConfiguredMasterRequired() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(190)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredFocusable() {
    return true;
  }

  @ConfigProperty(ConfigProperty.DOC)
  @Order(20)
  @ConfigPropertyValue("null")
  protected String getConfiguredDoc() {
    return null;
  }

  private Class<? extends IKeyStroke>[] getConfiguredKeyStrokes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
  }

  @ConfigOperation
  @Order(10)
  protected void execInitField() throws ProcessingException {
  }

  /**
   * On any value change or call to {@link #checkSaveNeeded()} this method is
   * called to calculate if the field needs save
   */
  @ConfigOperation
  @Order(11)
  protected boolean execIsSaveNeeded() throws ProcessingException {
    return false;
  }

  /**
   * Make field saved, for example a table is maring all rows as non-changed
   */
  @ConfigOperation
  @Order(12)
  protected void execMarkSaved() throws ProcessingException {
  }

  /**
   * on any value change or call to {@link #checkEmpty()} this method is called
   * to calculate if the field represents an empty state (semantics)
   */
  @ConfigOperation
  @Order(13)
  protected boolean execIsEmpty() throws ProcessingException {
    return true;
  }

  /**
   * see {@link IDesktop#dataChanged(Object...)}
   */
  @ConfigOperation
  @Order(14)
  protected void execDataChanged(Object... dataTypes) throws ProcessingException {
  }

  @ConfigOperation
  @Order(15)
  protected void execDisposeField() throws ProcessingException {
  }

  @Override
  public final void applySearch(SearchFilter search) {
    execAddSearchTerms(search);
  }

  /**
   * add verbose information to the search filter
   */
  protected void execAddSearchTerms(SearchFilter search) {
    applySearchInternal(search);
  }

  /**
   * override this method to apply new default handling
   */
  protected void applySearchInternal(final SearchFilter search) {
    ISearchFilterService sfs = SERVICES.getService(ISearchFilterService.class);
    if (sfs != null) {
      sfs.applySearchDelegate(this, search, true);
    }
  }

  /**
   * AFTER a new valid master value was stored, this method is called
   */
  @ConfigOperation
  @Order(50)
  protected void execChangedMasterValue(Object newMasterValue) throws ProcessingException {
  }

  protected void initConfig() {
    m_gridData = new GridData(-1, -1, 1, 1, -1, -1);
    m_gridDataHints = new GridData(-1, -1, 1, 1, -1, -1);
    propertySupport.setPropertyBool(PROP_EMPTY, true);
    setEnabled(getConfiguredEnabled());
    setVisible(getConfiguredVisible());
    setMandatory(getConfiguredMandatory());
    setTooltipText(getConfiguredTooltipText());
    setInitialLabel(getConfiguredLabel());
    setLabel(getConfiguredLabel());
    setLabelPosition(getConfiguredLabelPosition());
    setLabelWidthInPixel(getConfiguredLabelWidthInPixel());
    setLabelHorizontalAlignment(getConfiguredLabelHorizontalAlignment());
    setLabelVisible(getConfiguredLabelVisible());
    if (getConfiguredBackgroundColor() != null) {
      setBackgroundColor((getConfiguredBackgroundColor()));
    }
    if (getConfiguredForegroundColor() != null) {
      setForegroundColor((getConfiguredForegroundColor()));
    }
    if (getConfiguredFont() != null) {
      setFont(FontSpec.parse(getConfiguredFont()));
    }
    setFocusable(getConfiguredFocusable());
    setGridDataHints(new GridData(getConfiguredGridX(), getConfiguredGridY(), getConfiguredGridW(), getConfiguredGridH(), getConfiguredGridWeightX(), getConfiguredGridWeightY(), getConfiguredGridUseUiWidth(), getConfiguredGridUseUiHeight(), getConfiguredHorizontalAlignment(), getConfiguredVerticalAlignment(), getConfiguredFillHorizontal(), getConfiguredFillVertical(), getConfiguredWidthInPixel(), getConfiguredHeightInPixel()));
    setMasterRequired(getConfiguredMasterRequired());
    // private listener for subtree property change events
    addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        fireSubtreePropertyChange(e);
      }
    });
  }

  @Override
  public boolean isInitialized() {
    return m_initialized;
  }

  /**
   * do not use this method
   */
  @Override
  public void postInitConfig() throws ProcessingException {
    // key strokes, now all inner fields are built
    updateKeyStrokes();
    // master listener, now the inner field is available
    if (getConfiguredMasterField() != null) {
      IValueField master = findNearestFieldByClass(getConfiguredMasterField());
      setMasterField(master);
    }
  }

  /**
   * Searching the nearest field implementing the specified class by processing the enclosing field list bottom-up.
   */
  private <T extends IFormField> T findNearestFieldByClass(final Class<T> c) {
    List<ICompositeField> enclosingFields = getEnclosingFieldList();
    if (enclosingFields.isEmpty()) {
      // there are no enclosing fields (i.e. this field is not part of a field template)
      return getForm().getFieldByClass(c);
    }

    final Holder<T> found = new Holder<T>(c);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      @SuppressWarnings("unchecked")
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getClass() == c) {
          found.setValue((T) field);
        }
        return found.getValue() == null;
      }
    };

    // search requested field within critical parent field
    Collections.reverse(enclosingFields);
    for (ICompositeField parentField : enclosingFields) {
      parentField.visitFields(v, 0);
      if (found.getValue() != null) {
        return found.getValue();
      }
    }

    // field has not been found in a critical parent field
    return getForm().getFieldByClass(c);
  }

  /**
   * This is the init of the runtime model after the form and fields are built
   * and configured
   */
  @Override
  public final void initField() throws ProcessingException {
    try {
      setValueChangeTriggerEnabled(false);
      //
      initFieldInternal();
      execInitField();
    }
    finally {
      setValueChangeTriggerEnabled(true);
    }
  }

  protected void initFieldInternal() throws ProcessingException {
    checkSaveNeeded();
    checkEmpty();
  }

  @Override
  public final void disposeField() {
    try {
      disposeFieldInternal();
    }
    catch (Throwable t) {
      LOG.warn("Field " + getClass().getName(), t);
    }
    try {
      execDisposeField();
    }
    catch (Throwable t) {
      LOG.warn("Field " + getClass().getName(), t);
    }
  }

  protected void disposeFieldInternal() {
  }

  /**
   * Register a {@link DataChangeListener} on the desktop for these dataTypes<br>
   * Example:
   * 
   * <pre>
   * registerDataChangeListener(CRMEnum.Company, CRMEnum.Project, CRMEnum.Task);
   * </pre>
   */
  public void registerDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener == null) {
      m_internalDataChangeListener = new WeakDataChangeListener() {
        @Override
        public void dataChanged(Object... innerDataTypes) throws ProcessingException {
          execDataChanged(innerDataTypes);
        }
      };
    }
    IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
    if (desktop == null) {
      desktop = ClientSyncJob.getCurrentSession().getVirtualDesktop();
    }
    desktop.addDataChangeListener(m_internalDataChangeListener, dataTypes);
  }

  /**
   * Unregister the {@link DataChangeListener} from the desktop for these
   * dataTypes<br>
   * Example:
   * 
   * <pre>
   * unregisterDataChangeListener(CRMEnum.Company, CRMEnum.Project, CRMEnum.Task);
   * </pre>
   */
  public void unregisterDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener != null) {
      ClientSyncJob.getCurrentSession().getDesktop().removeDataChangeListener(m_internalDataChangeListener, dataTypes);
    }
  }

  protected void fireSubtreePropertyChange(PropertyChangeEvent e) {
    // fire up the tree
    IFormField parentField = getParentField();
    if (parentField instanceof AbstractFormField) {
      ((AbstractFormField) parentField).fireSubtreePropertyChange(e);
    }
    // fire my level
    if (m_subtreePropertyChangeSupport != null) {
      m_subtreePropertyChangeSupport.firePropertyChange(e);
    }
  }

  @Override
  public IForm getForm() {
    return m_form;
  }

  @Override
  public IGroupBox getParentGroupBox() {
    ICompositeField f = getParentField();
    while (f != null && !(f instanceof IGroupBox)) {
      f = f.getParentField();
    }
    return (IGroupBox) f;
  }

  @Override
  public ICompositeField getParentField() {
    return (ICompositeField) propertySupport.getProperty(PROP_PARENT_FIELD);
  }

  @Override
  public void setParentFieldInternal(ICompositeField f) {
    propertySupport.setProperty(PROP_PARENT_FIELD, f);
  }

  /**
   * do not use this internal method
   */
  @Override
  public void setFormInternal(IForm form) {
    m_form = form;
  }

  @Override
  public String toString() {
    return getLabel() + "/" + getFieldId() + " (" + getClass().getName() + ")";
  }

  @Override
  public void printField(PrintDevice device, Map<String, Object> parameters) {
    getForm().printField(this, device, parameters);
  }

  @Override
  public void setView(boolean visible, boolean enabled, boolean mandatory) {
    setVisible(visible);
    setEnabled(enabled);
    setMandatory(mandatory);
  }

  @Override
  public boolean isValueChangeTriggerEnabled() {
    return m_valueChangeTriggerEnabled >= 1;
  }

  @Override
  public void setValueChangeTriggerEnabled(boolean b) {
    if (b) {
      m_valueChangeTriggerEnabled++;
    }
    else {
      m_valueChangeTriggerEnabled--;
    }
  }

  @Override
  public void addSubtreePropertyChangeListener(PropertyChangeListener listener) {
    if (listener != null && m_subtreePropertyChangeSupport == null) {
      m_subtreePropertyChangeSupport = new BasicPropertySupport(this);
    }
    m_subtreePropertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addSubtreePropertyChangeListener(String propName, PropertyChangeListener listener) {
    if (listener != null && m_subtreePropertyChangeSupport == null) {
      m_subtreePropertyChangeSupport = new BasicPropertySupport(this);
    }
    m_subtreePropertyChangeSupport.addPropertyChangeListener(propName, listener);
  }

  @Override
  public void removeSubtreePropertyChangeListener(PropertyChangeListener listener) {
    if (m_subtreePropertyChangeSupport != null) {
      m_subtreePropertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

  @Override
  public void removeSubtreePropertyChangeListener(String propName, PropertyChangeListener listener) {
    if (m_subtreePropertyChangeSupport != null) {
      m_subtreePropertyChangeSupport.removePropertyChangeListener(propName, listener);
    }
  }

  @Override
  public boolean hasProperty(String name) {
    return propertySupport.hasProperty(name);
  }

  @Override
  public boolean isFieldChanging() {
    return propertySupport.isPropertiesChanging();
  }

  @Override
  public void setFieldChanging(boolean b) {
    propertySupport.setPropertiesChanging(b);
  }

  @Override
  public String getFieldId() {
    return getClass().getSimpleName();
  }

  /*
   * Data i/o
   */
  @Override
  public void exportFormFieldData(AbstractFormFieldData target) throws ProcessingException {
  }

  @Override
  public void importFormFieldData(AbstractFormFieldData source, boolean valueChangeTriggersEnabled) throws ProcessingException {
  }

  /*
   * XML i/o
   */
  @Override
  public void storeXML(SimpleXmlElement x) throws ProcessingException {
    List<ICompositeField> enclosingFieldList = getEnclosingFieldList();
    for (ICompositeField field : enclosingFieldList) {
      SimpleXmlElement enclosingField = new SimpleXmlElement("enclosingField");
      setXmlFormFieldIds(enclosingField, field);
      // Enclosing fields are traversed from outside to inside. Hence add XML child at the end.
      x.addChild(enclosingField);
    }
    // set field ids
    setXmlFormFieldIds(x, this);
  }

  private List<ICompositeField> getEnclosingFieldList() {
    List<ICompositeField> enclosingFieldList = new ArrayList<ICompositeField>();
    // compute enclosing field path
    Class<?> currentEnclosingFieldType = ConfigurationUtility.getEnclosingContainerType(this);
    ICompositeField p = getParentField();
    while (p != null) {
      Class<?> enclosingFieldType = ConfigurationUtility.getEnclosingContainerType(p);
      if (enclosingFieldType != currentEnclosingFieldType) {
        // Enclosing fields are traversed from inside to outside, but the path of enclosing
        // elements should be from outside to inside. Hence add XML child at the beginning.
        enclosingFieldList.add(0, p);
        currentEnclosingFieldType = enclosingFieldType;
      }
      p = p.getParentField();
    }
    return enclosingFieldList;
  }

  private void setXmlFormFieldIds(SimpleXmlElement x, IFormField f) {
    x.setAttribute("fieldId", f.getFieldId());
    x.setAttribute("fieldQname", f.getClass().getName());
  }

  @Override
  public void loadXML(SimpleXmlElement x) throws ProcessingException {
  }

  @Override
  public final void setXML(String xml) throws ProcessingException {
    if (xml == null) {
      return;
    }
    try {
      SimpleXmlElement root = new SimpleXmlElement();
      root.parseString(xml);
      loadXML(root);
    }
    catch (Exception e) {
      throw new ProcessingException("Error in AbstractFormField.setXML: ", e);
    }
  }

  @Override
  public final String getXML() throws ProcessingException {
    SimpleXmlElement x = new SimpleXmlElement("field");
    storeXML(x);
    StringWriter sw = new StringWriter();
    try {
      x.writeDocument(sw, null, "UTF-8");
    }
    catch (java.io.IOException ioe) {/* never */
    }
    return sw.toString();
  }

  @Override
  public String getInitialLabel() {
    return m_initialLabel;
  }

  @Override
  public void setInitialLabel(String name) {
    m_initialLabel = name;
  }

  @Override
  public String getLabel() {
    return propertySupport.getPropertyString(PROP_LABEL);
  }

  @Override
  public void setLabel(String name) {
    propertySupport.setPropertyString(PROP_LABEL, name);
  }

  @Override
  public int getLabelPosition() {
    return m_labelPosition;
  }

  @Override
  public void setLabelPosition(int position) {
    m_labelPosition = position;
  }

  @Override
  public int getLabelWidthInPixel() {
    return m_labelWidthInPixel;
  }

  @Override
  public void setLabelWidthInPixel(int w) {
    m_labelWidthInPixel = w;
  }

  @Override
  public int getLabelHorizontalAlignment() {
    return m_labelHorizontalAlignment;
  }

  @Override
  public void setLabelHorizontalAlignment(int a) {
    m_labelHorizontalAlignment = a;
  }

  @Override
  public String getFullyQualifiedLabel(String separator) {
    StringBuffer b = new StringBuffer();
    IFormField p = getParentField();
    if (p != null) {
      String s = p.getFullyQualifiedLabel(separator);
      if (s != null) {
        b.append(s);
      }
    }
    String s = getLabel();
    if (s != null) {
      if (b.length() > 0) {
        b.append(separator);
      }
      b.append(s);
    }
    return b.toString();
  }

  @Override
  public boolean isLabelVisible() {
    return propertySupport.getPropertyBool(PROP_LABEL_VISIBLE);
  }

  @Override
  public void setLabelVisible(boolean b) {
    m_labelVisible = b;
    calculateLabelVisible();
  }

  private void calculateLabelVisible() {
    propertySupport.setPropertyBool(PROP_LABEL_VISIBLE, m_labelVisible && (!m_labelSuppressed));
  }

  @Override
  public boolean isLabelSuppressed() {
    return m_labelSuppressed;
  }

  @Override
  public void setLabelSuppressed(boolean b) {
    m_labelSuppressed = b;
    calculateLabelVisible();
  }

  @Override
  public Object getCustomProperty(String propName) {
    return propertySupport.getProperty(propName);
  }

  @Override
  public void setCustomProperty(String propName, Object o) {
    propertySupport.setProperty(propName, o);
  }

  @Override
  public Permission getEnabledPermission() {
    return m_enabledPermission;
  }

  @Override
  public void setEnabledPermission(Permission p) {
    m_enabledPermission = p;
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      /*
       * inherited permission from container
       */
      ICompositeField container = getParentField();
      if (container != null) {
        b = container.isEnabledGranted();
      }
      else {
        b = getForm().isEnabledGranted();
      }
    }
    setEnabledGranted(b);
  }

  @Override
  public boolean isEnabledGranted() {
    return m_enabledGranted;
  }

  @Override
  public boolean getEnabledProperty() {
    return m_enabledProperty;
  }

  @Override
  public void setEnabledGranted(boolean b) {
    m_enabledGranted = b;
    calculateEnabled();
  }

  @Override
  public boolean isEnabledProcessingButton() {
    return m_enabledProcessingButton;
  }

  @Override
  public void setEnabledProcessingButton(boolean b) {
    m_enabledProcessingButton = b;
    calculateEnabled();
  }

  @Override
  public void setEnabled(boolean b) {
    m_enabledProperty = b;
    if (b) {
      m_enabledSlave = true;
    }
    calculateEnabled();
  }

  /**
   * no access control for system buttons CANCEL and CLOSE
   */
  protected void calculateEnabled() {
    // access control
    boolean applyAccessControl = true;
    if (this instanceof IButton) {
      IButton but = (IButton) this;
      switch (but.getSystemType()) {
        case IButton.SYSTEM_TYPE_CANCEL:
        case IButton.SYSTEM_TYPE_CLOSE: {
          applyAccessControl = false;
          break;
        }
      }
    }
    if (applyAccessControl) {
      propertySupport.setPropertyBool(PROP_ENABLED, m_enabledGranted && m_enabledProperty && m_enabledSlave);
    }
    else {
      propertySupport.setPropertyBool(PROP_ENABLED, m_enabledProperty && m_enabledSlave);
    }
  }

  @Override
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  @Override
  public Permission getVisiblePermission() {
    return m_visiblePermission;
  }

  @Override
  public void setVisiblePermission(Permission p) {
    m_visiblePermission = p;
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      /*
       * inherite permission from container
       */
      ICompositeField container = getParentField();
      if (container != null) {
        b = container.isVisibleGranted();
      }
      else {
        b = true;
      }
    }
    setVisibleGranted(b);
  }

  /**
   * for thread-safety-reason this method is final
   */
  @Override
  public final boolean isSaveNeeded() {
    return propertySupport.getPropertyBool(PROP_SAVE_NEEDED);
  }

  /**
   * Default implementation just calls {@link #execIsSaveNeeded()}<br>
   * For thread-safety-reason this method is final
   * 
   * @throws ProcessingException
   */
  @Override
  public final void checkSaveNeeded() {
    if (isInitialized()) {
      try {
        propertySupport.setPropertyBool(PROP_SAVE_NEEDED, m_touched || execIsSaveNeeded());
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  @Override
  public void touch() {
    m_touched = true;
    checkSaveNeeded();
  }

  /**
   * Default implementation does nothing
   */
  @Override
  public final void markSaved() {
    try {
      m_touched = false;
      execMarkSaved();
      checkSaveNeeded();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  @Override
  public final boolean isEmpty() {
    return propertySupport.getPropertyBool(PROP_EMPTY);
  }

  /**
   * Default implementation just calls {@link #execIsEmpty()}
   * 
   * @throws ProcessingException
   */
  protected final void checkEmpty() {
    if (isInitialized()) {
      try {
        propertySupport.setPropertyBool(PROP_EMPTY, execIsEmpty());
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
    }
  }

  @Override
  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  @Override
  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    calculateVisibleInternal();
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public void setVisible(boolean b) {
    m_visibleProperty = b;
    calculateVisibleInternal();
  }

  /**
   * do not use this internal method
   */
  protected boolean execCalculateVisible() {
    return true;
  }

  /**
   * do not use this internal method, there is no access control for system
   * buttons CANCEL and CLOSE
   */
  protected void calculateVisibleInternal() {
    // access control
    boolean applyAccessControl = true;
    if (this instanceof IButton) {
      IButton but = (IButton) this;
      switch (but.getSystemType()) {
        case IButton.SYSTEM_TYPE_CANCEL:
        case IButton.SYSTEM_TYPE_CLOSE: {
          applyAccessControl = false;
          break;
        }
      }
    }
    boolean changed;
    if (applyAccessControl) {
      changed = propertySupport.setPropertyBool(PROP_VISIBLE, m_visibleGranted && m_visibleProperty && execCalculateVisible());
    }
    else {
      changed = propertySupport.setPropertyBool(PROP_VISIBLE, m_visibleProperty && execCalculateVisible());
    }
    if (changed) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
    }
  }

  @Override
  public boolean isMandatory() {
    return propertySupport.getPropertyBool(PROP_MANDATORY);
  }

  @Override
  public void setMandatory(boolean b) {
    propertySupport.setPropertyBool(PROP_MANDATORY, b);
  }

  @Override
  public IProcessingStatus getErrorStatus() {
    return (IProcessingStatus) propertySupport.getProperty(PROP_ERROR_STATUS);
  }

  @Override
  public void setErrorStatus(String message) {
    setErrorStatus(new ProcessingStatus(message, null, 0, IProcessingStatus.ERROR));
  }

  @Override
  public void setErrorStatus(IProcessingStatus status) {
    propertySupport.setProperty(PROP_ERROR_STATUS, status);
  }

  @Override
  public void clearErrorStatus() {
    propertySupport.setProperty(PROP_ERROR_STATUS, null);
  }

  @Override
  public IValidateContentDescriptor validateContent() {
    if (!isContentValid()) {
      return new ValidateFormFieldDescriptor(this);
    }
    return null;
  }

  @Override
  public boolean isContentValid() {
    IProcessingStatus errorStatus = getErrorStatus();
    if (errorStatus != null && (errorStatus.getSeverity() == IProcessingStatus.ERROR || errorStatus.getSeverity() == IProcessingStatus.FATAL)) {
      return false;
    }
    /*
    if (isMandatory()) {
      //nop
    }
    */
    return true;
  }

  @Override
  public void setTooltipText(String text) {
    propertySupport.setPropertyString(PROP_TOOLTIP_TEXT, text);
  }

  @Override
  public String getTooltipText() {
    return propertySupport.getPropertyString(PROP_TOOLTIP_TEXT);
  }

  @Override
  public void setForegroundColor(String c) {
    propertySupport.setProperty(PROP_FOREGROUND_COLOR, c);
  }

  @Override
  public String getForegroundColor() {
    return (String) propertySupport.getProperty(PROP_FOREGROUND_COLOR);
  }

  @Override
  public void setBackgroundColor(String c) {
    propertySupport.setProperty(PROP_BACKGROUND_COLOR, c);
  }

  @Override
  public String getBackgroundColor() {
    return (String) propertySupport.getProperty(PROP_BACKGROUND_COLOR);
  }

  @Override
  public void setFont(FontSpec f) {
    propertySupport.setProperty(PROP_FONT, f);
  }

  @Override
  public FontSpec getFont() {
    return (FontSpec) propertySupport.getProperty(PROP_FONT);
  }

  @Override
  public GridData getGridData() {
    return new GridData(m_gridData);
  }

  @Override
  public void setGridDataInternal(GridData data) {
    m_gridData = new GridData(data);
  }

  @Override
  public GridData getGridDataHints() {
    return new GridData(m_gridDataHints);
  }

  @Override
  public void setGridDataHints(GridData hints) {
    m_gridDataHints = new GridData(hints);
  }

  @Override
  public void requestFocus() {
    IForm form = getForm();
    if (form != null) {
      form.requestFocus(this);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean fetchFocusRequested() {
    return false;
  }

  @Override
  public void setFocusable(boolean b) {
    propertySupport.setPropertyBool(PROP_FOCUSABLE, b);
  }

  @Override
  public boolean isFocusable() {
    return propertySupport.getPropertyBool(PROP_FOCUSABLE);
  }

  @Override
  public void setMasterField(IValueField field) {
    IValueField oldMasterField = getMasterField();
    // remove old listener
    if (oldMasterField != null) {
      if (m_currentMasterListener != null) {
        oldMasterField.removeMasterListener(m_currentMasterListener);
        m_currentMasterListener = null;
      }
    }
    // add new listener and set enabling
    if (field != null) {
      field.addMasterListener(m_currentMasterListener = new P_MasterListener());
      m_enabledSlave = (field.getValue() != null || !isMasterRequired());
      setEnabledGranted(m_enabledGranted);
    }
    m_masterField = field;
  }

  @Override
  public IValueField getMasterField() {
    return m_masterField;
  }

  // commodity helper
  @Override
  public Object getMasterValue() {
    if (getMasterField() != null) {
      return getMasterField().getValue();
    }
    return null;
  }

  @Override
  public void setMasterRequired(boolean b) {
    m_masterRequired = b;
  }

  @Override
  public boolean isMasterRequired() {
    return m_masterRequired;
  }

  @Override
  public void updateKeyStrokes() {
    HashMap<String, IKeyStroke> ksMap = new HashMap<String, IKeyStroke>();
    //
    IKeyStroke[] c = getLocalKeyStrokes();
    if (c != null) {
      for (IKeyStroke ks : c) {
        if (ks != null) {
          ksMap.put(ks.getKeyStroke().toUpperCase(), ks);
        }
      }
    }
    //
    c = getContributedKeyStrokes();
    if (c != null) {
      for (IKeyStroke ks : c) {
        if (ks != null) {
          ksMap.put(ks.getKeyStroke().toUpperCase(), ks);
        }
      }
    }
    propertySupport.setProperty(PROP_KEY_STROKES, ksMap.values().toArray(new IKeyStroke[ksMap.size()]));
  }

  @Override
  public IKeyStroke[] getContributedKeyStrokes() {
    return null;
  }

  @Override
  public IKeyStroke[] getLocalKeyStrokes() {
    HashMap<String, IKeyStroke> ksMap = new HashMap<String, IKeyStroke>();
    Class<? extends IKeyStroke>[] shortcutArray = getConfiguredKeyStrokes();
    for (int i = 0; i < shortcutArray.length; i++) {
      IKeyStroke ks;
      try {
        ks = ConfigurationUtility.newInnerInstance(this, shortcutArray[i]);
        ksMap.put(ks.getKeyStroke().toUpperCase(), ks);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("keyStroke: " + shortcutArray[i].getName(), t));
      }
    }
    return ksMap.values().toArray(new IKeyStroke[ksMap.size()]);
  }

  @Override
  public IKeyStroke[] getKeyStrokes() {
    IKeyStroke[] keyStrokes = (IKeyStroke[]) propertySupport.getProperty(PROP_KEY_STROKES);
    if (keyStrokes == null) {
      keyStrokes = new IKeyStroke[0];
    }
    return keyStrokes;
  }

  private class P_MasterListener implements MasterListener {
    @Override
    public void masterChanged(Object newMasterValue) {
      // only active if the unique listener itself
      if (this == m_currentMasterListener) {
        m_enabledSlave = (newMasterValue != null || !isMasterRequired());
        setEnabledGranted(m_enabledGranted);
        try {
          execChangedMasterValue(newMasterValue);
        }
        catch (ProcessingException e) {
          SERVICES.getService(IExceptionHandlerService.class).handleException(e);
        }
      }
    }
  }// end class

}
