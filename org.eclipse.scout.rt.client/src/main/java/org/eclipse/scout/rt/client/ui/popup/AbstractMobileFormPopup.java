/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.popup;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.classid.ClassId;

@ClassId("07574e67-a74e-4155-9f0f-9b8dadaf0af5")
public abstract class AbstractMobileFormPopup<T extends IForm> extends AbstractFormPopup<T> implements IMobilePopup<T> {

}
