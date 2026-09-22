import { access, mkdir, readFile, writeFile } from 'node:fs/promises';
import { constants } from 'node:fs';
import { basename, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const SOURCE_REPO = 'https://raw.githubusercontent.com/RedsLovesGames/random-info-pages';
const RENDER_PREFIX = '/fish/renders/';
const DEFAULT_CONCURRENCY = 16;

function renderFilename(path) {
  if (typeof path !== 'string' || !path.startsWith(RENDER_PREFIX)) return null;
  const filename = path.slice(RENDER_PREFIX.length);
  if (!filename || basename(filename) !== filename) return null;
  if (!/^[A-Za-z0-9._-]+\.png$/i.test(filename)) return null;
  return filename;
}

export function collectRenderFiles(catalog) {
  const files = new Set();

  for (const record of catalog?.records ?? []) {
    const candidates = [record?.render, ...Object.values(record?.variants ?? {})];
    for (const candidate of candidates) {
      const filename = renderFilename(candidate);
      if (filename) files.add(filename);
    }
  }

  return [...files];
}

async function exists(path) {
  try {
    await access(path, constants.F_OK);
    return true;
  } catch {
    return false;
  }
}

async function downloadRender({ filename, sourceRevision, outputDir, fetchImpl }) {
  const destination = resolve(outputDir, filename);
  if (await exists(destination)) return 'reused';

  const url = `${SOURCE_REPO}/${encodeURIComponent(sourceRevision)}/tideborne/assets/fish/renders/${encodeURIComponent(filename)}`;
  const response = await fetchImpl(url, {
    headers: { 'user-agent': 'Tideborne-Field-Guide-Render-Sync/1.0' },
  });
  if (!response.ok) throw new Error(`${url}: HTTP ${response.status}`);

  const bytes = Buffer.from(await response.arrayBuffer());
  await writeFile(destination, bytes);
  return 'downloaded';
}

async function mapConcurrent(items, concurrency, worker) {
  let cursor = 0;
  const results = new Array(items.length);
  const workerCount = Math.max(1, Math.min(concurrency, items.length || 1));

  async function run() {
    while (true) {
      const index = cursor++;
      if (index >= items.length) return;
      results[index] = await worker(items[index], index);
    }
  }

  await Promise.all(Array.from({ length: workerCount }, () => run()));
  return results;
}

export async function syncFishRenders({
  catalogFile = resolve('src/data/generated/fish.json'),
  outputDir = resolve('public/fish/renders'),
  fetchImpl = fetch,
  concurrency = DEFAULT_CONCURRENCY,
} = {}) {
  const catalog = JSON.parse(await readFile(catalogFile, 'utf8'));
  const sourceRevision = catalog?.meta?.sourceRevision;
  if (!sourceRevision || !/^[0-9a-f]{7,64}$/i.test(sourceRevision)) {
    throw new Error('Fish catalog is missing a valid pinned sourceRevision.');
  }

  const files = collectRenderFiles(catalog);
  await mkdir(outputDir, { recursive: true });

  const states = await mapConcurrent(files, concurrency, (filename) => downloadRender({
    filename,
    sourceRevision,
    outputDir,
    fetchImpl,
  }));

  return {
    total: files.length,
    downloaded: states.filter((state) => state === 'downloaded').length,
    reused: states.filter((state) => state === 'reused').length,
  };
}

async function main() {
  const result = await syncFishRenders();
  console.log(`Fish render sync complete: ${result.total} total, ${result.downloaded} downloaded, ${result.reused} reused.`);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  await main();
}
