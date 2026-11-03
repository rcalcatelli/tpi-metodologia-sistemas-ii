"""Genera el informe PDF del TPI a partir de los documentos Markdown de docs/.

Uso: python tools/generar_informe.py tools/informe.json salida.pdf

El JSON define los datos de portada (comisión, docente, enlaces). Los capítulos
se leen de docs/ para que el informe y el repositorio digan siempre lo mismo.
"""
import argparse
import json
import re
import sys
from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (KeepTogether, PageBreak, Paragraph, Preformatted, SimpleDocTemplate, Spacer,
                                Table, TableStyle)
from reportlab.platypus.tableofcontents import TableOfContents

FUENTES = Path("C:/Windows/Fonts")
pdfmetrics.registerFont(TTFont("Cuerpo", str(FUENTES / "calibri.ttf")))
pdfmetrics.registerFont(TTFont("Cuerpo-Negrita", str(FUENTES / "calibrib.ttf")))
pdfmetrics.registerFont(TTFont("Cuerpo-Cursiva", str(FUENTES / "calibrii.ttf")))
pdfmetrics.registerFont(TTFont("Cuerpo-NegritaCursiva", str(FUENTES / "calibriz.ttf")))
pdfmetrics.registerFont(TTFont("Codigo", str(FUENTES / "consola.ttf")))
pdfmetrics.registerFontFamily("Cuerpo", normal="Cuerpo", bold="Cuerpo-Negrita", italic="Cuerpo-Cursiva",
                              boldItalic="Cuerpo-NegritaCursiva")

AZUL = colors.HexColor("#1F3B5C")
AZUL_CLARO = colors.HexColor("#E8EEF5")
GRIS = colors.HexColor("#F4F5F7")
BORDE = colors.HexColor("#B8C2CC")

ESTILOS = {
    "titulo": ParagraphStyle("titulo", fontName="Cuerpo-Negrita", fontSize=24, leading=30, alignment=TA_CENTER,
                             textColor=AZUL, spaceAfter=10),
    "subtitulo": ParagraphStyle("subtitulo", fontName="Cuerpo-Cursiva", fontSize=13, leading=17,
                                alignment=TA_CENTER, textColor=colors.HexColor("#444444")),
    "portada": ParagraphStyle("portada", fontName="Cuerpo", fontSize=12, leading=17, alignment=TA_CENTER),
    "h1": ParagraphStyle("h1", fontName="Cuerpo-Negrita", fontSize=17, leading=21, textColor=AZUL,
                         spaceBefore=4, spaceAfter=8),
    "h2": ParagraphStyle("h2", fontName="Cuerpo-Negrita", fontSize=13, leading=16, textColor=AZUL,
                         spaceBefore=10, spaceAfter=4),
    "h3": ParagraphStyle("h3", fontName="Cuerpo-Negrita", fontSize=11, leading=14, spaceBefore=6, spaceAfter=3),
    "p": ParagraphStyle("p", fontName="Cuerpo", fontSize=10.5, leading=14, spaceAfter=5),
    "li": ParagraphStyle("li", fontName="Cuerpo", fontSize=10.5, leading=14, leftIndent=14, bulletIndent=4,
                         spaceAfter=2),
    "cita": ParagraphStyle("cita", fontName="Cuerpo-Cursiva", fontSize=10, leading=13, leftIndent=10,
                           textColor=colors.HexColor("#555555"), backColor=GRIS, borderPadding=5, spaceAfter=6),
    "celda": ParagraphStyle("celda", fontName="Cuerpo", fontSize=8.5, leading=10.5),
    "celda_chica": ParagraphStyle("celda_chica", fontName="Cuerpo", fontSize=7.3, leading=9),
    "codigo": ParagraphStyle("codigo", fontName="Codigo", fontSize=7.6, leading=9.4, backColor=GRIS,
                             borderPadding=5, leftIndent=4, rightIndent=4, spaceBefore=2, spaceAfter=8),
    "toc1": ParagraphStyle("toc1", fontName="Cuerpo", fontSize=11, leading=16, leftIndent=10),
}

REEMPLAZOS = {"✅": "Sí", "❌": "No —"}
ANCHO_UTIL = A4[0] - 4 * cm

PATRON_ITEM = r"^\s*([-*]|\d+\.)\s+"
PATRON_SEPARADOR_TABLA = r"^\|[\s:|-]+\|$"
PATRON_INICIO_BLOQUE = r"^(#|\||```|>|\s*([-*]|\d+\.)\s)"


def texto_en_linea(texto, tamano_codigo=8.5):
    """Markdown en línea (negrita, cursiva, código, enlaces) a marcado de reportlab."""
    for original, nuevo in REEMPLAZOS.items():
        texto = texto.replace(original, nuevo)
    # El código en línea se aparta primero para que su contenido no se interprete como Markdown.
    codigos = []

    def apartar_codigo(m):
        codigos.append(f'<font name="Codigo" size="{tamano_codigo}">{escape(m.group(1))}</font>')
        return f"\x00{len(codigos) - 1}\x00"

    texto = escape(re.sub(r"`([^`]+)`", apartar_codigo, texto))
    texto = re.sub(r"\[([^\]]+)\]\((https?://[^)]+)\)", r'<link href="\2" color="#1F5FA8"><u>\1</u></link>', texto)
    texto = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", texto)
    texto = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", texto)
    texto = re.sub(r"(?<![\w*])\*(?!\s)(.+?)(?<!\s)\*(?![\w*])", r"<i>\1</i>", texto)
    return re.sub(r"\x00(\d+)\x00", lambda m: codigos[int(m.group(1))], texto)


def anchos_de_columna(filas, minimo=1.3 * cm):
    """Reparte el ancho según el texto más largo de cada columna, con un mínimo por columna."""
    columnas = len(filas[0])
    largos = [max(len(f[i]) if i < len(f) else 0 for f in filas) for i in range(columnas)]
    pesos = [min(max(largo, 3), 70) for largo in largos]
    anchos = [ANCHO_UTIL * p / sum(pesos) for p in pesos]
    angostas = [i for i, a in enumerate(anchos) if a < minimo]
    if angostas:
        resto = ANCHO_UTIL - minimo * len(angostas)
        peso_resto = sum(p for i, p in enumerate(pesos) if i not in angostas)
        anchos = [minimo if i in angostas else resto * p / peso_resto for i, p in enumerate(pesos)]
    return anchos


def tabla(filas):
    columnas = len(filas[0])
    estilo = ESTILOS["celda_chica"] if columnas >= 6 else ESTILOS["celda"]
    encabezado = ParagraphStyle("encabezado", parent=estilo, fontName="Cuerpo-Negrita", textColor=colors.white)
    tamano_codigo = estilo.fontSize - 0.8
    datos = [[Paragraph(texto_en_linea(c.strip(), tamano_codigo), encabezado if n == 0 else estilo) for c in fila]
             for n, fila in enumerate(filas)]
    t = Table(datos, colWidths=anchos_de_columna(filas), repeatRows=1)
    t.setStyle(TableStyle([
        ("LEFTPADDING", (0, 0), (-1, -1), 3),
        ("RIGHTPADDING", (0, 0), (-1, -1), 3),
        ("BACKGROUND", (0, 0), (-1, 0), AZUL),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, AZUL_CLARO]),
        ("GRID", (0, 0), (-1, -1), 0.4, BORDE),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("TOPPADDING", (0, 0), (-1, -1), 3),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 3),
    ]))
    return [t, Spacer(1, 8)]


# Cada leer_* recibe la posición donde empieza su bloque y devuelve los elementos
# generados junto con la posición de la primera línea que no consumió.

def leer_codigo(lineas, i):
    i += 1
    bloque = []
    while i < len(lineas) and not lineas[i].startswith("```"):
        bloque.append(lineas[i])
        i += 1
    return [Preformatted("\n".join(bloque), ESTILOS["codigo"])], i + 1


def leer_tabla(lineas, i):
    filas = []
    while i < len(lineas) and lineas[i].startswith("|"):
        if not re.match(PATRON_SEPARADOR_TABLA, lineas[i]):
            filas.append(lineas[i].strip().strip("|").split("|"))
        i += 1
    return tabla(filas), i


def leer_titulo(coincidencia, i, nivel_base):
    nivel = min(len(coincidencia.group(1)) + nivel_base, 3)
    return [Paragraph(texto_en_linea(coincidencia.group(2)), ESTILOS[f"h{nivel}"])], i + 1


def continua_item(linea):
    return re.match(r"^\s{2,}\S", linea) and not re.match(PATRON_ITEM, linea)


def leer_item(coincidencia, lineas, i):
    contenido = coincidencia.group(2)
    while i + 1 < len(lineas) and continua_item(lineas[i + 1]):
        i += 1
        contenido += " " + lineas[i].strip()
    vineta = "•" if coincidencia.group(1) in "-*" else coincidencia.group(1)
    return [Paragraph(texto_en_linea(contenido), ESTILOS["li"], bulletText=vineta)], i + 1


def leer_cita(lineas, i):
    bloque = []
    while i < len(lineas) and lineas[i].startswith(">"):
        bloque.append(lineas[i].lstrip("> ").strip())
        i += 1
    return [Paragraph(texto_en_linea(" ".join(bloque)), ESTILOS["cita"])], i


def leer_parrafo(lineas, i):
    parrafo = [lineas[i].strip()]
    while i + 1 < len(lineas) and lineas[i + 1].strip() and not re.match(PATRON_INICIO_BLOQUE, lineas[i + 1]):
        i += 1
        parrafo.append(lineas[i].strip())
    return [Paragraph(texto_en_linea(" ".join(parrafo)), ESTILOS["p"])], i + 1


def leer_bloque(lineas, i, nivel_base):
    """Identifica el bloque que empieza en la línea i y delega en su lector."""
    linea = lineas[i]
    if linea.startswith("```"):
        return leer_codigo(lineas, i)
    if linea.startswith("|"):
        return leer_tabla(lineas, i)
    if titulo := re.match(r"^(#{1,4})\s+(.*)", linea):
        return leer_titulo(titulo, i, nivel_base)
    if item := re.match(PATRON_ITEM + r"(.*)", linea):
        return leer_item(item, lineas, i)
    if linea.startswith(">"):
        return leer_cita(lineas, i)
    if linea.strip() == "---":
        return [Spacer(1, 6)], i + 1
    if linea.strip():
        return leer_parrafo(lineas, i)
    return [], i + 1


def markdown_a_flowables(texto, nivel_base=0):
    """Convierte el subconjunto de Markdown usado en docs/ a elementos de reportlab."""
    flowables = []
    lineas = texto.splitlines()
    i = 0
    while i < len(lineas):
        elementos, i = leer_bloque(lineas, i, nivel_base)
        flowables.extend(elementos)
    return flowables


class Informe(SimpleDocTemplate):
    def afterFlowable(self, flowable):
        if isinstance(flowable, Paragraph) and flowable.style.name == "h1":
            self.notify("TOCEntry", (0, flowable.getPlainText(), self.page))


def pie_de_pagina(canvas, doc, titulo):
    canvas.saveState()
    canvas.setFont("Cuerpo", 8.5)
    canvas.setFillColor(colors.HexColor("#666666"))
    canvas.drawString(2 * cm, 1.2 * cm, titulo)
    canvas.drawRightString(A4[0] - 2 * cm, 1.2 * cm, f"Página {doc.page}")
    canvas.setStrokeColor(AZUL)
    canvas.setLineWidth(1.5)
    canvas.line(2 * cm, A4[1] - 1.3 * cm, A4[0] - 2 * cm, A4[1] - 1.3 * cm)
    canvas.restoreState()


def portada(datos):
    elementos = [Spacer(1, 2.2 * cm)]
    for linea in datos["institucion"]:
        elementos.append(Paragraph(escape(linea), ESTILOS["portada"]))
    elementos += [Spacer(1, 1.6 * cm),
                  Paragraph(escape(datos["materia"]), ESTILOS["subtitulo"]),
                  Spacer(1, 0.4 * cm),
                  Paragraph(escape(datos["titulo"]), ESTILOS["titulo"]),
                  Paragraph(escape(datos["subtitulo"]), ESTILOS["subtitulo"]),
                  Spacer(1, 2 * cm)]
    filas = [["Comisión", datos["comision"]], ["Docente", datos["docente"]],
             ["Integrantes", "\n".join(datos["integrantes"])], ["Modalidad", datos["modalidad"]],
             ["Fecha", datos["fecha"]], ["Repositorio", datos["repositorio"]], ["Video", datos["video"]]]
    t = Table([[Paragraph(f"<b>{escape(a)}</b>", ESTILOS["celda"]),
                Paragraph(texto_en_linea(b).replace("\n", "<br/>"), ESTILOS["celda"])] for a, b in filas],
              colWidths=[3.5 * cm, 10 * cm])
    t.setStyle(TableStyle([("GRID", (0, 0), (-1, -1), 0.4, BORDE), ("BACKGROUND", (0, 0), (0, -1), AZUL_CLARO),
                           ("VALIGN", (0, 0), (-1, -1), "MIDDLE"), ("TOPPADDING", (0, 0), (-1, -1), 5),
                           ("BOTTOMPADDING", (0, 0), (-1, -1), 5)]))
    elementos += [t, PageBreak()]
    return elementos


def seccion_indice():
    indice = TableOfContents()
    indice.levelStyles = [ESTILOS["toc1"]]
    return [Paragraph("Índice", ParagraphStyle("indice", parent=ESTILOS["h1"])), indice, PageBreak()]


def seccion_resumen(config):
    return [Paragraph("Resumen ejecutivo", ESTILOS["h1"]),
            *markdown_a_flowables(config["resumen_ejecutivo"]),
            Paragraph("Enlaces de la entrega", ESTILOS["h2"]),
            *markdown_a_flowables(config["enlaces"]),
            PageBreak()]


def seccion_capitulos(config, docs):
    elementos = []
    for capitulo in config["capitulos"]:
        elementos += markdown_a_flowables((docs / capitulo).read_text(encoding="utf-8"))
        elementos.append(PageBreak())
    return elementos


def seccion_anexos(config, docs):
    checklist = (docs / "checklist-code-review.md").read_text(encoding="utf-8").split("\n", 1)[1]
    return [Paragraph("Anexos", ESTILOS["h1"]),
            *markdown_a_flowables(config["anexos"]),
            Paragraph("Anexo A — Checklist de code review", ESTILOS["h2"]),
            *markdown_a_flowables(checklist, nivel_base=1)]


def construir_pdf(historia, config, salida):
    doc = Informe(salida, pagesize=A4, leftMargin=2 * cm, rightMargin=2 * cm, topMargin=2 * cm,
                  bottomMargin=2 * cm, title=config["titulo"], author=", ".join(config["integrantes"]),
                  subject=config["materia"])
    pie = lambda c, d: pie_de_pagina(c, d, f'{config["materia"]} — {config["titulo"]}')
    doc.multiBuild(historia, onLaterPages=pie)


def leer_argumentos(argv=None):
    parser = argparse.ArgumentParser(description="Genera el informe PDF del TPI a partir de docs/.")
    parser.add_argument("config", type=Path, help="JSON con portada, resumen, enlaces y capítulos (tools/informe.json)")
    parser.add_argument("salida", type=Path, help="ruta del PDF a generar")
    return parser.parse_args(argv)


def main(argv=None):
    argumentos = leer_argumentos(argv)
    if not argumentos.config.is_file():
        sys.exit(f"No existe el archivo de configuración: {argumentos.config}")
    config = json.loads(argumentos.config.read_text(encoding="utf-8"))
    docs = Path(__file__).resolve().parent.parent / "docs"
    historia = (portada(config) + seccion_indice() + seccion_resumen(config)
                + seccion_capitulos(config, docs) + seccion_anexos(config, docs))
    construir_pdf(historia, config, str(argumentos.salida))


if __name__ == "__main__":
    main()
