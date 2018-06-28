#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/**
 * Subclasses of Repository must set the resourceType property.
 *
 * @abstract
 * @class
 */
${rootArtifactId}.Repository = function() {
  this.entityType = null;
};

${rootArtifactId}.Repository.prototype.getJson = function(url, opts) {
  return ${rootArtifactId}.Repository.map(scout.ajax.getJson(url, this._ensureConverter(opts)));
};

${rootArtifactId}.Repository.prototype.postJson = function(url, data, opts) {
  return ${rootArtifactId}.Repository.map(scout.ajax.postJson(url, data, this._ensureConverter(opts)));
};

${rootArtifactId}.Repository.prototype.removeJson = function(url, opts) {
  return ${rootArtifactId}.Repository.map(scout.ajax.removeJson(url, this._ensureConverter(opts)));
};

${rootArtifactId}.Repository.prototype.putJson = function(url, data, opts) {
  return ${rootArtifactId}.Repository.map(scout.ajax.putJson(url, data, this._ensureConverter(opts)));
};

${rootArtifactId}.Repository.prototype._ensureConverter = function(opts) {
  return ${symbol_dollar}.extend({}, {
    converters: {
      "text json": this._mapJson
    }
  }, opts);
};

${rootArtifactId}.Repository.prototype._mapJson = function(data) {
  return JSON.parse(data, function(key, value) {
    if (key === '_type') {
      this.objectType = scout.app.appPrefix + value;
    }
    return value;
  });
};

${rootArtifactId}.Repository.prototype._first = function(items) {
  return items[0];
};

// ---- Static Objects ---- //

${rootArtifactId}.repositories = {};

/**
 * @static
 * @param {string} objectName
 * @returns {${rootArtifactId}.Repository}
 */
${rootArtifactId}.Repository.register = function(objectName) {
  var repository = scout.create(objectName);
  ${rootArtifactId}.repositories[repository.entityType] = repository;
  return repository;
};

${rootArtifactId}.Repository.map = function(promise) {
  return promise
    .then(function(response) {
      if (!response || !response.items) {
        return response;
      }

      return response.items
        .map(function(item) {
          return scout.create(item, {
            ensureUniqueId: false
          });
        });
    });
};

/**
 * @static
 * @param {string} resourceType
 * @returns {${rootArtifactId}.Repository} a repository for the given resourceType
 */
${rootArtifactId}.Repository.get = function(resourceType) {
  var repository = ${rootArtifactId}.repositories[resourceType];
  if (!repository) {
    throw new Error('no repository found for resourceType ' + resourceType);
  }
  return repository;
};
