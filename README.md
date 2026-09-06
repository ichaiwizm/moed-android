# Moed — Android

Native Jetpack Compose app for the Jewish calendar (*louah*): today's Hebrew date, zmanim for your
city, Shabbat candle lighting and havdala, parasha and haftara, the Omer count, hiloulot, and
personal Hebrew birthdays and yahrzeits.

**Fully offline, no account.** All astronomical and halakhic computation runs on the device from
deterministic libraries and bundled static datasets, so there is no per-user server cost and no
network requirement. This is the Android sibling of [moed-ios](https://github.com/ichaiwizm/moed-ios),
built from the same product specification (`NATIVE_SPEC.md`) and validated against the same
references.

## Architecture

- **On-device engine** (`app/src/main/java/com/moed/app/engine/`) — zmanim, Hebrew date conversion,
  candle lighting, haftara tables and gematria, on top of `com.kosherjava:zmanim` (NOAA solar).
- **Validated against the reference implementations** — `validation/validation-report.json` records
  the run: over a matrix of 10 cities x 2 years, 80/80 Hebrew dates exact and 1360/1360 zmanim within
  one minute of `kosher-zmanim` (max deviation 10 ms), 260/260 yahrzeit and Hebrew-birthday
  occurrences matching `@hebcal/core` including the Cheshvan 30 / Kislev 30 / Adar leap rules, and
  the haftara tables matching `@hebcal/leyning` for every Shabbat of Hebrew years 5784-5792 in both
  the Ashkenazi and Sephardic rites.
- **Compose UI, Material 3 Expressive** — Today, Calendar, Personal, Tsadikim, Settings, plus
  zmanim-by-city, tsadik detail and a date converter, with a custom design system (colours, shapes,
  variable fonts) and Hebrew RTL support.
- **Home-screen widgets with Glance** — candle lighting, Omer and zmanim, refreshed by WorkManager.
- **Local notifications** — scheduled ahead through AlarmManager receivers and re-armed on boot,
  since every reminder date is deterministic.
- **Optional, opt-in network** — a small Retrofit/OkHttp push client is the only remote code; the
  app is complete without it.
- **Release signing from CI** — the workflow decodes the keystore from a secret into
  `keystore.properties`; a clean clone falls back to the debug key and still builds.

## Stack

Kotlin, Jetpack Compose (Material 3), KosherJava zmanim, DataStore, Glance app widgets, WorkManager,
Retrofit + OkHttp + kotlinx.serialization, Gradle version catalogs, GitHub Actions.

## Build

```bash
./gradlew assembleDebug        # APK, no signing setup needed
./gradlew installDebug         # onto a connected device or emulator
```

`assembleRelease` uses the CI keystore when `keystore.properties` is present and silently falls back
to the debug signing config otherwise. Requires JDK 17, compileSdk 35, minSdk 26.

## Status

Not published on the Play Store. The release workflow and Play service account are wired but never
promoted to a track.

## Licence

No licence granted. Published for reading.
