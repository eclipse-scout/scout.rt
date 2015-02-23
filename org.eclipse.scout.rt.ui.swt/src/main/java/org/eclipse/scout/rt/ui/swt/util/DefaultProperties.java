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
package org.eclipse.scout.rt.ui.swt.util;

//package org.eclipse.scout.rt.ui.swt.util;
//
//import java.util.HashMap;
//import org.eclipse.scout.commons.logger.ScoutLoggManager;
//import org.eclipse.scout.commons.logger.IScoutLogger;
//import org.eclipse.scout.rt.ui.swt.ISwtEnvironment;
//
///** <h3>DefaultProperties</h3>
// *  String abstraction used to ensure the possibility to override properties in the environment.
// *
// * 
// * @since 1.0.0  10.04.2008
// */
//public class DefaultProperties {
//  private static final IScoutLogger LOG = ScoutLoggManager.getLogger(DefaultProperties.class);
//
//  private static DefaultProperties instance = new DefaultProperties();
//  private HashMap<String, Object> m_properties;
//
//  private DefaultProperties(){
//    int rowHeight = 21;
//    m_properties = new HashMap<String, Object>();
//    m_properties.put(ISwtEnvironment.PROP_BUTTON_HEIGHT, 23);
//    m_properties.put(ISwtEnvironment.PROP_ACTIVATION_BUTTON_HEIGHT, rowHeight);
//    m_properties.put(ISwtEnvironment.PROP_ACTIVATION_BUTTON_WIDTH, rowHeight);
//    m_properties.put(ISwtEnvironment.PROP_ACTIVATION_BUTTON_WIDH_MENU_WIDTH, 34);
//    m_properties.put(ISwtEnvironment.PROP_FIELD_LABEL_WIDTH, 130);
//    m_properties.put(ISwtEnvironment.PROP_GROUP_BOX_ROW_HEIGHT, rowHeight);
//    m_properties.put(ISwtEnvironment.PROP_GROUP_BOX_COLUMN_WIDTH, 360);
//    m_properties.put(ISwtEnvironment.PROP_GROUP_BOX_HORIZONTAL_SPACING, 4);
//    m_properties.put(ISwtEnvironment.PROP_GROUP_BOX_VERTICAL_SPACING, 4);
//    m_properties.put(ISwtEnvironment.PROP_DIALOG_MIN_HEIGHT, 170);
//    m_properties.put(ISwtEnvironment.PROP_DIALOG_MIN_WIDTH, 360);
//    m_properties.put(ISwtEnvironment.PROP_FORM_FIELD_HORIZONTAL_SPACING, 4);
//    m_properties.put(ISwtEnvironment.PROP_DISABLED_FOREGROUND_COLOR, "9e9e9e");
//  }
//
//  public static int getPropertyInt(String name){
//    return instance.getPropertyIntImpl(name);
//  }
//  private int getPropertyIntImpl(String name){
//    int value = 0;
//    Object prop = m_properties.get(name);
//    if(prop == null){
//      LOG.warn("no property defined for '"+name+"'");
//    } else if(prop instanceof Integer){
//      value =  ((Integer)prop).intValue();
//    }else{
//      LOG.warn("property '"+prop.getClass().getName()+"' not instance of Integer");
//    }
//    return value;
//  }
//
//  public static String getPropertyString(String name){
//    return instance.getPropertyStringImpl(name);
//  }
//  private String getPropertyStringImpl(String name){
//    String value = null;
//    Object prop = m_properties.get(name);
//    if(prop == null){
//      LOG.warn("no property defined for '"+name+"'");
//    } else if(prop instanceof String){
//      value =  (String)prop;
//    }else{
//      LOG.warn("property '"+prop.getClass().getName()+"' not instance of Integer");
//    }
//    return value;
//  }
//
//  public static boolean getPropertyBool(String name){
//    return instance.getPropertyBoolImpl(name);
//  }
//  private boolean getPropertyBoolImpl(String name){
//    boolean value = false;
//    Object prop = m_properties.get(name);
//    if(prop == null){
//      LOG.warn("no property defined for '"+name+"'");
//    } else if(prop instanceof Boolean){
//      value =  (Boolean)prop;
//    }else{
//      LOG.warn("property '"+prop.getClass().getName()+"' not instance of Integer");
//    }
//    return value;
//  }
//
//  public static Object getProperty(String name){
//    return instance.getPropertyImpl(name);
//  }
//  private Object getPropertyImpl(String name){
//    return m_properties.get(name);
//  }
//
//  public static void setProperty(String name, Object value) {
//    instance.setPropertyImpl(name, value);
//  }
//
//  private void setPropertyImpl(String name, Object value) {
//    m_properties.put(name, value);
//  }
//
// }
