scout.DataModel = function() {
  scout.DataModel.parent.call(this);
  this.entities = [];
};
scout.inherits(scout.DataModel, scout.ModelAdapter);

scout.DataModel.prototype.init = function(model, session) {
  scout.DataModel.parent.prototype.init.call(this, model, session);

  var i, entity;
  if (model.rootEntities) {
    for (i = 0; i < model.rootEntities.length; i++) {
      entity = scout.DataModel.resolveEntity(model, model.rootEntities[i]);
      this.entities.push(entity);
    }
  }

};

scout.DataModel.resolveEntity = function(model, entityRef) {
  var entity, j, k, attribute, attributeRef;
  entity = model[entityRef];

  if (!entity.attributes) {
    entity.attributes = [];
    if (entity.attributeRefs) {
      for (j = 0; j < entity.attributeRefs.length; j++) {
        attributeRef = entity.attributeRefs[j];
        attribute = model[attributeRef];
        attribute.operators = model[attribute.operatorsRef];
        attribute.aggregations = model[attribute.aggregationsRef];

        entity.attributes.push(attribute);
      }
    }
  }

  if (!entity.entities) {
    entity.entities = [];
    if (entity.entityRefs) {
      for (k = 0; k < entity.entityRefs.length; k++) {
        entity.entities.push(scout.DataModel.resolveEntity(model, entity.entityRefs[k]));
      }
    }
  }

  return entity;
};
