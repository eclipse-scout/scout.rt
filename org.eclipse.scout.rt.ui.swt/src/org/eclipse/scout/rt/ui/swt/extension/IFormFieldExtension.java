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
package org.eclipse.scout.rt.ui.swt.extension;

import org.eclipse.scout.rt.ui.swt.form.fields.ISwtScoutFormField;

/**
 * <h3>IFormFieldExtension</h3> The extension bean defining a form field
 * extension. A form field extension can have direct link to an ui class
 * (Class<? extends {@link ISwtScoutFormField}>) or a form field factory
 * (Class<? extends {@link IFormFieldFactory}>). A {@link IFormFieldFactory} is
 * used to dynamically decide about the UI implementation of a form field.</br>
 * Every extension has an attribute called scope. The scope might have one of
 * the following values:
 * <ul>
 * <li>
 *      {@link IFormFieldExtension#SCOPE_DEFAULT} to indicate this extension to be the default implementation. Is usually
 * used of external component providers (e.g. a PhotoShopField).</li>
 * <li>
 *      {@link IFormFieldExtension#SCOPE_GLOBAL} to indicate this extension to have a global scope (whole eclipse).
 * Global defined extensions overwrite the default implementation.</li>
 * <li>
 *      {@link IFormFieldExtension#SCOPE_LOCAL} to indicate this extension to have local scope. Local defined extensions
 * overwrite the global defined implementation. Local extensions should only be declared in application specific plugins
 * (e.g. com.bsiag.crm.ui.swt).</li>
 * </ul>
 * 
 * @since 1.0.8 17.04.2009
 */
public interface IFormFieldExtension {
  int SCOPE_DEFAULT = 0;
  int SCOPE_GLOBAL = 1;
  int SCOPE_LOCAL = 2;

  /**
   * @return a human readable name of the extension.
   */
  String getName();

  /**
   * <ul>
   * <li>
   *      {@link IFormFieldExtension#SCOPE_DEFAULT} to indicate this extension to be the default implementation.</li>
   * <li>
   *      {@link IFormFieldExtension#SCOPE_GLOBAL} to indicate this extension to have a global scope (whole eclipse).
   * Global defined extensions overwrite the default implementation.</li>
   * <li>
   *      {@link IFormFieldExtension#SCOPE_LOCAL} to indicate this extension to have local scope. Local defined
   * extensions overwrite the global defined implementation. Local extensions should only be declared in application
   * specific plugins (e.g. com.bsiag.crm.ui.swt).</li>
   * </ul>
   * 
   * @return one of {@link IFormFieldExtension#SCOPE_DEFAULT}, {@link IFormFieldExtension#SCOPE_GLOBAL},
   *         {@link IFormFieldExtension#SCOPE_LOCAL}
   */
  int getScope();

  /**
   * users of this interface should type check
   * 
   * @return either a marker interface class name or a class name instanceof
   *         IFormField
   */
  String getModelClassName();

  /**
   * users of this interface should type check
   * 
   * @return a class name instanceof ISwtScoutFormField
   */
  String getUiClassName();

  /**
   * users of this interface should type check
   * 
   * @return a class name instanceof IFormFieldFactory
   */
  String getFactoryClassName();

  boolean isActive();

  /**
   * The id of the contibutor bundle.
   * 
   * @return
   */
  String getContibuterBundleId();

}
