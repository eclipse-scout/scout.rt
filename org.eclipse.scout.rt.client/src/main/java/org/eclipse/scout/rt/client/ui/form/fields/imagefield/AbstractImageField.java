/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.imagefield;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.IImageFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDragRequestChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.imagebox.ImageFieldChains.ImageFieldDropRequestChain;
import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.ImageFieldContextMenu;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.data.basic.AffineTransformSpec;
import org.eclipse.scout.rt.shared.data.basic.BoundsSpec;

@ClassId("480ea07e-9cec-4591-ba73-4bb9aa45a60d")
public abstract class AbstractImageField extends AbstractFormField implements IImageField {
  private IImageFieldUIFacade m_uiFacade;
  private final FastListenerList<ImageFieldListener> m_listenerList = new FastListenerList<>();
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

  /**
   * Configures the image id of the field. Use this method instead of {@link #getConfiguredImageUrl()} if you have an
   * image id which may be resolved by the {@link IconLocator} rather than an URL.
   * <p>
   * If multiple image properties are set, the UI will read the first property which is not null according to the
   * following order:
   * <ol>
   * <li>Image (Binary resource)
   * <li>ImageUrl
   * <li>ImageId
   * </ol>
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(300)
  protected String getConfiguredImageId() {
    return null;
  }

  /**
   * Configures the image URL of the field. Use this method instead of {@link #getConfiguredImageId()} if you have a
   * real URL pointing to the image.
   * <p>
   * If multiple image properties are set, the UI will read the first property which is not null according to the
   * following order:
   * <ol>
   * <li>Image (Binary resource)
   * <li>ImageUrl
   * <li>ImageId
   * </ol>
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(310)
  protected String getConfiguredImageUrl() {
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

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(370)
  protected boolean getConfiguredUploadEnabled() {
    return false;
  }

  @ConfigProperty(ConfigProperty.FILE_EXTENSIONS)
  @Order(380)
  protected List<String> getConfiguredFileExtensions() {
    return CollectionUtility.arrayList("png", "bmp", "jpg", "jpeg", "gif");
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
   * {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   * {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(400)
  protected int getConfiguredDropType() {
    return 0;
  }

  /**
   * <p>
   * Configures the drag support of this image field.
   * </p>
   * <p>
   * Method marked as final as currently only drop is implemented for this field.
   * </p>
   *
   * @return {@code 0} for no support or one or more of {@link IDNDSupport#TYPE_FILE_TRANSFER},
   * {@link IDNDSupport#TYPE_IMAGE_TRANSFER}, {@link IDNDSupport#TYPE_JAVA_ELEMENT_TRANSFER} or
   * {@link IDNDSupport#TYPE_TEXT_TRANSFER} (e.g. {@code TYPE_TEXT_TRANSFER | TYPE_FILE_TRANSFER}).
   */
  @ConfigProperty(ConfigProperty.DRAG_AND_DROP_TYPE)
  @Order(410)
  protected final int getConfiguredDragType() {
    return 0;
  }

  @ConfigOperation
  @Order(500)
  protected final TransferObject execDragRequest() {
    return null;
  }

  @ConfigOperation
  @Order(510)
  protected void execDropRequest(TransferObject transferObject) {
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();
    setImageTransform(new AffineTransformSpec());
    setAutoFit(getConfiguredAutoFit());
    setImageId(getConfiguredImageId());
    setImageUrl(getConfiguredImageUrl());
    setPanDelta(getConfiguredPanDelta());
    setRotateDelta(getConfiguredRotateDelta());
    setZoomDelta(getConfiguredZoomDelta());
    setDragType(getConfiguredDragType());
    setDropType(getConfiguredDropType());
    setDropMaximumSize(getConfiguredDropMaximumSize());
    setScrollBarEnabled(getConfiguredScrollBarEnabled());
    setUploadEnabled(getConfiguredUploadEnabled());
    setFileExtensions(getConfiguredFileExtensions());
  }

  @Override
  protected IFormFieldContextMenu createContextMenu(OrderedCollection<IMenu> menus) {
    return new ImageFieldContextMenu(this, menus.getOrderedList());
  }

  @Override
  public IFastListenerList<ImageFieldListener> imageFieldListeners() {
    return m_listenerList;
  }

  private void fireZoomRectangle(BoundsSpec r) {
    fireImageBoxEventInternal(new ImageFieldEvent(this, ImageFieldEvent.TYPE_ZOOM_RECTANGLE, r));
  }

  private void fireAutoFit() {
    fireImageBoxEventInternal(new ImageFieldEvent(this, ImageFieldEvent.TYPE_AUTO_FIT));
  }

  private void fireImageBoxEventInternal(ImageFieldEvent e) {
    imageFieldListeners().list().forEach(listener -> listener.imageFieldChanged(e));
  }

  @Override
  public String getImageUrl() {
    return propertySupport.getPropertyString(PROP_IMAGE_URL);
  }

  @Override
  public void setImageUrl(String imageUrl) {
    propertySupport.setProperty(PROP_IMAGE_URL, imageUrl);
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

  @Override
  public boolean isUploadEnabled() {
    return propertySupport.getPropertyBool(PROP_UPLOAD_ENABLED);
  }

  @Override
  public void setUploadEnabled(boolean uploadEnabled) {
    propertySupport.setPropertyBool(PROP_UPLOAD_ENABLED, uploadEnabled);
  }

  @Override
  public void setFileExtensions(List<String> fileExtensions) {
    setProperty(PROP_FILE_EXTENSIONS, CollectionUtility.arrayListWithoutNullElements(fileExtensions));
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getFileExtensions() {
    return CollectionUtility.arrayList((List<String>) getProperty(PROP_FILE_EXTENSIONS));
  }

  /*
   * UI accessible
   */
  @Override
  public IImageFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  protected class P_UIFacade implements IImageFieldUIFacade {

    @Override
    public void setImageTransformFromUI(AffineTransformSpec t) {
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
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
      if (!isEnabledIncludingParents() || !isVisibleIncludingParents()) {
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
    return new LocalImageFieldExtension<>(this);
  }
}
