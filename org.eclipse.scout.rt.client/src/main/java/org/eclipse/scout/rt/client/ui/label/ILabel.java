/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

  String getValue();

  void setValue(String value);

  /**
   * local images and local resources bound to the html text
   */
  Set<BinaryResource> getAttachments();

  BinaryResource getAttachment(String filename);

  void setAttachments(Collection<? extends BinaryResource> attachments);

  ILabelUIFacade getUIFacade();
}
