/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.serialization;

import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DefaultSerializerBlacklistAppendProperty;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DefaultSerializerBlacklistReplaceProperty;

/**
 * Default blacklist used in {@link SerializationUtility} in particual in {@link BasicObjectSerializer}
 * <p>
 * Contains classes used in some <em>known</em> exploits from https://github.com/frohoff/ysoserial
 * <p>
 * However it is much better to use a whitelist approach when doing Object deserialization using an
 * {@link ObjectInputStream}.
 * <p>
 * Use {@link SerializationUtility} whenever possible.
 *
 * @since 11.0
 */
@ApplicationScoped
public class DefaultSerializerBlacklist implements Predicate<String> {
  /**
   * This set was created in the bsi-software.com sandbox lab playground/imo.cvedeserializer
   */
  public static final Set<String> PROBLEMATIC_CLASSES = new HashSet<>(Arrays.asList("bsh.BSHAllocationExpression", "bsh.BSHAmbiguousName", "bsh.BSHArguments", "bsh.BSHArrayDimensions", "bsh.BSHArrayInitializer", "bsh.BSHBlock",
      "bsh.BSHFormalParameter", "bsh.BSHFormalParameters", "bsh.BSHLiteral", "bsh.BSHMethodDeclaration", "bsh.BSHPrimaryExpression", "bsh.BSHPrimarySuffix", "bsh.BSHReturnStatement", "bsh.BSHType", "bsh.BshMethod", "bsh.NameSpace",
      "bsh.Primitive", "bsh.Token", "bsh.Variable", "bsh.XThis", "bsh.XThis$Handler", "clojure.core$comp$fn__4727", "clojure.core$constantly$fn__4614", "clojure.inspector.proxy$javax.swing.table.AbstractTableModel$ff19274a",
      "clojure.lang.PersistentArrayMap", "clojure.main$eval_opt", "com.mchange.v2.c3p0.PoolBackedDataSource", "com.mchange.v2.naming.ReferenceIndirector$ReferenceSerialized",
      "com.sun.corba.se.spi.orbutil.proxy.CompositeInvocationHandlerImpl", "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl", "com.sun.proxy.$Proxy0", "com.sun.proxy.$Proxy2", "com.sun.proxy.$Proxy3", "com.sun.proxy.$Proxy4",
      "com.sun.proxy.$Proxy5", "com.sun.proxy.$Proxy6", "com.sun.proxy.$Proxy7", "com.sun.rowset.JdbcRowSetImpl", "com.sun.syndication.feed.impl.CloneableBean", "com.sun.syndication.feed.impl.EqualsBean",
      "com.sun.syndication.feed.impl.ObjectBean", "com.sun.syndication.feed.impl.ToStringBean", "com.vaadin.data.util.NestedMethodProperty", "com.vaadin.data.util.PropertysetItem", "java.rmi.server.RemoteObjectInvocationHandler",
      "javax.management.BadAttributeValueExpException", "javax.management.openmbean.CompositeType", "javax.management.openmbean.SimpleType", "javax.management.openmbean.TabularDataSupport", "javax.management.openmbean.TabularType",
      "javax.naming.Reference", "javax.swing.event.EventListenerList", "net.sf.json.JSONObject", "org.apache.commons.beanutils.BeanComparator", "org.apache.commons.collections.comparators.ComparableComparator",
      "org.apache.commons.collections.functors.ChainedTransformer", "org.apache.commons.collections.functors.ConstantTransformer", "org.apache.commons.collections.functors.InstantiateTransformer",
      "org.apache.commons.collections.functors.InvokerTransformer", "org.apache.commons.collections.keyvalue.TiedMapEntry", "org.apache.commons.collections.map.LazyMap", "org.apache.commons.collections4.comparators.ComparableComparator",
      "org.apache.commons.collections4.comparators.TransformingComparator", "org.apache.commons.collections4.functors.ChainedTransformer", "org.apache.commons.collections4.functors.ConstantTransformer",
      "org.apache.commons.collections4.functors.InstantiateTransformer", "org.apache.commons.collections4.functors.InvokerTransformer", "org.apache.commons.fileupload.disk.DiskFileItem", "org.apache.el.ValueExpressionImpl",
      "org.apache.myfaces.view.facelets.el.ValueExpressionMethodExpression", "org.apache.wicket.util.upload.DiskFileItem", "org.codehaus.groovy.runtime.ConvertedClosure", "org.codehaus.groovy.runtime.MethodClosure",
      "org.hibernate.engine.spi.TypedValue", "org.hibernate.property.BasicPropertyAccessor$BasicGetter", "org.hibernate.tuple.component.PojoComponentTuplizer", "org.hibernate.type.ComponentType",
      "org.jboss.interceptor.builder.InterceptionModelImpl", "org.jboss.interceptor.builder.MethodReference$MethodHolderSerializationProxy", "org.jboss.interceptor.proxy.DefaultInvocationContextFactory",
      "org.jboss.interceptor.proxy.InterceptorMethodHandler", "org.jboss.interceptor.reader.ClassMetadataInterceptorReference", "org.jboss.interceptor.reader.DefaultMethodMetadata$DefaultMethodMetadataSerializationProxy",
      "org.jboss.interceptor.reader.ReflectiveClassMetadata", "org.jboss.interceptor.reader.SimpleInterceptorMetadata", "org.jboss.interceptor.spi.model.InterceptionType", "org.jboss.weld.interceptor.builder.InterceptionModelImpl",
      "org.jboss.weld.interceptor.builder.MethodReference", "org.jboss.weld.interceptor.proxy.DefaultInvocationContextFactory", "org.jboss.weld.interceptor.proxy.InterceptorMethodHandler",
      "org.jboss.weld.interceptor.reader.ClassMetadataInterceptorReference", "org.jboss.weld.interceptor.reader.DefaultMethodMetadata$DefaultMethodMetadataSerializationProxy", "org.jboss.weld.interceptor.reader.ReflectiveClassMetadata",
      "org.jboss.weld.interceptor.reader.SimpleInterceptorMetadata", "org.jboss.weld.interceptor.spi.model.InterceptionType", "org.mozilla.javascript.BaseFunction", "org.mozilla.javascript.ClassCache",
      "org.mozilla.javascript.IdFunctionObject", "org.mozilla.javascript.LazilyLoadedCtor", "org.mozilla.javascript.MemberBox", "org.mozilla.javascript.NativeArray", "org.mozilla.javascript.NativeBoolean",
      "org.mozilla.javascript.NativeCall", "org.mozilla.javascript.NativeDate", "org.mozilla.javascript.NativeError", "org.mozilla.javascript.NativeGenerator", "org.mozilla.javascript.NativeGlobal", "org.mozilla.javascript.NativeIterator",
      "org.mozilla.javascript.NativeIterator$StopIteration", "org.mozilla.javascript.NativeJavaArray", "org.mozilla.javascript.NativeJavaMethod", "org.mozilla.javascript.NativeJavaObject", "org.mozilla.javascript.NativeMath",
      "org.mozilla.javascript.NativeNumber", "org.mozilla.javascript.NativeObject", "org.mozilla.javascript.NativeScript", "org.mozilla.javascript.NativeString", "org.mozilla.javascript.NativeWith",
      "org.mozilla.javascript.ScriptableObject$GetterSlot", "org.mozilla.javascript.ScriptableObject$Slot", "org.mozilla.javascript.Undefined", "org.mozilla.javascript.UniqueTag", "org.mozilla.javascript.tools.shell.Environment",
      "org.python.core.CodeFlag", "org.python.core.CompilerFlags", "org.python.core.PyBytecode", "org.python.core.PyFunction", "org.python.core.PySequence$1", "org.python.core.PyString", "org.python.core.PyStringMap",
      "org.python.core.PyType$TypeResolver", "org.springframework.aop.framework.AdvisedSupport", "org.springframework.aop.framework.DefaultAdvisorChainFactory", "org.springframework.aop.framework.JdkDynamicAopProxy",
      "org.springframework.aop.target.SingletonTargetSource", "org.springframework.beans.factory.support.AutowireUtils$ObjectFactoryDelegatingInvocationHandler", "org.springframework.core.$Proxy8",
      "org.springframework.core.SerializableTypeWrapper$MethodInvokeTypeProvider", "sun.reflect.annotation.AnnotationInvocationHandler", "sun.rmi.server.ActivationGroupImpl"));

  protected Predicate<String> m_veryDarkPolicy = createVeryDarkListPolicy(PROBLEMATIC_CLASSES);
  protected Predicate<String> m_darkPolicy = createDarkListPolicy(PROBLEMATIC_CLASSES);
  protected Predicate<String> m_customPolicy = c -> false;
  protected final Map<String, Boolean> m_cache = new ConcurrentHashMap<>();

  @PostConstruct
  protected void postConstruct() {
    reset();
  }

  public void reset() {
    m_cache.clear();
    m_veryDarkPolicy = createVeryDarkListPolicy(PROBLEMATIC_CLASSES);
    m_darkPolicy = createDarkListPolicy(PROBLEMATIC_CLASSES);
    String customReplace = CONFIG.getPropertyValue(DefaultSerializerBlacklistReplaceProperty.class);
    String customAppend = CONFIG.getPropertyValue(DefaultSerializerBlacklistAppendProperty.class);
    if (customReplace != null) {
      m_veryDarkPolicy = c -> false;
      m_darkPolicy = c -> false;
    }
    m_customPolicy = SerializationUtility.createBlacklistPolicy(customReplace, customAppend);
  }

  public boolean isVeryDark(String classname) {
    return m_veryDarkPolicy.test(classname);
  }

  public boolean isDark(String classname) {
    return m_darkPolicy.test(classname);
  }

  public boolean isCustomBlocked(String classname) {
    return m_customPolicy.test(classname);
  }

  @Override
  public boolean test(String classname) {
    Boolean b = m_cache.get(classname);
    if (b == null) {
      b = isVeryDark(classname) || isDark(classname) || isCustomBlocked(classname);
      m_cache.put(classname, b);
    }
    return b.booleanValue();
  }

  protected static Predicate<String> createVeryDarkListPolicy(Set<String> classNames) {
    List<String> prefixes = classNames
        .stream()
        .map(c -> {
          int i = c.indexOf('$');
          if (i > 0) {
            c = c.substring(0, i);
          }
          return c;
        })
        .distinct()
        .collect(Collectors.toList());
    if (prefixes.isEmpty()) {
      return c -> false;
    }
    String regex = prefixes
        .stream()
        .map(Pattern::quote)
        .collect(Collectors.joining("|", "(", ").*"));
    Pattern p = Pattern.compile(regex);
    return c -> p.matcher(c).matches();
  }

  protected static Predicate<String> createDarkListPolicy(Set<String> classNames) {
    List<String> prefixes = classNames
        .stream()
        .map(c -> {
          int i = c.indexOf('$');
          if (i > 0) {
            c = c.substring(0, i);
          }
          i = c.lastIndexOf('.');
          if (i > 0) {
            c = c.substring(0, i);
          }
          return c;
        })
        .distinct()
        .collect(Collectors.toList());
    if (prefixes.isEmpty()) {
      return c -> false;
    }
    String regex = prefixes
        .stream()
        .map(Pattern::quote)
        .collect(Collectors.joining("|", "(", ")\\..*"));
    Pattern p = Pattern.compile(regex);
    return c -> p.matcher(c).matches();
  }
}
