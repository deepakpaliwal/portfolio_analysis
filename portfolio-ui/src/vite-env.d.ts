/// <reference types="vite/client" />

/**
 * Type declarations for Vite environment variables.
 * All custom env vars must be prefixed with VITE_ to be exposed to client code.
 */
interface ImportMetaEnv {
  /** Base URL for the backend API (e.g. http://localhost:8080) */
  readonly VITE_API_BASE_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
