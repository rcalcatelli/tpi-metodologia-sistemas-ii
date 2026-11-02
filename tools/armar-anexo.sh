#!/usr/bin/env bash
# Regenera las partes del anexo que salen del repositorio (código de los tags,
# docs, evidencias, cobertura, reportes de tests e historial) y arma el ZIP.
# No toca presentacion/ ni ia/, que se mantienen a mano.
# Uso: tools/armar-anexo.sh <carpeta-anexo> [mvn]
set -euo pipefail

ANEXO="${1:?Uso: tools/armar-anexo.sh <carpeta-anexo> [mvn]}"
MVN="${2:-mvn}"

"$MVN" -q clean verify

rm -rf "$ANEXO/codigo" "$ANEXO/docs" "$ANEXO/evidencias" "$ANEXO/cobertura-jacoco" "$ANEXO/reportes-tests"
mkdir -p "$ANEXO/codigo/estado-inicial" "$ANEXO/codigo/estado-refactorizado" "$ANEXO/docs" "$ANEXO/reportes-tests"

git archive estado-inicial | tar -x -C "$ANEXO/codigo/estado-inicial"
git archive estado-refactorizado | tar -x -C "$ANEXO/codigo/estado-refactorizado"
cp docs/*.md "$ANEXO/docs/"
cp -r docs/evidencias "$ANEXO/evidencias"
cp -r target/site/jacoco "$ANEXO/cobertura-jacoco"
cp target/surefire-reports/*.txt target/surefire-reports/*.xml "$ANEXO/reportes-tests/"
git log --decorate --format='%h %ad %an%d%n    %s' --date=iso > "$ANEXO/historial-git.txt"

ZIP="$(dirname "$ANEXO")/$(basename "$ANEXO").zip"
rm -f "$ZIP"
python -c "import shutil, sys; shutil.make_archive(sys.argv[1], 'zip', sys.argv[2])" "${ZIP%.zip}" "$ANEXO"
echo "Anexo actualizado: $ANEXO"
echo "ZIP: $ZIP"
