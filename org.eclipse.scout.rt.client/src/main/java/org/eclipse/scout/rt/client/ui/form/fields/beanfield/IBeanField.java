/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.beanfield;

import org.eclipse.scout.rt.client.ui.IAppLinkCapable;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.platform.html.AppLink;

/**
 * Bean fields may be used if a bean should be visualized. There is no default visualization, you have to provide the
 * gui component by yourself.
 * <p>
 * If you would like to display an app link, you may add a property of type {@link AppLink} to your bean.
 */
public interface IBeanField<VALUE> extends IValueField<VALUE>, IAppLinkCapable {
  IBeanFieldUIFacade getUIFacade();
}
