/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline;

import org.eclipse.scout.rt.client.ui.action.tool.IToolButton;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.form.AbstractFormMenu;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.classid.ClassId;

/**
 * A tool button that can be used in the {@link IDesktop} to toggle a form in the tools area.
 *
 * @deprecated use AbstractFormMenu instead. Will be removed in Scout 6.1
 */
@SuppressWarnings("deprecation")
@Deprecated
@ClassId("abc1e19e-222e-4849-9f52-88aba7f4c950")
public abstract class AbstractFormToolButton<FORM extends IForm> extends AbstractFormMenu<FORM> implements IToolButton {
}
