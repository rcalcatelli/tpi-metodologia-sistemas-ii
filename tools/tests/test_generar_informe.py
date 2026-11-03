"""Tests del generador del informe. Ejecutar con: python -m pytest tools/tests"""
import json
import sys
from pathlib import Path

import pytest
from reportlab.platypus import Paragraph, Preformatted, Spacer, Table

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import generar_informe as g  # noqa: E402


@pytest.fixture(scope="module", autouse=True)
def fuentes():
    g.registrar_fuentes()


def bloque(markdown, nivel_base=0):
    lineas = markdown.splitlines()
    return g.leer_bloque(lineas, 0, nivel_base)


class TestTextoEnLinea:

    def test_negrita_cursiva_y_enlace_externo(self):
        texto = g.texto_en_linea("**fuerte** *suave* [repo](https://example.com)")
        assert "<b>fuerte</b>" in texto
        assert "<i>suave</i>" in texto
        assert '<link href="https://example.com"' in texto

    def test_el_codigo_en_linea_no_se_interpreta_como_markdown(self):
        texto = g.texto_en_linea("`a **b** <c>`")
        assert "<b>" not in texto
        assert "a **b** &lt;c&gt;" in texto

    def test_enlace_relativo_queda_como_texto(self):
        assert g.texto_en_linea("[checklist](docs/x.md)") == "checklist"

    def test_reemplaza_emojis_que_la_fuente_no_tiene(self):
        assert g.texto_en_linea("✅ / ❌") == "Sí / No —"


class TestLeerBloque:

    def test_bloque_de_codigo_consume_hasta_el_cierre(self):
        elementos, siguiente = bloque("```java\nint x;\nint y;\n```\ndespués")
        assert isinstance(elementos[0], Preformatted)
        assert siguiente == 4

    def test_tabla_ignora_la_fila_separadora(self):
        elementos, siguiente = bloque("| A | B |\n|---|:-:|\n| 1 | 2 |\nfin")
        tabla = elementos[0]
        assert isinstance(tabla, Table)
        assert len(tabla._cellvalues) == 2
        assert siguiente == 3

    @pytest.mark.parametrize("markdown, nivel_base, estilo", [
        ("# Título", 0, "h1"), ("## Sub", 0, "h2"), ("## Sub", 1, "h3"), ("#### Muy abajo", 0, "h3")])
    def test_titulo_respeta_nivel_base_y_tope(self, markdown, nivel_base, estilo):
        elementos, siguiente = bloque(markdown, nivel_base)
        assert elementos[0].style.name == estilo
        assert siguiente == 1

    def test_item_une_las_lineas_de_continuacion(self):
        elementos, siguiente = bloque("- primera\n  sigue\n- otro")
        assert elementos[0].bulletText == "•"
        assert "primera sigue" in elementos[0].text
        assert siguiente == 2

    def test_item_numerado_conserva_el_numero(self):
        elementos, _ = bloque("3. tercero")
        assert elementos[0].bulletText == "3."

    def test_cita_junta_todas_sus_lineas(self):
        elementos, siguiente = bloque("> uno\n> dos\ntexto")
        assert elementos[0].style.name == "cita"
        assert "uno dos" in elementos[0].text
        assert siguiente == 2

    def test_parrafo_termina_donde_empieza_otro_bloque(self):
        elementos, siguiente = bloque("línea uno\nlínea dos\n| tabla |")
        assert elementos[0].style.name == "p"
        assert "línea uno línea dos" in elementos[0].text
        assert siguiente == 2

    def test_separador_y_linea_vacia(self):
        assert isinstance(bloque("---")[0][0], Spacer)
        assert bloque("   ") == ([], 1)


def test_markdown_a_flowables_recorre_todo_el_documento():
    elementos = g.markdown_a_flowables("# T\n\nTexto.\n\n- a\n- b\n\n| x |\n|---|\n| 1 |\n")
    tipos = [type(e).__name__ for e in elementos]
    assert tipos == ["Paragraph", "Paragraph", "Paragraph", "Paragraph", "Table", "Spacer"]


class TestPendientes:

    @pytest.fixture
    def docs(self, tmp_path):
        (tmp_path / "cap.md").write_text("# Cap\n\nlisto\n[COMPLETAR — algo]\n", encoding="utf-8")
        (tmp_path / "checklist-code-review.md").write_text("# Checklist\n", encoding="utf-8")
        return tmp_path

    def test_encuentra_marcas_en_el_json_anidado_y_en_los_capitulos(self, docs):
        config = {"docente": "[COMPLETAR DOCENTE]", "integrantes": ["Ana"], "capitulos": ["cap.md"]}
        assert g.buscar_pendientes(config, docs) == ["informe.json > docente", "docs/cap.md:4"]

    def test_sin_borrador_termina_con_error(self, docs):
        config = {"docente": "[COMPLETAR DOCENTE]", "capitulos": []}
        with pytest.raises(SystemExit) as salida:
            g.verificar_pendientes(config, docs, borrador=False)
        assert "informe.json > docente" in str(salida.value)

    def test_con_borrador_solo_avisa(self, docs, capsys):
        g.verificar_pendientes({"docente": "[COMPLETAR DOCENTE]", "capitulos": []}, docs, borrador=True)
        assert "Aviso" in capsys.readouterr().err

    def test_main_no_genera_el_pdf_si_hay_pendientes(self, tmp_path):
        config = json.loads((Path(g.__file__).parent / "informe.json").read_text(encoding="utf-8"))
        config["docente"] = "[COMPLETAR DOCENTE]"
        ruta_config = tmp_path / "informe.json"
        ruta_config.write_text(json.dumps(config), encoding="utf-8")
        salida = tmp_path / "informe.pdf"
        with pytest.raises(SystemExit):
            g.main([str(ruta_config), str(salida)])
        assert not salida.exists()


class TestFuentes:

    def test_usa_el_siguiente_juego_si_falta_el_preferido(self, tmp_path, capsys):
        inexistente = ("Calibri/Consolas", [tmp_path / "no-existe"], g.JUEGOS_DE_FUENTES[0][2])
        assert g.registrar_fuentes([inexistente, g.JUEGOS_DE_FUENTES[-1]]) == g.JUEGOS_DE_FUENTES[-1][0]
        assert "no se encontró Calibri" in capsys.readouterr().err

    def test_falla_con_mensaje_si_no_hay_ningun_juego(self, tmp_path):
        with pytest.raises(RuntimeError):
            g.registrar_fuentes([("Ninguno", [tmp_path], {"Cuerpo": "falta.ttf"})])


def test_argumentos():
    argumentos = g.leer_argumentos(["config.json", "salida.pdf", "--borrador"])
    assert argumentos.config == Path("config.json")
    assert argumentos.borrador
    assert not g.leer_argumentos(["c.json", "s.pdf"]).borrador
