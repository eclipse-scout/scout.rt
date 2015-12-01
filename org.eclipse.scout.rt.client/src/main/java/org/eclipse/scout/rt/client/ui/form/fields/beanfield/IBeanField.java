/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
