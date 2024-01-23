/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.events.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.management.ObjectName;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.events.ListenerListRegistry;
import org.eclipse.scout.rt.platform.events.ListenerListSnapshot;
import org.eclipse.scout.rt.platform.jmx.MBeanUtility;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@ApplicationScoped
@CreateImmediately
public class ListenerListMonitorMBean implements IListenerListMonitorMBean {

  /*
   * JMX registration
   */

  protected ObjectName jmxObjectName() {
    return MBeanUtility.toJmxName("org.eclipse.scout.rt.platform", "EventListeners");
  }

  @PostConstruct
  protected void postConstruct() {
    MBeanUtility.register(jmxObjectName(), this);
  }

  @PreDestroy
  protected void preDestroy() {
    MBeanUtility.unregister(jmxObjectName());
  }

  /*
   * MBean implementation
   */

  @Override
  public int getListenerListCount() {
    return ListenerListRegistry.globalInstance().getListenerListCount();
  }

  @Override
  public ListenerListInfo[] getListenerListInfos() {
    ListenerListSnapshot snapshot = ListenerListRegistry
        .globalInstance()
        .createSnapshot();
    //remap listener lists and merge all lists with same class name
    Map<String, Integer> listenerListCount = new TreeMap<>();
    Map<String, Map<String, List<String>>> listenerListTypes = new TreeMap<>();
    snapshot
        .getData()
        .forEach(
            (listenerList, types) -> {
              String className = listenerList.getClass().getName();
              listenerListCount.put(className, listenerListCount.getOrDefault(className, 0) + 1);
              Map<String, List<String>> mergedTypes = listenerListTypes.computeIfAbsent(className, className2 -> new TreeMap<>());
              types.forEach((type, listeners) -> listeners.forEach(listener -> mergedTypes
                  .computeIfAbsent(type, type2 -> new ArrayList<>())
                  .add(listener.getClass().getName())));
            });
    return listenerListTypes
        .entrySet()
        .stream()
        .map(e -> createListenerListInfo(e.getKey(), listenerListCount.get(e.getKey()), e.getValue()))
        .toArray(ListenerListInfo[]::new);
  }

  protected ListenerListInfo createListenerListInfo(String listenerListClassName, int listenerListInstanceCount, Map<String, List<String>> listenerTypes) {
    return new ListenerListInfo(
        listenerListClassName,
        listenerListInstanceCount,
        listenerTypes
            .entrySet()
            .stream()
            .map(e -> createListenerType(e.getKey(), e.getValue()))
            .toArray(EventType[]::new));
  }

  protected EventType createListenerType(String listenerType, List<String> listeners) {
    return new EventType(listenerType,
        listeners
            .stream()
            .collect(Collectors.groupingBy(name -> name))
            .entrySet()
            .stream()
            .map(e -> createListenerInfo(e.getKey(), e.getValue().size()))
            .toArray(ListenerInfo[]::new));
  }

  protected ListenerInfo createListenerInfo(String listenerClassName, int listenerInstanceCount) {
    return new ListenerInfo(listenerClassName, listenerInstanceCount);
  }
}
