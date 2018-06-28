#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.jdbc.derby.AbstractDerbySqlService;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.conf.MappedSchema;
import org.jooq.conf.RenderMapping;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

import ${package}.persistence.PersistenceProperties.DialectProperty;
import ${package}.persistence.PersistenceProperties.JdbcMappingNameProperty;
import ${package}.persistence.PersistenceProperties.PasswordProperty;
import ${package}.persistence.PersistenceProperties.SchemaProperty;
import ${package}.persistence.PersistenceProperties.UsernameProperty;

@CreateImmediately
public class JooqSqlService extends AbstractDerbySqlService implements IJooqService {

  private final Configuration m_configuration;
  private final ConnectionProvider m_connectionProvider;

  protected JooqSqlService() {
    m_configuration = initializeConfiguration();
    m_connectionProvider = new ScoutConnectionProvider();
  }

  private static Configuration initializeConfiguration() {
    Configuration configuration = new DefaultConfiguration();
    configuration.set(CONFIG.getPropertyValue(DialectProperty.class));
    Settings s = configuration.settings();
    s.withRenderMapping(new RenderMapping()
        .withSchemata(
            new MappedSchema()
			    .withInputExpression(Pattern.compile(CONFIG.getPropertyValue(SchemaProperty.class)))
                .withOutput(CONFIG.getPropertyValue(SchemaProperty.class))));

    configuration.set(s);
    return configuration;
  }

  @Override
  protected String getConfiguredJdbcDriverName() {
    return CONFIG.getPropertyValue(PersistenceProperties.DriverProperty.class);
  }

  @Override
  protected String getConfiguredUsername() {
    return CONFIG.getPropertyValue(UsernameProperty.class);
  }

  @Override
  protected String getConfiguredPassword() {
    return CONFIG.getPropertyValue(PasswordProperty.class);
  }

  @Override
  protected String getConfiguredJdbcMappingName() {
    return CONFIG.getPropertyValue(JdbcMappingNameProperty.class);
  }

  @Override
  public <T> T apply(Function<DSLContext, T> task) {
    try (DSLContext ctx = DSL.using(m_configuration.derive(m_connectionProvider))) {
      return task.apply(ctx);
    }
  }

  @Override
  public void accept(Consumer<DSLContext> task) {
    apply(c -> {
      task.accept(c);
      return null;
    });
  }

  private class ScoutConnectionProvider implements ConnectionProvider {

    @Override
    public Connection acquire() throws DataAccessException {
      return getConnection();
    }

    @Override
    public void release(Connection connection) throws DataAccessException {
      // NOP
    }
  }
}
