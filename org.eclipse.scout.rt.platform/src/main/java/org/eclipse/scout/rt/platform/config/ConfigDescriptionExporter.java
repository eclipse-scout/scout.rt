/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.config;

import static java.nio.CharBuffer.wrap;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.write;
import static java.util.Comparator.comparing;
import static org.eclipse.scout.rt.platform.BEANS.all;
import static org.eclipse.scout.rt.platform.BEANS.get;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;
import static org.eclipse.scout.rt.platform.util.StringUtility.hasText;
import static org.eclipse.scout.rt.platform.util.StringUtility.replace;
import static org.eclipse.scout.rt.platform.util.TypeCastUtility.getGenericsParameterClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigDescriptionExporter {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigDescriptionExporter.class);

  private Predicate<IConfigProperty> m_filter;

  public static void main(final String[] args) {
    final Path outFile;
    if (args.length == 1 && hasText(args[0])) {
      outFile = Paths.get(args[0]).normalize();
    }
    else {
      outFile = null;
    }
    new ConfigDescriptionExporter().exportToAdoc(outFile);
  }

  public void exportToAdoc() {
    exportToAdoc(null);
  }

  public void exportToAdoc(final Path asciiDoctorTarget) {
    final AsciiDoctorConfigWriter writer = get(AsciiDoctorConfigWriter.class);
    if (asciiDoctorTarget != null) {
      writer.withTargetFile(asciiDoctorTarget);
    }
    exportUsing(writer);
  }

  public void exportUsing(final IConfigPropertyDescriptionWriter writer) {
    LOG.info("Exporting config property descriptions using exporter '{}'.", assertNotNull(writer).getClass().getName());
    final List<IConfigProperty> allProperties = all(IConfigProperty.class);
    allProperties.sort(comparing(IConfigProperty::getKey));
    writer.accept(allProperties.stream()
        .filter(filter().orElseGet(() -> p -> true)));
  }

  public Optional<Predicate<IConfigProperty>> filter() {
    return Optional.ofNullable(m_filter);
  }

  public ConfigDescriptionExporter withFilter(final Predicate<IConfigProperty> filter) {
    m_filter = filter;
    return this;
  }

  @Bean
  @FunctionalInterface
  public interface IConfigPropertyDescriptionWriter extends Consumer<Stream<IConfigProperty>> {
  }

  public static class AsciiDoctorConfigWriter implements IConfigPropertyDescriptionWriter {

    private Path m_targetFile = Paths.get(System.getProperty("user.home"), "config_export", "config.adoc");

    @Override
    @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE") // targetFile() cannot be null
    public void accept(final Stream<IConfigProperty> configProperties) {
      final StringBuilder adoc = asciiDoctorDescFor(configProperties);
      final byte[] rawContent = StandardCharsets.UTF_8.encode(wrap(adoc)).array();
      try {
        createDirectories(targetFile().getParent());
        write(targetFile(), rawContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LOG.info("Config property description written to '{}'.", targetFile());
      }
      catch (IOException e) {
        LOG.error("Error writing to file '{}'.", targetFile(), e);
      }
    }

    protected StringBuilder asciiDoctorDescFor(final Stream<IConfigProperty> configProperties) {
      final AsciiDoctorTableBuilder builder = get(AsciiDoctorTableBuilder.class);
      builder
          .withName("Config Properties")
          .withColumn("Key", 2)
          .withColumn("Description", 4)
          .withColumn("Data Type", 1)
          .withColumn("Kind", 1);

      configProperties
          .forEach(p -> builder
              .withCell('`' + p.getKey() + '`')
              .withCell(p.description())
              .withCell(dataTypeOf(p))
              .withCell("Config Property"));

      return builder.build();
    }

    @SuppressWarnings("findbugs:RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE") // getGenericsParameterClass may return null
    protected String dataTypeOf(IConfigProperty<?> property) {
      if (property instanceof AbstractSubjectConfigProperty) {
        return "Subject name as " + String.class.getSimpleName();
      }
      if (property instanceof AbstractPortConfigProperty) {
        return Integer.class.getSimpleName() + " between 1 and 65535";
      }
      if (property instanceof AbstractBinaryConfigProperty) {
        return "Base64 encoded " + String.class.getSimpleName();
      }
      if (property instanceof AbstractClassConfigProperty) {
        final Class<?> type = getGenericsParameterClass(property.getClass(), AbstractClassConfigProperty.class, 0);
        return "Fully qualified class name. The class must have '" + type.getName() + "' in its super hierarchy.";
      }

      final Class<?> type = getGenericsParameterClass(property.getClass(), IConfigProperty.class, 0);
      if (type == null) {
        LOG.warn("Cannot calculate data type of property '{}'.", property.getClass().getName());
        return "?";
      }

      if ((Integer.class == type && property instanceof AbstractPositiveIntegerConfigProperty)
          || (Long.class == type && property instanceof AbstractPositiveLongConfigProperty)) {
        return type.getSimpleName() + " >= 0";
      }
      return type.getSimpleName();
    }

    public AsciiDoctorConfigWriter withTargetFile(final Path newTargetFile) {
      m_targetFile = assertNotNull(newTargetFile);
      return this;
    }

    public Path targetFile() {
      return m_targetFile;
    }
  }

  @Bean
  public static class AsciiDoctorTableBuilder {
    private final StringBuilder m_content;
    private final Map<String /* name */, Integer /* rel width */> m_columns;
    private String m_tableName;

    public AsciiDoctorTableBuilder() {
      m_content = new StringBuilder(1024);
      m_columns = new LinkedHashMap<>();
    }

    public AsciiDoctorTableBuilder withColumn(final String name, int width) {
      assertTrue(m_content.length() < 1); // do not allow to add columns when content has been written
      m_columns.put(name, width);
      return this;
    }

    public AsciiDoctorTableBuilder withName(final String name) {
      m_tableName = name;
      return this;
    }

    public AsciiDoctorTableBuilder withCell(final String text) {
      appendCell(text, m_content);
      return this;
    }

    protected String escape(final String s) {
      if (s == null) {
        return "";
      }
      return replace(replace(replace(s, "|", "\\|"), "'", "`"), "\n", "\n\n");
    }

    private void appendCell(final String cellText, StringBuilder collector) {
      collector.append('|').append(escape(cellText)).append("\n");
    }

    public StringBuilder build() {
      final StringBuilder result = new StringBuilder();

      result.append("[cols=\"").append(CollectionUtility.format(m_columns.values(), ",", false)).append("\", options=\"header\"]\n"); // table declaration
      if (hasText(m_tableName)) {
        result.append('.').append(m_tableName).append("\n"); // table name
      }
      result.append("|===\n");
      for (String header : m_columns.keySet()) { // headers
        appendCell(header, result);
      }

      result.append(m_content);

      result.append("|===\n");
      return result;
    }
  }
}
