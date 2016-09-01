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
package org.eclipse.scout.rt.client.ui.form.fields.groupbox;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.form.fields.groupbox.IGroupBoxExtension;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.MenuUtility;
import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.FormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractCompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.VerticalSmartGroupBoxBodyGrid;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;

@ClassId("6a093505-c2b1-4df2-84d6-e799f91e6e7c")
public abstract class AbstractGroupBox extends AbstractCompositeField implements IGroupBox {

  private IGroupBoxUIFacade m_uiFacade;
  private boolean m_mainBoxFlag = false;
  private TriState m_scrollable;
  private List<IFormField> m_controlFields;
  private List<IGroupBox> m_groupBoxes;
  private List<IButton> m_customButtons;
  private List<IButton> m_systemButtons;
  private IGroupBoxBodyGrid m_bodyGrid;

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
   * @return the body grid responsible to set {@link GridData} to the fields in this group box.
   */
  @ConfigProperty(ConfigProperty.GROUP_BOX_BODY_GRID)
  @Order(210)
  protected Class<? extends IGroupBoxBodyGrid> getConfiguredBodyGrid() {
    return VerticalSmartGroupBoxBodyGrid.class;
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
   *         {@link TriState#UNDEFINED} if default logic should be applied
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

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    Class<? extends IGroupBoxBodyGrid> bodyGridClazz = getConfiguredBodyGrid();
    if (bodyGridClazz != null) {
      IGroupBoxBodyGrid bodyGrid;
      try {
        bodyGrid = bodyGridClazz.newInstance();
        setBodyGrid(bodyGrid);
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + bodyGridClazz.getName() + "'.", e));
      }
    }
    super.initConfig();
    categorizeFields();

    setExpandable(getConfiguredExpandable());
    setExpanded(getConfiguredExpanded());
    setBorderVisible(getConfiguredBorderVisible());
    setBorderDecoration(getConfiguredBorderDecoration());
    setGridColumnCountHint(getConfiguredGridColumnCount());
    setBackgroundImageName(getConfiguredBackgroundImageName());
    setBackgroundImageHorizontalAlignment(getConfiguredBackgroundImageHorizontalAlignment());
    setBackgroundImageVerticalAlignment(getConfiguredBackgroundImageVerticalAlignment());
    setScrollable(getConfiguredScrollable());
    setSelectionKeyStroke(getConfiguredSelectionKeyStroke());
    initMenus();
  }

  private void initMenus() {
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      menus.addOrdered(ConfigurationUtility.newInnerInstance(this, menuClazz));
    }
    menus.addAllOrdered(contributedMenus);
    injectMenusInternal(menus);
    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    // set container on menus
    IFormFieldContextMenu contextMenu = new FormFieldContextMenu<IGroupBox>(this, menus.getOrderedList());
    contextMenu.setContainerInternal(this);
    setContextMenu(contextMenu);
  }

  @Override
  protected void initFieldInternal() {
    super.initFieldInternal();
    // init actions
    ActionUtility.initActions(getMenus());
  }

  private void categorizeFields() {
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
  public <T extends IMenu> T getMenuByClass(Class<T> menuType) {
    return MenuUtility.getMenuByClass(this, menuType);
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @Override
  public void rebuildFieldGrid() {
    m_bodyGrid.validate(this);
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
  public IGroupBoxBodyGrid getBodyGrid() {
    return m_bodyGrid;
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
    propertySupport.setPropertyInt(PROP_GRID_COLUMN_COUNT_HINT, c);
    if (isInitialized()) {
      rebuildFieldGrid();
    }
  }

  @Override
  public int getGridColumnCountHint() {
    return propertySupport.getPropertyInt(PROP_GRID_COLUMN_COUNT_HINT);
  }

  @Override
  public TriState isScrollable() {
    return m_scrollable;
  }

  @Override
  public void setScrollable(TriState scrollable) {
    if (scrollable == null) {
      scrollable = TriState.UNDEFINED;
    }
    if (scrollable.equals(m_scrollable)) {
      return;
    }
    m_scrollable = scrollable;
    if (m_scrollable.isTrue()) {
      // force weighty to be > 0
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
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    ActionUtility.disposeActions(getMenus());
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
    return new LocalGroupBoxExtension<AbstractGroupBox>(this);
  }
}
