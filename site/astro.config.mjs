import { defineConfig } from 'astro/config';
import react from '@astrojs/react';
import starlight from '@astrojs/starlight';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  site: 'https://redslovesgames.github.io',
  base: '/TIDEBORNE',
  output: 'static',
  integrations: [
    starlight({
      title: 'Tideborne',
      description: 'Official Tideborne field guide and wiki.',
      favicon: '/tideborne-icon.png',
      customCss: ['./src/styles/global.css'],
      pagefind: true,
      sidebar: [
        {
          label: 'Start Here',
          items: [
            { slug: 'getting-started', label: 'Getting Started' },
            { slug: 'fishing-system', label: 'Fishing System 2.0' },
          ],
        },
        {
          label: 'Explore',
          items: [
            { label: 'Fish Explorer', link: '/fish/' },
            { label: 'Gear Builder & Recipes', link: '/gear/' },
            { label: 'FishScore Calculator', link: '/tools/fishscore/' },
          ],
        },
        {
          label: 'Specimens',
          items: [
            { label: 'Specimen System', link: '/specimens/' },
            { slug: 'specimens-and-traits', label: 'Specimens & Traits Reference' },
            { slug: 'fishscore', label: 'FishScore Reference' },
          ],
        },
        {
          label: 'Progression & Records',
          items: [
            { label: "Angler's Satchel", link: '/satchel/' },
            { label: 'Journal, Records & Hall', link: '/records/' },
            { slug: 'team-journal-and-records', label: 'Team Journal Reference' },
            { slug: 'hall-record-display', label: 'Hall Display Reference' },
          ],
        },
        {
          label: 'Reference',
          items: [
            { slug: 'fishing-gear-and-integrations', label: 'Fishing Gear & Integrations' },
            { slug: 'recipes', label: 'Recipes' },
            { slug: 'configuration-and-commands', label: 'Configuration & Commands' },
            { slug: 'updating-and-compatibility', label: 'Updating & Compatibility' },
            { slug: 'faq', label: 'FAQ' },
          ],
        },
      ],
      social: [
        {
          icon: 'github',
          label: 'GitHub',
          href: 'https://github.com/RedsLovesGames/TIDEBORNE',
        },
      ],
    }),
    react(),
  ],
  vite: {
    plugins: [tailwindcss()],
  },
});
