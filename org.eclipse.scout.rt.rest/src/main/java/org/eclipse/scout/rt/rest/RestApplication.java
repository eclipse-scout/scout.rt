/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Application;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.StreamUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS REST application registering all provided classes, singletons and properties to the JAX-RS context.
 *
 * @see IRestApplicationClassesContributor
 * @see IRestApplicationSingletonsContributor
 * @see IRestApplicationPropertiesContributor
 */
public class RestApplication extends Application {

  private static final Logger LOG = LoggerFactory.getLogger(RestApplication.class);

  private final Set<Class<?>> m_classes;
  private final Set<Object> m_singletons;
  private final Map<String, Object> m_properties;

  public RestApplication() {
    long t0 = System.nanoTime();
    m_classes = initClasses();
    m_singletons = initSingletons();
    m_properties = initProperties();
    if (LOG.isDebugEnabled()) {
      LOG.info("{} initialized in {} ms\nClasses ({}):\n{}\nSingletons ({}):\n{}\nProperties ({}):\n{}",
          getClass().getName(), StringUtility.formatNanos(System.nanoTime() - t0),
          m_classes.size(), (m_classes.isEmpty() ? "  (none)" : m_classes.stream().map(c -> "- " + c.getName()).sorted().collect(Collectors.joining("\n"))),
          m_singletons.size(), (m_singletons.isEmpty() ? "  (none)" : m_singletons.stream().map(o -> "- " + o).sorted().collect(Collectors.joining("\n"))),
          m_properties.size(), (m_properties.isEmpty() ? "  (none)" : m_properties.keySet().stream().map(k -> "- " + k).sorted().collect(Collectors.joining("\n"))));
    }
    else {
      LOG.info("{} initialized in {} ms [{} classes, {} singletons, {} properties]",
          getClass().getName(), StringUtility.formatNanos(System.nanoTime() - t0),
          m_classes.size(), m_singletons.size(), m_properties.size());
    }
  }

  protected boolean filterClass(Class<?> clazz) {
    return true;
  }

  protected Set<Class<?>> initClasses() {
    return BEANS.all(IRestApplicationClassesContributor.class).stream()
        .flatMap(contributor -> {
          Set<Class<?>> classes = contributor.contribute();
          LOG.debug("Contributed {} classes by {}", classes.size(), contributor.getClass());
          return classes.stream();
        })
        .filter(this::filterClass)
        .collect(Collectors.toSet());
  }

  protected Set<Object> initSingletons() {
    return BEANS.all(IRestApplicationSingletonsContributor.class).stream().flatMap(contributor -> {
      Set<Object> singletons = contributor.contribute();
      LOG.debug("Contributed {} singletons by {}", singletons.size(), contributor.getClass());
      return singletons.stream();
    }).collect(Collectors.toSet());
  }

  protected Map<String, Object> initProperties() {
    return BEANS.all(IRestApplicationPropertiesContributor.class).stream().flatMap(contributor -> {
      Map<String, Object> properties = contributor.contribute();
      LOG.debug("Contributed {} properties by {}", properties.size(), contributor.getClass());
      return properties.entrySet().stream();
    }).collect(StreamUtility.toMap(Entry::getKey, Entry::getValue, StreamUtility.ignoringMerger())); // use first provided property value for multiple properties with the same key
  }

  @Override
  public Set<Class<?>> getClasses() {
    return m_classes;
  }

  @Override
  public Set<Object> getSingletons() {
    return m_singletons;
  }

  @Override
  public Map<String, Object> getProperties() {
    return m_properties;
  }

  @ApplicationScoped
  public interface IRestApplicationClassesContributor {
    Set<Class<?>> contribute();
  }

  @ApplicationScoped
  public interface IRestApplicationSingletonsContributor {
    Set<Object> contribute();
  }

  @ApplicationScoped
  public interface IRestApplicationPropertiesContributor {
    Map<String, Object> contribute();
  }
}
