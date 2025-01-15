/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.groupbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.IGroupBoxExtension;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.LogicalGridLayoutConfig;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.GroupBoxProcessButtonGrid;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.VerticalSmartGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.notification.INotification;
import org.eclipse.scout.rt.client.ui.notification.NotificationEvent;
import org.eclipse.scout.rt.client.ui.notification.NotificationListener;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TriState;

@ClassId("6a093505-c2b1-4df2-84d6-e799f91e6e7c")
public abstract class AbstractGroupBox extends AbstractCompositeField implements IGroupBox {

  private IGroupBoxUIFacade m_uiFacade;
  private boolean m_mainBoxFlag = false;
  private List<IFormField> m_controlFields;
  private List<IGroupBox> m_groupBoxes;
  private List<IButton> m_customButtons;
  private List<IButton> m_systemButtons;
  private IGroupBoxBodyGrid m_bodyGrid;
  private GroupBoxProcessButtonGrid m_customProcessButtonGrid;
  private GroupBoxProcessButtonGrid m_systemProcessButtonGrid;

  public AbstractGroupBox() {
    this(true);
  }

  public AbstractGroupBox(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * {@inheritDoc} Default for group boxes is true.
   */
  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return true;
  }

  /**
   * Configures the text of the sub label. The sub label is usually displayed below the label.
   */
  protected String getConfiguredSubLabel() {
    return null;
  }

  /**
   * Configures the number of columns used in this group box.<br>
   * A typical {@link IFormField} inside a group box spans one column. This behavior can be changed by setting
   * {@link AbstractFormField#getConfiguredGridW()}.
   * <p>
   * Subclasses can override this method. Default is -1 which typically means 2 columns.
   *
   * @return the number of columns used in this group box
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(200)
  protected int getConfiguredGridColumnCount() {
    return -1;
  }

  /**
   * @return the body grid responsible to set {@link GridData} to the fields in this group box.
   */
  @ConfigProperty(ConfigProperty.GROUP_BOX_BODY_GRID)
  @Order(210)
  protected Class<? extends IGroupBoxBodyGrid> getConfiguredBodyGrid() {
    return VerticalSmartGroupBoxBodyGrid.class;
  }

  /**
   * Configures the layout hints.
   * <p>
   * The hints are set to -1 by default which means the values will be set by the UI.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(215)
  protected LogicalGridLayoutConfig getConfiguredBodyLayoutConfig() {
    return new LogicalGridLayoutConfig();
  }

  /**
   * Configures the border visibility for this group box. <br>
   * If the property is set to true, a border will be displayed around the group box. The style of the border is
   * configured by {@link #getConfiguredBorderDecoration()}.<br>
   * If the property is set to false, no border will be displayed and the margin reserved for the border will be
   * removed.
   * <p>
   * <b>Hint:</b> Keep in mind that setting the border to invisible also removes the margin which could lead to a
   * misalignment of the fields if several group boxes are used on a form. In order to preserve the correct alignment
   * consider using {@link #getConfiguredBorderDecoration()} with {@link IGroupBox#BORDER_DECORATION_EMPTY} and
   * {@link #getConfiguredBorderVisible()} with {@code true} instead.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if the border is visible, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(230)
  protected boolean getConfiguredBorderVisible() {
    return true;
  }

  /**
   * Configures whether this group box should be expandable or not.<br>
   * This property depends on the border decoration which can be configured by {@link #getConfiguredBorderDecoration()}.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the group box should be expandable, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(231)
  protected boolean getConfiguredExpandable() {
    return false;
  }

  /**
   * Configures whether this group box is initially expanded. <br>
   * This property only has an effect if the group box is expandable which can be configured by
   * {@link #getConfiguredExpandable()}.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if the group box should be initially expanded, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(232)
  protected boolean getConfiguredExpanded() {
    return true;
  }

  /**
   * Configures whether the expanded state of the group box should be cached. <br>
   * This property only has an effect if the group box is expandable which can be configured by
   * {@link #getConfiguredExpandable()}.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if the expanded state of the group box should be cached, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(232)
  protected boolean getConfiguredCacheExpanded() {
    return false;
  }

  /**
   * Configures the border decoration for this group box. See {@code IGroupBox#BORDER_DECORATION_*} constants for valid
   * values.<br>
   * This property only has an effect if the border is visible which can be configured by
   * {@link #getConfiguredBorderVisible()}.
   * <p>
   * Subclasses can override this method. Default is {@link IGroupBox#BORDER_DECORATION_AUTO}.
   *
   * @return the border decoration of the group box
   */
  @ConfigProperty(ConfigProperty.BORDER_DECORATION)
  @Order(233)
  protected String getConfiguredBorderDecoration() {
    return BORDER_DECORATION_AUTO;
  }

  /**
   * Configures the background image for this group box.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the ID (name) of the image
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(240)
  protected String getConfiguredBackgroundImageName() {
    return null;
  }

  /**
   * Configures the horizontal alignment of the background image.<br>
   * This property only has an effect if the group box has a background image which can be configured by
   * {@link #getConfiguredBackgroundImageName()}
   * <p>
   * Subclasses can override this method. Default alignment is center.
   *
   * @return -1 for left, 0 for center and 1 for right alignment
   */
  @ConfigProperty(ConfigProperty.HORIZONTAL_ALIGNMENT)
  @Order(250)
  protected int getConfiguredBackgroundImageHorizontalAlignment() {
    return 0;
  }

  /**
   * Configures the vertical alignment of the background image.<br>
   * This property only has an effect if the group box has a background image which can be configured by
   * {@link #getConfiguredBackgroundImageName()}
   * <p>
   * Subclasses can override this method. Default alignment is center.
   *
   * @return -1 for top, 0 for center and 1 for bottom alignment
   */
  @ConfigProperty(ConfigProperty.VERTICAL_ALIGNMENT)
  @Order(260)
  protected int getConfiguredBackgroundImageVerticalAlignment() {
    return 0;
  }

  /**
   * Configures whether this group box should be scrollable in vertical direction.</br>
   * If the property is set to {@link TriState#TRUE}, a vertical scrollbar will appear if the content is too large to be
   * displayed.<br>
   * If the property is set to {@link TriState#UNDEFINED}, it will be true if the groupbox is the mainbox in a form.
   * Otherwise it will be false.
   * <p>
   * By default {@link TriState#UNDEFINED} is returned which means every mainbox is scrollable. If you want another
   * groupbox to be scrollable, you have to set the groupbox to scrollable while setting the mainbox to scrollable =
   * false.
   * <p>
   * Subclasses can override this method. Default is {@link TriState#UNDEFINED}.
   *
   * @return {@link TriState#TRUE} if the group box should be scrollable, {@link TriState#FALSE} if not,
   * {@link TriState#UNDEFINED} if default logic should be applied
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected TriState getConfiguredScrollable() {
    return TriState.UNDEFINED;
  }

  /**
   * Configures the keystroke to select this group box.<br>
   * If the groupbox is not inside a tabbox, this configured selection keyStroke will be ignored.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the keyStroke to select this groupbox (inside a tabbox)
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(280)
  protected String getConfiguredSelectionKeyStroke() {
    return null;
  }

  /**
   * Configures the column span of this group box.<br>
   * The value defined by this property refers to the number of columns defined by the container of this group box. <br>
   * The column count of the container, which actually is the parent group box, can be configured by
   * {@link #getConfiguredGridColumnCount()} (you need to configure that in the parent group box).
   * <p>
   * <b>Example:</b> If the column count of the container is set to 3 and a column span of this group box is set to 2 it
   * means 2/3 of the container width is used for this group box.
   * <p>
   * Subclasses can override this method. Default is {@link IFormField#FULL_WIDTH} which means it spans every column of
   * the container.
   *
   * @return the number of columns to span
   */
  @Override
  protected int getConfiguredGridW() {
    return FULL_WIDTH;
  }

  /**
   * Override to set the menuBar position for this {@link IGroupBox}. By default, {@link #MENU_BAR_POSITION_AUTO} is
   * configured.
   * <ul>
   * <li>{@link #MENU_BAR_POSITION_AUTO}</li>
   * <li>{@link #MENU_BAR_POSITION_TOP}</li>
   * <li>{@link #MENU_BAR_POSITION_BOTTOM}</li>
   * <li>{@link #MENU_BAR_POSITION_TITLE}</li>
   * </ul>
   */
  @ConfigProperty(ConfigProperty.GROUP_BOX_MENU_BAR_POSITION)
  @Order(300)
  protected String getConfiguredMenuBarPosition() {
    return MENU_BAR_POSITION_AUTO;
  }

  /**
   * Override to set the menuBar ellipsis position for this {@link IGroupBox}. By default,
   * {@link #MENU_BAR_ELLIPSIS_POSITION_RIGHT} is configured.
   * <ul>
   * <li>{@link #MENU_BAR_ELLIPSIS_POSITION_LEFT}</li>
   * <li>{@link #MENU_BAR_ELLIPSIS_POSITION_RIGHT}</li>
   * </ul>
   */
  @ConfigProperty(ConfigProperty.GROUP_BOX_MENU_BAR_ELLIPSIS_POSITION)
  @Order(310)
  protected String getConfiguredMenuBarEllipsisPosition() {
    return MENU_BAR_ELLIPSIS_POSITION_RIGHT;
  }

  /**
   * Configures whether this group box should be responsive.</br>
   * If the property is set to {@link TriState#TRUE}, the content of the group box will be adjusted to ensure best
   * readability, when the width of the group box is less than its preferred size.<br>
   * If the property is set to {@link TriState#UNDEFINED}, it will be true if the group box is the main box in a form.
   * Otherwise it will be false.
   * <p>
   * By default {@link TriState#UNDEFINED} is returned which means every main box is responsive.
   * <p>
   * Subclasses can override this method. Default is {@link TriState#UNDEFINED}.
   *
   * @return {@link TriState#TRUE} if the group box should be responsive, {@link TriState#FALSE} if not,
   * {@link TriState#UNDEFINED} if default logic should be applied
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(320)
  protected TriState getConfiguredResponsive() {
    return TriState.UNDEFINED;
  }

  @Override
  public String getPreferenceBaseKey() {
    return getClass().getName();
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    Class<? extends IGroupBoxBodyGrid> bodyGridClazz = getConfiguredBodyGrid();
    if (bodyGridClazz != null) {
      IGroupBoxBodyGrid bodyGrid;
      try {
        bodyGrid = bodyGridClazz.getConstructor().newInstance();
        setBodyGrid(bodyGrid);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + bodyGridClazz.getName() + "'.", e));
      }
    }
    m_customProcessButtonGrid = new GroupBoxProcessButtonGrid(this, true, false);
    m_systemProcessButtonGrid = new GroupBoxProcessButtonGrid(this, false, true);
    super.initConfig();
    categorizeFields();

    setSubLabel(getConfiguredSubLabel());
    setExpandable(getConfiguredExpandable());
    setExpanded(getConfiguredExpanded());
    setCacheExpanded(getConfiguredCacheExpanded());
    setBorderVisible(getConfiguredBorderVisible());
    setBorderDecoration(getConfiguredBorderDecoration());
    setGridColumnCount(getConfiguredGridColumnCount());
    setBackgroundImageName(getConfiguredBackgroundImageName());
    setBackgroundImageHorizontalAlignment(getConfiguredBackgroundImageHorizontalAlignment());
    setBackgroundImageVerticalAlignment(getConfiguredBackgroundImageVerticalAlignment());
    setScrollable(getConfiguredScrollable());
    setSelectionKeyStroke(getConfiguredSelectionKeyStroke());
    setBodyLayoutConfig(getConfiguredBodyLayoutConfig());
    setMenuBarPosition(getConfiguredMenuBarPosition());
    setMenuBarEllipsisPosition(getConfiguredMenuBarEllipsisPosition());
    setResponsive(getConfiguredResponsive());

    if (isCacheExpanded()) {
      Optional.ofNullable(ClientUIPreferences.getInstance().isFieldCollapsed(this)).ifPresent(b -> setExpanded(!b));
    }
  }

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    if (isCacheExpanded()) {
      ClientUIPreferences.getInstance().setFieldCollapsed(this, !isExpanded());
    }
  }

  private void categorizeFields() {
    // categorize items
    List<IFormField> controlList = new ArrayList<>();
    List<IGroupBox> groupList = new ArrayList<>();
    List<IButton> customButtonList = new ArrayList<>();
    List<IButton> systemButtonList = new ArrayList<>();
    for (IFormField field : getFields()) {
      if (field instanceof IGroupBox) {
        groupList.add((IGroupBox) field);
        controlList.add(field);
      }
      else if (field instanceof IButton) {
        IButton b = (IButton) field;
        if (b.isProcessButton()) {
          if (b.getSystemType() != IButton.SYSTEM_TYPE_NONE) {
            systemButtonList.add((IButton) field);
          }
          else {
            customButtonList.add((IButton) field);
          }
        }
        else {
          controlList.add(field);
        }
      }
      else {
        controlList.add(field);
      }
    }
    m_controlFields = controlList;
    m_groupBoxes = groupList;
    m_customButtons = customButtonList;
    m_systemButtons = systemButtonList;
  }

  private void ensureCategorized() {
    if (m_controlFields == null || m_groupBoxes == null || m_customButtons == null || m_systemButtons == null) {
      categorizeFields();
    }
  }

  private void clearCategorization() {
    m_controlFields = null;
    m_groupBoxes = null;
    m_customButtons = null;
    m_systemButtons = null;
  }

  @Override
  public void rebuildFieldGrid() {
    m_bodyGrid.validate(this);
    m_customProcessButtonGrid.validate();
    m_systemProcessButtonGrid.validate();
    if (isInitConfigDone() && getForm() != null) {
      getForm().structureChanged(this);
    }
  }

  @Override
  public String getSubLabel() {
    return propertySupport.getPropertyString(PROP_SUB_LABEL);
  }

  @Override
  public void setSubLabel(String subLabel) {
    propertySupport.setPropertyString(PROP_SUB_LABEL, subLabel);
  }

  @Override
  public boolean isMainBox() {
    return m_mainBoxFlag;
  }

  @Override
  public void setMainBox(boolean b) {
    m_mainBoxFlag = b;
  }

  @Override
  public int getGroupBoxIndex(IGroupBox groupBox) {
    return m_groupBoxes.indexOf(groupBox);
  }

  @Override
  public int getCustomProcessButtonCount() {
    return m_customButtons.size();
  }

  @Override
  public int getGroupBoxCount() {
    return m_groupBoxes.size();
  }

  @Override
  public int getSystemProcessButtonCount() {
    return m_systemButtons.size();
  }

  @Override
  public List<IGroupBox> getGroupBoxes() {
    ensureCategorized();
    return CollectionUtility.arrayList(m_groupBoxes);
  }

  @Override
  public List<IFormField> getControlFields() {
    ensureCategorized();
    return CollectionUtility.arrayList(m_controlFields);
  }

  @Override
  public List<IButton> getCustomProcessButtons() {
    ensureCategorized();
    return CollectionUtility.arrayList(m_customButtons);
  }

  @Override
  public List<IButton> getSystemProcessButtons() {
    ensureCategorized();
    return CollectionUtility.arrayList(m_systemButtons);
  }

  public void setBodyGrid(IGroupBoxBodyGrid bodyGrid) {
    m_bodyGrid = bodyGrid;
  }

  @Override
  public void addField(IFormField f) {
    super.addField(f);
    clearCategorization();
  }

  @Override
  public void removeField(IFormField f) {
    super.removeField(f);
    clearCategorization();
  }

  @Override
  public IGroupBoxBodyGrid getFieldGrid() {
    return m_bodyGrid;
  }

  @Override
  public void setGridColumnCount(int c) {
    propertySupport.setPropertyInt(PROP_GRID_COLUMN_COUNT, c);
    if (isInitConfigDone()) {
      rebuildFieldGrid();
    }
  }

  @Override
  public int getGridColumnCount() {
    return propertySupport.getPropertyInt(PROP_GRID_COLUMN_COUNT);
  }

  @Override
  public LogicalGridLayoutConfig getBodyLayoutConfig() {
    return (LogicalGridLayoutConfig) propertySupport.getProperty(PROP_BODY_LAYOUT_CONFIG);
  }

  @Override
  public void setBodyLayoutConfig(LogicalGridLayoutConfig layoutConfig) {
    propertySupport.setProperty(PROP_BODY_LAYOUT_CONFIG, layoutConfig);
  }

  @Override
  public TriState isScrollable() {
    return (TriState) propertySupport.getProperty(PROP_SCROLLABLE);
  }

  @Override
  public void setScrollable(TriState scrollable) {
    if (scrollable == null) {
      scrollable = TriState.UNDEFINED;
    }
    if (scrollable.equals(isScrollable())) {
      return;
    }
    propertySupport.setProperty(PROP_SCROLLABLE, scrollable);
    if (scrollable.isTrue()) {
      // force weightY to be > 0
      GridData gd = getGridDataHints();
      if (gd.weightY <= 0) {
        gd.weightY = 1;
        setGridDataHints(gd);
      }
    }
  }

  @Override
  public void setScrollable(boolean scrollable) {
    setScrollable(TriState.parse(scrollable));
  }

  // box is only visible when it has at least one visible item
  @Override
  protected void handleChildFieldVisibilityChanged() {
    super.handleChildFieldVisibilityChanged();
    if (isInitConfigDone()) {
      rebuildFieldGrid();
    }
  }

  @Override
  public boolean isBorderVisible() {
    return propertySupport.getPropertyBool(PROP_BORDER_VISIBLE);
  }

  @Override
  public void setBorderVisible(boolean b) {
    propertySupport.setPropertyBool(PROP_BORDER_VISIBLE, b);
  }

  @Override
  public String getBorderDecoration() {
    return propertySupport.getPropertyString(PROP_BORDER_DECORATION);
  }

  @Override
  public void setBorderDecoration(String s) {
    propertySupport.setPropertyString(PROP_BORDER_DECORATION, s);
  }

  @Override
  public boolean isExpandable() {
    return propertySupport.getPropertyBool(PROP_EXPANDABLE);
  }

  @Override
  public void setExpandable(boolean b) {
    propertySupport.setPropertyBool(PROP_EXPANDABLE, b);
  }

  @Override
  public boolean isExpanded() {
    return propertySupport.getPropertyBool(PROP_EXPANDED);
  }

  @Override
  public void setExpanded(boolean b) {
    propertySupport.setPropertyBool(PROP_EXPANDED, b);
  }

  @Override
  public boolean isCacheExpanded() {
    return propertySupport.getPropertyBool(PROP_CACHE_EXPANDED);
  }

  @Override
  public void setCacheExpanded(boolean b) {
    propertySupport.setPropertyBool(PROP_CACHE_EXPANDED, b);
  }

  @Override
  public String getSelectionKeyStroke() {
    return propertySupport.getPropertyString(PROP_SELECTION_KEYSTROKE);
  }

  @Override
  public void setSelectionKeyStroke(String keystroke) {
    propertySupport.setPropertyString(PROP_SELECTION_KEYSTROKE, keystroke);
  }

  @Override
  public IGroupBoxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public void setBackgroundImageName(String imageName) {
    propertySupport.setPropertyString(PROP_BACKGROUND_IMAGE_NAME, imageName);
  }

  @Override
  public String getBackgroundImageName() {
    return propertySupport.getPropertyString(PROP_BACKGROUND_IMAGE_NAME);
  }

  @Override
  public INotification getNotification() {
    return (INotification) propertySupport.getProperty(PROP_NOTIFICATION);
  }

  @Override
  public void setNotification(INotification notification) {
    if (notification != null) {
      notification.setParentInternal(this);
      notification.addNotificationListener(new NotificationListener() {
        @Override
        public void notificationChanged(NotificationEvent event) {
          if (NotificationEvent.TYPE_CLOSED == event.getType()) {
            if (event.getNotification() != null) {
              event.getNotification().removeNotificationListener(this);
            }
            removeNotification();
          }
        }
      });
    }
    propertySupport.setProperty(PROP_NOTIFICATION, notification);
  }

  @Override
  public void removeNotification() {
    propertySupport.setProperty(PROP_NOTIFICATION, null);
  }

  /**
   * @since Build 178
   */
  @Override
  public void setBackgroundImageVerticalAlignment(int a) {
    propertySupport.setPropertyInt(PROP_BACKGROUND_IMAGE_VERTICAL_ALIGNMENT, a);
  }

  /**
   * @since Build 178
   */
  @Override
  public int getBackgroundImageVerticalAlignment() {
    return propertySupport.getPropertyInt(PROP_BACKGROUND_IMAGE_VERTICAL_ALIGNMENT);
  }

  /**
   * @since Build 178
   */
  @Override
  public void setBackgroundImageHorizontalAlignment(int a) {
    propertySupport.setPropertyInt(PROP_BACKGROUND_IMAGE_HORIZONTAL_ALIGNMENT, a);
  }

  /**
   * @since Build 178
   */
  @Override
  public int getBackgroundImageHorizontalAlignment() {
    return propertySupport.getPropertyInt(PROP_BACKGROUND_IMAGE_HORIZONTAL_ALIGNMENT);
  }

  @Override
  public String getMenuBarPosition() {
    return propertySupport.getPropertyString(PROP_MENU_BAR_POSITION);
  }

  @Override
  public void setMenuBarPosition(String menuBarPosition) {
    propertySupport.setPropertyString(PROP_MENU_BAR_POSITION, menuBarPosition);
  }

  @Override
  public String getMenuBarEllipsisPosition() {
    return propertySupport.getPropertyString(PROP_MENU_BAR_ELLIPSIS_POSITION);
  }

  @Override
  public void setMenuBarEllipsisPosition(String menuBarEllipsisPosition) {
    propertySupport.setPropertyString(PROP_MENU_BAR_ELLIPSIS_POSITION, menuBarEllipsisPosition);
  }

  @Override
  public TriState isResponsive() {
    return (TriState) propertySupport.getProperty(PROP_RESPONSIVE);
  }

  @Override
  public void setResponsive(TriState responsive) {
    if (responsive == null) {
      responsive = TriState.UNDEFINED;
    }
    propertySupport.setProperty(PROP_RESPONSIVE, responsive);
  }

  @Override
  public void setResponsive(boolean responsive) {
    setResponsive(TriState.parse(responsive));
  }

  protected class P_UIFacade implements IGroupBoxUIFacade {
    @Override
    public void setExpandedFromUI(boolean expanded) {
      setExpanded(expanded);
    }
  }

  protected static class LocalGroupBoxExtension<OWNER extends AbstractGroupBox> extends LocalCompositeFieldExtension<OWNER> implements IGroupBoxExtension<OWNER> {

    public LocalGroupBoxExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IGroupBoxExtension<? extends AbstractGroupBox> createLocalExtension() {
    return new LocalGroupBoxExtension<>(this);
  }
}
