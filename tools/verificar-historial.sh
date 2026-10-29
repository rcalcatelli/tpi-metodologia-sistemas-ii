#!/usr/bin/env bash
# Ejecuta la suite de tests en cada commit desde que existen los tests de
# caracterización, para demostrar que ningún refactoring rompió el comportamiento.
# Uso: tools/verificar-historial.sh [commit-inicial] [mvn]
set -euo pipefail

DESDE="${1:-$(git rev-list --reverse HEAD -- src/test/java/ar/edu/utn/tup/solicitudes/CaracterizacionGestorTest.java | head -1)}"
MVN="${2:-mvn}"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

printf "%-9s %-72s %6s %6s %6s\n" "Commit" "Mensaje" "Tests" "Fallos" "Errores"
for commit in $(git rev-list --reverse "$DESDE^..HEAD"); do
    dir="$TMP/$commit"
    mkdir -p "$dir"
    git archive "$commit" | tar -x -C "$dir"
    (cd "$dir" && "$MVN" -q -o test >/dev/null 2>&1) || true
    read -r total fallos errores < <(cat "$dir"/target/surefire-reports/*.txt 2>/dev/null \
        | grep "Tests run" | awk -F'[:,]' '{t+=$2; f+=$4; e+=$6} END {print t+0, f+0, e+0}')
    printf "%-9s %-72s %6s %6s %6s\n" "${commit:0:7}" "$(git log -1 --format=%s "$commit" | cut -c1-72)" "$total" "$fallos" "$errores"
done
