#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.persistence.common;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;

import ${package}.persistence.IJooqService;

public abstract class AbstractRepository<TABLE extends Table<RECORD>, RECORD extends Record, DO extends DoEntity> implements IBaseService<TABLE, RECORD, DO> {

  abstract protected DoEntityBeanMappings<DO, RECORD> mappings();

  /**
   * Returns true if a record with the provided id exists using the specified context
   */
  protected boolean exists(DSLContext context, String id) {
    return context.fetchExists(
        context.select()
            .from(getTable())
            .where(getIdColumn().eq(id)));
  }

  @Override
  public RECORD newRecord() {
    return BEANS.get(IJooqService.class).apply(c -> c.newRecord(getTable()));
  }

  @Override
  public int remove(String id) {
    return BEANS.get(IJooqService.class).apply(c -> c
        .deleteFrom(getTable())
        .where(getIdColumn().eq(id))
        .execute());
  }

  @Override
  public Optional<RECORD> get(String id) {
    return BEANS.get(IJooqService.class).apply(C -> Optional.ofNullable(C
        .selectFrom(getTable())
        .where(getIdColumn().eq(id))
        .fetchOne()));
  }

  @Override
  public Stream<RECORD> getAll() {
    return BEANS.get(IJooqService.class).apply(c -> c.selectFrom(getTable()).fetchStream());
  }

  @Override
  public void store(String id, RECORD record) {
    BEANS.get(IJooqService.class).accept(c -> {
      if (exists(c, id)) {
        c.update(getTable())
            .set(record)
            .where(getIdColumn().eq(id))
            .execute();
      }
      else {
        c.insertInto(getTable())
            .set(record)
            .execute();
      }
    });
  }

  protected RECORD fromDoToRecord(DO cDo, RECORD cBean) {
    mappings().fromDoToRecord(cDo, cBean);
    return cBean;
  }

  protected DO fromRecordToDo(RECORD cBean, DO cDo) {
    mappings().fromEntityBeanToDo(cBean, cDo);
    return cDo;
  }
}
