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
package org.eclipse.scout.commons;

/**
 * A config bean property is a config.ini entry in the format
 * beanName#propertyName=value Example config.ini entries:
 * com.myapp.MyService#realm=MyRealm
 * <p>
 * realm=MyRealm2 com.myapp.MyService#realm=${realm}
 */
public class ConfigIniProperty {
  private String beanName;
  private String filter;
  private String propertyName;
  private String value;

  public ConfigIniProperty(String beanName, String filter, String propertyName, String value) {
    this.beanName = beanName;
    this.filter = filter;
    this.propertyName = propertyName;
    this.value = value;
  }

  /**
   * Examples<br>
   * <li>com.mypackage.MyService</li> <li>com.mypackage.MyFilter, com.mypackage.MyBean</li>
   */
  public String getBeanName() {
    return beanName;
  }

  /**
   * Examples<br>
   * <li>/process</li> <li>/query</li>
   */
  public String getFilter() {
    return filter;
  }

  /**
   * Examples<br>
   * <li>name</li> <li>realm</li> <li>username</li>
   */
  public String getPropertyName() {
    return propertyName;
  }

  public String getValue() {
    return value;
  }

}
