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
package org.eclipse.scout.rt.client.ui.form.fields;

import java.beans.PropertyChangeListener;
import java.security.Permission;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.w3c.dom.Element;

/**
 * The {@link IForm} and <code>IFormField</code> classes are the prominent classes of the application model: An
 * <code>IForm</code> consists of multiple <code>IFormField</code>s.<br/>
 * A wide variety of form fields exist, the most important are the following:<br/>
 * <ul>
 * <li>{@link IValueField}:<br/>
 * Value fields allow user input through the GUI and contain a value of a certain type. Typical examples are:
 * <ul>
 * <li>{@link org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField IStringField}: A text field containing
 * a single line string (no line breaks).</li>
 * <li>{@link org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField IDateField}: A field containing a
 * formatted date.</li>
 * <li>{@link org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField ISmartField}: A smart field allows to
 * choose from a possibly predefined list of values.</li>
 * </ul>
 * </li>
 * <li>{@link org.eclipse.scout.rt.client.ui.form.fields.button.IButton IButton}:<br/>
 * Buttons allow the user to trigger events on the GUI. Typical examples on a form are the 'Ok' and 'Close' buttons (see
 * {@link org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton AbstractOkButton} or
 * {@link org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton AbstractCancelButton}).</li>
 * <li>{@link IComposite}:<br/>
 * Composite fields group multiple form fields. The most common are:<br/>
 * <ul>
 * <li>{@link IGroupBox}: Groups multiple form fields and draws a border on the GUI.</li>
 * <li>{@link org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox ITabBox}: Groups multiple form fields which are
 * represented within tabs.</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @see IForm
 */
public interface IFormField extends IPropertyObserver, ITypeWithClassId, IOrdered, IStyleable {
  /*
   * Properties
   */
  /**
   * {@link ICompositeField}
   */
  String PROP_PARENT_FIELD = "parentField";
  String PROP_VISIBLE = "visible";
  String PROP_ENABLED = "enabled";
  String PROP_MANDATORY = "mandatory";
  String PROP_ORDER = "order";
  String PROP_ERROR_STATUS = "errorStatus";
  String PROP_TOOLTIP_TEXT = "tooltipText";
  String PROP_FOREGROUND_COLOR = "foregroundColor";
  String PROP_BACKGROUND_COLOR = "backgroundColor";
  String PROP_FONT = "font";
  String PROP_LABEL_FOREGROUND_COLOR = "labelForegroundColor";
  String PROP_LABEL_BACKGROUND_COLOR = "labelBackgroundColor";
  String PROP_LABEL_FONT = "labelFont";
  String PROP_SAVE_NEEDED = "saveNeeded";
  String PROP_EMPTY = "empty";
  String PROP_LABEL = "label";
  String PROP_LABEL_VISIBLE = "labelVisible";
  String PROP_KEY_STROKES = "keyStrokes";
  String PROP_STATUS_VISIBLE = "statusVisible";
  String PROP_STATUS_POSITION = "statusPosition";
  String PROP_GRID_DATA = "gridData";
  String PROP_GRID_DATA_HINTS = "gridDataHints";

  /**
   * if the field is focusable or not, value is of type {@link Boolean}
   */
  String PROP_FOCUSABLE = "focusable";// Build 205

  /**
   * if the field can get the initial focus when the form is opened, value is of type {@link Boolean}
   *
   * @since 6.1
   */
  String PROP_PREVENT_INITIAL_FOCUS = "preventInitialFocus";

  /**
   * Flag to indicate whether this field is currently loading data. Default is <code>false</code>. The exact
   * interpretation of this flag (and also if it should be respected at all) is left to the UI.
   */
  String PROP_LOADING = "loading";

  /**
   * see {@link #getGridDataHints()}<br>
   * this marker value defines the field to have a logical spanning all over the group box width
   */
  int FULL_WIDTH = 0;

  /**
   * position the label at the default location (normally left of the field)<br>
   * see {@link #setLabelPosition(int)} and {@link #getLabelPosition()}
   */
  int LABEL_POSITION_DEFAULT = 0;
  /**
   * position the label left of the field<br>
   * see {@link #setLabelPosition(int)} and {@link #getLabelPosition()}
   */
  int LABEL_POSITION_LEFT = 1;
  /**
   * position the label on the field, meaning that the label is only displayed when the field is empty (vista style)<br>
   * see {@link #setLabelPosition(int)} and {@link #getLabelPosition()}
   */
  int LABEL_POSITION_ON_FIELD = 2;
  /**
   * position the label right of the field<br>
   * see {@link #setLabelPosition(int)} and {@link #getLabelPosition()}
   */
  int LABEL_POSITION_RIGHT = 3;

  /**
   * position the label on the top of the field
   */
  int LABEL_POSITION_TOP = 4;

  /**
   * use the systemwide default label with see {@link #setLabelWidthInPixel(int)} and {@link #getLabelWidthInPixel()}
   */
  int LABEL_WIDTH_DEFAULT = 0;

  /**
   * use the ui-specific "real" with of the label see {@link #setLabelWidthInPixel(int)} and
   * {@link #getLabelWidthInPixel()}
   */
  int LABEL_WIDTH_UI = -1;

  /**
   * use the systemwide default label alignment
   */
  int LABEL_HORIZONTAL_ALIGNMENT_DEFAULT = 1000;

  String STATUS_POSITION_DEFAULT = "default";

  String STATUS_POSITION_TOP = "top";

  IForm getForm();

  /**
   * Use this listener only in very rare cases and only if absolutely needed (performance!)
   */
  void addSubtreePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Use this listener only in very rare cases and only if absolutely needed (performance!)
   */
  void addSubtreePropertyChangeListener(String propName, PropertyChangeListener listener);

  void removeSubtreePropertyChangeListener(PropertyChangeListener listener);

  void removeSubtreePropertyChangeListener(String propName, PropertyChangeListener listener);

  /**
   * do not use this internal method
   */
  void setFormInternal(IForm form);

  /**
   * get the first ancestor of this field (not including this field) which is of type IGroupBox
   */
  IGroupBox getParentGroupBox();

  /**
   * get the first ancestor of this field (not including this field) which is of type ICompositeField
   */
  ICompositeField getParentField();

  /**
   * do not use this internal method
   */
  void setParentFieldInternal(ICompositeField f);

  /**
   * do not use this internal method
   */
  void postInitConfig();

  void initField();

  void disposeField();

  void setView(boolean visible, boolean enabled, boolean mandatory);

  /**
   * create a FormData structure to be sent to the backend the configurator is creating typed subclasses of FormData and
   * FormFieldData
   * <p>
   * Do not override this method
   */
  void exportFormFieldData(AbstractFormFieldData target);

  /**
   * apply FormData to this form field
   * <p>
   * Do not override this method
   */
  void importFormFieldData(AbstractFormFieldData source, boolean valueChangeTriggersEnabled);

  String storeToXmlString();

  void loadFromXmlString(String xml);

  void storeToXml(Element x);

  void loadFromXml(Element x);

  /**
   * add verbose information to the search filter
   */
  void applySearch(SearchFilter search);

  boolean hasProperty(String name);

  /**
   * marks field as changing all model events and property events are cached until the change is done
   * <p>
   * when done, all cached events are sent as a batch
   */
  void setFieldChanging(boolean b);

  boolean isFieldChanging();

  /**
   * This property controls whether value changes are calling {@link AbstractValueField#execChangedValue()}. The
   * {@link IValueField#PROP_VALUE} property change is always fired.
   * <p>
   * When FormFieldData is being applied this property may be set to false, see
   * {@link #importFormFieldData(AbstractFormFieldData, boolean)} to disable sideeffects when the new data is applied to
   * the form
   * <p>
   */
  boolean isValueChangeTriggerEnabled();

  /**
   * This property controls whether value changes are calling {@link AbstractValueField#execChangedValue()} The
   * {@link IValueField#PROP_VALUE} property change is always fired.
   * <p>
   * When FormFieldData is being applied this property may be set to false, see
   * {@link #importFormFieldData(AbstractFormFieldData, boolean)} to disable sideeffects when the new data is applied to
   * the form
   * <p>
   * This property is a transitive property, that means callers need not to care when setting and resetting this
   * property multiple times or nested. But they have to care to set/unset this property inside a try...finally block.
   * <p>
   * recommended: <code><xmp>
   * try{
   *   setValueChangeTriggerEnabled(false);
   *   ...
   * }
   * finally{
   *   setValueChangeTriggerEnabled(true);
   * }
   * </xmp></code>
   * <p>
   * do <b>not</b> write: <code><xmp>
   * boolean oldValue=isValueChangeTriggerEnabled();
   * try{
   *   setValueChangeTriggerEnabled(false);
   *   ...
   * }
   * finally{
   *   setValueChangeTriggerEnabled(oldValue);
   * }
   * </xmp></code>
   */
  void setValueChangeTriggerEnabled(boolean b);

  /**
   * the default field ID is the simple class name of a field
   */
  String getFieldId();

  /**
   * @return Returns the list of fields that are enclosing this field, starting with the furthermost (from outside to
   *         inside). An enclosing field is part of the enclosing classes path that is abstract or the outermost
   *         enclosing class. The latter is the primary type.
   * @since 3.8.1
   */
  List<ICompositeField> getEnclosingFieldList();

  String getLabel();

  void setLabel(String name);

  String getInitialLabel();

  void setInitialLabel(String name);

  /**
   * @since 19.11.2009
   * @return one of the LABEL_POSITION_* constants or a custom constants interpreted by the ui
   */
  int getLabelPosition();

  /**
   * @since 19.11.2009
   * @param pos
   *          one of the LABEL_POSITION_* constants or a custom constants interpreted by the ui
   */
  void setLabelPosition(int pos);

  /**
   * @since 19.11.2009
   * @return the fixed label witdh &gt;0 or LABEL_WIDTH_DEFAULT or LABEL_WIDTH_UI for ui-dependent label width
   */
  int getLabelWidthInPixel();

  /**
   * @since 19.11.2009
   * @param w
   *          the fixed label witdh &gt;0 or LABEL_WIDTH_DEFAULT or LABEL_WIDTH_UI for ui-dependent label width
   */
  void setLabelWidthInPixel(int w);

  /**
   * @since 19.11.2009
   * @return negative for left, 0 for center and positive for right, LABEL_HORIZONTAL_ALIGNMENT_DEFAULT for default of
   *         ui
   */
  int getLabelHorizontalAlignment();

  /**
   * @since 19.11.2009
   * @param a
   *          negative for left, 0 for center and positive for right, LABEL_HORIZONTAL_ALIGNMENT_DEFAULT for default of
   *          ui
   */
  void setLabelHorizontalAlignment(int a);

  /**
   * @return fully qualified label. This is the path in the container tree
   */
  String getFullyQualifiedLabel(String separator);

  boolean isLabelVisible();

  void setLabelVisible(boolean b);

  /**
   * Meta property over labelVisible<br>
   * This property is used to suppress the label even if it is visible<br>
   * see {@link org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox ISequenceBox} where the label of the
   * first visible child field is suppressed and appended to the range box's label
   */
  boolean isLabelSuppressed();

  void setLabelSuppressed(boolean b);

  Object getProperty(String name);

  /**
   * With this method it's possible to set (custom) properties.
   * <p>
   * <b>Important: </b> Although this method is intended to be used for custom properties, it's actually possible to
   * change main properties as well. Keep in mind that directly changing main properties may result in unexpected
   * behavior, so do it only if you really know what you are doing. Rather use the officially provided api instead. <br>
   * Example for an unexpected behavior: setVisible() does not only set the property PROP_VISIBLE but also executes
   * additional code. This code would NOT be executed by directly setting the property PROP_VISIBLE with setProperty().
   */
  void setProperty(String name, Object value);

  boolean isInitialized();

  Permission getEnabledPermission();

  void setEnabledPermission(Permission p);

  /**
   * Enabling of a field has two levels: granting and property level.
   * <p>
   * This is the "harder" level of granting.
   * <p>
   * Note that a field with grantEnabled=false remains disabled even though setEnabled(true) was called.
   */
  boolean isEnabledGranted();

  /**
   * Enabling of a field has two levels: granting and property level.
   * <p>
   * This is the "softer" level of property.
   * <p>
   * Note that a field with grantEnabled=false remains disabled even though setEnabled(true) was called.
   */
  boolean getEnabledProperty();

  void setEnabledGranted(boolean b);

  /**
   * This property is used by buttons. Buttons set the property to false while in work.
   *
   * @return true if process button is not in {@link org.eclipse.scout.rt.client.ui.form.fields.button.IButton#doClick()
   *         IButton#doClick()} action
   */
  boolean isEnabledProcessingButton();

  void setEnabledProcessingButton(boolean b);

  /**
   * @return {@link #isEnabledGranted()} &amp;&amp; {@link #getEnabledProperty()}
   */
  boolean isEnabled();

  /**
   * do NOT override this method
   */
  void setEnabled(boolean b);

  Permission getVisiblePermission();

  void setVisiblePermission(Permission p);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  boolean isVisible();

  void setVisible(boolean b);

  boolean isMandatory();

  void setMandatory(boolean b);

  /**
   * Adds an error status to field. use {@link ScoutFieldStatus} in order to set a custom icon.
   *
   * @param newStatus
   */
  void addErrorStatus(IStatus newStatus);

  /**
   * Adds an error status of type {@link DefaultFieldStatus} with the given message to the field.
   *
   * @param message
   */
  void addErrorStatus(String message);

  /**
   * Removes all error status of the given type.
   */
  void removeErrorStatus(Class<? extends IStatus> statusClazz);

  /**
   * @return null iff no status (e.g. error status) is set, non-null if a status is set, e.g. if the value has semantic
   *         errors.
   */
  IMultiStatus getErrorStatus();

  /**
   * @param status
   *          Sets the error multi-status. Use {@link #addErrorStatus(IStatus)} to add a single error.
   */
  void setErrorStatus(IMultiStatus status);

  /**
   * clear all error statuses
   */
  void clearErrorStatus();

  /**
   * @return true if field content (value on value fields) is valid, no error status is set on field and mandatory
   *         property is met. Shorthand form for {@link #getContentProblemDescriptor()==null}
   */
  boolean isContentValid();

  /**
   * @return either null when everything is valid or a problem descriptor that contains more details.
   */
  IValidateContentDescriptor validateContent();

  String getTooltipText();

  void setTooltipText(String text);

  /**
   * Rebuild the {@link IFormField#PROP_KEY_STROKES} property using the internal set of properties and by calling
   * {@link #getContributedKeyStrokes()} and {@link #getLocalKeyStrokes()}
   */
  void updateKeyStrokes();

  /**
   * @return local and contributed key strokes
   */
  List<IKeyStroke> getKeyStrokes();

  String getForegroundColor();

  void setForegroundColor(String c);

  String getBackgroundColor();

  void setBackgroundColor(String c);

  FontSpec getFont();

  void setFont(FontSpec f);

  String getLabelForegroundColor();

  void setLabelForegroundColor(String c);

  String getLabelBackgroundColor();

  void setLabelBackgroundColor(String c);

  FontSpec getLabelFont();

  void setLabelFont(FontSpec f);

  /**
   * @return the grid data hints used by the {@link org.eclipse.scout.rt.client.ui.form.fields.internal.GridDataBuilder
   *         GridDataBuilder} to create the final grid data which can be accessed using {@link #getGridData()}.
   */
  GridData getGridDataHints();

  void setGridDataHints(GridData data);

  /**
   * @return the resulting (validated) grid data which is also used by the ui layout manager to layout this field in a
   *         logical grid.
   */
  GridData getGridData();

  /**
   * Sets the life {@link GridData} of this field.<br>
   * Do not use this internal method, for grid layout hints use {@link #setGridDataHints(GridData)}.
   */
  void setGridDataInternal(GridData data);

  /**
   * true if the field has data that requires save
   */
  boolean isSaveNeeded();

  void checkSaveNeeded();

  /**
   * mark form so that<br>
   * {@link IFormField#isSaveNeeded()} returns true
   */
  void touch();

  void markSaved();

  /**
   * true if the field does not contain data (semantics)
   */
  boolean isEmpty();

  boolean isFocusable();

  void setFocusable(boolean b);

  boolean isPreventInitialFocus();

  void setPreventInitialFocus(boolean preventInitialFocus);

  /**
   * Convenience for {@link IForm#requestFocus(IFormField)}
   */
  void requestFocus();

  /**
   * MasterSlave
   */
  IValueField getMasterField();

  void setMasterField(IValueField field);

  boolean isMasterRequired();

  void setMasterRequired(boolean b);

  // commodity helper
  Object getMasterValue();

  /**
   * Returns whether or not the status icon is visible.
   *
   * @return {@code true} if status icon is visible, {@code false} otherwise
   */
  boolean isStatusVisible();

  /**
   * Sets whether or not the status icon is visible.
   *
   * @param statusVisible
   *          {@code true} if status icon should be visible, {@code false} otherwise
   */
  void setStatusVisible(boolean statusVisible);

  String getStatusPosition();

  void setStatusPosition(String statusPosition);

  /**
   * @return true, if the mandatory property is fulfilled (a value set or not mandatory)
   */
  boolean isMandatoryFulfilled();

  void setLoading(boolean loading);

  boolean isLoading();
}
