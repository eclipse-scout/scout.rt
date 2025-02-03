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

import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.util.event.IFastListenerList;
import org.eclipse.scout.rt.shared.data.basic.AffineTransformSpec;
import org.eclipse.scout.rt.shared.data.basic.BoundsSpec;

/**
 * You may add IKeyStrokes to the image field<br>
 * A default set of key strokes could look as follows
 * <ul>
 * <li>The AutoFitKeyStroke reacts on 'a' and calls doAutoFit()
 * <li>The PanUpKeyStroke reacts on 'up' and calls doRelativePan(0,-getPanDelta())
 * <li>The PanDownKeyStroke reacts on 'down' and calls doRelativePan(0,getPanDelta())
 * <li>The PanLeftKeyStroke reacts on 'left' and calls doRelativePan(-getPanDelta(),0)
 * <li>The PanRightKeyStroke reacts on 'right' and calls doRelativePan(getPanDelta(),0)
 * <li>The RotateLeftKeyStroke reacts on 'ctrl-left' and calls doRelativeRotate(-getRotateDelta())
 * <li>The RotateRightKeyStroke reacts on 'ctrl-right' and calls doRelativeRotate(getRotateDelta())
 * <li>The ZoomInKeyStroke reacts on '+' and calls doRelativeZoom(getZoomDeltaValue(),getZoomDeltaValue())
 * <li>The ZoomOutKeyStroke reacts on '-' and calls doRelativeZoom(1.0/getZoomDeltaValue(),1.0/getZoomDeltaValue())
 * </ul>
 */
public interface IImageField extends IFormField, IDNDSupport {
  String PROP_IMAGE_URL = "imageUrl";
  String PROP_IMAGE_ID = "imageId";
  String PROP_IMAGE = "image";
  String PROP_IMAGE_TRANSFORM = "imageTransform";
  String PROP_ANALYSIS_RECTANGLE = "analysisRectangle";
  String PROP_AUTO_FIT = "autoFit";
  String PROP_SCROLL_BAR_ENABLED = "scrollBarEnabled";
  String PROP_UPLOAD_ENABLED = "uploadEnabled";
  String PROP_FILE_EXTENSIONS = "fileExtensions";

  String getImageUrl();

  void setImageUrl(String imageUrl);

  String getImageId();

  void setImageId(String imageId);

  Object getImage();

  void setImage(Object imgObj);

  byte[] getByteArrayValue();

  boolean isAutoFit();

  void setAutoFit(boolean b);

  BoundsSpec getAnalysisRectangle();

  void setAnalysisRectangle(int x, int y, int width, int heigth);

  void setAnalysisRectangle(BoundsSpec rect);

  AffineTransformSpec getImageTransform();

  void setImageTransform(AffineTransformSpec t);

  IFastListenerList<ImageFieldListener> imageFieldListeners();

  default void addImageFieldListener(ImageFieldListener listener) {
    imageFieldListeners().add(listener);
  }

  default void removeImageFieldListener(ImageFieldListener listener) {
    imageFieldListeners().remove(listener);
  }

  double getZoomDeltaValue();

  void setZoomDelta(double d);

  double getPanDelta();

  void setPanDelta(double d);

  /**
   * degrees
   */
  double getRotateDelta();

  /**
   * degrees
   */
  void setRotateDelta(double delta);

  void setRotateDeltaInRadians(double rad);

  void doAutoFit();

  void doPan(double dx, double dy);

  void doRelativePan(double dx, double dy);

  void doZoom(double fx, double fy);

  void doZoomRectangle(int x, int y, int w, int h);

  void doRelativeZoom(double fx, double fy);

  void doRotate(double angle);

  void doRelativeRotate(double angle);

  IImageFieldUIFacade getUIFacade();

  boolean isScrollBarEnabled();

  void setScrollBarEnabled(boolean scrollBarEnabled);

  boolean isUploadEnabled();

  void setUploadEnabled(boolean uploadEnabled);

  List<String> getFileExtensions();

  void setFileExtensions(List<String> fileExtensions);
}
