#!/usr/bin/env node
// Dane przykładowe (F11): tworzy przez API 3 serie, rejestruje dla każdej czujnik
// i wprowadza wyniki generatorem (sensor-generator.mjs → POST /api/measurements).
// Serie o istniejącej nazwie są pomijane, więc skrypt można uruchamiać wielokrotnie.
//
//   ADMIN_USERNAME=admin ADMIN_PASSWORD=<hasło> node generator/seed-sample-data.mjs --api http://localhost:8080

import { parseArgs } from 'node:util';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const GENERATOR = path.join(path.dirname(fileURLToPath(import.meta.url)), 'sensor-generator.mjs');

const SAMPLES = [
  {
    series: { name: 'Temperatura', minValue: -30, maxValue: 50, color: '#E4572E', icon: 'thermometer', unit: '°C' },
    generator: ['--mode', 'sine', '--min', '12', '--max', '24'],
  },
  {
    series: { name: 'Wilgotność', minValue: 0, maxValue: 100, color: '#17BEBB', icon: 'droplet', unit: '%' },
    generator: ['--mode', 'sine', '--min', '40', '--max', '70'],
  },
  {
    series: { name: 'Ciśnienie', minValue: 950, maxValue: 1050, color: '#4C6EF5', icon: 'gauge', unit: 'hPa' },
    generator: ['--mode', 'walk', '--min', '1005', '--max', '1020', '--noise', '0.6'],
  },
];

const { values: o } = parseArgs({
  options: {
    api: { type: 'string' }, user: { type: 'string' }, pass: { type: 'string' },
    count: { type: 'string', default: '48' }, interval: { type: 'string', default: '1800' },
  },
});
const api = (o.api ?? process.env.API_URL ?? '').replace(/\/+$/, '');
const username = o.user ?? process.env.ADMIN_USERNAME;
const password = o.pass ?? process.env.ADMIN_PASSWORD;
if (!api || !username || !password) {
  console.error('Użycie: ADMIN_USERNAME=<login> ADMIN_PASSWORD=<hasło> node generator/seed-sample-data.mjs --api <URL> [--count 48] [--interval 1800]');
  process.exit(2);
}

async function call(method, p, { token, body } = {}) {
  const res = await fetch(api + p, {
    method,
    headers: {
      Accept: 'application/json',
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });
  const json = await res.json().catch(() => null);
  if (!res.ok) throw new Error(`${method} ${p}: ${res.status} ${json?.detail ?? res.statusText}`);
  return json;
}

const { accessToken: token } = await call('POST', '/api/auth/login', { body: { username, password } });
const existing = new Set((await call('GET', '/api/series')).map((s) => s.name));

for (const sample of SAMPLES) {
  if (existing.has(sample.series.name)) {
    console.log(`Seria „${sample.series.name}” już istnieje – pomijam.`);
    continue;
  }
  const series = await call('POST', '/api/series', { token, body: sample.series });
  const sensor = await call('POST', '/api/sensors', { token, body: { name: `${sample.series.name} – generator`, seriesId: series.id } });
  console.log(`\nSeria „${series.name}” (id ${series.id}), czujnik id ${sensor.id} – uruchamiam generator:`);
  const run = spawnSync(process.execPath, [GENERATOR, '--api', api, '--history', '--count', o.count, '--interval', o.interval, ...sample.generator], {
    stdio: 'inherit',
    env: { ...process.env, SENSOR_API_KEY: sensor.apiKey },
  });
  if (run.status !== 0) {
    console.error(`Generator zakończył się kodem ${run.status} dla serii „${series.name}”.`);
    process.exitCode = 1;
  }
}

await call('POST', '/api/auth/logout', { token }).catch(() => {});
