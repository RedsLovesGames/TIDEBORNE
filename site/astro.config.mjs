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
