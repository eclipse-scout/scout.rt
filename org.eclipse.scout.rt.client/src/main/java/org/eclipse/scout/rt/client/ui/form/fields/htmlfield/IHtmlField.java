/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.htmlfield;

import java.util.Collection;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

/**
 * With a html field it is possible to display custom html content.
 * <p>
 * Compared to {@link ILabelField} with {@link ILabelField#isHtmlEnabled()} = true it provides some more functionality
 * like scrolling. <br>
 * Compared to the {@link IBrowserField}, the content is embedded directly into the main html document without using an
 * iframe.
 *
 * @see IBrowserField
 * @see ILabelField
 */
public interface IHtmlField extends IValueField<String>, IAppLinkCapable, IHtmlCapable {

  String PROP_SELECTABLE = "selectable";
  String PROP_SCROLL_BAR_ENABLED = "scrollBarEnabled";
  String PROP_SCROLL_TO_END = "scrollToEnd";
  String PROP_SCROLL_TO_ANCHOR = "scrollToAnchor";

  void setSelectable(boolean selectable);

  boolean isSelectable();

  void setScrollToAnchor(String anchorName);

  String getScrollToAnchor();

  void setScrollBarEnabled(boolean scrollBarEnabled);

  boolean isScrollBarEnabled();

  void scrollToEnd();

  IHtmlFieldUIFacade getUIFacade();

  /**
   * local images and local resources bound to the html text
   */
  Set<BinaryResource> getAttachments();

  BinaryResource getAttachment(String filename);

  void setAttachments(Collection<? extends BinaryResource> attachments);

  void addAttachment(BinaryResource attachment);

  void removeAttachment(BinaryResource attachment);

  String getPlainText();
}
