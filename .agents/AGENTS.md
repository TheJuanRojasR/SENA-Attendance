# Reglas del proyecto SENA Attendance

## Regla de cambios visuales en el Frontend

Antes de sugerir cualquier cambio visual (color, tamaño, tipografía, espaciado), seguir este orden estricto:

1. **Primero:** Verificar si ese estilo está controlado por una variable global de Bootstrap/SCSS en `_bootstrap-variables.scss` o en los archivos del tema Bootswatch (litera).
2. **Segundo:** Si existe una variable global que lo controle, indicar el cambio en ese archivo global solamente.
3. **Tercero:** Solo si no existe variable global aplicable, indicar el cambio directo en el componente — y siempre explicar por qué no se puede hacer de forma global.

**Nunca** sugerir hardcoding de colores, tamaños o tipografías en componentes individuales si ese valor está disponible como variable global o clase de utilidad de Bootstrap.

## Jerarquía de archivos de estilos globales en este proyecto

```
_bootstrap-variables.scss   ← PRIMERO revisar aquí (variables de Bootstrap/tema)
app.scss                    ← SEGUNDO (estilos globales de la app)
header.scss                 ← Solo para el encabezado
home.scss                   ← Solo para la página de inicio
i18n/es/*.json              ← Para textos, nunca hardcodear texto en .tsx
```

## Estilo de comunicación

- La usuaria es principiante. Usar analogías de la vida diaria.
- Evitar jerga técnica sin explicación.
- Siempre indicar: archivo exacto, número de línea, código antes y después.
- La usuaria hace los cambios ella misma — solo guiar, no modificar archivos directamente salvo que se pida explícitamente.
