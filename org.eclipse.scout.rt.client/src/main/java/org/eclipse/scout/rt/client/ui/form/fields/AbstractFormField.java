/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldAddSearchTermsChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldCalculateVisibleChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldChangedMasterValueChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldDataChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldDisposeFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldInitFieldChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldIsEmptyChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldIsSaveNeededChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.FormFieldChains.FormFieldMarkSavedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.services.common.search.ISearchFilterService;
import org.eclipse.scout.rt.client.ui.AbstractWidget;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.FormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.datachange.IDataChangeListener;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.AbstractSequenceBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformError;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.reflect.BasicPropertySupport;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.status.MultiStatus;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.visitor.IBreadthFirstTreeVisitor;
import org.eclipse.scout.rt.platform.util.visitor.TreeVisitResult;
import org.eclipse.scout.rt.security.ACCESS;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.NamedBitMaskHelper;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.dimension.IDimensions;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.ContributionComposite;
import org.eclipse.scout.rt.shared.extension.IContributionOwner;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@ClassId("cb3204c4-71bf-4dc6-88a4-3a8f81a7ca10")
@FormData(value = AbstractFormFieldData.class, sdkCommand = SdkCommand.USE)
public abstract class AbstractFormField extends AbstractWidget implements IFormField, IContributionOwner, IExtensibleObject {

  private static final String TOUCHED = "TOUCHED";
  private static final String LABEL_VISIBLE = "LABEL_VISIBLE";
  private static final String MASTER_REQUIRED = "MASTER_REQUIRED";

  private static final Logger LOG = LoggerFactory.getLogger(AbstractFormField.class);
  private static final NamedBitMaskHelper VISIBLE_BIT_HELPER = new NamedBitMaskHelper(IDimensions.VISIBLE, IDimensions.VISIBLE_GRANTED);
  private static final NamedBitMaskHelper LABEL_VISIBLE_BIT_HELPER = new NamedBitMaskHelper(LABEL_VISIBLE);
  private static final NamedBitMaskHelper FLAGS_BIT_HELPER = new NamedBitMaskHelper(TOUCHED, MASTER_REQUIRED);

  /**
   * Provides 8 dimensions for visibility.<br>
   * Internally used: {@link IDimensions#VISIBLE}, {@link IDimensions#VISIBLE_GRANTED}.<br>
   * 6 dimensions remain for custom use. This FormField is visible, if all dimensions are visible (all bits set).
   */
  private byte m_visible;

  /**
   * Provides 8 dimensions for label visibility.<br>
   * Internally used: {@link #LABEL_VISIBLE} and one level by the {@link AbstractSequenceBox}.<br>
   * 6 dimensions remain for custom use. This FormField's label is visible, if all dimensions are visible (all bits
   * set).
   */
  private byte m_labelVisible;

  /**
   * Provides 8 boolean flags.<br>
   * Currently used: {@link #TOUCHED}, {@link #MASTER_REQUIRED}
   */
  private byte m_flags;

  private IForm m_form;
  private byte m_labelHorizontalAlignment;
  private Permission m_visiblePermission;
  private IValueField<?> m_masterField;
  protected int m_valueChangeTriggerEnabled = 1;// >=1 is true
  private BasicPropertySupport m_subtreePropertyChangeSupport;
  private P_MasterListener m_currentMasterListener;// my master
  private final P_FieldPropertyChangeListener m_fieldPropertyChangeListener;
  private IDataChangeListener m_internalDataChangeListener;
  protected ContributionComposite m_contributionHolder;
  private String m_initialLabel;
  private final ObjectExtensions<AbstractFormField, IFormFieldExtension<? extends AbstractFormField>> m_objectExtensions;
  private IValidateContentDescriptor m_validateContentDescriptor;

  public AbstractFormField() {
    this(true);
  }

  public AbstractFormField(boolean callInitializer) {
    super(false);
    m_visible = NamedBitMaskHelper.ALL_BITS_SET; // default visible
    m_labelVisible = NamedBitMaskHelper.ALL_BITS_SET; // default label visible
    m_objectExtensions = new ObjectExtensions<>(this, false);
    m_fieldPropertyChangeListener = new P_FieldPropertyChangeListener();
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

  @Override
  public <T extends IWidget> TreeVisitResult getWidgetByClassInternal(Holder<T> result, Class<T> widgetClassToFind) {
    return CompositeFieldUtility.getWidgetByClassInternal(this, result, widgetClassToFind);
  }

  @Override
  protected void initConfigInternal() {
    try {
      setValueChangeTriggerEnabled(false);
      m_objectExtensions.initConfigAndBackupExtensionContext(createLocalExtension(), this::initConfig);
      handleChildFieldVisibilityChanged();
      // attach a proxy controller to each child field in the group for: visible, saveNeeded, isEmpty
      for (IWidget w : getFirstChildFormFields(false)) {
        addChildFieldPropertyChangeListener(w);
      }
    }
    finally {
      setValueChangeTriggerEnabled(true);
    }
  }

  @Override
  public final List<? extends IFormFieldExtension<? extends AbstractFormField>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected IFormFieldExtension<? extends AbstractFormField> createLocalExtension() {
    return new LocalFormFieldExtension<>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  /**
   * @return The text to show as the label of the current {@link IFormField}.
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(10)
  protected String getConfiguredLabel() {
    return null;
  }

  /**
   * @return One of the <code>LABEL_POSITION_*</code> constants. Default is {@link #LABEL_POSITION_DEFAULT}.
   */
  @Order(15)
  @ConfigProperty(ConfigProperty.LABEL_POSITION)
  protected byte getConfiguredLabelPosition() {
    return LABEL_POSITION_DEFAULT;
  }

  /**
   * @return the fixed label width &gt;0 or LABEL_WIDTH_DEFAULT or LABEL_WIDTH_UI for ui-dependent label width
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(16)
  protected int getConfiguredLabelWidthInPixel() {
    return LABEL_WIDTH_DEFAULT;
  }

  /**
   * @return {@code true} if this fields label should be as width as preferred by the UI, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(17)
  protected boolean getConfiguredLabelUseUiWidth() {
    return false;
  }

  /**
   * @return One of the <code>LABEL_HORIZONTAL_*</code> constants. Default is
   *         {@link #LABEL_HORIZONTAL_ALIGNMENT_DEFAULT}.
   */
  @Order(18)
  @ConfigProperty(ConfigProperty.LABEL_HORIZONTAL_ALIGNMENT)
  protected byte getConfiguredLabelHorizontalAlignment() {
    return LABEL_HORIZONTAL_ALIGNMENT_DEFAULT;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(20)
  protected boolean getConfiguredLabelVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(25)
  protected boolean getConfiguredLabelHtmlEnabled() {
    return false;
  }

  /**
   * @return One of the <code>FIELD_STYLE_*</code> constants. Default is {@link #FIELD_STYLE_ALTERNATIVE}.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(32)
  protected String getConfiguredFieldStyle() {
    return FIELD_STYLE_ALTERNATIVE;
  }

  /**
   * @return One of the <code>DISABLED_STYLE_*</code> constants. Default is {@link #DISABLED_STYLE_DEFAULT}.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(35)
  protected int getConfiguredDisabledStyle() {
    return DISABLED_STYLE_DEFAULT;
  }

  /**
   * Specifies if the form field is visible initially.<br>
   * Affects only the field itself. In case of a composite field the property does not get broadcasted initially.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(40)
  protected boolean getConfiguredVisible() {
    return true;
  }

  /**
   * Specifies if the form field is mandatory (required) initially.<br>
   * Affects only the field itself. In case of a composite field the property does not get broadcasted initially.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(45)
  protected boolean getConfiguredMandatory() {
    return false;
  }

  /**
   * Configures the view order of this form field. The view order determines the order in which the field appears.<br>
   * The view order of field with no configured view order ({@code < 0}) is initialized based on the {@link Order}
   * annotation of the form field class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this form field.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(145)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(50)
  protected String getConfiguredTooltipText() {
    return null;
  }

  /**
   * @return One of the <code>TOOLTIP_ANCHOR_*</code> constants. Default is {@link #TOOLTIP_ANCHOR_DEFAULT}.
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(55)
  protected String getConfiguredTooltipAnchor() {
    return TOOLTIP_ANCHOR_DEFAULT;
  }

  @ConfigProperty(ConfigProperty.COLOR)
  @Order(60)
  protected String getConfiguredForegroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.COLOR)
  @Order(70)
  protected String getConfiguredBackgroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.FONT)
  @Order(80)
  protected String getConfiguredFont() {
    return null;
  }

  /**
   * Configures the foreground color of the label. The color is represented by the HEX value (e.g. FFFFFF).
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Foreground color HEX value of the label.
   */
  @ConfigProperty(ConfigProperty.COLOR)
  @Order(60)
  protected String getConfiguredLabelForegroundColor() {
    return null;
  }

  /**
   * Configures the background color of the label. The color is represented by the HEX value (e.g. FFFFFF).
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Background color HEX value of the label.
   */
  @ConfigProperty(ConfigProperty.COLOR)
  @Order(70)
  protected String getConfiguredLabelBackgroundColor() {
    return null;
  }

  /**
   * Configures the font of the label. See {@link FontSpec#parse(String)} for the appropriate format.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Font of the label.
   */
  @ConfigProperty(ConfigProperty.FONT)
  @Order(80)
  protected String getConfiguredLabelFont() {
    return null;
  }

  /**
   * Configures the horizontal alignment of the fields inside this group box.<br>
   * This property typically only has an effect if fill horizontal is set to false which can be configured by
   * {@link #getConfiguredFillHorizontal()}.
   * <p>
   * Subclasses can override this method. Default alignment is left.
   *
   * @return -1 for left, 0 for center and 1 for right alignment
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.HORIZONTAL_ALIGNMENT)
  @Order(85)
  protected int getConfiguredHorizontalAlignment() {
    return -1;
  }

  /**
   * Configures the vertical alignment of the fields inside this group box.<br>
   * This property typically only has an effect if fill vertical is set to false which can be configured by
   * {@link #getConfiguredFillVertical()}.
   * <p>
   * Subclasses can override this method. Default alignment is top.
   *
   * @return -1 for top, 0 for center and 1 for bottom alignment
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.VERTICAL_ALIGNMENT)
  @Order(86)
  protected int getConfiguredVerticalAlignment() {
    return -1;
  }

  /**
   * Configures whether this field should horizontally fill the grid cell.<br>
   * If the property is set to true, the field takes all the horizontal space and therefore is as width as the grid
   * cell.<br>
   * If it's set to false, the width is computed based on the properties {@link #getConfiguredGridUseUiWidth()} and
   * {@link #getConfiguredWidthInPixel()}. If non of these are set, a default value is used which typically is the width
   * of a logical grid column.
   * <p>
   * Subclasses can override this method. Default is true.
   *
   * @return {@code true} if this field should horizontally fill the grid cell, {@code false} otherwise
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(87)
  protected boolean getConfiguredFillHorizontal() {
    return true;
  }

  /**
   * Configures whether this field should vertically fill the grid cell.<br>
   * If the property is set to true, the field takes all the vertical space and therefore is as height as the grid cell.
   * <br>
   * If it's set to false, the height is computed based on the properties {@link #getConfiguredGridUseUiHeight()} and
   * {@link #getConfiguredHeightInPixel()}. If non of these are set, a default value is used which typically is the
   * height of a logical grid row.
   * <p>
   * Subclasses can override this method. Default is true.
   *
   * @return {@code true} if this field should vertically fill the grid cell, {@code false} otherwise
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(88)
  protected boolean getConfiguredFillVertical() {
    return true;
  }

  /**
   * Configures the x position (horizontal) of this field in the logical grid of the surrounding group box.<br>
   * If the value is set to -1, the property will be ignored. If the value is >= 0, it's considered as grid column. <br>
   * It is not necessary to explicitly set a column count by {@link AbstractGroupBox#getConfiguredGridColumnCount()}.
   * <p>
   * This property only has an effect if every field inside the group box has a fix position which means every field
   * inside the group box need to have x and y to be set which can be configured by this method and
   * {@link #getConfiguredGridY()}.
   * <p>
   * Subclasses can override this method. Default is -1.
   *
   * @return the x position in the grid.
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(90)
  protected int getConfiguredGridX() {
    return -1;
  }

  /**
   * Configures the y (vertical) position of this field in the logical grid of the surrounding group box.<br>
   * If the value is set to -1, the property will be ignored. If the value is >= 0, it's considered as grid row. <br>
   * <p>
   * This property only has an effect if every field inside the group box has a fix position which means every field
   * inside the group box need to have x and y to be set which can be configured by {@link #getConfiguredGridX()} and
   * this method.
   * <p>
   * Subclasses can override this method. Default is -1.
   *
   * @return the y position in the grid.
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(95)
  protected int getConfiguredGridY() {
    return -1;
  }

  /**
   * Configures the column span of this field.<br>
   * The value defined by this property refers to the number of columns defined by the group box which contains this
   * field. This column count can be configured by {@link AbstractGroupBox#getConfiguredGridColumnCount()}.
   * <p>
   * <b>Example:</b><br>
   * If the column count of the group box is set to 3 and a column span of this field is set to 2 it means 2/3 of the
   * group box width is used for this field:<br>
   * <table border="1">
   * <colgroup align="center" width="40"/> <colgroup align="center" width="40"/> <colgroup align="center" width="40"/>
   * <tr>
   * <td colspan="2">this</td>
   * <td>...</td>
   * </tr>
   * <tr>
   * <td>...</td>
   * <td>...</td>
   * <td>...</td>
   * </tr>
   * </table>
   * <p>
   * Subclasses can override this method. Default is 1.
   *
   * @return the number of columns to span
   * @see #getConfiguredGridWeightX(), {@link #getGridData()}, {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(100)
  protected int getConfiguredGridW() {
    return 1;
  }

  /**
   * Configures the row span of this field.<br>
   * Compared to the number of columns, which is a configurable value and therefore static, the number of rows is
   * dynamic. That number depends on the number of fields used in the group box which contains this field, as well as
   * the number of columns defined by that group box.
   * <p>
   * <b>Example:</b> A group box with 2 columns contains 3 fields: The first 2 fields have gridW = 1 and gridH = 1 and
   * the third field has gridW = 1 and gridH = 2. In this case the third field would be as height as the other 2 fields
   * together because it spans two rows:<br>
   * <table border="1">
   * <colgroup align="center" width="40"/> <colgroup align="center" width="40"/>
   * <tr>
   * <td>1</td>
   * <td rowspan="2">3</td>
   * </tr>
   * <tr>
   * <td>2</td>
   * </tr>
   * </table>
   * <p>
   * Note that this value is actually the minimum logical height if the field is scalable (see
   * {@link #getConfiguredGridWeightY()}.
   * <p>
   * Subclasses can override this method. Default is 1.
   *
   * @return the number of rows to span
   * @see #getConfiguredGridWeightY() comment about weightY logic which depends on the gridH value configured here
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(105)
  protected int getConfiguredGridH() {
    return 1;
  }

  /**
   * Configures how much a grid cell should horizontally grow or shrink.<br>
   * <p>
   * The value for this property can either be -1 or between 0 and 1.
   * <ul>
   * <li>0 means fixed width and the grid cell won't grow or shrink.</li>
   * <li>Greater 0 means the grid cell will grab the excess horizontal space and therefore grow or shrink. If the group
   * box contains more than one field with weightX > 0, the weight is used to specify how strong the width of the grid
   * cell should be adjusted.</li>
   * <li>-1 means the ui computes the optimal value so that the fields proportionally grab the excess space.</li>
   * </ul>
   * <b>Examples:</b>
   * <ul>
   * <li>A group box with 3 columns contains 3 fields: Every field has gridW = 1 and weightX = -1. This leads to 1 row
   * and 3 grid cells which would grow and shrink proportionally because weightX is automatically set to > 0.</li>
   * <li>If the weight of these 3 fields were set to 0.1, 0.1 and 1, the first two fields would adjust the size very
   * slowly and would mostly be as big as a logical grid column (because gridW is set to 1), whereas the third field
   * would adjust it's size very fast.</li>
   * </ul>
   * <p>
   * Subclasses can override this method. Default is -1.
   *
   * @return a value between 0 and 1, or -1
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(130)
  protected double getConfiguredGridWeightX() {
    return -1;
  }

  /**
   * Configures how much a grid cell should vertically grow or shrink.<br>
   * <p>
   * The value for this property can either be -1 or between 0 and 1.
   * <ul>
   * <li>0 means fixed height and the grid cell won't grow or shrink.</li>
   * <li>Greater 0 means the grid cell will grab the excess vertical space and therefore grow or shrink. If the group
   * box contains more than one field with weightY > 0, the weight is used to specify how strong the height of the grid
   * cell should be adjusted.</li>
   * <li>-1 means the ui computes the optimal value so that the fields proportionally grab the excess space, but only if
   * gridH is > 1. If gridH is 1 a weight of 0 is set and the grid cell does not grow or shrink.</li>
   * </ul>
   * <b>Examples:</b>
   * <ul>
   * <li>A group box with 1 column contains 3 fields: Every field has gridH = 1 and weightY = -1. This leads to 3 rows
   * with fixed height, no additional space is grabbed, because weightY will automatically be set to 0.</li>
   * <li>If the weight of these 3 fields were set to 1, the fields would grow and shrink proportionally.</li>
   * <li>If the weight of these 3 fields were set to 0.1, 0.1 and 1, the first two fields would adjust the size very
   * slowly and would mostly be a as big as one logical grid row (because gridH is set to 1), whereas the third field
   * would adjust it's size very fast.</li>
   * </ul>
   * <p>
   * Subclasses can override this method. Default is -1.
   *
   * @return a value between 0 and 1, or -1
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(140)
  protected double getConfiguredGridWeightY() {
    return -1;
  }

  /**
   * Configures whether the field should be as width as preferred by the UI. The preferred width normally is the
   * computed width of the child fields.<br>
   * This property typically has less priority than {@link #getConfiguredWidthInPixel()} and therefore only has an
   * effect if no explicit width is set.
   * <p>
   * Subclasses can override this method. Default is false.
   *
   * @return {@code true} if this field should be as width as preferred by the UI, {@code false} otherwise
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(142)
  protected boolean getConfiguredGridUseUiWidth() {
    return false;
  }

  /**
   * Configures whether the field should be as height as preferred by the UI. The preferred height normally is the
   * computed height of the child fields.<br>
   * This property typically has less priority than {@link #getConfiguredHeightInPixel()} and therefore only has an
   * effect if no explicit height is set.
   * <p>
   * Subclasses can override this method. Default is false.
   *
   * @return {@code true} if this field should be as height as preferred by the UI, {@code false} otherwise
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(142)
  protected boolean getConfiguredGridUseUiHeight() {
    return false;
  }

  /**
   * Configures the preferred width of the field. <br>
   * If the value is <=0 the property will be ignored by the UI layout manager.
   * <p>
   * Subclasses can override this method. Default is 0.
   *
   * @return the preferred width in pixel
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(150)
  protected int getConfiguredWidthInPixel() {
    return 0;
  }

  /**
   * Configures the preferred height of the field. <br>
   * If the value is <=0 the property will be ignored by the UI layout manager.
   * <p>
   * Subclasses can override this method. Default is 0.
   *
   * @return the preferred height in pixel
   * @see #getGridData(), {@link #getGridDataHints()}
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(160)
  protected int getConfiguredHeightInPixel() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.MASTER_FIELD)
  @Order(170)
  protected Class<? extends IValueField> getConfiguredMasterField() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(180)
  protected boolean getConfiguredMasterRequired() {
    return false;
  }

  /**
   * @return <code>false</code> if this field can get the initial focus when the form is opened (default). Set to
   *         <code>true</code> to prevent this field from getting the initial focus. In both cases, the field will still
   *         be manually focusable by the user.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(195)
  protected boolean getConfiguredPreventInitialFocus() {
    return false;
  }

  /**
   * Configures whether or not the space for the status is visible.
   * <p>
   * If set to false, the space is not visible unless there is a status message, tooltip or menu. <br>
   * If set to true the space is always visible, even if there is no status message, no tooltip and no menu.
   *
   * @return {@code true} if the space for the status should always be visible, {@code false} otherwise
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected boolean getConfiguredStatusVisible() {
    return true;
  }

  /**
   * Configures the position of the status.
   * <p>
   * Subclasses can override this method. Default is {@value IFormField#STATUS_POSITION_DEFAULT}.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(200)
  protected String getConfiguredStatusPosition() {
    return STATUS_POSITION_DEFAULT;
  }

  private List<Class<? extends IKeyStroke>> getConfiguredKeyStrokes() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IKeyStroke>> fca = ConfigurationUtility.filterClasses(dca, IKeyStroke.class);
    return ConfigurationUtility.removeReplacedClasses(fca);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), getMenus(), getKeyStrokesInternal());
  }

  @ConfigOperation
  @Order(10)
  protected void execInitField() {
  }

  /**
   * On any value change or call to {@link #checkSaveNeeded()} this method is called to calculate if the field needs
   * save.
   */
  @ConfigOperation
  @Order(11)
  protected boolean execIsSaveNeeded() {
    for (IFormField f : getFirstChildFormFields(false)) {
      if (f.isSaveNeeded()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Make field saved, for example a table is marking all rows as non-changed.
   */
  @ConfigOperation
  @Order(12)
  protected void execMarkSaved() {
  }

  protected boolean areChildrenEmpty() {
    for (IFormField f : getFirstChildFormFields(false)) {
      if (!f.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /**
   * On any value change or call to {@link #checkEmpty()} this method is called to calculate if the field represents an
   * empty state (semantics).
   */
  @ConfigOperation
  @Order(13)
  protected boolean execIsEmpty() {
    return areChildrenEmpty();
  }

  /**
   * See {@link IDesktop#dataChanged(Object...)} and
   * {@link IDesktop#fireDataChangeEvent(org.eclipse.scout.rt.client.ui.desktop.datachange.DataChangeEvent)}.
   */
  @ConfigOperation
  @Order(14)
  protected void execDataChanged(Object... dataTypes) {
  }

  @ConfigOperation
  @Order(15)
  protected void execDisposeField() {
  }

  @Override
  public final void applySearch(SearchFilter search) {
    interceptAddSearchTerms(search);
  }

  /**
   * add verbose information to the search filter
   */
  @ConfigOperation
  protected void execAddSearchTerms(SearchFilter search) {
    applySearchInternal(search);
  }

  /**
   * override this method to apply new default handling
   */
  protected void applySearchInternal(final SearchFilter search) {
    ISearchFilterService sfs = BEANS.get(ISearchFilterService.class);
    if (sfs != null) {
      sfs.applySearchDelegate(this, search, true);
    }
  }

  /**
   * AFTER a new valid master value was stored, this method is called
   */
  @ConfigOperation
  @Order(50)
  protected void execChangedMasterValue(Object newMasterValue) {
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setGridDataInternal(new GridData(-1, -1, 1, 1, -1, -1));
    setGridDataHints(new GridData(-1, -1, 1, 1, -1, -1));
    propertySupport.setPropertyBool(PROP_EMPTY, true);
    m_contributionHolder = new ContributionComposite(this);
    m_form = IForm.CURRENT.get();
    setFieldStyle(getConfiguredFieldStyle());
    setDisabledStyle(getConfiguredDisabledStyle());
    setVisible(getConfiguredVisible());
    setMandatory(getConfiguredMandatory());
    setOrder(calculateViewOrder());
    setTooltipText(getConfiguredTooltipText());
    setTooltipAnchor(getConfiguredTooltipAnchor());
    setInitialLabel(getConfiguredLabel());
    setLabel(getConfiguredLabel());
    setLabelPosition(getConfiguredLabelPosition());
    setLabelWidthInPixel(getConfiguredLabelWidthInPixel());
    setLabelUseUiWidth(getConfiguredLabelUseUiWidth());
    setLabelHorizontalAlignment(getConfiguredLabelHorizontalAlignment());
    setLabelVisible(getConfiguredLabelVisible());
    setLabelHtmlEnabled(getConfiguredLabelHtmlEnabled());
    setStatusVisible(getConfiguredStatusVisible());
    setStatusPosition(getConfiguredStatusPosition());
    setCssClass((getConfiguredCssClass()));
    setValidateContentDescriptor(new ValidateFormFieldDescriptor(this));
    if (getConfiguredBackgroundColor() != null) {
      setBackgroundColor((getConfiguredBackgroundColor()));
    }
    if (getConfiguredForegroundColor() != null) {
      setForegroundColor((getConfiguredForegroundColor()));
    }
    if (getConfiguredFont() != null) {
      setFont(FontSpec.parse(getConfiguredFont()));
    }
    if (getConfiguredLabelBackgroundColor() != null) {
      setLabelBackgroundColor((getConfiguredLabelBackgroundColor()));
    }
    if (getConfiguredLabelForegroundColor() != null) {
      setLabelForegroundColor((getConfiguredLabelForegroundColor()));
    }
    if (getConfiguredLabelFont() != null) {
      setLabelFont(FontSpec.parse(getConfiguredLabelFont()));
    }
    setPreventInitialFocus(getConfiguredPreventInitialFocus());
    setGridDataHints(new GridData(getConfiguredGridX(), getConfiguredGridY(), getConfiguredGridW(), getConfiguredGridH(), getConfiguredGridWeightX(), getConfiguredGridWeightY(), getConfiguredGridUseUiWidth(), getConfiguredGridUseUiHeight(),
        getConfiguredHorizontalAlignment(), getConfiguredVerticalAlignment(), getConfiguredFillHorizontal(), getConfiguredFillVertical(), getConfiguredWidthInPixel(), getConfiguredHeightInPixel()));
    setMasterRequired(getConfiguredMasterRequired());
    // private listener for subtree property change events
    addPropertyChangeListener(this::fireSubtreePropertyChange);
    initMenus();
  }

  protected void initMenus() {
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    OrderedCollection<IMenu> menus = new OrderedCollection<>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      menus.addOrdered(ConfigurationUtility.newInnerInstance(this, menuClazz));
    }
    menus.addAllOrdered(contributedMenus);
    injectMenusInternal(menus);
    new MoveActionNodesHandler<>(menus).moveModelObjects();
    setContextMenu(createContextMenu(menus));
    setStatusMenuMappings(createStatusMenuMappings());
  }

  /**
   * Calculates the formfield's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0.
   * If no {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   */
  @SuppressWarnings("squid:S1244") // Floating point numbers should not be tested for equality
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      Class<?> cls = getClass();
      while (cls != null && IFormField.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
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

    // search requested field within critical parent field
    Collections.reverse(enclosingFields);
    for (ICompositeField parentField : enclosingFields) {
      T field = parentField.getFieldByClass(c);
      if (field != null) {
        return field;
      }
    }

    // field has not been found in a critical parent field
    return getForm().getFieldByClass(c);
  }

  @Override
  protected void initInternal() {
    super.initInternal();

    // key strokes, now all inner fields are built
    updateKeyStrokes();

    // master listener, now the inner field is available
    if (getConfiguredMasterField() != null) {
      IValueField master = findNearestFieldByClass(getConfiguredMasterField());
      setMasterField(master);
    }

    try {
      setValueChangeTriggerEnabled(false);
      initFieldInternal();
      interceptInitField();
    }
    finally {
      setValueChangeTriggerEnabled(true);
    }
  }

  /**
   * Gets the next child form fields that exist in the child tree of this form field. Only the first level is
   * returned.<br>
   * This means the resulting {@link List} contains all the direct child form fields event if a non-form-field-widget
   * exists in the child tree between this instance and the child.
   * <p>
   * <b>Example:</b>
   *
   * <pre>
   *                            this
   *              ----------------|----------------
   *              |                               |
   *      child form field A                    menu X
   *              |                               |
   *      ------------------                      |
   *      |                |                      |
   *  form field B      form field C      form field in menu D
   * </pre>
   * <p>
   * In this scenario this method would return field A and D.
   *
   * @param limitToSameFieldTree
   *          Specifies if only the same-field-tree should be considered. A same-field-tree is a tree that only consists
   *          of {@link IFormField}s and {@link IForm}s. So if this parameter is {@code true}, {@link IFormField}s in
   *          e.g. menus are not returned because they don't belong to the same-field-tree according to the
   *          specification above.
   * @return A {@link List} with the child {@link IFormField}s. Is never {@code null}.
   */
  protected List<IFormField> getFirstChildFormFields(boolean limitToSameFieldTree) {
    List<IFormField> nextFormFieldCollector = new ArrayList<>();
    Function<IWidget, TreeVisitResult> nextFormFieldsVisitor = w -> {
      if (w == AbstractFormField.this) {
        return TreeVisitResult.CONTINUE; // the first visited item is the current instance. Always skip.
      }
      if (w instanceof IFormField) {
        nextFormFieldCollector.add((IFormField) w);
        return TreeVisitResult.SKIP_SUBTREE;
      }
      if (limitToSameFieldTree) {
        // only step into forms. form fields that are not part of the inner forms are skipped because not part of the same field tree (new tree like in a menu).
        return w instanceof IForm ? TreeVisitResult.CONTINUE : TreeVisitResult.SKIP_SUBTREE;
      }
      return TreeVisitResult.CONTINUE;
    };
    visit(nextFormFieldsVisitor);
    return nextFormFieldCollector;
  }

  protected void initFieldInternal() {
    checkSaveNeeded();
    checkEmpty();
    for (IStatusMenuMapping mapping : getStatusMenuMappings()) {
      mapping.init();
    }
  }

  @Override
  protected final void disposeInternal() {
    try {
      disposeFieldInternal();
    }
    catch (RuntimeException e) {
      LOG.warn("Cannot dispose field [{}]", getClass().getName(), e);
    }
    try {
      interceptDisposeField();
    }
    catch (RuntimeException e) {
      LOG.warn("Cannot dispose field [{}]", getClass().getName(), e);
    }
    super.disposeInternal();
  }

  protected void disposeFieldInternal() {
    unregisterDataChangeListener();
  }

  @Override
  public void registerDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener == null) {
      m_internalDataChangeListener = event -> interceptDataChanged(event.getDataType());
    }
    IDesktop.CURRENT.get().dataChangeListeners().add(m_internalDataChangeListener, true, dataTypes);
  }

  @Override
  public void unregisterDataChangeListener(Object... dataTypes) {
    if (m_internalDataChangeListener != null) {
      IDesktop.CURRENT.get().removeDataChangeListener(m_internalDataChangeListener, dataTypes);
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
    return getParentOfType(IGroupBox.class);
  }

  @Override
  public ICompositeField getParentField() {
    return getParentOfType(ICompositeField.class);
  }

  @Override
  public void setFormInternal(IForm form) {
    m_form = form;
    setFormOnChildren(form);
  }

  protected void setFormOnChildren(IForm form) {
    for (IFormField child : getFirstChildFormFields(false)) {
      child.setFormInternal(form);
    }
  }

  @Override
  public String toString() {
    return getLabel() + "/" + getFieldId() + " (" + getClass().getName() + ")";
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
    if (listener == null) {
      return;
    }
    if (m_subtreePropertyChangeSupport == null) {
      m_subtreePropertyChangeSupport = new BasicPropertySupport(this);
    }
    m_subtreePropertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addSubtreePropertyChangeListener(String propName, PropertyChangeListener listener) {
    if (listener == null) {
      return;
    }
    if (m_subtreePropertyChangeSupport == null) {
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
  public boolean isFieldChanging() {
    return propertySupport.isPropertiesChanging();
  }

  @Override
  public void setFieldChanging(boolean b) {
    propertySupport.setPropertiesChanging(b);
  }

  @Override
  public String getFieldId() {
    Class<?> c = getClass();
    while (c.isAnnotationPresent(Replace.class)) {
      c = c.getSuperclass();
    }
    return c.getSimpleName();
  }

  /**
   * Computes a unique class id.
   * <p>
   * If the class is annotated with {@link ClassId}, the annotation value is used. If the field is defined in a template
   * (outside its form class), the ids of the enclosing fields are appended, if necessary, to make the id unique.
   * <p>
   * If the class is not annotated with {@link ClassId}, a unique id is computed using the simple class name.
   * <p>
   * For dynamically injected fields this method needs to be overridden to make sure it is unique.
   */
  @Override
  public String classId() {
    StringBuilder classId = new StringBuilder(computeClassId());

    boolean appendFormId = !getClass().isAnnotationPresent(ClassId.class);
    if (appendFormId && getForm() != null) {
      classId.append(ID_CONCAT_SYMBOL).append(getForm().classId());
    }
    boolean duplicate = existsDuplicateClassId();
    if (duplicate) {
      LOG.warn("Found a duplicate classid for {}, adding field index. Override classId for dynamically injected fields.", classId);
      int fieldIndex = (getParentField() == null ? 0 : getParentField().getFieldIndex(this));
      classId.append(ID_CONCAT_SYMBOL).append(fieldIndex);
    }

    return classId.toString();
  }

  /**
   * Computes a class id by considering the enclosing field list.
   * <p>
   * Does not consider the complete path for lenient support. For dynamically injected fields {@link #classId()} needs
   * to be overridden.
   * </p>
   */
  private String computeClassId() {
    StringBuilder fieldId = new StringBuilder();
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), true);
    fieldId.append(simpleClassId);
    List<ICompositeField> enclosingFieldList = getEnclosingFieldList();
    for (int i = enclosingFieldList.size() - 1; i >= 0; --i) {
      ICompositeField enclosingField = enclosingFieldList.get(i);
      String enclosingClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(enclosingField.getClass(), true);
      fieldId.append(ID_CONCAT_SYMBOL).append(enclosingClassId);
    }
    return fieldId.toString();
  }

  /**
   * Sanity check for class ids. Scans all fields in a form to find duplicate class ids.
   *
   * @return <code>true</code>, if another field with the same id is found. <code>false</code> otherwise.
   */
  private boolean existsDuplicateClassId() {
    IForm form = getForm();
    if (form != null) {
      FindClassIdVisitor visitor = new FindClassIdVisitor(computeClassId(), this);
      form.visit(visitor, IFormField.class);
      return visitor.isFound();
    }
    return false;
  }

  /*
   * Data i/o
   */
  @Override
  public void exportFormFieldData(AbstractFormFieldData target) {
  }

  @Override
  public void importFormFieldData(AbstractFormFieldData source, boolean valueChangeTriggersEnabled) {
  }

  /*
   * XML i/o
   */
  @Override
  public void storeToXml(Element x) {
    List<ICompositeField> enclosingFieldList = getEnclosingFieldList();
    for (ICompositeField field : enclosingFieldList) {
      Element enclosingField = x.getOwnerDocument().createElement("enclosingField");
      setXmlFormFieldId(enclosingField, field);
      // Enclosing fields are traversed from outside to inside. Hence add XML child at the end.
      x.appendChild(enclosingField);
    }
    // set field ids
    setXmlFormFieldId(x, this);
  }

  @Override
  public List<ICompositeField> getEnclosingFieldList() {
    List<ICompositeField> enclosingFieldList = new ArrayList<>();
    // compute enclosing field path
    ICompositeField p = getParentField();
    while (p != null) {
      if (!(p instanceof AbstractCompositeField) || ((AbstractCompositeField) p).isTemplateField()) {
        enclosingFieldList.add(0, p);
      }
      p = p.getParentField();
    }
    return enclosingFieldList;
  }

  protected final void setXmlFormFieldId(Element x, IFormField f) {
    x.setAttribute("fieldId", f.getFieldId());
    x.setAttribute("fieldQname", f.getClass().getName());
  }

  @Override
  public void loadFromXml(Element x) {
  }

  @Override
  public final void loadFromXmlString(String xml) {
    if (xml == null) {
      return;
    }
    try {
      Document doc = XmlUtility.getXmlDocument(xml);
      Element root = doc.getDocumentElement();
      loadFromXml(root);
    }
    catch (Exception e) {
      throw new ProcessingException("Error in AbstractFormField.setXML: ", e);
    }
  }

  @Override
  public final String storeToXmlString() {
    Document x = XmlUtility.createNewXmlDocument("field");
    storeToXml(x.getDocumentElement());
    return XmlUtility.wellformDocument(x);
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
  public byte getLabelPosition() {
    return propertySupport.getPropertyByte(PROP_LABEL_POSITION);
  }

  @Override
  public void setLabelPosition(byte position) {
    propertySupport.setPropertyByte(PROP_LABEL_POSITION, position);
  }

  @Override
  public int getLabelWidthInPixel() {
    return propertySupport.getPropertyInt(PROP_LABEL_WIDTH_IN_PIXEL);
  }

  @Override
  public void setLabelWidthInPixel(int w) {
    propertySupport.setPropertyInt(PROP_LABEL_WIDTH_IN_PIXEL, w);
  }

  @Override
  public boolean isLabelUseUiWidth() {
    return propertySupport.getPropertyBool(PROP_LABEL_USE_UI_WIDTH);
  }

  @Override
  public void setLabelUseUiWidth(boolean labelUseUiWidth) {
    propertySupport.setPropertyBool(PROP_LABEL_USE_UI_WIDTH, labelUseUiWidth);
  }

  @Override
  public byte getLabelHorizontalAlignment() {
    return m_labelHorizontalAlignment;
  }

  @Override
  public void setLabelHorizontalAlignment(byte a) {
    m_labelHorizontalAlignment = a;
  }

  @Override
  public String getFullyQualifiedLabel(String separator) {
    StringBuilder b = new StringBuilder();
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
  public void setLabelVisible(boolean labelVisible) {
    setLabelVisible(labelVisible, LABEL_VISIBLE);
  }

  @Override
  public void setLabelVisible(boolean visible, String dimension) {
    m_labelVisible = LABEL_VISIBLE_BIT_HELPER.changeBit(dimension, visible, m_labelVisible);
    calculateLabelVisibleInternal();
  }

  @Override
  public boolean isLabelVisible(String dimension) {
    return LABEL_VISIBLE_BIT_HELPER.isBitSet(dimension, m_labelVisible);
  }

  /**
   * Do not use this internal method
   */
  protected void calculateLabelVisibleInternal() {
    propertySupport.setPropertyBool(PROP_LABEL_VISIBLE, NamedBitMaskHelper.allBitsSet(m_labelVisible));
  }

  @Override
  public void setLabelHtmlEnabled(boolean labelHtmlEnabled) {
    propertySupport.setProperty(PROP_LABEL_HTML_ENABLED, labelHtmlEnabled);
  }

  @Override
  public boolean isLabelHtmlEnabled() {
    return propertySupport.getPropertyBool(PROP_LABEL_HTML_ENABLED);
  }

  private void setEnabledSlave(boolean enabled) {
    setEnabled(enabled, IDimensions.ENABLED_SLAVE);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (enabled) {
      setEnabledSlave(true);
    }
  }

  @Override
  public void setFieldStyle(String fieldStyle) {
    setFieldStyle(fieldStyle, isInitConfigDone());
  }

  @Override
  public void setFieldStyle(String fieldStyle, boolean recursive) {
    propertySupport.setPropertyString(PROP_FIELD_STYLE, fieldStyle);

    if (recursive) {
      for (IFormField f : getFirstChildFormFields(true)) {
        f.setFieldStyle(fieldStyle, true);
      }
    }
  }

  @Override
  public String getFieldStyle() {
    return propertySupport.getPropertyString(PROP_FIELD_STYLE);
  }

  @Override
  public void setDisabledStyle(int disabledStyle) {
    setDisabledStyle(disabledStyle, isInitConfigDone());
  }

  @Override
  public void setDisabledStyle(int disabledStyle, boolean recursive) {
    propertySupport.setPropertyInt(PROP_DISABLED_STYLE, disabledStyle);
    if (recursive) {
      for (IFormField f : getFirstChildFormFields(true)) {
        f.setDisabledStyle(disabledStyle, true);
      }
    }
  }

  @Override
  public int getDisabledStyle() {
    return propertySupport.getPropertyInt(PROP_DISABLED_STYLE);
  }

  @Override
  public Permission getVisiblePermission() {
    return m_visiblePermission;
  }

  @Override
  public void setVisiblePermission(Permission p) {
    m_visiblePermission = p;
    boolean b = true;
    if (p != null) {
      b = ACCESS.check(p);
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
   * Default implementation just calls {@link #interceptIsSaveNeeded()}<br>
   * For thread-safety-reason this method is final
   */
  @Override
  public final void checkSaveNeeded() {
    if (isInitConfigDone()) {
      try {
        propertySupport.setPropertyBool(PROP_SAVE_NEEDED, isTouched() || interceptIsSaveNeeded());
      }
      catch (RuntimeException | PlatformError e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  private boolean isTouched() {
    return FLAGS_BIT_HELPER.isBitSet(TOUCHED, m_flags);
  }

  private void setTouched(boolean touched) {
    m_flags = FLAGS_BIT_HELPER.changeBit(TOUCHED, touched, m_flags);
  }

  @Override
  public void touch() {
    setTouched(true);
    checkSaveNeeded();
  }

  @Override
  public final void markSaved() {
    for (IFormField f : getFirstChildFormFields(false)) {
      f.markSaved();
    }

    try {
      setTouched(false);
      interceptMarkSaved();
      checkSaveNeeded();
    }
    catch (RuntimeException | PlatformError e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  public final boolean isEmpty() {
    return propertySupport.getPropertyBool(PROP_EMPTY);
  }

  /**
   * Default implementation just calls {@link #interceptIsEmpty()}
   */
  protected final void checkEmpty() {
    if (isInitConfigDone()) {
      try {
        propertySupport.setPropertyBool(PROP_EMPTY, interceptIsEmpty());
      }
      catch (RuntimeException | PlatformError e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  @Override
  public boolean isVisibleGranted() {
    return isVisible(IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public void setVisibleGranted(boolean visible) {
    setVisibleGranted(visible, false);
  }

  @Override
  public void setVisibleGranted(boolean visible, boolean updateParents) {
    setVisibleGranted(visible, updateParents, false);
  }

  @Override
  public void setVisibleGranted(boolean visible, boolean updateParents, boolean updateChildren) {
    setVisible(visible, updateParents, updateChildren, IDimensions.VISIBLE_GRANTED);
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public void setVisible(boolean visible) {
    setVisible(visible, false);
  }

  @Override
  public void setVisible(boolean visible, boolean updateParents) {
    setVisible(visible, updateParents, false);
  }

  @Override
  public void setVisible(boolean visible, boolean updateParents, boolean updateChildren) {
    setVisible(visible, updateParents, updateChildren, IDimensions.VISIBLE);
  }

  @Override
  public void setVisible(boolean visible, boolean updateParents, final String dimension) {
    setVisible(visible, updateParents, false, dimension);
  }

  @Override
  public void setVisible(final boolean visible, final boolean updateParents, final boolean updateChildren, final String dimension) {
    m_visible = VISIBLE_BIT_HELPER.changeBit(dimension, visible, m_visible);
    calculateVisibleInternal();

    if (visible && updateParents) {
      // also enable all parents
      visitParents(field -> {
        field.setVisible(true, dimension);
        return true;
      }, IFormField.class);
    }

    if (updateChildren) {
      // propagate change to children
      for (IWidget w : getChildren()) {
        Consumer<IFormField> visitor = field -> field.setVisible(visible, dimension);
        w.visit(visitor, IFormField.class);
      }
    }
  }

  @Override
  public void setVisible(boolean visible, String dimension) {
    setVisible(visible, false, dimension);
  }

  @ConfigOperation
  protected boolean execCalculateVisible() {
    return true;
  }

  @Override
  public boolean isVisible(String dimension) {
    return VISIBLE_BIT_HELPER.isBitSet(dimension, m_visible);
  }

  @Override
  public boolean isVisibleIncludingParents() {
    if (!isVisible()) {
      return false;
    }

    Predicate<IFormField> visitor = IFormField::isVisible;
    return visitParents(visitor, IFormField.class);
  }

  /**
   * Do not use this internal method
   */
  protected void calculateVisibleInternal() {
    boolean changed = propertySupport.setPropertyBool(PROP_VISIBLE, NamedBitMaskHelper.allBitsSet(m_visible) && interceptCalculateVisible());
    if (!changed) {
      return;
    }

    IForm form = getForm();
    if (form != null) {
      form.structureChanged(this);
    }
  }

  @Override
  public boolean isMandatory() {
    return propertySupport.getPropertyBool(PROP_MANDATORY);
  }

  @Override
  public void setMandatory(boolean b) {
    setMandatory(b, isInitConfigDone());
  }

  @Override
  public void setMandatory(boolean b, boolean recursive) {
    propertySupport.setPropertyBool(PROP_MANDATORY, b);

    if (recursive) {
      for (IFormField f : getFirstChildFormFields(false)) {
        f.setMandatory(b, true);
      }
    }
  }

  @Override
  public double getOrder() {
    return propertySupport.getPropertyDouble(PROP_ORDER);
  }

  @Override
  public void setOrder(double order) {
    propertySupport.setPropertyDouble(PROP_ORDER, order);
  }

  @Override
  public IMultiStatus getErrorStatus() {
    final IMultiStatus ms = getErrorStatusInternal();
    return (ms == null) ? null : new MultiStatus(ms);
  }

  /**
   * @return the live error status
   */
  protected MultiStatus getErrorStatusInternal() {
    return (MultiStatus) propertySupport.getProperty(PROP_ERROR_STATUS);
  }

  @Override
  public void setErrorStatus(IMultiStatus status) {
    setErrorStatusInternal(new MultiStatus(status));
  }

  protected void setErrorStatusInternal(MultiStatus status) {
    propertySupport.setProperty(PROP_ERROR_STATUS, status);
  }

  @Override
  public void clearErrorStatus() {
    propertySupport.setProperty(PROP_ERROR_STATUS, null);
  }

  @Override
  public void addErrorStatus(String message) {
    addErrorStatus(new DefaultFieldStatus(message));
  }

  /**
   * Adds an error status
   */
  @Override
  public void addErrorStatus(IStatus newStatus) {
    final MultiStatus status = ensureMultiStatus(getErrorStatusInternal());
    // Create a copy, otherwise no PropertyChange event is fired
    final MultiStatus copy = new MultiStatus(status);
    copy.add(newStatus);
    setErrorStatus(copy);
  }

  /**
   * Remove IStatus of a specific type
   */
  @Override
  public void removeErrorStatus(Class<? extends IStatus> statusClazz) {
    final MultiStatus ms = getErrorStatusInternal();
    if (ms != null && ms.containsStatus(statusClazz)) {
      // Create a copy, otherwise no PropertyChange event is fired
      final MultiStatus copy = new MultiStatus(ms);
      copy.removeAll(statusClazz);
      if (copy.getChildren().isEmpty()) {
        clearErrorStatus();
      }
      else {
        setErrorStatusInternal(copy);
      }
    }
  }

  private MultiStatus ensureMultiStatus(IStatus s) {
    if (s instanceof MultiStatus) {
      return (MultiStatus) s;
    }
    final MultiStatus ms = new MultiStatus();
    if (s != null) {
      ms.add(s);
    }
    return ms;
  }

  @Override
  public IValidateContentDescriptor validateContent() {
    if (isContentValid()) {
      return null;
    }
    return getValidateContentDescriptor();
  }

  @Override
  public boolean isContentValid() {
    return !hasError() && isMandatoryFulfilled();
  }

  @Override
  public boolean isMandatoryFulfilled() {
    return !isMandatory() || !isEmpty();
  }

  /**
   * @return true, if it contains an error status with severity >= IStatus.ERROR
   */
  protected boolean hasError() {
    IStatus errorStatus = getErrorStatus();
    return errorStatus != null && (errorStatus.getSeverity() >= IStatus.ERROR);
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
  public void setTooltipAnchor(String tooltipAnchor) {
    propertySupport.setPropertyString(PROP_TOOLTIP_ANCHOR, tooltipAnchor);
  }

  @Override
  public String getTooltipAnchor() {
    return propertySupport.getPropertyString(PROP_TOOLTIP_ANCHOR);
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
  public void setLabelForegroundColor(String c) {
    propertySupport.setProperty(PROP_LABEL_FOREGROUND_COLOR, c);
  }

  @Override
  public String getLabelForegroundColor() {
    return (String) propertySupport.getProperty(PROP_LABEL_FOREGROUND_COLOR);
  }

  @Override
  public void setLabelBackgroundColor(String c) {
    propertySupport.setProperty(PROP_LABEL_BACKGROUND_COLOR, c);
  }

  @Override
  public String getLabelBackgroundColor() {
    return (String) propertySupport.getProperty(PROP_LABEL_BACKGROUND_COLOR);
  }

  @Override
  public void setLabelFont(FontSpec f) {
    propertySupport.setProperty(PROP_LABEL_FONT, f);
  }

  @Override
  public FontSpec getLabelFont() {
    return (FontSpec) propertySupport.getProperty(PROP_LABEL_FONT);
  }

  @Override
  public GridData getGridData() {
    return (GridData) propertySupport.getProperty(PROP_GRID_DATA);
  }

  @Override
  public void setGridDataInternal(GridData data) {
    propertySupport.setProperty(PROP_GRID_DATA, data);
  }

  @Override
  public GridData getGridDataHints() {
    return new GridData((GridData) propertySupport.getProperty(PROP_GRID_DATA_HINTS));
  }

  @Override
  public void setGridDataHints(GridData hints) {
    propertySupport.setProperty(PROP_GRID_DATA_HINTS, new GridData(hints));
  }

  @Override
  public void requestFocus() {
    IForm form = getForm();
    if (form != null) {
      form.requestFocus(this);
    }
  }

  @Override
  public void requestInput() {
    IForm form = getForm();
    if (form != null) {
      form.requestInput(this);
    }
  }

  @Override
  public void setPreventInitialFocus(boolean preventInitialFocus) {
    propertySupport.setPropertyBool(PROP_PREVENT_INITIAL_FOCUS, preventInitialFocus);
  }

  @Override
  public boolean isPreventInitialFocus() {
    return propertySupport.getPropertyBool(PROP_PREVENT_INITIAL_FOCUS);
  }

  @Override
  public void setMasterField(IValueField field) {
    IValueField oldMasterField = getMasterField();
    // remove old listener
    if (oldMasterField != null && m_currentMasterListener != null) {
      oldMasterField.removeMasterListener(m_currentMasterListener);
      m_currentMasterListener = null;
    }
    // add new listener and set enabling
    if (field != null) {
      m_currentMasterListener = new P_MasterListener();
      field.addMasterListener(m_currentMasterListener);
      setEnabledSlave(field.getValue() != null || !isMasterRequired());
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
  public void setMasterRequired(boolean masterRequired) {
    m_flags = FLAGS_BIT_HELPER.changeBit(MASTER_REQUIRED, masterRequired, m_flags);
  }

  @Override
  public boolean isMasterRequired() {
    return FLAGS_BIT_HELPER.isBitSet(MASTER_REQUIRED, m_flags);
  }

  @Override
  public void updateKeyStrokes() {
    m_objectExtensions.runInExtensionContext(() -> {
      m_contributionHolder.resetContributionsByClass(AbstractFormField.this, IKeyStroke.class);
      List<IKeyStroke> keyStrokes = initLocalKeyStrokes();
      propertySupport.setPropertyList(PROP_KEY_STROKES, keyStrokes);
    });
  }

  protected List<IKeyStroke> initLocalKeyStrokes() {
    List<Class<? extends IKeyStroke>> configuredKeyStrokes = getConfiguredKeyStrokes();
    List<IKeyStroke> contributedKeyStrokes = m_contributionHolder.getContributionsByClass(IKeyStroke.class);

    Map<String, IKeyStroke> ksMap = new HashMap<>(configuredKeyStrokes.size() + contributedKeyStrokes.size());
    for (Class<? extends IKeyStroke> keystrokeClazz : configuredKeyStrokes) {
      IKeyStroke ks = ConfigurationUtility.newInnerInstance(this, keystrokeClazz);
      ks.init();
      if (ks.getKeyStroke() != null) {
        ksMap.put(ks.getKeyStroke().toUpperCase(), ks);
      }
    }

    for (IKeyStroke ks : contributedKeyStrokes) {
      ks.init();
      if (ks.getKeyStroke() != null) {
        ksMap.put(ks.getKeyStroke().toUpperCase(), ks);
      }
    }
    return CollectionUtility.arrayListWithoutNullElements(ksMap.values());
  }

  @Override
  public List<IKeyStroke> getKeyStrokes() {
    return CollectionUtility.arrayList(getKeyStrokesInternal());
  }

  protected List<IKeyStroke> getKeyStrokesInternal() {
    return propertySupport.getPropertyList(PROP_KEY_STROKES);
  }

  @Override
  public boolean isStatusVisible() {
    return propertySupport.getPropertyBool(PROP_STATUS_VISIBLE);
  }

  @Override
  public void setStatusVisible(boolean statusVisible) {
    setStatusVisible(statusVisible, isInitConfigDone());
  }

  @Override
  public void setStatusVisible(boolean statusVisible, boolean recursive) {
    propertySupport.setPropertyBool(PROP_STATUS_VISIBLE, statusVisible);
    if (recursive) {
      for (IFormField f : getFirstChildFormFields(true)) {
        f.setStatusVisible(statusVisible, true);
      }
    }
  }

  @Override
  public String getStatusPosition() {
    return propertySupport.getPropertyString(PROP_STATUS_POSITION);
  }

  @Override
  public void setStatusPosition(String statusPosition) {
    propertySupport.setPropertyString(PROP_STATUS_POSITION, statusPosition);
  }

  protected void handleChildFieldVisibilityChanged() {
    // nop
  }

  protected void removeChildFieldPropertyChangeListener(IPropertyObserver f) {
    f.removePropertyChangeListener(m_fieldPropertyChangeListener);
  }

  protected void addChildFieldPropertyChangeListener(IPropertyObserver f) {
    f.addPropertyChangeListener(m_fieldPropertyChangeListener);
  }

  @Override
  public IValidateContentDescriptor getValidateContentDescriptor() {
    return m_validateContentDescriptor;
  }

  @Override
  public void setValidateContentDescriptor(IValidateContentDescriptor validateContentDescriptor) {
    m_validateContentDescriptor = validateContentDescriptor;
  }

  protected IFormFieldContextMenu createContextMenu(OrderedCollection<IMenu> menus) {
    return new FormFieldContextMenu<IFormField>(this, menus.getOrderedList());
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to add and/or remove menus<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
  }

  protected void setContextMenu(IFormFieldContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public IFormFieldContextMenu getContextMenu() {
    return (IFormFieldContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  @Override
  public <M extends IMenu> M getMenuByClass(Class<M> menuType) {
    return MenuUtility.getMenuByClass(this, menuType);
  }

  protected List<IStatusMenuMapping> createStatusMenuMappings() {
    List<Class<IStatusMenuMapping>> configuredMappings = getConfiguredStatusMenuMappings();
    List<IStatusMenuMapping> mappings = new ArrayList<>();
    for (Class<? extends IStatusMenuMapping> clazz : configuredMappings) {
      IStatusMenuMapping mapping = ConfigurationUtility.newInnerInstance(this, clazz);
      mappings.add(mapping);
    }
    for (IStatusMenuMapping mapping : mappings) {
      mapping.setParentFieldInternal(this);
    }
    return mappings;
  }

  protected List<Class<IStatusMenuMapping>> getConfiguredStatusMenuMappings() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClasses(dca, IStatusMenuMapping.class);
  }

  @Override
  public void setStatusMenuMappings(List<IStatusMenuMapping> mappings) {
    propertySupport.setProperty(PROP_STATUS_MENU_MAPPINGS, new ArrayList<>(mappings));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<IStatusMenuMapping> getStatusMenuMappings() {
    return (List<IStatusMenuMapping>) propertySupport.getProperty(PROP_STATUS_MENU_MAPPINGS);
  }

  private final class FindClassIdVisitor implements IBreadthFirstTreeVisitor<IFormField> {
    private final String m_currentClassId;
    private boolean m_found = false;
    private final AbstractFormField m_fixedField;

    private FindClassIdVisitor(String currentClassId, AbstractFormField fixedField) {
      m_currentClassId = currentClassId;
      m_fixedField = fixedField;
    }

    @Override
    public TreeVisitResult visit(IFormField element, int level, int index) {
      if (m_fixedField != element && m_currentClassId.equals(ConfigurationUtility.getAnnotatedClassIdWithFallback(element.getClass(), true))) {
        m_found = true;
        return TreeVisitResult.TERMINATE;
      }

      return TreeVisitResult.CONTINUE;
    }

    public boolean isFound() {
      return m_found;
    }

  }

  /**
   * Implementation of PropertyChangeListener Proxy on all attached fields (not groups)
   */
  class P_FieldPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent e) {
      if (PROP_VISIBLE.equals(e.getPropertyName())) {
        handleChildFieldVisibilityChanged();
      }
      else if (PROP_SAVE_NEEDED.equals(e.getPropertyName())) {
        checkSaveNeeded();
      }
      else if (PROP_EMPTY.equals(e.getPropertyName())) {
        checkEmpty();
      }
    }
  }

  private class P_MasterListener implements MasterListener {
    @Override
    public void masterChanged(Object newMasterValue) {
      // only active if the unique listener itself
      if (this == m_currentMasterListener) {
        setEnabledSlave(newMasterValue != null || !isMasterRequired());
        try {
          interceptChangedMasterValue(newMasterValue);
        }
        catch (RuntimeException | PlatformError e) {
          BEANS.get(ExceptionHandler.class).handle(e);
        }
      }
    }
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalFormFieldExtension<OWNER extends AbstractFormField> extends AbstractExtension<OWNER> implements IFormFieldExtension<OWNER> {

    public LocalFormFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execDataChanged(FormFieldDataChangedChain chain, Object... dataTypes) {
      getOwner().execDataChanged(dataTypes);
    }

    @Override
    public void execAddSearchTerms(FormFieldAddSearchTermsChain chain, SearchFilter search) {
      getOwner().execAddSearchTerms(search);
    }

    @Override
    public void execChangedMasterValue(FormFieldChangedMasterValueChain chain, Object newMasterValue) {
      getOwner().execChangedMasterValue(newMasterValue);
    }

    @Override
    public void execDisposeField(FormFieldDisposeFieldChain chain) {
      getOwner().execDisposeField();
    }

    @Override
    public void execInitField(FormFieldInitFieldChain chain) {
      getOwner().execInitField();
    }

    @Override
    public boolean execCalculateVisible(FormFieldCalculateVisibleChain chain) {
      return getOwner().execCalculateVisible();
    }

    @Override
    public void execMarkSaved(FormFieldMarkSavedChain chain) {
      getOwner().execMarkSaved();
    }

    @Override
    public boolean execIsEmpty(FormFieldIsEmptyChain chain) {
      return getOwner().execIsEmpty();
    }

    @Override
    public boolean execIsSaveNeeded(FormFieldIsSaveNeededChain chain) {
      return getOwner().execIsSaveNeeded();
    }

  }

  protected final void interceptDataChanged(Object... dataTypes) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    FormFieldDataChangedChain chain = new FormFieldDataChangedChain(extensions);
    chain.execDataChanged(dataTypes);
  }

  protected final void interceptAddSearchTerms(SearchFilter search) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    FormFieldAddSearchTermsChain chain = new FormFieldAddSearchTermsChain(extensions);
    chain.execAddSearchTerms(search);
  }

  protected final void interceptChangedMasterValue(Object newMasterValue) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    FormFieldChangedMasterValueChain chain = new FormFieldChangedMasterValueChain(extensions);
    chain.execChangedMasterValue(newMasterValue);
  }

  protected final void interceptDisposeField() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    FormFieldDisposeFieldChain chain = new FormFieldDisposeFieldChain(extensions);
    chain.execDisposeField();
  }

  protected final void interceptInitField() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    FormFieldInitFieldChain chain = new FormFieldInitFieldChain(extensions);
    chain.execInitField();
  }

  protected final boolean interceptCalculateVisible() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    FormFieldCalculateVisibleChain chain = new FormFieldCalculateVisibleChain(extensions);
    return chain.execCalculateVisible();
  }

  protected final void interceptMarkSaved() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    FormFieldMarkSavedChain chain = new FormFieldMarkSavedChain(extensions);
    chain.execMarkSaved();
  }

  protected final boolean interceptIsEmpty() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    FormFieldIsEmptyChain chain = new FormFieldIsEmptyChain(extensions);
    return chain.execIsEmpty();
  }

  protected final boolean interceptIsSaveNeeded() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    FormFieldIsSaveNeededChain chain = new FormFieldIsSaveNeededChain(extensions);
    return chain.execIsSaveNeeded();
  }
}
