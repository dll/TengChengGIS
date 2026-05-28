import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/frontend/setup.js'],
    include: ['src/test/frontend/**/*.test.js'],
  },
});
