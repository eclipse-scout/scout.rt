/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import java.beans.PropertyChangeListener;
import java.security.Permission;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeObserver;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.Status;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.security.ACCESS;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.dimension.IVisibleDimension;
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
 * <li>{@link ICompositeField}:<br/>
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
public interface IFormField extends IWidget, IOrdered, IStyleable, IVisibleDimension, IDataChangeObserver, IContextMenuOwner {

  String PROP_VISIBLE = "visible";
  String PROP_MANDATORY = "mandatory";
  String PROP_ORDER = "order";
  String PROP_ERROR_STATUS = "errorStatus";
  String PROP_TOOLTIP_TEXT = "tooltipText";
  String PROP_TOOLTIP_ANCHOR = "tooltipAnchor";
  String PROP_FOREGROUND_COLOR = "foregroundColor";
  String PROP_BACKGROUND_COLOR = "backgroundColor";
  String PROP_FONT = "font";
  String PROP_LABEL_FOREGROUND_COLOR = "labelForegroundColor";
  String PROP_LABEL_BACKGROUND_COLOR = "labelBackgroundColor";
  String PROP_LABEL_FONT = "labelFont";
  String PROP_SAVE_NEEDED = "saveNeeded";
  String PROP_EMPTY = "empty";
  String PROP_LABEL = "label";
  String PROP_LABEL_POSITION = "labelPosition";
  String PROP_LABEL_VISIBLE = "labelVisible";
  String PROP_LABEL_WIDTH_IN_PIXEL = "labelWidthInPixel";
  String PROP_LABEL_USE_UI_WIDTH = "labelUseUiWidth";
  String PROP_LABEL_HTML_ENABLED = "labelHtmlEnabled";
  String PROP_KEY_STROKES = "keyStrokes";
  String PROP_STATUS_VISIBLE = "statusVisible";
  String PROP_STATUS_POSITION = "statusPosition";
  String PROP_GRID_DATA = "gridData";
  String PROP_GRID_DATA_HINTS = "gridDataHints";
  String PROP_CONTEXT_MENU = "contextMenu";
  String PROP_STATUS_MENU_MAPPINGS = "statusMenuMappings";

  /**
   * if the field can get the initial focus when the form is opened, value is of type {@link Boolean}
   *
   * @since 6.1
   */
  String PROP_PREVENT_INITIAL_FOCUS = "preventInitialFocus";

  /**
   * The style of the field.
   *
   * @since 8.0
   */
  String PROP_FIELD_STYLE = "fieldStyle";

  /**
   * Style to apply when the field is rendered as "disabled".
   * <p>
   * Note that this property may be moved to the "IWidget" in the future.
   */
  String PROP_DISABLED_STYLE = "disabledStyle";

  /**
   * Position the label at the default location (normally on the left of the field)<br>
   * see {@link #setLabelPosition(byte)} and {@link #getLabelPosition()}
   */
  byte LABEL_POSITION_DEFAULT = 0;
  /**
   * Position the label on the left of the field
   */
  byte LABEL_POSITION_LEFT = 1;
  /**
   * Position the label on the field, meaning that the label is only displayed when the field is empty
   */
  byte LABEL_POSITION_ON_FIELD = 2;
  /**
   * Position the label on the right of the field. Currently, no fields implement this position style.
   */
  byte LABEL_POSITION_RIGHT = 3;
  /**
   * Position the label on the top of the field.
   */
  byte LABEL_POSITION_TOP = 4;
  /**
   * Position the label on the bottom of the field. Currently, this display style is only supported by process buttons.
   */
  byte LABEL_POSITION_BOTTOM = 5;

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

  String FIELD_STYLE_CLASSIC = "classic";
  String FIELD_STYLE_ALTERNATIVE = "alternative";

  int DISABLED_STYLE_DEFAULT = 0;
  int DISABLED_STYLE_READ_ONLY = 1;

  /**
   * Tooltip anchor is automatically determined by the form field. In most cases the tooltip will be shown on the status
   * icon.
   */
  String TOOLTIP_ANCHOR_DEFAULT = "default";
  /**
   * Tooltip anchor is the center on the form field.
   */
  String TOOLTIP_ANCHOR_ON_FIELD = "onField";

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
   * do not use this internal method. Sets the form of this field and all its child fields.
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

  void setView(boolean visible, boolean enabled, boolean mandatory);

  /**
   * create a FormData structure to be sent to the backend the Scout SDK is creating typed subclasses of FormData and
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

  /**
   * See {@link #loadFromXml(Element)}
   */
  boolean loadFromXmlString(String xml);

  void storeToXml(Element x);

  /**
   * Imports the form field state from an XML element.
   *
   * @param x
   *          The XML element that contains the field state
   * @return True, if the XML element was loaded successfully
   */
  boolean loadFromXml(Element x);

  /**
   * add verbose information to the search filter
   */
  void applySearch(SearchFilter search);

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
   * setValueChangeTriggerEnabled(false);
   * ...
   * }
   * finally{
   * setValueChangeTriggerEnabled(true);
   * }
   * </xmp></code>
   * <p>
   * do <b>not</b> write: <code><xmp>
   * boolean oldValue=isValueChangeTriggerEnabled();
   * try{
   * setValueChangeTriggerEnabled(false);
   * ...
   * }
   * finally{
   * setValueChangeTriggerEnabled(oldValue);
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
   * @return one of the LABEL_POSITION_* constants or a custom constants interpreted by the ui
   * @since 19.11.2009
   */
  byte getLabelPosition();

  /**
   * @param pos
   *          one of the LABEL_POSITION_* constants or a custom constant interpreted by the ui
   * @since 19.11.2009
   */
  void setLabelPosition(byte pos);

  /**
   * Sets whether the form-field label is HTML enabled (true) or plain-text (false). When label position is
   * <code>LABEL_POSITION_ON_FIELD</code> this property has no effect, since we can only render plain text.
   */
  void setLabelHtmlEnabled(boolean labelHtmlEnabled);

  /**
   * @return whether the form-field label is HTML enabled (true) or plain-text (false)
   */
  boolean isLabelHtmlEnabled();

  /**
   * @return the fixed label witdh &gt;0 or LABEL_WIDTH_DEFAULT or LABEL_WIDTH_UI for ui-dependent label width
   * @since 19.11.2009
   */
  int getLabelWidthInPixel();

  /**
   * @param w
   *          the fixed label witdh &gt;0 or LABEL_WIDTH_DEFAULT or LABEL_WIDTH_UI for ui-dependent label width
   * @since 19.11.2009
   */
  void setLabelWidthInPixel(int w);

  /**
   * @return {@code true} if this fields label should be as width as preferred by the ui, {@code false} otherwise
   * @since 10.09.2020
   */
  boolean isLabelUseUiWidth();

  /**
   * @param labelUseUiWidth
   *          {@code true} if this fields label should be as width as preferred by the ui, {@code false} otherwise
   * @since 10.09.2020
   */
  void setLabelUseUiWidth(boolean labelUseUiWidth);

  /**
   * @return negative for left, 0 for center and positive for right, LABEL_HORIZONTAL_ALIGNMENT_DEFAULT for default of
   *         ui
   * @since 19.11.2009
   */
  byte getLabelHorizontalAlignment();

  /**
   * @param a
   *          negative for left, 0 for center and positive for right, LABEL_HORIZONTAL_ALIGNMENT_DEFAULT for default of
   *          ui
   * @since 19.11.2009
   */
  void setLabelHorizontalAlignment(byte a);

  /**
   * @return fully qualified label. This is the path in the container tree
   */
  String getFullyQualifiedLabel(String separator);

  boolean isMandatory();

  void setMandatory(boolean b);

  void setMandatory(boolean b, boolean recursive);

  /**
   * Adds an error status to field. use {@link Status} in order to set a custom icon.
   */
  void addErrorStatus(IStatus newStatus);

  /**
   * Adds an error status of type {@link DefaultFieldStatus} with the given message to the field.
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
   *         property is met.
   */
  boolean isContentValid();

  /**
   * @return either null when everything is valid or a problem descriptor that contains more details.
   */
  IValidateContentDescriptor validateContent();

  String getTooltipText();

  void setTooltipText(String text);

  void setTooltipAnchor(String tooltipAnchor);

  String getTooltipAnchor();

  /**
   * Rebuild the {@link IFormField#PROP_KEY_STROKES} property using the internal set of properties and by calling
   * getContributedKeyStrokes() and getLocalKeyStrokes()
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
   * Convenience for {@link IForm#requestInput(IFormField)}
   */
  void requestInput();

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

  void setStatusVisible(boolean statusVisible, boolean recursive);

  String getStatusPosition();

  void setStatusPosition(String statusPosition);

  /**
   * @return The {@link IValidateContentDescriptor} that will be used by {@link IForm#validateForm()} if this field
   *         contains invalid content (see {@link #validateContent()}). May be {@code null}. The resulting instance may
   *         be modified directly or replaced using {@link #setValidateContentDescriptor(IValidateContentDescriptor)}.
   */
  IValidateContentDescriptor getValidateContentDescriptor();

  /**
   * Sets the {@link IValidateContentDescriptor} to be used by {@link IForm#validateForm()} if this field contains
   * invalid content (see {@link #validateContent()}).
   *
   * @param validateContentDescriptor
   *          The new {@link IValidateContentDescriptor} or {@code null} if no descriptor is required.
   */
  void setValidateContentDescriptor(IValidateContentDescriptor validateContentDescriptor);

  /**
   * @return true, if the mandatory property is fulfilled (a value set or not mandatory)
   */
  boolean isMandatoryFulfilled();

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
  void setVisible(boolean visible);

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
   * @see ACCESS#check(Permission)
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
   * @param visibleGranted
   *          The new visible-granted value.
   */
  void setVisibleGranted(boolean visibleGranted);

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
   * Sets the field style on this field and on every child field.<br>
   * During the initialization phase the children are not changed.
   *
   * @param fieldStyle
   *          One of {@link #FIELD_STYLE_CLASSIC}, {@link #FIELD_STYLE_ALTERNATIVE}.
   */
  void setFieldStyle(String fieldStyle);

  /**
   * Sets the field style on this field and on every child field if requested.
   *
   * @param fieldStyle
   *          One of {@link #FIELD_STYLE_CLASSIC}, {@link #FIELD_STYLE_ALTERNATIVE}.
   * @param recursive
   *          {@code true} if the field style property of the children should be changed as well.
   */
  void setFieldStyle(String fieldStyle, boolean recursive);

  /**
   * @return the field style. One of {@link #FIELD_STYLE_CLASSIC}, {@link #FIELD_STYLE_ALTERNATIVE}.
   */
  String getFieldStyle();

  /**
   * Sets the disabled style on this field and on every child field.<br>
   * During the initialization phase the children are not changed.
   *
   * @param disabledStyle
   *          One of {@link #DISABLED_STYLE_DEFAULT}, {@link #DISABLED_STYLE_READ_ONLY}.
   */
  void setDisabledStyle(int disabledStyle);

  /**
   * Sets the disabled style on this field and on every child field if requested.
   *
   * @param disabledStyle
   *          One of {@link #DISABLED_STYLE_DEFAULT}, {@link #DISABLED_STYLE_READ_ONLY}.
   * @param recursive
   *          {@code true} if the disabled style property of the children should be changed as well.
   */
  void setDisabledStyle(int disabledStyle, boolean recursive);

  /**
   * @return the disabled style. One of {@link #DISABLED_STYLE_DEFAULT}, {@link #DISABLED_STYLE_READ_ONLY}.
   */
  int getDisabledStyle();

  /**
   * @return the mappings between menu and status. The mappings may be set explicitly using
   *         {@link #setStatusMenuMappings(List)} or by defining an inner class at the form field extending from
   *         {@link AbstractStatusMenuMapping}.
   */
  List<IStatusMenuMapping> getStatusMenuMappings();

  /**
   * Defines which menus should be displayed when an error status is shown. If the list is empty no menus are displayed.
   */
  void setStatusMenuMappings(List<IStatusMenuMapping> mappings);
}
