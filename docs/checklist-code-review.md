# Checklist de code review

Plantilla reutilizable. Cada ítem se marca **Cumple**, **Cumple parcialmente**
(con observación) o **No cumple** (bloquea la aceptación si es de severidad Alta).

## A. Diseño y responsabilidades
- [ ] A1. Cada clase tiene una única razón de cambio (SRP).
- [ ] A2. Agregar una variante (tipo, estado) no obliga a modificar código existente disperso (OCP).
- [ ] A3. Las implementaciones son intercambiables a través de sus interfaces (LSP).
- [ ] A4. Las interfaces son pequeñas y específicas (ISP).
- [ ] A5. La lógica depende de abstracciones; las dependencias se inyectan (DIP).
- [ ] A6. No hay dependencias cíclicas entre paquetes.

## B. Legibilidad (Clean Code)
- [ ] B1. Nombres expresivos, sin abreviaturas ni letras sueltas fuera de lambdas cortas.
- [ ] B2. Métodos de menos de ~20 líneas y una sola tarea.
- [ ] B3. Sin números ni textos mágicos repetidos.
- [ ] B4. Sin código duplicado.
- [ ] B5. Sin código muerto ni comentarios que suplan código poco claro.
- [ ] B6. Ningún parámetro oculta un campo de la clase.

## C. Encapsulamiento y dominio
- [ ] C1. Campos privados; el estado solo cambia mediante métodos que validan reglas.
- [ ] C2. Las reglas de negocio viven en la entidad que posee los datos.
- [ ] C3. Las invariantes no dependen de que el llamador valide antes.
- [ ] C4. Listas largas de parámetros reemplazadas por objetos parámetro.

## D. Errores y recursos
- [ ] D1. Errores con excepciones tipadas y mensajes claros; sin `catch (Exception)` genérico.
- [ ] D2. Las causas de excepciones técnicas se conservan.
- [ ] D3. Archivos abiertos con `try-with-resources` y codificación explícita.
- [ ] D4. Una operación fallida no deja datos a medio modificar.
- [ ] D5. La interfaz de usuario no termina ante entradas inválidas.

## E. Pruebas
- [ ] E1. Toda la suite pasa (`mvn verify`).
- [ ] E2. Los tests de caracterización no fueron modificados durante el refactoring.
- [ ] E3. Cada clase extraída tiene tests propios.
- [ ] E4. Los tests no dependen del orden de ejecución ni de estado global.
- [ ] E5. Los tests son deterministas (sin dependencia de la fecha u hora real).

## F. Repositorio
- [ ] F1. Commits pequeños, con mensaje que explica el *qué* y el *por qué*.
- [ ] F2. Tags de etapa presentes.
- [ ] F3. README con instalación, uso, funcionalidades y tecnologías.
- [ ] F4. Sin datos personales reales (solo datos sintéticos).

## Criterios de aceptación técnica
1. Ningún ítem de severidad **Alta** en "No cumple".
2. Suite completa en verde y tests de caracterización sin cambios.
3. Salida del golden master idéntica a la versión inicial.
4. Cada observación tiene una decisión registrada: corregida (con commit),
   aceptada (con justificación) o pendiente (con plan).
