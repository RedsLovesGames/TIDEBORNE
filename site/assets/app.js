const PAGES = [
  { id: 'home', file: 'README.md', title: 'Wiki Home', group: 'Start', icon: '⌂', blurb: 'Overview and guide to the official Tideborne 2.1.0 documentation.' },
  { id: 'getting-started', file: 'GETTING_STARTED.md', title: 'Getting Started', group: 'Start', icon: '→', blurb: 'Requirements, installation, first steps, and what Tideborne changes.' },
  { id: 'fishing-system', file: 'FISHING_SYSTEM.md', title: 'Fishing System 2.0', group: 'Core Systems', icon: '≈', blurb: 'Catch pipeline, Fishing Luck, Perfect Catch, Strength, and Tempo.' },
  { id: 'specimens', file: 'SPECIMENS_AND_TRAITS.md', title: 'Specimens & Traits', group: 'Core Systems', icon: '◆', blurb: 'Body Type, Condition, Pigmentation, quality, Trait Luck, and Momentum.' },
  { id: 'fishscore', file: 'FISHSCORE.md', title: 'FishScore', group: 'Core Systems', icon: '★', blurb: 'Exact 1–3000 scoring formula, point values, and interactive calculator.' },
  { id: 'satchel', file: 'ANGLERS_SATCHEL.md', title: "Angler's Satchel", group: 'Progression', icon: '▣', blurb: 'Crafting, conversion, upgrades, sorting, Auto-Stow, and Trophy Lock.' },
  { id: 'team-journal', file: 'TEAM_JOURNAL_AND_RECORDS.md', title: 'Team Journal & Records', group: 'Progression', icon: '♟', blurb: 'FTB Teams sharing, leaderboards, history, ownership, and commands.' },
  { id: 'hall-display', file: 'HALL_RECORD_DISPLAY.md', title: 'Hall Record Display', group: 'Progression', icon: '▤', blurb: 'Personal and team fishing history displayed inside the world.' },
  { id: 'gear', file: 'FISHING_GEAR_AND_INTEGRATIONS.md', title: 'Gear & Integrations', group: 'Reference', icon: '⚓', blurb: 'Fishing gear plus Myths of the Sea and Apex Waters integration.' },
  { id: 'recipes', file: 'RECIPES.md', title: 'Recipes', group: 'Reference', icon: '▦', blurb: 'Crafting recipes verified from the Tideborne 2.1.0 release JAR.' },
  { id: 'configuration', file: 'CONFIGURATION_AND_COMMANDS.md', title: 'Configuration & Commands', group: 'Reference', icon: '⚙', blurb: 'Server configuration, player commands, and operator tools.' },
  { id: 'updating', file: 'UPDATING_AND_COMPATIBILITY.md', title: 'Updating & Compatibility', group: 'Reference', icon: '↻', blurb: 'Safely migrate from older modular Tideborne releases.' },
  { id: 'faq', file: 'FAQ.md', title: 'FAQ', group: 'Reference', icon: '?', blurb: 'Fast answers to common Tideborne questions.' }
];

const pageContent = document.getElementById('pageContent');
const wikiNav = document.getElementById('wikiNav');
const sidebar = document.getElementById('sidebar');
const mobileMenu = document.getElementById('mobileMenu');
const searchButton = document.getElementById('searchButton');
const searchDialog = document.getElementById('searchDialog');
const searchInput = document.getElementById('searchInput');
const searchResults = document.getElementById('searchResults');

const pageCache = new Map();
let searchIndex = [];

marked.setOptions({ gfm: true, breaks: false });

function renderNav() {
  const groups = [...new Set(PAGES.map(page => page.group))];
  wikiNav.innerHTML = groups.map(group => `
    <section class="nav-group">
      <div class="nav-group-title">${escapeHtml(group)}</div>
      ${PAGES.filter(page => page.group === group).map(page => `
        <a class="nav-item" data-page="${page.id}" href="#/${page.id}">
          <span class="nav-icon" aria-hidden="true">${page.icon}</span>
          <span>${escapeHtml(page.title)}</span>
        </a>`).join('')}
    </section>`).join('');
}

function routeId() {
  const id = location.hash.replace(/^#\/?/, '').split('?')[0];
  return PAGES.some(page => page.id === id) ? id : 'home';
}

async function loadMarkdown(page) {
  if (pageCache.has(page.id)) return pageCache.get(page.id);
  const response = await fetch(`content/${page.file}`, { cache: 'no-cache' });
  if (!response.ok) throw new Error(`Could not load ${page.file} (${response.status})`);
  const text = await response.text();
  pageCache.set(page.id, text);
  return text;
}

function rewriteWikiLinks(html) {
  const wrapper = document.createElement('div');
  wrapper.innerHTML = html;
  wrapper.querySelectorAll('a[href]').forEach(link => {
    const href = link.getAttribute('href');
    const fileMatch = PAGES.find(page => page.file.toLowerCase() === href?.split('#')[0].toLowerCase());
    if (fileMatch) {
      const anchor = href.includes('#') ? `?anchor=${encodeURIComponent(href.split('#')[1])}` : '';
      link.setAttribute('href', `#/${fileMatch.id}${anchor}`);
    } else if (/^https?:\/\//i.test(href)) {
      link.setAttribute('target', '_blank');
      link.setAttribute('rel', 'noreferrer');
    }
  });
  return wrapper.innerHTML;
}

function stripWikiPager(markdown) {
  return markdown.replace(/^\[←[^\n]+\n\n/, '');
}

function slugifyHeading(text) {
  return text.toLowerCase().trim().replace(/[^a-z0-9\s-]/g, '').replace(/\s+/g, '-').replace(/-+/g, '-');
}

function applyHeadingIds() {
  const seen = new Map();
  pageContent.querySelectorAll('.doc h1, .doc h2, .doc h3, .doc h4, .doc h5, .doc h6').forEach(heading => {
    const base = slugifyHeading(heading.textContent) || 'section';
    const count = seen.get(base) || 0;
    seen.set(base, count + 1);
    heading.id = count ? `${base}-${count + 1}` : base;
  });
}

function homeShell(rendered) {
  const features = PAGES.filter(page => ['fishing-system', 'specimens', 'fishscore', 'satchel', 'team-journal', 'gear'].includes(page.id));
  return `
    <section class="hero">
      <div class="hero-mark"><img src="assets/tideborne-icon.png" alt=""><span>Tide 2 fishing, expanded</span></div>
      <h1>Every catch becomes a specimen worth remembering.</h1>
      <p>Tideborne adds persistent fish traits, FishScore, long-term collection progression, team records, trophy displays, and optional ocean integrations to Tide 2.</p>
      <div class="hero-badges">
        <span class="badge">Tideborne 2.1.0</span><span class="badge">Minecraft 1.21.1</span><span class="badge">Fabric</span><span class="badge">Tide 2.1.1</span><span class="badge">Client + Server</span>
      </div>
    </section>
    <section class="feature-grid" aria-label="Featured documentation">
      ${features.map(page => `<a class="feature-card" href="#/${page.id}"><span class="feature-icon">${page.icon}</span><b>${escapeHtml(page.title)}</b><span>${escapeHtml(page.blurb)}</span></a>`).join('')}
    </section>
    <article class="doc">${rendered}</article>`;
}

function fishScoreCalculator() {
  return `
  <section class="fishscore-tool" id="fishscore-calculator">
    <h2>FishScore Calculator</h2>
    <p>Uses the exact Tideborne 2.1.0 FishScore V2 public formula. Enter the specimen's final percentile, not its original natural percentile.</p>
    <div class="score-form">
      <div class="field"><label for="scoreRarity">Species rarity</label><select id="scoreRarity"><option value="50">1 star</option><option value="100">2 stars</option><option value="175">3 stars</option><option value="250">4 stars</option><option value="350">5 stars</option></select></div>
      <div class="field"><label for="scorePercentile">Final percentile (0–100)</label><input id="scorePercentile" type="number" min="0" max="100" step="0.1" value="50"></div>
      <div class="field"><label for="scoreBody">Body Type</label><select id="scoreBody"><option value="0">Normal</option><option value="40">Giant</option><option value="40">Dwarf</option></select></div>
      <div class="field"><label for="scoreCondition">Condition</label><select id="scoreCondition"><option value="0">Normal</option><option value="20">Scarred</option><option value="35">Parasite-Ridden</option></select></div>
      <div class="field"><label for="scorePigment">Pigmentation</label><select id="scorePigment"><option value="0">Normal</option><option value="70">Albino</option><option value="100">Iridescent</option></select></div>
      <div class="field"><label for="scoreQuality">Specimen Quality</label><select id="scoreQuality"><option value="0">Normal</option><option value="100">Perfect Specimen</option></select></div>
      <div class="score-result"><div><div class="score-meta" id="scoreRaw">Raw score: 200</div><div class="score-number" id="scoreNumber">515</div></div><div class="score-meta">Public range<br><strong>1–3000</strong></div></div>
    </div>
  </section>`;
}

function initFishScoreCalculator() {
  const ids = ['scoreRarity', 'scorePercentile', 'scoreBody', 'scoreCondition', 'scorePigment', 'scoreQuality'];
  if (!document.getElementById('scoreRarity')) return;
  const update = () => {
    const rarity = Number(document.getElementById('scoreRarity').value);
    const percentile = Math.max(0, Math.min(100, Number(document.getElementById('scorePercentile').value) || 0));
    const body = Number(document.getElementById('scoreBody').value);
    const condition = Number(document.getElementById('scoreCondition').value);
    const pigment = Number(document.getElementById('scorePigment').value);
    const quality = Number(document.getElementById('scoreQuality').value);
    const raw = rarity + 3 * percentile + body + condition + pigment + quality;
    const score = Math.max(1, Math.min(3000, Math.round(1 + 2999 * ((raw - 50) / 875))));
    document.getElementById('scoreRaw').textContent = `Raw score: ${Number.isInteger(raw) ? raw : raw.toFixed(1)}`;
    document.getElementById('scoreNumber').textContent = score.toLocaleString();
  };
  ids.forEach(id => document.getElementById(id).addEventListener('input', update));
  update();
}

async function renderPage() {
  const id = routeId();
  const page = PAGES.find(item => item.id === id);
  document.querySelectorAll('.nav-item').forEach(item => item.classList.toggle('active', item.dataset.page === id));
  document.title = page.id === 'home' ? 'Tideborne Wiki' : `${page.title} · Tideborne Wiki`;
  pageContent.innerHTML = '<div class="loading">Loading Tideborne documentation…</div>';
  sidebar.classList.remove('open');
  try {
    const markdown = stripWikiPager(await loadMarkdown(page));
    let html = rewriteWikiLinks(marked.parse(markdown));
    if (page.id === 'home') {
      pageContent.innerHTML = homeShell(html);
    } else {
      if (page.id === 'fishscore') html = fishScoreCalculator() + html;
      pageContent.innerHTML = `<div class="breadcrumb"><a href="#/home">Tideborne Wiki</a> / ${escapeHtml(page.title)}</div><article class="doc">${html}</article>`;
    }
    applyHeadingIds();
    initFishScoreCalculator();
    const anchor = new URLSearchParams(location.hash.split('?')[1] || '').get('anchor');
    if (anchor) requestAnimationFrame(() => document.getElementById(anchor)?.scrollIntoView());
    else window.scrollTo({ top: 0, behavior: 'auto' });
    document.getElementById('main').focus({ preventScroll: true });
  } catch (error) {
    pageContent.innerHTML = `<div class="error-card"><h1>Documentation unavailable</h1><p>${escapeHtml(error.message)}</p><p><a href="https://github.com/RedsLovesGames/TIDEBORNE/tree/dev/docs/wiki">Open the Markdown wiki on GitHub</a>.</p></div>`;
  }
}

function plainText(markdown) {
  return markdown
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')
    .replace(/[#>*_|~-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

async function buildSearchIndex() {
  if (searchIndex.length) return;
  const rows = await Promise.all(PAGES.map(async page => {
    try {
      const markdown = await loadMarkdown(page);
      return { ...page, text: plainText(markdown).toLowerCase() };
    } catch {
      return { ...page, text: '' };
    }
  }));
  searchIndex = rows;
}

function searchWiki(query) {
  const terms = query.toLowerCase().trim().split(/\s+/).filter(Boolean);
  if (!terms.length) return [];
  return searchIndex.map(page => {
    const title = page.title.toLowerCase();
    let score = 0;
    terms.forEach(term => {
      if (title.includes(term)) score += 12;
      const count = page.text.split(term).length - 1;
      score += Math.min(10, count);
    });
    return { page, score };
  }).filter(row => row.score > 0).sort((a, b) => b.score - a.score).slice(0, 8);
}

function renderSearchResults() {
  const query = searchInput.value;
  if (!query.trim()) {
    searchResults.innerHTML = '<div class="search-empty">Search across all 13 wiki pages.</div>';
    return;
  }
  const results = searchWiki(query);
  searchResults.innerHTML = results.length ? results.map(({ page }) => `
    <a class="search-result" href="#/${page.id}" data-search-page="${page.id}"><b>${escapeHtml(page.title)}</b><span>${escapeHtml(page.blurb)}</span></a>`).join('') : '<div class="search-empty">No matching wiki page found.</div>';
}

async function openSearch() {
  await buildSearchIndex();
  if (!searchDialog.open) searchDialog.showModal();
  searchInput.focus();
  searchInput.select();
  renderSearchResults();
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, char => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#039;', '"':'&quot;' }[char]));
}

renderNav();
window.addEventListener('hashchange', renderPage);
mobileMenu.addEventListener('click', () => sidebar.classList.toggle('open'));
searchButton.addEventListener('click', openSearch);
searchInput.addEventListener('input', renderSearchResults);
searchResults.addEventListener('click', event => { if (event.target.closest('a')) searchDialog.close(); });
document.addEventListener('keydown', event => {
  if (event.key === '/' && !['INPUT', 'SELECT', 'TEXTAREA'].includes(document.activeElement.tagName)) { event.preventDefault(); openSearch(); }
  if (event.key === 'Escape' && sidebar.classList.contains('open')) sidebar.classList.remove('open');
});

renderPage();
buildSearchIndex();
