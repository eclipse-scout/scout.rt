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
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.dimension.IEnabledDimension;
import org.eclipse.scout.rt.shared.dimension.IVisibleDimension;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
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
public interface IFormField extends IPropertyObserver, ITypeWithClassId, IOrdered, IStyleable, IVisibleDimension, IEnabledDimension {
  /*
   * Properties
   */
  /**
   * {@link ICompositeField}
   */
  String PROP_PARENT_FIELD = "parentField";
  String PROP_VISIBLE = "visible";
  String PROP_ENABLED = "enabled";
  String PROP_ENABLED_COMPUTED = "enabledComputed";
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
   * Style to apply when the field is rendered as "disabled".
   * <p>
   * Note that this property may be moved to the not-yet-existing "IWidget" in the future.
   */
  String PROP_DISABLED_STYLE = "disabledStyle";

  /**
   * position the label at the default location (normally left of the field)<br>
   * see {@link #setLabelPosition(int)} and {@link #getLabelPosition()}
   */
  byte LABEL_POSITION_DEFAULT = 0;
  /**
   * position the label left of the field<br>
   * see {@link #setLabelPosition(int)} and {@link #getLabelPosition()}
   */
  byte LABEL_POSITION_LEFT = 1;
  /**
   * position the label on the field, meaning that the label is only displayed when the field is empty (vista style)<br>
   * see {@link #setLabelPosition(int)} and {@link #getLabelPosition()}
   */
  byte LABEL_POSITION_ON_FIELD = 2;
  /**
   * position the label right of the field<br>
   * see {@link #setLabelPosition(int)} and {@link #getLabelPosition()}
   */
  byte LABEL_POSITION_RIGHT = 3;

  /**
   * position the label on the top of the field
   */
  byte LABEL_POSITION_TOP = 4;

  /**
   * see {@link #getGridDataHints()}<br>
   * this marker value defines the field to have a logical spanning all over the group box width
   */
  int FULL_WIDTH = 0;

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
   * use the default label alignment of the UI
   */
  byte LABEL_HORIZONTAL_ALIGNMENT_DEFAULT = 127;

  /**
   * left alignment
   */
  byte LABEL_HORIZONTAL_ALIGNMENT_LEFT = -1;

  /**
   * center alignment
   */
  byte LABEL_HORIZONTAL_ALIGNMENT_CENTER = 0;

  /**
   * right alignment
   */
  byte LABEL_HORIZONTAL_ALIGNMENT_RIGHT = 1;

  String STATUS_POSITION_DEFAULT = "default";

  String STATUS_POSITION_TOP = "top";

  int DISABLED_STYLE_DEFAULT = 0;

  int DISABLED_STYLE_READ_ONLY = 1;

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
  byte getLabelPosition();

  /**
   * @since 19.11.2009
   * @param pos
   *          one of the LABEL_POSITION_* constants or a custom constants interpreted by the ui
   */
  void setLabelPosition(byte pos);

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
  byte getLabelHorizontalAlignment();

  /**
   * @since 19.11.2009
   * @param a
   *          negative for left, 0 for center and positive for right, LABEL_HORIZONTAL_ALIGNMENT_DEFAULT for default of
   *          ui
   */
  void setLabelHorizontalAlignment(byte a);

  /**
   * @return fully qualified label. This is the path in the container tree
   */
  String getFullyQualifiedLabel(String separator);

  Object getProperty(String name);

  /**
   * With this method it's possible to set (custom) properties.
   * <p>
   * <b>Important: </b> Although this method is intended to be used for custom properties, it's actually possible to
   * change main properties as well. Keep in mind that directly changing main properties may result in unexpected
   * behavior, so do it only if necessary. Rather use the provided API instead.<br>
   */
  void setProperty(String name, Object value);

  boolean isInitialized();

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

  /**
   * @return If the label of this {@link IFormField} is visible. It is visible if all label-visibility dimensions are
   *         <code>true</code>.
   */
  boolean isLabelVisible();

  /**
   * Changes the value of the default label-visibility dimension to the given value.
   *
   * @param b
   *          The new value specifying if the label of this {@link IFormField} is visible.
   */
  void setLabelVisible(boolean b);

  /**
   * Checks if the given label-visibility dimension is set to <code>true</code>.
   *
   * @param dimension
   *          The dimension to check. Must not be <code>null</code>.
   * @return <code>true</code> if the given dimension is set to visible. By default all dimensions are visible.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. This {@link IFormField} supports up to
   *           {@link NamedBitMaskHelper#NUM_BITS} dimensions for label visibility. One dimension is already used by the
   *           {@link IFormField} itself. Therefore 7 dimensions may be used by developers.<br>
   *           <b>Note:</b> these dimensions are shared amongst all {@link IFormField}s of an application. They are not
   *           available by instance but by class!
   */
  boolean isLabelVisible(String dimension);

  /**
   * Changes the label-visibility value of the given dimension.
   *
   * @param visible
   *          The new value for the given dimension.
   * @param dimension
   *          The dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. This {@link IFormField} supports up to
   *           {@link NamedBitMaskHelper#NUM_BITS} dimensions for label visibility. One dimension is already used by the
   *           {@link IFormField} itself. Therefore 7 dimensions may be used by developers.<br>
   *           <b>Note:</b> these dimensions are shared amongst all {@link IFormField}s of an application. They are not
   *           available by instance but by class!
   */
  void setLabelVisible(boolean visible, String dimension);

  /**
   * @return <code>true</code> if this {@link IFormField} and all parent {@link IFormField}s are visible (all
   *         dimensions).
   * @see #isVisible()
   */
  boolean isVisibleIncludingParents();

  /**
   * @return If this {@link IFormField} is visible. It is visible if all visibility-dimensions are <code>true</code>.
   */
  boolean isVisible();

  /**
   * Changes the visible property of this {@link IFormField} to the given value.
   *
   * @param visible
   *          The new visible value.
   */
  void setVisible(boolean b);

  /**
   * Changes the visible property of this {@link IFormField} to the given value.
   *
   * @param visible
   *          The new visible value.
   * @param updateParents
   *          if <code>true</code> the visible property of all parent {@link IFormField}s are updated to same value as
   *          well.
   */
  void setVisible(boolean visible, boolean updateParents);

  /**
   * Changes the visible property of this {@link IFormField} to the given value.
   *
   * @param visible
   *          The new visible value.
   * @param updateParents
   *          if <code>true</code> the visible property of all parent {@link IFormField}s are updated to same value as
   *          well.
   * @param updateChildren
   *          if <code>true</code> the visible property of all child {@link IFormField}s (recursive) are updated to same
   *          value as well.
   */
  void setVisible(boolean visible, boolean updateParents, boolean updateChildren);

  /**
   * Sets a new visible-permission that is used to calculate the visible-granted property of this {@link IFormField}.
   *
   * @param p
   *          The new {@link Permission} that is used to calculate the visible-granted value.
   * @see IAccessControlService#checkPermission(Permission)
   * @see #setVisibleGranted(boolean)
   */
  void setVisiblePermission(Permission p);

  /**
   * @return The visible-permission of this {@link IFormField}.
   */
  Permission getVisiblePermission();

  /**
   * @return The visible-granted property of this {@link IFormField}.
   */
  boolean isVisibleGranted();

  /**
   * Changes the visible-granted property of this {@link IFormField} to the given value.
   *
   * @param visible
   *          The new visible-granted value.
   */
  void setVisibleGranted(boolean b);

  /**
   * Changes the visible-granted property of this {@link IFormField} to the given value.
   *
   * @param visible
   *          The new visible-granted value.
   * @param updateParents
   *          if <code>true</code> the visible-granted property of all parent {@link IFormField}s are updated to same
   *          value as well.
   */
  void setVisibleGranted(boolean visible, boolean updateParents);

  /**
   * Changes the visible-granted property of this {@link IFormField} to the given value.
   *
   * @param visible
   *          The new visible-granted value.
   * @param updateParents
   *          if <code>true</code> the visible-granted property of all parent {@link IFormField}s are updated to same
   *          value as well.
   * @param updateChildren
   *          if <code>true</code> the visible-granted property of all child {@link IFormField}s (recursive) are updated
   *          to same value as well.
   */
  void setVisibleGranted(boolean visible, boolean updateParents, boolean updateChildren);

  /**
   * Changes the field-visibility value of the given dimension.
   *
   * @param visible
   *          The new visibility value for the given dimension.
   * @param dimension
   *          The dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. This {@link IFormField} supports up to
   *           {@link NamedBitMaskHelper#NUM_BITS} dimensions for visibility. Two dimensions are already used by the
   *           {@link IFormField} itself ({@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}). Therefore 6
   *           dimensions may be used by developers.<br>
   *           <b>Note:</b> these dimensions are shared amongst all {@link IFormField}s of an application. They are not
   *           available by instance but by class!
   */
  @Override
  void setVisible(boolean visible, String dimension);

  /**
   * Changes the field-visibility value of the given dimension.
   *
   * @param visible
   *          The new visibility value for the given dimension.
   * @param updateParents
   *          if <code>true</code> all parent {@link IFormField}s are updated to same value as well.
   * @param dimension
   *          The dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. This {@link IFormField} supports up to
   *           {@link NamedBitMaskHelper#NUM_BITS} dimensions for visibility. Two dimensions are already used by the
   *           {@link IFormField} itself ({@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}). Therefore 6
   *           dimensions may be used by developers.<br>
   *           <b>Note:</b> these dimensions are shared amongst all {@link IFormField}s of an application. They are not
   *           available by instance but by class!
   */
  void setVisible(boolean visible, boolean updateParents, String dimension);

  /**
   * Changes the field-visibility value of the given dimension.
   *
   * @param visible
   *          The new visibility value for the given dimension.
   * @param updateParents
   *          if <code>true</code> all parent {@link IFormField}s are updated to same value as well.
   * @param updateChildren
   *          if <code>true</code> all child {@link IFormField}s (recursive) are updated to same value as well.
   * @param dimension
   *          The dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. This {@link IFormField} supports up to
   *           {@link NamedBitMaskHelper#NUM_BITS} dimensions for visibility. Two dimensions are already used by the
   *           {@link IFormField} itself ({@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}). Therefore 6
   *           dimensions may be used by developers.<br>
   *           <b>Note:</b> these dimensions are shared amongst all {@link IFormField}s of an application. They are not
   *           available by instance but by class!
   */
  void setVisible(boolean visible, boolean updateParents, boolean updateChildren, String dimension);

  /**
   * @return <code>true</code> if this {@link IFormField} and all parent {@link IFormField}s are enabled (all
   *         dimensions).
   * @see #isEnabled()
   */
  boolean isEnabledIncludingParents();

  /**
   * @return If this {@link IFormField} is enabled. It is enabled if all enabled-dimensions are <code>true</code>.
   */
  boolean isEnabled();

  /**
   * @return The enabled property value of this {@link IFormField}.
   */
  boolean getEnabledProperty();

  /**
   * Changes the enabled property of this {@link IFormField} to the given value.
   *
   * @param enabled
   *          The new enabled value.
   */
  void setEnabled(boolean b);

  /**
   * Changes the enabled property of this {@link IFormField} to the given value.
   *
   * @param enabled
   *          The new enabled value.
   * @param updateParents
   *          if <code>true</code> the enabled property of all parent {@link IFormField}s are updated to same value as
   *          well.
   */
  void setEnabled(boolean enabled, boolean updateParents);

  /**
   * Changes the enabled property of this {@link IFormField} to the given value.
   *
   * @param enabled
   *          The new enabled value.
   * @param updateParents
   *          if <code>true</code> the enabled property of all parent {@link IFormField}s are updated to same value as
   *          well.
   * @param updateChildren
   *          if <code>true</code> the enabled property of all child {@link IFormField}s (recursive) are updated to same
   *          value as well.
   */
  void setEnabled(boolean enabled, boolean updateParents, boolean updateChildren);

  /**
   * @return The enabled-permission of this {@link IFormField}.
   */
  Permission getEnabledPermission();

  /**
   * Sets a new enabled-permission that is used to calculate the enabled-granted property of this {@link IFormField}.
   *
   * @param p
   *          The new {@link Permission} that is used to calculate the enabled-granted value.
   * @see IAccessControlService#checkPermission(Permission)
   * @see #setEnabledGranted(boolean)
   */
  void setEnabledPermission(Permission p);

  /**
   * @return The enable-granted property of this {@link IFormField}.
   */
  boolean isEnabledGranted();

  /**
   * Changes the enabled-granted property of this {@link IFormField} to the given value.
   *
   * @param enabled
   *          The new enable-granted value.
   */
  void setEnabledGranted(boolean b);

  /**
   * Changes the enabled-granted property of this {@link IFormField} to the given value.
   *
   * @param enabled
   *          The new enable-granted value.
   * @param updateParents
   *          if <code>true</code> the enabled-granted property of all parent {@link IFormField}s are updated to same
   *          value as well.
   */
  void setEnabledGranted(boolean enabled, boolean updateParents);

  /**
   * Changes the enabled-granted property of this {@link IFormField} to the given value.
   *
   * @param enabled
   *          The new enable-granted value.
   * @param updateParents
   *          if <code>true</code> the enabled-granted property of all parent {@link IFormField}s are updated to same
   *          value as well.
   * @param updateChildren
   *          if <code>true</code> the enabled-granted property of all child {@link IFormField}s (recursive) are updated
   *          to same value as well.
   */
  void setEnabledGranted(boolean enabled, boolean updateParents, boolean updateChildren);

  /**
   * Changes the enabled-state value of the given dimension.
   *
   * @param enabled
   *          The new enabled-state value for the given dimension.
   * @param dimension
   *          The dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. This {@link IFormField} supports up to
   *           {@link NamedBitMaskHelper#NUM_BITS} dimensions for enabled-state. Three dimensions are already used by
   *           the {@link IFormField} itself ({@link IDimensions#ENABLED}, {@link IDimensions#ENABLED_GRANTED},
   *           ENABLED_SLAVE). Therefore 5 dimensions may be used by developers.<br>
   *           <b>Note:</b> these dimensions are shared amongst all {@link IFormField}s of an application. They are not
   *           available by instance but by class!
   */
  @Override
  void setEnabled(boolean enabled, String dimension);

  /**
   * Changes the enabled-state value of the given dimension.
   *
   * @param enabled
   *          The new enabled-state value for the given dimension.
   * @param updateParents
   *          if <code>true</code> all parent {@link IFormField}s are updated to same value as well.
   * @param dimension
   *          The dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. This {@link IFormField} supports up to
   *           {@link NamedBitMaskHelper#NUM_BITS} dimensions for enabled-state. Three dimensions are already used by
   *           the {@link IFormField} itself ({@link IDimensions#ENABLED}, {@link IDimensions#ENABLED_GRANTED},
   *           ENABLED_SLAVE). Therefore 5 dimensions may be used by developers.<br>
   *           <b>Note:</b> these dimensions are shared amongst all {@link IFormField}s of an application. They are not
   *           available by instance but by class!
   */
  void setEnabled(boolean enabled, boolean updateParents, String dimension);

  /**
   * Changes the enabled-state value of the given dimension.
   *
   * @param enabled
   *          The new enabled-state value for the given dimension.
   * @param updateParents
   *          if <code>true</code> all parent {@link IFormField}s are updated to same value as well.
   * @param updateChildren
   *          if <code>true</code> all child {@link IFormField}s (recursive) are updated to same value as well.
   * @param dimension
   *          The dimension to change. Must not be <code>null</code>.
   * @throws AssertionException
   *           if the given dimension is <code>null</code>.
   * @throws IllegalStateException
   *           if too many dimensions are used. This {@link IFormField} supports up to
   *           {@link NamedBitMaskHelper#NUM_BITS} dimensions for enabled-state. Three dimensions are already used by
   *           the {@link IFormField} itself ({@link IDimensions#ENABLED}, {@link IDimensions#ENABLED_GRANTED},
   *           ENABLED_SLAVE). Therefore 5 dimensions may be used by developers.<br>
   *           <b>Note:</b> these dimensions are shared amongst all {@link IFormField}s of an application. They are not
   *           available by instance but by class!
   */
  void setEnabled(boolean enabled, boolean updateParents, boolean updateChildren, String dimension);

  /**
   * Accepts the given {@link IFormFieldVisitor}. This {@link IFormField} and all child {@link IFormField}s are visited
   * recursively.
   *
   * @param visitor
   *          The {@link IFormFieldVisitor} to use. Must not be <code>null</code>.
   * @param level
   *          The start level. Usually zero.
   * @param fieldIndex
   *          The field index of this {@link IFormField} in its parent list.
   * @param includeThis
   *          <code>true</code> if this {@link IFormField} and all children should be visited. <code>false</code> if
   *          only the child {@link IFormField}s should be visited.
   * @return <code>true</code> if all fields have been visited. <code>false</code> if the visitor cancelled the visit.
   * @see IFormFieldVisitor#visitField(IFormField, int, int)
   */
  boolean acceptVisitor(IFormFieldVisitor visitor, int level, int fieldIndex, boolean includeThis);

  /**
   * Visits all parent {@link IFormField}s
   *
   * @param v
   *          The {@link IFormFieldVisitor} to use. Must not be <code>null</code>.
   * @return <code>true</code> if all parent fields have been visited. <code>false</code> if the visitor cancelled the
   *         visit.
   * @see IFormFieldVisitor#visitField(IFormField, int, int)
   */
  boolean visitParents(IFormFieldVisitor visitor);

  void setDisabledStyle(int disabledStyle);

  int getDisabledStyle();
}
