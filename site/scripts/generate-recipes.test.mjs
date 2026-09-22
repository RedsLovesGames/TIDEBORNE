import { mkdtemp, mkdir, readFile, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';
import { generateRecipes } from './generate-recipes.mjs';

describe('generateRecipes', () => {
  it('extracts shaped and shapeless recipes plus optional-mod requirements', async () => {
    const root = await mkdtemp(join(tmpdir(), 'tideborne-recipes-'));
    const compatibility = join(root, 'data/tidebound_compatibility/recipe');
    const traits = join(root, 'data/tide_traits/recipe');
    const output = join(root, 'generated/recipes.json');
    await mkdir(compatibility, { recursive: true });
    await mkdir(traits, { recursive: true });

    await writeFile(join(compatibility, 'leader.json'), JSON.stringify({
      'fabric:load_conditions': [{ condition: 'fabric:all_mods_loaded', values: ['apexwaters'] }],
      type: 'minecraft:crafting_shaped', pattern: [' A ', 'ABA', ' A '],
      key: { A: { item: 'minecraft:copper_ingot' }, B: { item: 'minecraft:chain' } },
      result: { id: 'tidebound_compatibility:copper_leader', count: 1 },
    }));
    await writeFile(join(traits, 'satchel.json'), JSON.stringify({
      type: 'minecraft:crafting_shapeless', ingredients: [{ item: 'minecraft:leather' }],
      result: { id: 'tide_traits:anglers_satchel', count: 1 },
    }));

    const result = await generateRecipes({ resourceRoot: root, outputFile: output });
    expect(result.recipes).toHaveLength(2);
    expect(result.recipes[0]).toMatchObject({
      result: { id: 'tide_traits:anglers_satchel', count: 1 }, type: 'shapeless', requiredMods: [],
    });
    expect(result.recipes[1]).toMatchObject({
      result: { id: 'tidebound_compatibility:copper_leader', count: 1 }, type: 'shaped', requiredMods: ['apexwaters'],
    });
    expect(JSON.parse(await readFile(output, 'utf8'))).toEqual(result);
  });
});
