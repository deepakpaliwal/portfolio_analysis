import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import type { AxiosResponse } from "axios";

/**
 * Axios instance pre-configured for the portfolio analysis API.
 *
 * Base URL is read from the VITE_API_BASE_URL environment variable,
 * falling back to "/api" which is proxied by Vite dev server to port 8080.
 *
 * Features:
 *  - Automatic JWT Bearer token injection via request interceptor
 *  - 401 response interceptor that clears stored credentials and
 *    redirects to the login page when the token is expired or invalid
 *  - Sensible timeout of 15 seconds for all requests
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15_000,
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * Request interceptor — attaches the JWT access token (if present)
 * to every outgoing request as an Authorization: Bearer header.
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig): InternalAxiosRequestConfig => {
    const token = localStorage.getItem("token");

    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor — handles authentication failures globally.
 *
 * When a 401 Unauthorized response is received:
 *  1. Clears the stored access token
 *  2. Redirects the user to /login
 *
 * TODO: Implement refresh-token rotation flow so users are not
 *       logged out when their short-lived access token expires.
 */
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");

      // Only redirect if we are not already on the login page
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
