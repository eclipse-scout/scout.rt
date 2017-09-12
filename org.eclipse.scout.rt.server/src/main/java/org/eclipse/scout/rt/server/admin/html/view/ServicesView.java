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
package org.eclipse.scout.rt.server.admin.html.view;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.service.IService;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.admin.html.AbstractHtmlAction;
import org.eclipse.scout.rt.server.admin.html.AdminSession;
import org.eclipse.scout.rt.server.admin.html.widget.table.HtmlComponent;
import org.eclipse.scout.rt.server.admin.inspector.ReflectServiceInventory;
import org.eclipse.scout.rt.server.admin.inspector.ServiceInspector;
import org.eclipse.scout.rt.shared.security.UpdateServiceConfigurationPermission;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("bsiRulesDefinition:htmlInString")
public class ServicesView extends DefaultView {

  private static final Logger LOG = LoggerFactory.getLogger(ServicesView.class);

  private ServiceInspector[] m_serviceInspectors;
  private ServiceInspector m_selectedService;

  public ServicesView(AdminSession as) {
    super(as);
  }

  @Override
  public boolean isVisible() {
    return ACCESS.check(new UpdateServiceConfigurationPermission());
  }

  @Override
  public void activated() {
    // read all services
    m_serviceInspectors = null;
    try {
      ArrayList<ServiceInspector> list = new ArrayList<>();
      for (IService service : BEANS.all(IService.class)) {
        list.add(new ServiceInspector(service));
      }
      m_serviceInspectors = list.toArray(new ServiceInspector[list.size()]);
    }
    catch (Exception e) { // NOSONAR
      // nop
    }
  }

  @Override
  public void produceTitle(HtmlComponent p) {
    p.print("Services");
  }

  @Override
  public void produceBody(HtmlComponent p) {
    p.startTable(0, 5, 5);
    p.startTableRow();
    p.startTableCell();
    renderServiceTables(p);
    p.startTableCell();
    // selected call
    if (m_selectedService != null) {
      renderServiceDetail(p, m_selectedService);
    }
    p.endTableCell();
    p.endTableRow();
    p.endTable();
  }

  private void renderServiceTables(HtmlComponent p) {
    // categorize services
    SortedMap<CompositeObject, Collection<ServiceInspector>> servicesMap = new TreeMap<>();
    if (m_serviceInspectors != null) {
      for (ServiceInspector inspector : m_serviceInspectors) {
        String serviceName = inspector.getService().getClass().getSimpleName();
        String sectionName = null;
        int sectionOrder;
        try {
          if (serviceName.matches(".*ProcessService")) {
            sectionOrder = 1;
            sectionName = "Process Services";
          }
          else if (serviceName.matches(".*OutlineService")) {
            sectionOrder = 2;
            sectionName = "Outline Services";
          }
          else if (serviceName.matches(".*LookupService")) {
            sectionOrder = 3;
            sectionName = "Lookup Services";
          }
          else {
            List<Class<?>> serviceInterfaces = BeanUtility.getInterfacesHierarchy(inspector.getService().getClass(), Object.class);
            Class topInterface = (!serviceInterfaces.isEmpty() ? serviceInterfaces.get(serviceInterfaces.size() - 1) : null);
            if (topInterface != null && topInterface.getPackage() != null && topInterface.getPackage().getName().contains(".common.")) {
              sectionOrder = 4;
              sectionName = "Common Services";
            }
            else {
              sectionOrder = 5;
              sectionName = "Other Services";
            }
          }
          CompositeObject key = new CompositeObject(sectionOrder, sectionName);
          Collection<ServiceInspector> list = servicesMap.computeIfAbsent(key, k -> new ArrayList<>());
          list.add(inspector);
        }
        catch (RuntimeException e) {
          LOG.warn("Failed inspecting service {}", inspector.getService().getClass(), e);
        }
      }
    }
    // tables per section
    for (Entry<CompositeObject, Collection<ServiceInspector>> e : servicesMap.entrySet()) {
      String sectionName = (String) e.getKey().getComponent(1);
      Collection<ServiceInspector> list = e.getValue();
      renderServiceTable(p, sectionName, list);
      p.p();
    }
  }

  private void renderServiceTable(HtmlComponent p, String sectionName, Collection<ServiceInspector> serviceInspectors) {
    // sort
    SortedMap<String, ServiceInspector> sortMap = new TreeMap<>();
    for (ServiceInspector inspector : serviceInspectors) {
      String s = inspector.getService().getClass().getName();
      sortMap.put(s, inspector);
    }

    p.p(sectionName);
    p.startListBox("listBox", 1, true);
    p.listBoxOption(" ", new AbstractHtmlAction("selectService.choose") {

      @Override
      public void run() {
        // do nothing
      }
    }, false);
    for (ServiceInspector serviceInspector : sortMap.values()) {
      boolean selected = m_selectedService != null && (m_selectedService.getService() == serviceInspector.getService());
      final ServiceInspector finalServiceInspector = serviceInspector;
      p.listBoxOption(serviceInspector.getService().getClass().getName(), new AbstractHtmlAction("selectService2." + serviceInspector.getService().getClass().getName()) {

        @Override
        public void run() {
          m_selectedService = finalServiceInspector;
        }
      }, selected);
    }
    p.endListBox();
  }

  private void renderServiceDetail(HtmlComponent p, ServiceInspector service) {
    p.bold(service.getService().getClass().getSimpleName());
    p.p();
    ReflectServiceInventory inv;
    try {
      inv = service.buildInventory();
    }
    catch (RuntimeException t) {
      p.raw("<font color=red>Inventory failed: " + t + "</font>");
      return;
    }
    renderHierarchy(p, service, inv);
    renderProperties(p, service, inv);
    renderOperations(p, service, inv);
    renderStates(p, service, inv);
  }

  @SuppressWarnings("squid:S1643")
  private void renderHierarchy(HtmlComponent p, ServiceInspector service, ReflectServiceInventory inv) {
    // hierarchy
    Class serviceClass = service.getService().getClass();
    List<Class> interfaceHierarchy = new ArrayList<>();
    for (Class c : serviceClass.getInterfaces()) {
      interfaceHierarchy.addAll(BeanUtility.getInterfacesHierarchy(c, Object.class));
    }
    if (interfaceHierarchy.isEmpty()) {
      interfaceHierarchy.addAll(BeanUtility.getInterfacesHierarchy(serviceClass, Object.class));
    }
    interfaceHierarchy.add(serviceClass);
    List<Class> classHierarchy = new ArrayList<>();
    Class test = service.getService().getClass();
    while (test != null) {
      if (Object.class.isAssignableFrom(test)) {
        classHierarchy.add(0, test);
      }
      test = test.getSuperclass();
    }
    //
    p.pBold("Hierarchy");
    p.startTable(1, 0, 4);
    p.startTableRow();
    p.tableHeaderCell("Interfaces");
    p.tableHeaderCell("Classes");
    p.endTableRow();
    p.startTableRow();
    p.startTableCell();
    String prefix = "";
    for (Iterator it = interfaceHierarchy.iterator(); it.hasNext();) {
      Class c = (Class) it.next();
      p.print(prefix + c.getName());
      if (it.hasNext()) {
        p.br();
        prefix = prefix + "&nbsp;&nbsp;";
      }
    }
    p.endTableCell();
    p.startTableCell();
    prefix = "";
    for (Iterator it = classHierarchy.iterator(); it.hasNext();) {
      Class c = (Class) it.next();
      p.print(prefix + c.getName());
      if (it.hasNext()) {
        p.br();
        prefix = prefix + "&nbsp;&nbsp;";
      }
    }
    p.endTableCell();
    p.endTableRow();
    p.endTable();
  }

  private void renderProperties(HtmlComponent p, final ServiceInspector service, ReflectServiceInventory inv) {
    PropertyDescriptor[] properties = inv.getProperties();
    if (properties.length > 0) {
      p.pBold("Properties (" + properties.length + ")");
      p.startTable(1, 0, 4);
      p.startTableRow();
      p.tableHeaderCell("Property name");
      p.tableHeaderCell("Value");
      p.endTableRow();
      for (PropertyDescriptor desc : properties) {
        String propName = desc.getName();
        String propValue = "[value not available]";
        if (desc.getReadMethod() != null) {
          try {
            propValue = formatPropertyValue(desc, desc.getReadMethod().invoke(service.getService(), (Object[]) null));
          }
          catch (Exception e) { // NOSONAR
            // nop
          }
        }
        boolean editable = desc.getWriteMethod() != null;
        //
        if (editable) {
          final PropertyDescriptor finalDesc = desc;
          p.startForm(
              new AbstractHtmlAction("changeProp." + service.getService().getClass().getName() + "." + desc.getName()) {

                @Override
                public void run() {
                  String propText = getFormParameter("value", "");
                  if (propText.isEmpty()) {
                    propText = null;
                  }
                  try {
                    service.changeProperty(finalDesc, propText);
                  }
                  catch (Exception e) {
                    LOG.error("setting {}={}", finalDesc.getName(), propText, e);
                  }
                }
              });
        }
        p.startTableRow();
        p.tableCell(propName);
        p.startTableCell();
        if (editable) {
          p.formTextArea("value", getPropertyDisplayValue(propName, propValue));
          p.formSubmit("Change");
        }
        else {
          p.print(getPropertyDisplayValue(propName, propValue));
        }
        p.endTableCell();
        p.endTableRow();
        if (editable) {
          p.endForm();
        }
      }
      p.endTable();
    }
  }

  private void renderOperations(HtmlComponent p, ServiceInspector service, ReflectServiceInventory inv) {
    Method[] operations = inv.getOperations();
    if (operations.length > 0) {
      p.pBold("Operations (" + operations.length + ")");
      p.startTable(1, 0, 4);
      p.startTableRow();
      p.tableHeaderCell("Operation");
      p.tableHeaderCell("Detail");
      p.endTableRow();
      for (Method m : operations) {
        p.startTableRow();
        p.tableCell(m.getName());
        p.tableCell(createSignature(m.getReturnType()) + " " + m.getName() + "(" + createSignature(m.getParameterTypes()) + ")");
        p.endTableRow();
      }
      p.endTable();
    }
  }

  private void renderStates(HtmlComponent p, ServiceInspector service, ReflectServiceInventory inv) {
    String[] states = inv.getStates();
    if (states.length > 0) {
      p.pBold("State");
      p.raw("<pre>");
      for (String s : states) {
        p.print(s);
        p.br();
      }
      p.raw("</pre>");
    }
  }

  /**
   * @return Value to be displayed (sensitive information is not displayed).
   */
  private String getPropertyDisplayValue(String propertyName, String propertyValue) {
    if (isPropertySuppressed(propertyName)) {
      return "***";
    }
    else {
      return propertyValue;
    }
  }

  /**
   * @return true if property contains sensitive information and is therefore not shown
   */
  private boolean isPropertySuppressed(String propertyName) {
    return propertyName != null && propertyName.toLowerCase().contains("password");
  }

  private String formatPropertyValue(PropertyDescriptor p, Object value) {
    Object formattedValue = value;
    return StringUtility.valueOf(formattedValue);
  }

  private String createSignature(Class c) {
    if (c == null) {
      return "void";
    }
    else {
      return createSignature(new Class[]{c});
    }
  }

  private String createSignature(Class[] a) {
    StringBuilder sig = new StringBuilder();
    for (Class c : a) {
      if (sig.length() > 0) {
        sig.append(", ");
      }
      sig.append(c.getSimpleName());
    }
    return sig.toString();
  }

}
