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
package org.eclipse.scout.jaxws.apt.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.jaxws.provider.annotation.Clazz;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFormatter;

/**
 * Helper class in dealing with annotations which are not necessarily compiled yet.
 *
 * @since 5.1
 */
public final class AnnotationUtil {

  private AnnotationUtil() {
  }

  /**
   * Returns the {@link AnnotationValue} for the given mirror and field, or <code>null</code> if not found.
   */
  public static AnnotationValue getAnnotationValue(final AnnotationMirror annotationMirror, final String fieldName, final Elements elementUtils) {
    final Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues = elementUtils.getElementValuesWithDefaults(annotationMirror);

    for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> annotationValueEntry : annotationValues.entrySet()) {
      if (annotationValueEntry.getKey().getSimpleName().toString().equals(fieldName)) {
        return annotationValueEntry.getValue();
      }
    }
    throw new PlatformException("Field on annotation not found [annotation={}, field={}]", annotationMirror.getAnnotationType().toString(), fieldName);
  }

  public static TypeElement getTypeElement(final AnnotationMirror annotationMirror, final String fieldName, final Elements elementUtils, final Types typeUtils) {
    final AnnotationValue annotationValue = AnnotationUtil.getAnnotationValue(annotationMirror, fieldName, elementUtils);
    return (TypeElement) typeUtils.asElement((TypeMirror) annotationValue.getValue());
  }

  /**
   * Annotates the given target class with a copy of the given annotations.
   */
  public static void addAnnotations(final JCodeModel model, final JDefinedClass targetClazz, final List<AnnotationMirror> _annotations) {
    for (final AnnotationMirror _annotation : _annotations) {
      final JAnnotationUse targetAnnotation = targetClazz.annotate(model.ref(_annotation.getAnnotationType().toString()));

      // Add annotation attributes.
      for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> _annotationParamEntry : _annotation.getElementValues().entrySet()) {
        final String paramName = _annotationParamEntry.getKey().getSimpleName().toString();
        final AnnotationValue _paramValue = _annotationParamEntry.getValue();
        targetAnnotation.param(paramName, AnnotationUtil.createExpression(model, _paramValue));
      }
    }
  }

  /**
   * Creates an expression for the given annotation value. Thereby, all annotation member types as specified by JLS are
   * supported: primitive, String, Class, Enum, annotation, 1-dimensional array.
   */
  public static JExpression createExpression(final JCodeModel model, final AnnotationValue _paramValue) {
    final Object _rawParamValue = _paramValue.getValue();

    // Class member type.
    if (_rawParamValue instanceof TypeMirror) {
      final TypeMirror _clazz = (TypeMirror) _rawParamValue;
      return JExpr.dotclass(model.ref(_clazz.toString()));
    }
    // Enum member type.
    else if (_rawParamValue instanceof VariableElement) {
      final VariableElement _enum = (VariableElement) _rawParamValue;
      final JClass enumType = model.ref(_enum.asType().toString());
      final String enumValue = _enum.getSimpleName().toString();

      return new JExpressionImpl() {

        @Override
        public void generate(final JFormatter f) {
          f.t(enumType).p('.').p(enumValue);
        }
      };
    }
    // Annotation member type.
    else if (_rawParamValue instanceof AnnotationMirror) {
      final AnnotationMirror _refAnnotation = (AnnotationMirror) _rawParamValue;

      final JClass refAnnotationClazz = model.ref(_refAnnotation.getAnnotationType().toString());
      final P_AnnotationExpression refAnnotationExpression = new P_AnnotationExpression(refAnnotationClazz);
      for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> _annotationParamEntry : _refAnnotation.getElementValues().entrySet()) {
        final String paramName = _annotationParamEntry.getKey().getSimpleName().toString();
        refAnnotationExpression.param(paramName, AnnotationUtil.createExpression(model, _annotationParamEntry.getValue()));
      }
      return refAnnotationExpression;
    }
    // Array member type.
    else if (_rawParamValue instanceof List<?>) {
      final List<JExpression> expressions = new ArrayList<>();
      for (final Object _arrayElementValue : (List<?>) _rawParamValue) {
        expressions.add(AnnotationUtil.createExpression(model, (AnnotationValue) _arrayElementValue));
      }
      return new JExpressionImpl() {

        @Override
        public void generate(final JFormatter f) {
          if (expressions.size() == 1) {
            f.g(expressions.get(0));
          }
          else {
            f.p("{").g(expressions).p("}");
          }
        }
      };
    }
    else if (_rawParamValue instanceof String) {
      return JExpr.lit((String) _rawParamValue);
    }
    else if (_rawParamValue instanceof Integer) {
      return JExpr.lit((Integer) _rawParamValue);
    }
    else if (_rawParamValue instanceof Float) {
      return JExpr.lit((Float) _rawParamValue);
    }
    else if (_rawParamValue instanceof Double) {
      return JExpr.lit((Double) _rawParamValue);
    }
    else if (_rawParamValue instanceof Boolean) {
      return JExpr.lit((Boolean) _rawParamValue);
    }
    else if (_rawParamValue instanceof Character) {
      return JExpr.lit((Character) _rawParamValue);
    }
    else if (_rawParamValue instanceof Byte) {
      return JExpr.lit((Byte) _rawParamValue);
    }
    else {
      return JExpr._null();
    }
  }

  /**
   * Resolves the qualified class name of the given {@link Clazz} annotation mirror.
   *
   * @throws IllegalArgumentException
   *           is thrown if the class could not be resolved.
   */
  public static String resolveClass(final AnnotationMirror clazzAnnotationMirror, final ProcessingEnvironment env) {
    // 1. Try resolve by qualified name.
    final String qualifiedName = (String) AnnotationUtil.getAnnotationValue(clazzAnnotationMirror, "qualifiedName", env.getElementUtils()).getValue();
    if (!qualifiedName.isEmpty()) {
      return qualifiedName;
    }

    // 2. Try resolve by class.
    final TypeElement handlerClazz = AnnotationUtil.getTypeElement(clazzAnnotationMirror, "value", env.getElementUtils(), env.getTypeUtils());
    if (!Clazz.NullClazz.class.getName().equals(handlerClazz.getQualifiedName().toString())) {
      return handlerClazz.getQualifiedName().toString();
    }

    throw new IllegalArgumentException("Invalid handler class specified: missing 'value' or 'qualified name' attribute");
  }

  /**
   * Returns the {@link AnnotationMirror} of the specified fully qualified name declared on declaring type.
   */
  public static AnnotationMirror findAnnotationMirror(final String fullyQualifiedName, final Element _declaringType) {
    for (final AnnotationMirror _annotationMirror : _declaringType.getAnnotationMirrors()) {
      if (fullyQualifiedName.equals(_annotationMirror.getAnnotationType().toString())) {
        return _annotationMirror;
      }
    }

    Assertions.fail("AnnotationMirror of the type '{}' not found on '{}'", fullyQualifiedName, _declaringType.getSimpleName());
    return null;
  }

  /**
   * Represents an expression for an annotation.
   */
  private static class P_AnnotationExpression extends JExpressionImpl {

    private final JClass m_annotationClazz;
    private final Map<String, JExpression> m_paramMap;

    public P_AnnotationExpression(final JClass annotationClazz) {
      m_annotationClazz = annotationClazz;
      m_paramMap = new HashMap<>();
    }

    /**
     * Adds a parameter with the given value to this expression.
     */
    public P_AnnotationExpression param(final String paramName, final JExpression paramExpression) {
      m_paramMap.put(paramName, paramExpression);
      return this;
    }

    @Override
    public void generate(final JFormatter f) {
      f.p('@').t(m_annotationClazz);

      if (m_paramMap.isEmpty()) {
        return;
      }

      f.p('(');

      if (m_paramMap.size() == 1 && m_paramMap.containsKey("value")) {
        f.g(m_paramMap.get("value")); // short parameter form
      }
      else {
        boolean first = true;
        for (final Entry<String, JExpression> paramEntry : m_paramMap.entrySet()) {
          if (!first) {
            f.p(',');
          }

          f.p(paramEntry.getKey()).p('=').g(paramEntry.getValue());
          first = false;
        }
      }
      f.p(')');
    }
  }
}
