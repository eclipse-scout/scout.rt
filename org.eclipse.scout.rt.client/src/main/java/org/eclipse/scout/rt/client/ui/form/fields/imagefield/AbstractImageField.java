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
package org.eclipse.scout.rt.client.ui.form.fields.imagefield;

import java.util.EventListener;
import java.util.List;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.IImageFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDropRequestChain;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.FormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.data.basic.AffineTransformSpec;
import org.eclipse.scout.rt.shared.data.basic.BoundsSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("480ea07e-9cec-4591-ba73-4bb9aa45a60d")
public abstract class AbstractImageField extends AbstractFormField implements IImageField {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractImageField.class);
  private IImageFieldUIFacade m_uiFacade;
  private final EventListenerList m_listenerList = new EventListenerList();
  private IContextMenu m_contextMenu;
  private double m_zoomDelta;
  private double m_panDelta;
  private double m_rotateDelta;

  public AbstractImageField() {
    this(true);
  }

  public AbstractImageField(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  protected int getConfiguredVerticalAlignment() {
    return 0;
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 0;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(300)
  protected String getConfiguredImageId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(320)
  protected boolean getConfiguredAutoFit() {
    return false;
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(330)
  protected double getConfiguredZoomDelta() {
    return 1.25;
  }

  @Override
  @Order(190)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredFocusable() {
    return false;
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(340)
  protected double getConfiguredPanDelta() {
    return 10;
  }

  /**
   * in degrees 0..360
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(350)
  protected double getConfiguredRotateDelta() {
    return 10;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(360)
  protected boolean getConfiguredScrollBarEnabled() {
    return false;
  }

  /**
   * Configures the maximum size for a drop request (in bytes).
   * <p>
   * Subclasses can override this method. Default is defined by {@link IDNDSupport#DEFAULT_DROP_MAXIMUM_SIZE}.
   *
   * @return maximum size in bytes.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(190)
  protected long getConfiguredDropMaximumSize() {
    return DEFAULT_DROP_MAXIMUM_SIZE;
  }

  /**
   * Configures the drop support of this image field.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drop support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(400)
  protected int getConfiguredDropType() {
    return 0;
  }

  /**
   * Configures the drag support of this image field.
   * <p>
   * Subclasses can override this method. Default is {@code 0} (no drag support).
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   *         {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   *         {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(410)
  protected int getConfiguredDragType() {
    return 0;
  }

  @ConfigOperation
  @Order(500)
  protected TransferObject execDragRequest() {
    return null;
  }

  @ConfigOperation
  @Order(510)
  protected void execDropRequest(TransferObject transferObject) {
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> filtered = ConfigurationUtility.filterClasses(dca, IMenu.class);
    return ConfigurationUtility.removeReplacedClasses(filtered);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    setImageTransform(new AffineTransformSpec());
    setAutoFit(getConfiguredAutoFit());
    setImageId(getConfiguredImageId());
    setPanDelta(getConfiguredPanDelta());
    setRotateDelta(getConfiguredRotateDelta());
    setZoomDelta(getConfiguredZoomDelta());
    setDragType(getConfiguredDragType());
    setDropType(getConfiguredDropType());
    setDropMaximumSize(getConfiguredDropMaximumSize());
    setScrollBarEnabled(getConfiguredScrollBarEnabled());

    // menus
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      menus.addOrdered(ConfigurationUtility.newInnerInstance(this, menuClazz));
    }
    menus.addAllOrdered(contributedMenus);
    injectMenusInternal(menus);
    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    m_contextMenu = new FormFieldContextMenu<IImageField>(this, menus.getOrderedList());
    m_contextMenu.setContainerInternal(this);
  }

  @Override
  protected void initFieldInternal() {
    super.initFieldInternal();
    // init actions
    ActionUtility.initActions(getMenus());
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

  /*
   * Runtime
   */

  /**
   * model observer
   */

  @Override
  public void addImageFieldListener(ImageFieldListener listener) {
    m_listenerList.add(ImageFieldListener.class, listener);
  }

  @Override
  public void removeImageFieldListener(ImageFieldListener listener) {
    m_listenerList.remove(ImageFieldListener.class, listener);
  }

  private void fireZoomRectangle(BoundsSpec r) {
    fireImageBoxEventInternal(new ImageFieldEvent(this, ImageFieldEvent.TYPE_ZOOM_RECTANGLE, r));
  }

  private void fireAutoFit() {
    fireImageBoxEventInternal(new ImageFieldEvent(this, ImageFieldEvent.TYPE_AUTO_FIT));
  }

  private void fireImageBoxEventInternal(ImageFieldEvent e) {
    EventListener[] a = m_listenerList.getListeners(ImageFieldListener.class);
    if (a != null) {
      for (int i = 0; i < a.length; i++) {
        ((ImageFieldListener) a[i]).imageFieldChanged(e);
      }
    }
  }

  @Override
  public Object getImage() {
    return propertySupport.getProperty(PROP_IMAGE);
  }

  @Override
  public void setImage(Object imgObj) {
    propertySupport.setProperty(PROP_IMAGE, imgObj);
  }

  @Override
  public String getImageId() {
    return propertySupport.getPropertyString(PROP_IMAGE_ID);
  }

  @Override
  public void setImageId(String imageId) {
    propertySupport.setPropertyString(PROP_IMAGE_ID, imageId);
  }

  @Override
  public IContextMenu getContextMenu() {
    return m_contextMenu;
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  @Override
  public double getZoomDeltaValue() {
    return m_zoomDelta;
  }

  @Override
  public void setZoomDelta(double d) {
    m_zoomDelta = d;
  }

  @Override
  public double getPanDelta() {
    return m_panDelta;
  }

  @Override
  public void setPanDelta(double d) {
    m_panDelta = d;
  }

  @Override
  public double getRotateDelta() {
    return m_rotateDelta;
  }

  @Override
  public void setRotateDelta(double deg) {
    m_rotateDelta = deg;
  }

  @Override
  public void setRotateDeltaInRadians(double rad) {
    setRotateDelta(Math.toDegrees(rad));
  }

  @Override
  public AffineTransformSpec getImageTransform() {
    return new AffineTransformSpec((AffineTransformSpec) propertySupport.getProperty(PROP_IMAGE_TRANSFORM));
  }

  @Override
  public void setImageTransform(AffineTransformSpec t) {
    propertySupport.setProperty(PROP_IMAGE_TRANSFORM, new AffineTransformSpec(t));
  }

  @Override
  public BoundsSpec getAnalysisRectangle() {
    return (BoundsSpec) propertySupport.getProperty(PROP_ANALYSIS_RECTANGLE);
  }

  @Override
  public void setAnalysisRectangle(BoundsSpec rect) {
    propertySupport.setProperty(PROP_ANALYSIS_RECTANGLE, rect);
  }

  @Override
  public void setAnalysisRectangle(int x, int y, int w, int h) {
    setAnalysisRectangle(new BoundsSpec(x, y, w, h));
  }

  @Override
  public boolean isAutoFit() {
    return propertySupport.getPropertyBool(PROP_AUTO_FIT);
  }

  @Override
  public void setAutoFit(boolean b) {
    propertySupport.setPropertyBool(PROP_AUTO_FIT, b);
  }

  @Override
  public boolean isScrollBarEnabled() {
    return propertySupport.getPropertyBool(PROP_SCROLL_BAR_ENABLED);
  }

  @Override
  public void setScrollBarEnabled(boolean b) {
    propertySupport.setPropertyBool(PROP_SCROLL_BAR_ENABLED, b);
  }

  @Override
  public void setDragType(int dragType) {
    propertySupport.setPropertyInt(PROP_DRAG_TYPE, dragType);
  }

  @Override
  public int getDragType() {
    return propertySupport.getPropertyInt(PROP_DRAG_TYPE);
  }

  @Override
  public void setDropType(int dropType) {
    propertySupport.setPropertyInt(PROP_DROP_TYPE, dropType);
  }

  @Override
  public int getDropType() {
    return propertySupport.getPropertyInt(PROP_DROP_TYPE);
  }

  @Override
  public void setDropMaximumSize(long dropMaximumSize) {
    propertySupport.setPropertyLong(PROP_DROP_MAXIMUM_SIZE, dropMaximumSize);
  }

  @Override
  public long getDropMaximumSize() {
    return propertySupport.getPropertyInt(PROP_DROP_MAXIMUM_SIZE);
  }

  @Override
  public byte[] getByteArrayValue() {
    Object value = getImage();
    byte[] b = null;
    if (value instanceof byte[]) {
      b = (byte[]) value;
    }
    return b;
  }

  @Override
  public void doAutoFit() {
    fireAutoFit();
  }

  @Override
  public void doZoomRectangle(int x, int y, int w, int h) {
    fireZoomRectangle(new BoundsSpec(x, y, w, h));
  }

  @Override
  public void doPan(double dx, double dy) {
    AffineTransformSpec t = getImageTransform();
    t.dx = dx;
    t.dy = dy;
    setImageTransform(t);
  }

  @Override
  public void doRelativePan(double dx, double dy) {
    AffineTransformSpec t = getImageTransform();
    t.dx = t.dx + dx;
    t.dy = t.dy + dy;
    setImageTransform(t);
  }

  @Override
  public void doZoom(double fx, double fy) {
    AffineTransformSpec t = getImageTransform();
    t.sx = fx;
    t.sy = fy;
    setImageTransform(t);
  }

  @Override
  public void doRelativeZoom(double fx, double fy) {
    AffineTransformSpec t = getImageTransform();
    t.sx = t.sx * fx;
    t.sy = t.sy * fy;
    setImageTransform(t);
  }

  @Override
  public void doRotate(double angle) {
    AffineTransformSpec t = getImageTransform();
    t.angle = angle;
    setImageTransform(t);
  }

  @Override
  public void doRelativeRotate(double angleInDegrees) {
    AffineTransformSpec t = getImageTransform();
    t.angle = t.angle + Math.toRadians(angleInDegrees);
    setImageTransform(t);
  }

  /*
   * UI accessible
   */
  @Override
  public IImageFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    for (IMenu menu : getMenus()) {
      try {
        menu.dispose();
      }
      catch (RuntimeException e) {
        LOG.warn("Exception while disposing menu.", e);
      }
    }
  }

  private class P_UIFacade implements IImageFieldUIFacade {

    @Override
    public void setImageTransformFromUI(AffineTransformSpec t) {
      if (!isEnabled() || !isVisible()) {
        return;
      }
      setImageTransform(t);
    }

    @Override
    public TransferObject fireDragRequestFromUI() {
      TransferObject t = null;
      t = interceptDragRequest();
      return t;
    }

    @Override
    public void fireDropActionFromUi(TransferObject scoutTransferable) {
      if (!isEnabled() || !isVisible()) {
        //can not drop anything into field if its disabled.
        return;
      }
      interceptDropRequest(scoutTransferable);
    }

  }// end private class

  protected final TransferObject interceptDragRequest() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ImageFieldDragRequestChain chain = new ImageFieldDragRequestChain(extensions);
    return chain.execDragRequest();
  }

  protected final void interceptDropRequest(TransferObject transferObject) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ImageFieldDropRequestChain chain = new ImageFieldDropRequestChain(extensions);
    chain.execDropRequest(transferObject);
  }

  protected static class LocalImageFieldExtension<OWNER extends AbstractImageField> extends LocalFormFieldExtension<OWNER> implements IImageFieldExtension<OWNER> {

    public LocalImageFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public TransferObject execDragRequest(ImageFieldDragRequestChain chain) {
      return getOwner().execDragRequest();
    }

    @Override
    public void execDropRequest(ImageFieldDropRequestChain chain, TransferObject transferObject) {
      getOwner().execDropRequest(transferObject);
    }
  }

  @Override
  protected IImageFieldExtension<? extends AbstractImageField> createLocalExtension() {
    return new LocalImageFieldExtension<AbstractImageField>(this);
  }

}
