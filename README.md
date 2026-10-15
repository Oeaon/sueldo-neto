# Sueldo Neto

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white) ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white) ![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)

Calculadora de sueldo neto, paro e indemnización por despido en España, con comparador de ofertas de trabajo.

App Android nativa desarrollada en **Kotlin** con **Jetpack Compose** (Material 3).

- **Nómina**: bruto anual → neto mensual, pagas extra, Seguridad Social e IRPF con el procedimiento oficial de retenciones. 12 o 14 pagas, contrato indefinido o temporal, hijos y menores de 3 años.
- **Comparar ofertas** (Premium o 24 h con un anuncio): diferencia neta entre dos sueldos y cuánto de cada euro bruto de más te queda.
- **Paro**: duración según los días cotizados y el importe en cada tramo (70 % / 60 % / 50 %) con sus topes.
- **Despido**: indemnización (improcedente, objetivo o fin de contrato) y vacaciones pendientes.
- Funciona sin conexión: todo se calcula en el móvil.

## Anuncios y Premium

| Formato | Dónde | Límite |
|---|---|---|
| Banner adaptativo | Pantallas de consulta | — |
| Intersticial | Solo en pausas naturales (al cambiar de calculadora) | Uno cada 3 min como mucho |
| App Open | Al volver a la app desde segundo plano, nunca en el primer arranque | Uno cada 5 min como mucho |
| Recompensado | Cuando el usuario lo pide para desbloquear el comparador de ofertas durante 24 h | — |

Premium (Google Play Billing) quita todos los anuncios y desbloquea el comparador de ofertas para siempre:

- `premium_monthly`: suscripción mensual.
- `premium_lifetime`: producto de compra única.

Créalos en Play Console con esos IDs. Al arrancar se consulta la tienda, así que si la suscripción caduca vuelven los anuncios. Para una app con muchos ingresos conviene añadir verificación de compras en servidor.

Los IDs de AdMob van en `admob.properties` (no se sube a git; plantilla en `admob.properties.example`). Sin ese archivo se usan las unidades de prueba de Google. El consentimiento RGPD se pide con UMP antes de cargar ningún anuncio.

## Idiomas

Castellano (por defecto), catalán, gallego e inglés.

## Valores fiscales

Están todos en `core/src/main/kotlin/com/sueldoneto/core/TaxYear.kt` (`TaxYears.Y2026`). Antes de publicar, y cada enero, revisa con el BOE:

| Valor | Usado |
|---|---|
| Cotización del trabajador | 4,70 % CC + 0,10 % FP + 0,15 % MEI + 1,55 % desempleo (1,60 % temporal) |
| Base máxima mensual | 5.101,20 € |
| Cuota de solidaridad (total) | 1,15 % / 1,25 % / 1,46 % por tramos |
| Escala de retenciones | 19 / 24 / 30 / 37 / 45 / 47 % |
| Reducción por rendimientos del trabajo | 7.302 € hasta 14.852 € de rendimiento neto |
| Mínimo personal / por hijos | 5.550 € / 2.400, 2.700, 4.000, 4.500 € (+2.800 € menores de 3) |
| Límite sin retención | 15.876 € (sin hijos), 16.342 € (1), 16.867 € (2 o más) |
| IPREM mensual (paro) | 600 € |

Supuestos: contribuyente sin cónyuge a cargo, menor de 65 años, sin discapacidad, con los hijos compartidos con el otro progenitor. No cubre los regímenes forales del País Vasco y Navarra.

## Estructura

```
core/   Kotlin puro: nómina, comparación, paro e indemnización (con tests)
app/    Android: Compose y Material 3
  ui/             pestañas y componentes
  monetization/   consentimiento, anuncios y compras de Premium
```

## Compilar

Requisitos: Android Studio reciente (JDK 17+), Android SDK 36.

```bash
./gradlew :core:test        # tests de la lógica
./gradlew installDebug      # instala en el emulador o el móvil (anuncios de prueba)
./gradlew bundleRelease     # AAB para Google Play
```

### Release

1. Crea el keystore de subida y un `keystore.properties` en la raíz (no se sube a git):
   ```
   storeFile=upload.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```
2. Copia `admob.properties.example` como `admob.properties` y pon los IDs reales de AdMob de esta app.
3. Crea en Play Console los productos `premium_monthly` (suscripción) y `premium_lifetime` (compra única).
4. Publica `docs/privacy.html` (por ejemplo con GitHub Pages) y enlázala en la ficha de Play.
