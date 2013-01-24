/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.composer.attribute;

import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.shared.data.model.IDataModelAttribute;

/**
 * @deprecated use {@link IDataModelAttribute}
 */
@Deprecated
public interface IComposerAttribute extends IDataModelAttribute {

  /**
   * @deprecated the id must always by the class simple name
   *             for dynamic attributes use {@link IComposerField#getMetaDataOfAttribute(IComposerAttribute)}
   */
  @Deprecated
  String getId();

  /**
   * @deprecated the id must always by the class simple name
   *             for dynamic attributes use {@link IComposerField#getMetaDataOfAttribute(IComposerAttribute)}
   */
  @Deprecated
  void setId(String s);

}
