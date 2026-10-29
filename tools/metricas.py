"""Métricas aproximadas de mantenibilidad para el código Java del proyecto.

Para cada clase informa: líneas de código (sin vacías ni comentarios), cantidad de
métodos, método más largo y complejidad ciclomática aproximada (1 + puntos de
decisión: if, for, while, case, catch, &&, ||, ?).

Uso: python tools/metricas.py [directorio_fuente]
"""
import re
import sys
from pathlib import Path

DECISIONES = re.compile(r"\b(if|for|while|case|catch)\b|&&|\|\||\?")
FIRMA_METODO = re.compile(
    r"^\s*(public|private|protected|static|\s)*[\w<>\[\], ]+\s+(\w+)\s*\([^;]*\)\s*(throws [\w, ]+)?\s*\{\s*$")
PALABRAS_NO_METODO = {"if", "for", "while", "switch", "catch", "else", "try", "return", "new"}


def lineas_de_codigo(texto):
    sin_bloques = re.sub(r"/\*.*?\*/", "", texto, flags=re.S)
    utiles = []
    for linea in sin_bloques.splitlines():
        sin_comentario = re.sub(r"//.*", "", linea).rstrip()
        if sin_comentario.strip():
            utiles.append(sin_comentario)
    return utiles


def metodos(lineas):
    resultado = []
    i = 0
    while i < len(lineas):
        m = FIRMA_METODO.match(lineas[i])
        if m and m.group(2) not in PALABRAS_NO_METODO and " class " not in lineas[i] and " record " not in lineas[i]:
            nivel, j = 0, i
            while j < len(lineas):
                nivel += lineas[j].count("{") - lineas[j].count("}")
                if nivel == 0:
                    break
                j += 1
            cuerpo = lineas[i:j + 1]
            complejidad = 1 + sum(len(DECISIONES.findall(l)) for l in cuerpo)
            resultado.append((m.group(2), len(cuerpo), complejidad))
            i = j + 1
        else:
            i += 1
    return resultado


def main():
    raiz = Path(sys.argv[1] if len(sys.argv) > 1 else "src/main/java")
    print(f"{'Clase':<38}{'LOC':>5}{'Métodos':>9}{'Mayor método':>32}{'CC máx':>8}{'CC total':>10}")
    total_loc = 0
    for archivo in sorted(raiz.rglob("*.java")):
        lineas = lineas_de_codigo(archivo.read_text(encoding="utf-8"))
        ms = metodos(lineas)
        total_loc += len(lineas)
        if ms:
            mayor = max(ms, key=lambda m: m[1])
            cc_max = max(m[2] for m in ms)
            cc_total = sum(m[2] for m in ms)
            mayor_txt = f"{mayor[0]} ({mayor[1]} l.)"
        else:
            mayor_txt, cc_max, cc_total = "-", 0, 0
        print(f"{archivo.stem:<38}{len(lineas):>5}{len(ms):>9}{mayor_txt:>32}{cc_max:>8}{cc_total:>10}")
    print(f"{'TOTAL':<38}{total_loc:>5}")


if __name__ == "__main__":
    main()
