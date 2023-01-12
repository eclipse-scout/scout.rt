/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.services.common.icon.IconLocator;
import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

public interface IHtmlTile extends ITile, IHtmlCapable {
  String PROP_CONTENT = "content";

  String getContent();

  /**
   * The given HTML content will be inserted as it is into the tile. To minimize the risk of accidentally injected HTML
   * code, make sure to encode the external content by using {@link HtmlHelper#escape(String)}, or to make it even
   * easier, just use {@link HTML} to build your content.
   * <p>
   * If you reference binary resources (dynamic images) in your HTML code you need to register them using
   * {@link #setAttachments(Collection)} or {@link #addAttachment(BinaryResource)}. This is not necessary for static
   * resources like icons which may be obtained using {@link IconLocator} respectively {@link IIconProviderService}.
   */
  void setContent(String content);

  void setAttachments(Collection<? extends BinaryResource> attachments);

  void addAttachment(BinaryResource attachment);

  void removeAttachment(BinaryResource attachment);

  Set<BinaryResource> getAttachments();

  BinaryResource getAttachment(String filename);
}
