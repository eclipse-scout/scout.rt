#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.db.generator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.jooq.DSLContext;
import org.jooq.codegen.GenerationTool;
import org.jooq.codegen.JavaGenerator;
import org.jooq.meta.derby.DerbyDatabase;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.ForcedType;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Target;

import ${package}.db.Environment;
import ${package}.persistence.PersistenceProperties.SchemaProperty;
import ${package}.persistence.common.DateConverter;

public class GeneratorApplication {

  public static final String OUTPUT_DIRECTORY = "../${rootArtifactId}.persistence/src/generated/java";
  public static final String OUTPUT_PACKAGE = "${package}.persistence";

  public static void main(String[] args) {
    new Environment().runWithConfig(new GeneratorApplication()::generate);
  }

  public void generate(DSLContext context) {
      Configuration configuration = new Configuration()
          .withGenerator(new Generator()
              .withName(JavaGenerator.class.getName())
              .withDatabase(new Database()
                  .withForcedTypes(
                      new ForcedType().withName(UUID.class.getName()).withIncludeTypes("varchar(36)"),
                      new ForcedType().withUserType(Date.class.getName()).withConverter(DateConverter.class.getName()).withIncludeTypes("timestamp"),
                      new ForcedType().withName(BigDecimal.class.getName()).withIncludeTypes("bigint"))
                  .withName(DerbyDatabase.class.getName())
                  .withIncludes(".*")
                  .withInputSchema(CONFIG.getPropertyValue(SchemaProperty.class))
                  .withOutputSchema("Schema")
                  .withExcludes("SYS*.*"))
              .withTarget(new Target()
                  .withDirectory(OUTPUT_DIRECTORY)
                  .withPackageName(OUTPUT_PACKAGE)));

      GenerationTool tool = new GenerationTool();
      tool.setConnection(context.configuration().connectionProvider().acquire());
      try {
        tool.run(configuration);
      }
      catch (Exception e) {
        throw new PlatformException("Error generating jooq classes.", e);
      }
    }
  }
