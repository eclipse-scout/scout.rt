/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.extension.ui.form.fields.filechooserbutton;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.filechooserbutton.AbstractFileChooserButton;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

public abstract class AbstractFileChooserButtonExtension<OWNER extends AbstractFileChooserButton> extends AbstractValueFieldExtension<BinaryResource, OWNER> implements IFileChooserButtonExtension<OWNER> {

  public AbstractFileChooserButtonExtension(OWNER owner) {
    super(owner);
  }
}
