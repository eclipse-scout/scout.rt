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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.action.keystroke.DefaultFormEnterKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.DefaultFormEscapeKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.GroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.GroupBoxProcessButtonGrid;

@ClassId("6a093505-c2b1-4df2-84d6-e799f91e6e7c")
public abstract class AbstractGroupBox extends AbstractCompositeField implements IGroupBox {

  private IGroupBoxUIFacade m_uiFacade;
  private boolean m_mainBoxFlag = false;
  private int m_gridColumnCountHint;
  private boolean m_scrollable;
  private List<IFormField> m_controlFields;
  private List<IGroupBox> m_groupBoxes;
  private List<IButton> m_customButtons;
  private List<IButton> m_systemButtons;
  private GroupBoxBodyGrid m_bodyGrid;
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
   * Configures the border visibility for this group box. <br>
   * If the property is set to true, a border will be displayed
   * around the group box. The style of the border is configured by {@link #getConfiguredBorderDecoration()}.<br>
   * If the
   * property is set to false, no border will be displayed and the margin reserved for the border will be removed.
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
   * It typically only has an effect if the border decoration is set to {@link IGroupBox#BORDER_DECORATION_SECTION} or
   * {@link IGroupBox#BORDER_DECORATION_AUTO}.
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
   * Configures whether this group box should be scrollable.</br>
   * If the property is set to true, a vertical scrollbar will appear if the content is too large to be displayed.
   * <p>
   * Subclasses can override this method. Default is false.
   * 
   * @return {@code true} if the group box should be scrollable, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredScrollable() {
    return false;
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

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    m_bodyGrid = new GroupBoxBodyGrid(this);
    m_customProcessButtonGrid = new GroupBoxProcessButtonGrid(this, true, false);
    m_systemProcessButtonGrid = new GroupBoxProcessButtonGrid(this, false, true);
    super.initConfig();
    // categorize items
    List<IFormField> controlList = new ArrayList<IFormField>();
    List<IGroupBox> groupList = new ArrayList<IGroupBox>();
    List<IButton> customButtonList = new ArrayList<IButton>();
    List<IButton> systemButtonList = new ArrayList<IButton>();
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
    m_controlFields = Collections.unmodifiableList(controlList);
    m_groupBoxes = Collections.unmodifiableList(groupList);
    m_customButtons = Collections.unmodifiableList(customButtonList);
    m_systemButtons = Collections.unmodifiableList(systemButtonList);
    //
    setExpandable(getConfiguredExpandable());
    setExpanded(getConfiguredExpanded());
    setBorderVisible(getConfiguredBorderVisible());
    setBorderDecoration(getConfiguredBorderDecoration());
    setGridColumnCountHint(getConfiguredGridColumnCount());
    setBackgroundImageName(getConfiguredBackgroundImageName());
    setBackgroundImageHorizontalAlignment(getConfiguredBackgroundImageHorizontalAlignment());
    setBackgroundImageVerticalAlignment(getConfiguredBackgroundImageVerticalAlignment());
    setScrollable(getConfiguredScrollable());
  }

  @Override
  public List<IKeyStroke> getContributedKeyStrokes() {
    List<IKeyStroke> list = new ArrayList<IKeyStroke>(2);
    if (isMainBox() && (getForm() != null && getForm().getOuterForm() == null)) {
      // add default escape and enter key stroke only if no similar key stroke
      // is defined on the mainbox
      boolean hasEnter = false;
      boolean hasEscape = false;
      for (IKeyStroke ks : getLocalKeyStrokes()) {
        if ("enter".equalsIgnoreCase(ks.getKeyStroke())) {
          hasEnter = true;
        }
        if ("escape".equalsIgnoreCase(ks.getKeyStroke())) {
          hasEscape = true;
        }
      }
      if (!hasEnter) {
        list.add(new DefaultFormEnterKeyStroke(getForm()));
      }
      if (!hasEscape) {
        list.add(new DefaultFormEscapeKeyStroke(getForm()));
      }
    }
    return Collections.unmodifiableList(list);
  }

  @Override
  public void rebuildFieldGrid() {
    m_bodyGrid.validate();
    m_customProcessButtonGrid.validate();
    m_systemProcessButtonGrid.validate();
    if (isInitialized()) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
    }
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
    return CollectionUtility.unmodifiableListCopy(m_groupBoxes);
  }

  @Override
  public List<IFormField> getControlFields() {
    return CollectionUtility.unmodifiableListCopy(m_controlFields);
  }

  @Override
  public List<IButton> getCustomProcessButtons() {
    return CollectionUtility.unmodifiableListCopy(m_customButtons);
  }

  @Override
  public List<IButton> getSystemProcessButtons() {
    return CollectionUtility.unmodifiableListCopy(m_systemButtons);
  }

  @Override
  public final int getGridColumnCount() {
    return m_bodyGrid.getGridColumnCount();
  }

  @Override
  public final int getGridRowCount() {
    return m_bodyGrid.getGridRowCount();
  }

  @Override
  public void setGridColumnCountHint(int c) {
    m_gridColumnCountHint = c;
    if (isInitialized()) {
      rebuildFieldGrid();
    }
  }

  @Override
  public int getGridColumnCountHint() {
    return m_gridColumnCountHint;
  }

  @Override
  public boolean isScrollable() {
    return m_scrollable;
  }

  @Override
  public void setScrollable(boolean scrollable) {
    if (m_scrollable != scrollable) {
      m_scrollable = scrollable;
      if (m_scrollable) {
        // force weighty to be > 0
        GridData gd = getGridDataHints();
        if (gd.weightY <= 0) {
          gd.weightY = 1;
          setGridDataHints(gd);
        }
      }
    }
  }

  // box is only visible when it has at least one visible item
  @Override
  protected void handleFieldVisibilityChanged() {
    super.handleFieldVisibilityChanged();
    if (isInitialized()) {
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

  private class P_UIFacade implements IGroupBoxUIFacade {
    @Override
    public void setExpandedFromUI(boolean expanded) {
      setExpanded(expanded);
    }
  }
}
