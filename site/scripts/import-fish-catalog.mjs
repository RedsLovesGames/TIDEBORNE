import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { gunzipSync } from 'node:zlib';
import { normalizeCatalog } from './normalize-fish-data.mjs';

export const SOURCE_REVISION = '97f217b1931e47e66a133a7527ad6a5c4d2d074f';
const SOURCE_ROOT = `https://raw.githubusercontent.com/RedsLovesGames/random-info-pages/${SOURCE_REVISION}/tideborne/assets/fish`;

const urls = {
  first: `${SOURCE_ROOT}/fish-wiki-data-0.json.gz`,
  second: `${SOURCE_ROOT}/fish-wiki-data-1.json.gz`,
  scope: `${SOURCE_ROOT}/modpack-scope.json`,
  manifest: `${SOURCE_ROOT}/fish-render-manifest.json`,
};

async function fetchRequired(url) {
  const response = await fetch(url, { headers: { 'user-agent': 'Tideborne-Field-Guide-Importer/1.0' } });
  if (!response.ok) throw new Error(`${url}: HTTP ${response.status}`);
  return response;
}

async function fetchJson(url) {
  return (await fetchRequired(url)).json();
}

async function fetchGzipJson(url) {
  const response = await fetchRequired(url);
  const compressed = Buffer.from(await response.arrayBuffer());
  return JSON.parse(gunzipSync(compressed).toString('utf8'));
}

export async function importFishCatalog({ outputFile = resolve('src/data/generated/fish.json') } = {}) {
  const [first, second, scope, manifest] = await Promise.all([
    fetchGzipJson(urls.first),
    fetchGzipJson(urls.second),
    fetchJson(urls.scope),
    fetchJson(urls.manifest),
  ]);

  const catalog = normalizeCatalog({ first, second, scope, manifest, sourceRevision: SOURCE_REVISION });
  await mkdir(dirname(outputFile), { recursive: true });
  await writeFile(outputFile, `${JSON.stringify(catalog, null, 2)}\n`, 'utf8');
  return catalog;
}

if (import.meta.url === `file://${process.argv[1]}`) {
  const catalog = await importFishCatalog();
  console.log(`Imported ${catalog.records.length} fish from ${SOURCE_REVISION}.`);
}
