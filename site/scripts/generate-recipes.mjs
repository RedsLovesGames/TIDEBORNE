import { mkdir, readFile, readdir, writeFile } from 'node:fs/promises';
import { dirname, join, relative, resolve } from 'node:path';

const recipeNamespaces = ['tide_traits', 'tidebound_compatibility'];

const requiredModsFrom = (recipe) => {
  const conditions = Array.isArray(recipe['fabric:load_conditions']) ? recipe['fabric:load_conditions'] : [];
  const values = conditions
    .filter((condition) => condition?.condition === 'fabric:all_mods_loaded' && Array.isArray(condition.values))
    .flatMap((condition) => condition.values)
    .filter((value) => typeof value === 'string');
  return [...new Set(values)].sort();
};

const ingredient = (value) => {
  if (!value || typeof value !== 'object') return null;
  if (typeof value.item === 'string') return { type: 'item', id: value.item };
  if (typeof value.tag === 'string') return { type: 'tag', id: value.tag };
  return null;
};

const normalizeRecipe = (recipe, sourcePath) => {
  const shaped = recipe.type === 'minecraft:crafting_shaped';
  const shapeless = recipe.type === 'minecraft:crafting_shapeless';
  if (!shaped && !shapeless) return null;
  if (!recipe.result?.id) return null;

  const output = {
    sourcePath,
    type: shaped ? 'shaped' : 'shapeless',
    category: typeof recipe.category === 'string' ? recipe.category : null,
    requiredMods: requiredModsFrom(recipe),
    result: {
      id: recipe.result.id,
      count: Number.isFinite(Number(recipe.result.count)) ? Number(recipe.result.count) : 1,
    },
  };

  if (shaped) {
    output.pattern = Array.isArray(recipe.pattern) ? recipe.pattern.map(String) : [];
    output.key = Object.fromEntries(
      Object.entries(recipe.key ?? {})
        .map(([symbol, value]) => [symbol, ingredient(value)])
        .filter(([, value]) => value),
    );
  } else {
    output.ingredients = (Array.isArray(recipe.ingredients) ? recipe.ingredients : [])
      .map(ingredient)
      .filter(Boolean);
  }

  return output;
};

export async function generateRecipes({
  resourceRoot = resolve('../src/main/resources'),
  outputFile = resolve('src/data/generated/recipes.json'),
} = {}) {
  const recipes = [];

  for (const namespace of recipeNamespaces) {
    const recipeDir = join(resourceRoot, 'data', namespace, 'recipe');
    let files = [];
    try {
      files = await readdir(recipeDir, { withFileTypes: true });
    } catch (error) {
      if (error?.code === 'ENOENT') continue;
      throw error;
    }

    for (const file of files.filter((entry) => entry.isFile() && entry.name.endsWith('.json')).sort((a, b) => a.name.localeCompare(b.name))) {
      const path = join(recipeDir, file.name);
      const raw = JSON.parse(await readFile(path, 'utf8'));
      const normalized = normalizeRecipe(raw, relative(resourceRoot, path).replaceAll('\\', '/'));
      if (normalized) recipes.push(normalized);
    }
  }

  recipes.sort((a, b) => a.result.id.localeCompare(b.result.id));
  const result = { schemaVersion: 1, recipeCount: recipes.length, recipes };
  await mkdir(dirname(outputFile), { recursive: true });
  await writeFile(outputFile, `${JSON.stringify(result, null, 2)}\n`, 'utf8');
  return result;
}

if (import.meta.url === `file://${process.argv[1]}`) {
  const result = await generateRecipes();
  console.log(`Generated ${result.recipeCount} Tideborne recipes.`);
}
