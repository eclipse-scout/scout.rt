${simpleArtifactName}.DataOutline = function() {
  ${simpleArtifactName}.DataOutline.parent.call(this);
};
scout.inherits(${simpleArtifactName}.DataOutline, scout.Outline);

${simpleArtifactName}.DataOutline.prototype._jsonModel = function() {
  return scout.models.getModel('${simpleArtifactName}.DataOutline');
};
