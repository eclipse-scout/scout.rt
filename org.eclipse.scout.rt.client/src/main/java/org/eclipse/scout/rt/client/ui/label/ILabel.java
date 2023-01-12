/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.label;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;

public interface ILabel extends IWidget, IExtensibleObject, IHtmlCapable, IAppLinkCapable {

  String PROP_VALUE = "value";
  String PROP_SCROLLABLE = "scrollable";

  String getValue();

  void setValue(String value);

  /**
   * local images and local resources bound to the html text
   */
  Set<BinaryResource> getAttachments();

  BinaryResource getAttachment(String filename);

  void setAttachments(Collection<? extends BinaryResource> attachments);

  boolean isScrollable();

  void setScrollable(boolean scrollable);

  ILabelUIFacade getUIFacade();
}
