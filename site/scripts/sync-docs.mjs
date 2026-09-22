import { mkdir, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { dirname, extname, join, resolve } from 'node:path';

const here = dirname(fileURLToPath(import.meta.url));

function toSlug(filename) {
  if (filename.toLowerCase() === 'readme.md') return 'wiki-home.md';
  const stem = filename.slice(0, -extname(filename).length);
  return `${stem
    .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
    .replace(/[_\s]+/g, '-')
    .replace(/[^a-zA-Z0-9-]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
    .toLowerCase()}.md`;
}

function deriveTitle(markdown, filename) {
  const heading = markdown.match(/^#\s+(.+?)\s*$/m)?.[1]?.trim();
  if (heading) return heading;

  const stem = filename.slice(0, -extname(filename).length);
  return stem
    .replace(/[_-]+/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function yamlTitle(title) {
  return /^[A-Za-z0-9 .&'()/-]+$/.test(title) ? title : JSON.stringify(title);
}

function rewriteLocalMarkdownLinks(markdown, filenameMap) {
  return markdown.replace(/\]\(([^)]+)\)/g, (match, destination) => {
    const trimmed = destination.trim();
    if (!trimmed || trimmed.startsWith('#') || trimmed.startsWith('/') || /^[a-z][a-z0-9+.-]*:/i.test(trimmed)) {
      return match;
    }

    const fragmentIndex = trimmed.indexOf('#');
    const queryIndex = trimmed.indexOf('?');
    const suffixIndex = [fragmentIndex, queryIndex].filter((index) => index >= 0).sort((a, b) => a - b)[0] ?? -1;
    const filePart = suffixIndex >= 0 ? trimmed.slice(0, suffixIndex) : trimmed;
    const suffix = suffixIndex >= 0 ? trimmed.slice(suffixIndex) : '';

    if (!filePart.toLowerCase().endsWith('.md') || filePart.includes('/')) return match;

    const outputName = filenameMap.get(filePart.toLowerCase());
    return outputName ? `](${outputName}${suffix})` : match;
  });
}

function withFrontmatter(markdown, filename) {
  if (/^\uFEFF?---\s*\r?\n/.test(markdown)) return markdown;
  const title = deriveTitle(markdown, filename);
  return `---\ntitle: ${yamlTitle(title)}\n---\n\n${markdown}`;
}

export async function syncDocs({ sourceDir, outputDir }) {
  await rm(outputDir, { recursive: true, force: true });
  await mkdir(outputDir, { recursive: true });

  const entries = (await readdir(sourceDir, { withFileTypes: true }))
    .filter((entry) => entry.isFile() && entry.name.toLowerCase().endsWith('.md'))
    .sort((a, b) => a.name.localeCompare(b.name));
  const filenameMap = new Map(entries.map((entry) => [entry.name.toLowerCase(), toSlug(entry.name)]));

  for (const entry of entries) {
    const sourcePath = join(sourceDir, entry.name);
    const outputPath = join(outputDir, toSlug(entry.name));
    const markdown = await readFile(sourcePath, 'utf8');
    const rewritten = rewriteLocalMarkdownLinks(markdown, filenameMap);
    await writeFile(outputPath, withFrontmatter(rewritten, entry.name), 'utf8');
  }
}

async function main() {
  const sourceDir = resolve(here, '../../docs/wiki');
  const outputDir = resolve(here, '../src/content/docs');
  await syncDocs({ sourceDir, outputDir });
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  await main();
}
