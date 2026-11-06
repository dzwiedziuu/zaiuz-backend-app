#!/usr/bin/env node
// Generator danych (emulator czujnika) – F13.
// Wysyła wyniki wyłącznie przez API aplikacji: POST /api/measurements z nagłówkiem X-API-Key.
// Wymaga Node.js 20+ (wbudowany fetch). Brak zewnętrznych zależności.
//
// Przykłady:
//   SENSOR_API_KEY=<klucz> node generator/sensor-generator.mjs --api http://localhost:8080 --history --count 48 --interval 1800 --mode sine --min 10 --max 25
//   SENSOR_API_KEY=<klucz> node generator/sensor-generator.mjs --api http://localhost:8080 --interval 5 --mode random --min 0 --max 100

import { parseArgs } from 'node:util';

const HELP = `Generator danych – emulator czujnika (wysyła wyniki do POST /api/measurements)

Użycie:
  node generator/sensor-generator.mjs --api <URL> [opcje]

Parametry:
  --api <URL>          adres aplikacji/API, np. https://moja-app.example.com   (lub zmienna API_URL)
  --key <klucz>        klucz API czujnika                                     (lub zmienna SENSOR_API_KEY – zalecane)
  --history            tryb historyczny: wysyła --count wyników ze znacznikami czasu z przeszłości,
                       co --interval sekund, kończąc na chwili bieżącej (do przygotowania danych przykładowych)
                       bez tej flagi: tryb bieżący – wysyła wynik co --interval sekund z czasem serwera
  --count <n>          liczba wyników (domyślnie 20; w trybie bieżącym 0 = bez końca)
  --interval <s>       odstęp między wynikami w sekundach (domyślnie 60 w trybie historycznym, 5 w bieżącym)
  --mode <tryb>        sposób generowania wartości:
                         random – wartości losowe z przedziału [min, max] (domyślnie)
                         walk   – przebieg losowy (błądzenie losowe) w przedziale [min, max]
                         sine   – sinusoida o środku (min+max)/2 i amplitudzie (max-min)/2, z niewielkim szumem
  --min <x>            dolna granica generowanych wartości (domyślnie 0)
  --max <x>            górna granica generowanych wartości (domyślnie 100)
  --period <n>         liczba wyników na pełny okres sinusoidy (domyślnie 24)
  --noise <x>          odchylenie standardowe szumu w trybach sine/walk (domyślnie 2% zakresu)
  --decimals <n>       liczba miejsc po przecinku (domyślnie 1)
  --help               ta pomoc

Wskazówka: aby pokazać odrzucenie wyniku (F4), ustaw --min/--max szerzej niż przedział serii.`;

function fail(message) {
  console.error(`Błąd: ${message}\n\nUruchom z --help, aby zobaczyć dostępne parametry.`);
  process.exit(2);
}

function number(value, name, { integer = false, min } = {}) {
  const n = Number(value);
  if (value === undefined || value === '' || !Number.isFinite(n) || (integer && !Number.isInteger(n)) || (min !== undefined && n < min)) {
    fail(`parametr --${name} musi być ${integer ? 'liczbą całkowitą' : 'liczbą'}${min !== undefined ? ` ≥ ${min}` : ''} (podano: ${value})`);
  }
  return n;
}

function parseOptions(argv) {
  let values;
  try {
    ({ values } = parseArgs({
      args: argv,
      options: {
        api: { type: 'string' }, key: { type: 'string' }, history: { type: 'boolean', default: false },
        count: { type: 'string' }, interval: { type: 'string' }, mode: { type: 'string', default: 'random' },
        min: { type: 'string', default: '0' }, max: { type: 'string', default: '100' },
        period: { type: 'string', default: '24' }, noise: { type: 'string' }, decimals: { type: 'string', default: '1' },
        help: { type: 'boolean', default: false },
      },
    }));
  }
  catch (e) {
    fail(e.message);
  }
  if (values.help) {
    console.log(HELP);
    process.exit(0);
  }
  const api = values.api ?? process.env.API_URL;
  const key = values.key ?? process.env.SENSOR_API_KEY;
  if (!api) fail('brak adresu API (--api lub zmienna API_URL)');
  if (!key) fail('brak klucza API czujnika (--key lub zmienna SENSOR_API_KEY)');
  if (!['random', 'walk', 'sine'].includes(values.mode)) fail(`nieznany tryb --mode ${values.mode}; dostępne: random, walk, sine`);
  const min = number(values.min, 'min');
  const max = number(values.max, 'max');
  if (min >= max) fail('--min musi być mniejsze od --max');
  return {
    api: api.replace(/\/+$/, ''),
    key,
    history: values.history,
    count: number(values.count ?? '20', 'count', { integer: true, min: values.history ? 1 : 0 }),
    interval: number(values.interval ?? (values.history ? '60' : '5'), 'interval', { min: values.history ? 1 : 0.1 }),
    mode: values.mode,
    min,
    max,
    period: number(values.period, 'period', { min: 2 }),
    noise: values.noise !== undefined ? number(values.noise, 'noise', { min: 0 }) : (max - min) * 0.02,
    decimals: number(values.decimals, 'decimals', { integer: true, min: 0 }),
  };
}

function gaussian() {
  const u = 1 - Math.random();
  const v = Math.random();
  return Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * v);
}

function createValueSource(o) {
  const clamp = (x) => Math.min(o.max, Math.max(o.min, x));
  const round = (x) => Number(x.toFixed(o.decimals));
  let current = (o.min + o.max) / 2;
  return (i) => {
    switch (o.mode) {
      case 'sine': {
        const mid = (o.min + o.max) / 2;
        const amplitude = (o.max - o.min) / 2;
        return round(clamp(mid + amplitude * 0.9 * Math.sin((2 * Math.PI * i) / o.period) + o.noise * gaussian()));
      }
      case 'walk':
        current = clamp(current + o.noise * gaussian());
        return round(current);
      default:
        return round(o.min + Math.random() * (o.max - o.min));
    }
  };
}

function describeProblem(body) {
  if (!body || typeof body !== 'object') return '';
  const details = (body.errors ?? []).map((e) => e.message).filter(Boolean);
  return [body.detail, ...details].filter(Boolean).join('; ');
}

async function send(o, measurement) {
  let res;
  try {
    res = await fetch(`${o.api}/api/measurements`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json', 'X-API-Key': o.key },
      body: JSON.stringify(measurement),
      signal: AbortSignal.timeout(20_000),
    });
  }
  catch (e) {
    return { ok: false, fatal: false, message: `brak połączenia z ${o.api} (${e.cause?.code ?? e.message})` };
  }
  let body = null;
  try { body = await res.json(); } catch { /* odpowiedź bez treści */ }
  if (res.status === 201) return { ok: true, id: body?.id };
  if (res.status === 401) {
    return { ok: false, fatal: true, message: 'ODRZUCONO (401): klucz API jest niepoprawny albo czujnik został wyrejestrowany' };
  }
  if (res.status === 422) {
    return { ok: false, fatal: false, message: `ODRZUCONO (422): ${describeProblem(body) || 'dane niezgodne z regułami serii'}` };
  }
  return { ok: false, fatal: res.status < 500 && res.status !== 429, message: `ODRZUCONO (${res.status}): ${describeProblem(body) || res.statusText}` };
}

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function main() {
  const o = parseOptions(process.argv.slice(2));
  const nextValue = createValueSource(o);
  const stats = { sent: 0, rejected: 0 };
  let stopping = false;
  process.on('SIGINT', () => { stopping = true; });

  const report = (i, measurement, result) => {
    const when = measurement.timestamp ?? 'czas serwera';
    if (result.ok) {
      stats.sent++;
      console.log(`[${i + 1}] ${when}  wartość ${measurement.value}  → zapisano (id ${result.id})`);
    }
    else {
      stats.rejected++;
      console.error(`[${i + 1}] ${when}  wartość ${measurement.value}  → ${result.message}`);
    }
  };

  console.log(`Generator: ${o.history ? `tryb historyczny, liczba wyników: ${o.count}, co ${o.interval} s` : `tryb bieżący, wynik co ${o.interval} s${o.count ? `, liczba wyników: ${o.count}` : ' (Ctrl+C kończy)'}`}; ` +
    `wartości ${o.mode} w [${o.min}, ${o.max}] → ${o.api}`);

  if (o.history) {
    const end = Date.now();
    for (let i = 0; i < o.count && !stopping; i++) {
      const timestamp = new Date(end - (o.count - 1 - i) * o.interval * 1000).toISOString();
      const measurement = { value: nextValue(i), timestamp };
      const result = await send(o, measurement);
      report(i, measurement, result);
      if (result.fatal) break;
    }
  }
  else {
    for (let i = 0; (o.count === 0 || i < o.count) && !stopping; i++) {
      const measurement = { value: nextValue(i) };
      const result = await send(o, measurement);
      report(i, measurement, result);
      if (result.fatal) break;
      if (o.count === 0 || i < o.count - 1) await sleep(o.interval * 1000);
    }
  }

  console.log(`Zakończono: zapisano ${stats.sent}, odrzucono ${stats.rejected}.`);
  process.exit(stats.rejected > 0 ? 1 : 0);
}

main();
