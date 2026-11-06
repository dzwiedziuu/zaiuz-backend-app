# Generator danych (emulator czujnika)

Narzędzie konsolowe w Node.js (wersja 20+, bez dodatkowych zależności), które wysyła wyniki
pomiarów wyłącznie przez API aplikacji: `POST /api/measurements` z kluczem czujnika w nagłówku `X-API-Key`.
Klucz API nie jest zapisany w kodzie. Podaje się go w zmiennej środowiskowej `SENSOR_API_KEY`
(zalecane) albo parametrem `--key`.

## sensor-generator.mjs

```bash
# tryb historyczny: 48 wyników co 30 minut, kończąc na chwili bieżącej (dane przykładowe)
SENSOR_API_KEY=<klucz> node generator/sensor-generator.mjs --api https://moja-app.example.com \
  --history --count 48 --interval 1800 --mode sine --min 12 --max 24

# tryb bieżący: wynik co 5 sekund, bez końca (Ctrl+C kończy)
SENSOR_API_KEY=<klucz> node generator/sensor-generator.mjs --api https://moja-app.example.com \
  --interval 5 --count 0 --mode random --min 0 --max 100
```

W PowerShellu zmienną ustawia się poleceniem `$env:SENSOR_API_KEY = "<klucz>"`.

| Parametr | Opis | Domyślnie |
|---|---|---|
| `--api <URL>` | adres aplikacji (lub zmienna `API_URL`) | – |
| `--key <klucz>` | klucz API czujnika (lub zmienna `SENSOR_API_KEY`) | – |
| `--history` | tryb historyczny: `--count` wyników ze znacznikami czasu z przeszłości, co `--interval` s, kończąc teraz; bez flagi tryb bieżący (wynik co `--interval` s z czasem serwera) | tryb bieżący |
| `--count <n>` | liczba wyników; w trybie bieżącym `0` = bez końca | 20 |
| `--interval <s>` | odstęp między wynikami w sekundach | 60 (historyczny), 5 (bieżący) |
| `--mode <tryb>` | `random` – losowo z [min, max]; `walk` – przebieg losowy; `sine` – sinusoida z szumem | `random` |
| `--min`, `--max` | zakres generowanych wartości | 0, 100 |
| `--period <n>` | liczba wyników na okres sinusoidy | 24 |
| `--noise <x>` | odchylenie standardowe szumu (`sine`, `walk`) | 2% zakresu |
| `--decimals <n>` | miejsca po przecinku | 1 |

Każdy wynik jest wypisywany w konsoli. Odrzucenia są opisane czytelnym komunikatem (F4), np.
`ODRZUCONO (422): Wartość musi mieścić się w przedziale serii` albo
`ODRZUCONO (401): klucz API jest niepoprawny albo czujnik został wyrejestrowany`. Po 401 generator kończy pracę.
Aby pokazać odrzucenie wyniku spoza przedziału, ustaw `--min`/`--max` szerzej niż przedział serii.

## seed-sample-data.mjs – dane przykładowe (F11)

Loguje się jako administrator, tworzy przez API 3 serie (Temperatura, Wilgotność, Ciśnienie),
rejestruje dla każdej czujnik i uruchamia `sensor-generator.mjs` z kluczem tego czujnika
(domyślnie 48 wyników co 30 minut z ostatniej doby). Serie o istniejącej nazwie są pomijane.

```bash
ADMIN_USERNAME=admin ADMIN_PASSWORD=<hasło> node generator/seed-sample-data.mjs --api https://moja-app.example.com
```

Opcjonalnie `--count <n>` i `--interval <s>` zmieniają liczbę i odstęp wyników.
