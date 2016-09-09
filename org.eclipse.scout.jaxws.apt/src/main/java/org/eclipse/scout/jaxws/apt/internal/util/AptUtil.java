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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import org.eclipse.scout.rt.platform.util.StringUtility;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JPackage;

/**
 * Helper class to work with APT.
 *
 * @since 5.1
 */
public final class AptUtil {

  private AptUtil() {
  }

  private static final int JAVA_DOC_LINE_LENGTH = 120;

  /**
   * Builds and persists the compilation unit.
   */
  public static void buildAndPersist(final JCodeModel jCodeModel, final Filer filer) throws IOException {
    jCodeModel.build(new CodeWriter() {

      @Override
      public OutputStream openBinary(final JPackage pkg, final String fileName) throws IOException {
        final String fqn = StringUtility.join(".", pkg.name(), fileName.substring(0, fileName.length() - ".java".length()));

        return new ByteArrayOutputStream() {

          @Override
          public void close() throws IOException {
            final String compilationUnit = interceptContent(toString(StandardCharsets.UTF_8.name()));
            try (PrintWriter writer = new PrintWriter(filer.createSourceFile(fqn).openOutputStream())) {
              writer.write(compilationUnit);
              writer.flush();
            }
          }
        };
      }

      @Override
      public void close() throws IOException {
        // NOOP
      }
    });
  }

  /**
   * Method invoked to intercept the compilation unit content prior being persisted.
   */
  private static String interceptContent(String content) {
    // replace new lines before '{'
    content = content.replaceAll("\\s*\\{", " \\{");
    // replace new lines before 'throws'
    content = content.replaceAll("\\s*throws", " throws");
    // replace new lines before 'implements'
    content = content.replaceAll("\\s*implements", " implements");
    // replace new lines before 'extends'
    content = content.replaceAll("\\s*extends", " extends");
    // change indent space two 2 spaces
    content = content.replaceAll("    ", "  ");
    // Remove redundant Callable imports (Generics bug in JCodeModel)
    content = content.replaceAll("(import\\s+java\\.util\\.concurrent\\.Callable\\;\\s+){2,}", "$1");

    return content;
  }

  public static void addJavaDoc(final JDocCommentable element, final String... javaDocLines) {
    for (int i = 0; i < javaDocLines.length; i++) {
      element.javadoc().add(StringUtility.wrapWord(javaDocLines[i], JAVA_DOC_LINE_LENGTH));
      if (i < javaDocLines.length - 1) {
        element.javadoc().add("\n");
      }
    }
  }

  public static String toSimpleName(final String qualifiedName) {
    final String[] tokens = qualifiedName.split("\\.");
    return tokens[tokens.length - 1];
  }

  public static boolean isSubtype(final TypeElement candidate, final Class<?> superType, final ProcessingEnvironment env) {
    return env.getTypeUtils().isSubtype(candidate.asType(), env.getElementUtils().getTypeElement(superType.getName()).asType());
  }
}
