import { configureStore } from "@reduxjs/toolkit";
import { useDispatch, useSelector } from "react-redux";
import type { TypedUseSelectorHook } from "react-redux";
import authReducer from "./slices/authSlice";
import portfolioReducer from "./slices/portfolioSlice";

/**
 * Central Redux store for the portfolio analysis application.
 *
 * Slices:
 *  - auth:      Authentication state (user info, tokens, login status)
 *  - portfolio:  Portfolio holdings, allocations, and performance data
 *
 * TODO: Add additional slices as features grow:
 *  - screener:   Saved screener filters and results
 *  - trading:    Open orders, execution history
 *  - risk:       Cached risk metrics and scenario parameters
 *  - ui:         Theme, sidebar collapse state, notification queue
 */
export const store = configureStore({
  reducer: {
    auth: authReducer,
    portfolio: portfolioReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // Ignore non-serializable values in specific action paths if needed
        ignoredActions: [],
        ignoredPaths: [],
      },
    }),
  devTools: import.meta.env.DEV,
});

/** Inferred type of the full Redux state tree */
export type RootState = ReturnType<typeof store.getState>;

/** Inferred type of the store's dispatch function */
export type AppDispatch = typeof store.dispatch;

/**
 * Typed hooks — use these throughout the app instead of plain
 * `useDispatch` and `useSelector` to get full type inference.
 */
export const useAppDispatch: () => AppDispatch = useDispatch;
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
