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
      favicon: '/TIDEBORNE/tideborne-icon.png',
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
            { label: 'Fish Explorer', link: '/TIDEBORNE/fish/' },
            { label: 'Gear Builder & Recipes', link: '/TIDEBORNE/gear/' },
            { label: 'FishScore Calculator', link: '/TIDEBORNE/tools/fishscore/' },
          ],
        },
        {
          label: 'Specimens',
          items: [
            { slug: 'specimens-and-traits', label: 'Specimens & Traits' },
            { slug: 'fishscore', label: 'FishScore' },
          ],
        },
        {
          label: 'Progression & Records',
          items: [
            { label: "Angler's Satchel", link: '/TIDEBORNE/satchel/' },
            { slug: 'team-journal-and-records', label: 'Team Journal & Records' },
            { slug: 'hall-record-display', label: 'Hall Record Display' },
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
