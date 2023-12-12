/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.config;

import static java.nio.CharBuffer.wrap;
import static java.nio.file.Files.*;
import static java.util.Comparator.comparing;
import static org.eclipse.scout.rt.platform.BEANS.*;
import static org.eclipse.scout.rt.platform.util.Assertions.*;
import static org.eclipse.scout.rt.platform.util.StringUtility.*;
import static org.eclipse.scout.rt.platform.util.TypeCastUtility.getGenericsParameterClass;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigDescriptionExporter {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigDescriptionExporter.class);

  private Predicate<IConfigProperty<?>> m_filter;

  public static void main(final String[] args) {
    final Path outFile;
    if (args.length == 1 && hasText(args[0])) {
      outFile = Paths.get(args[0]).normalize();
    }
    else {
      outFile = null;
    }
    new ConfigDescriptionExporter().exportToAdoc(outFile);
    Platform.get().stop();
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
    //noinspection rawtypes
    List<IConfigProperty> rawProps = all(IConfigProperty.class);
    final List<IConfigProperty<?>> allProperties = new ArrayList<>(rawProps.size());
    for (IConfigProperty<?> prop : rawProps) {
      allProperties.add(prop);
    }
    allProperties.sort(comparing(IConfigProperty::getKey));
    writer.accept(allProperties.stream()
        .filter(filter().orElseGet(() -> p -> true)));
  }

  public Optional<Predicate<IConfigProperty<?>>> filter() {
    return Optional.ofNullable(m_filter);
  }

  public ConfigDescriptionExporter withFilter(final Predicate<IConfigProperty<?>> filter) {
    m_filter = filter;
    return this;
  }

  @Bean
  @FunctionalInterface
  public interface IConfigPropertyDescriptionWriter extends Consumer<Stream<IConfigProperty<?>>> {
  }

  public static class AsciiDoctorConfigWriter implements IConfigPropertyDescriptionWriter {

    private Path m_targetFile = Paths.get(System.getProperty("user.home"), "config_export", "config.adoc");

    @Override
    @SuppressWarnings("findbugs:NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE") // targetFile() cannot be null
    public void accept(final Stream<IConfigProperty<?>> configProperties) {
      StringBuilder adoc = asciiDoctorDescFor(configProperties);
      ByteBuffer buffer = StandardCharsets.UTF_8.encode(wrap(adoc));
      byte[] rawContent = new byte[buffer.remaining()];
      buffer.get(rawContent);
      try {
        createDirectories(targetFile().getParent());
        write(targetFile(), rawContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LOG.info("Config property description written to '{}'.", targetFile());
      }
      catch (IOException e) {
        LOG.error("Error writing to file '{}'.", targetFile(), e);
      }
    }

    protected StringBuilder asciiDoctorDescFor(final Stream<IConfigProperty<?>> configProperties) {
      AsciiDoctorListBuilder builder = get(AsciiDoctorListBuilder.class);
      configProperties.forEach(p -> builder.appendProperty(p.getKey(), p.description(), dataTypeOf(p)));
      return builder.get();
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
  public static class AsciiDoctorListBuilder {
    private static final Pattern NL_PAT = Pattern.compile("\\r?\\n");
    private static final Pattern EXAMPLE_PAT = Pattern.compile("\\r?\\nExample:\\s");
    private static final String NL = "\n";
    private static final String ADOC_LIST_NL = NL + "+" + NL;
    private static final String ADOC_LISTING = NL + "Example:" + ADOC_LIST_NL + "[listing]" + NL;
    private StringBuilder m_builder = new StringBuilder();

    public void appendProperty(String propertyKey, String description, String dataType) {
      m_builder.append("`").append(propertyKey).append("` ::").append(NL);
      String desc = NL_PAT.matcher(description).replaceAll(ADOC_LIST_NL);
      desc = EXAMPLE_PAT.matcher(desc).replaceAll(ADOC_LISTING);
      desc = desc.replace('\'', '`');
      desc = desc.replace("[listing]\n+\n", "[listing]\n");
      m_builder.append(desc).append(ADOC_LIST_NL);
      m_builder.append("Data type: ").append(dataType.replace('\'', '`')).append(NL).append(NL);
    }

    public StringBuilder get() {
      return m_builder;
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
