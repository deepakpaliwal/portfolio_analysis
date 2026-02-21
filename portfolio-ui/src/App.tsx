import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAppSelector } from "./store/store";
import Layout from "./components/Layout";
import Dashboard from "./pages/Dashboard";
import Portfolio from "./pages/Portfolio";
import Screener from "./pages/Screener";
import RiskAnalytics from "./pages/RiskAnalytics";
import Trading from "./pages/Trading";
import TradingAdvisor from "./pages/TradingAdvisor";
import BatchManagement from "./pages/BatchManagement";
import CorrelationAnalysis from "./pages/CorrelationAnalysis";
import AdminPanel from "./pages/AdminPanel";
import Login from "./pages/Login";
import Register from "./pages/Register";
import NotFound from "./pages/NotFound";
import Home from "./pages/Home";

/**
 * ProtectedRoute wrapper — redirects unauthenticated users to /login.
 * In a production app this would also check token expiry and roles.
 */
interface ProtectedRouteProps {
  children: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated } = useAppSelector((state) => state.auth);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

/**
 * App component — defines the top-level route structure.
 *
 * Routes:
 *  /login        — Public login page
 *  /             — Redirects to /dashboard
 *  /dashboard    — Portfolio overview with key metrics and charts
 *  /portfolio    — Detailed portfolio holdings and allocation
 *  /screener     — Stock / asset screener with filters
 *  /risk         — Risk analytics (VaR, drawdown, stress tests)
 *  /trading      — Order entry and execution management
 *  /admin        — Admin panel for user and system management
 *  *             — 404 fallback
 */
const App: React.FC = () => {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Protected routes wrapped in the sidebar layout */}
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="portfolio" element={<Portfolio />} />
        <Route path="screener" element={<Screener />} />
        <Route path="risk" element={<RiskAnalytics />} />
        <Route path="trading" element={<Trading />} />
        <Route path="advisor" element={<TradingAdvisor />} />
        <Route path="batch" element={<BatchManagement />} />
        <Route path="correlation" element={<CorrelationAnalysis />} />
        <Route path="admin" element={<AdminPanel />} />
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default App;
