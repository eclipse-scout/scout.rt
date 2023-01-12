/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.transformation;

import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.service.IService;

/**
 * @since 3.9.0
 */
public interface IDeviceTransformationService extends IService {

  void install(IDesktop desktop);

  void uninstall();

  IDeviceTransformer getDeviceTransformer();

  void excludeTransformation(IDeviceTransformation transformation);

  void removeTransformationExclusion(IDeviceTransformation transformation);

  boolean isTransformationExcluded(IDeviceTransformation transformation);

  void excludeForm(IForm form);

  void excludeFormTransformation(IForm form, IDeviceTransformation transformation);

  void excludeField(IFormField formField);

  void excludeFieldTransformation(IFormField formField, IDeviceTransformation transformation);

  void enableTransformation(IDeviceTransformation transformation);

  void disableTransformation(IDeviceTransformation transformation);

  boolean isTransformationEnabled(IDeviceTransformation transformation);

  boolean isTransformationEnabled(IDeviceTransformation transformation, IForm form);

  boolean isTransformationEnabled(IDeviceTransformation transformation, IFormField field);
}
