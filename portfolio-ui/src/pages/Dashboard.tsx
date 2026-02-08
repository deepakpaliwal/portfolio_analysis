import React from 'react';
import { useAppSelector } from '../store/store';

/**
 * Dashboard page — displays portfolio overview with key metrics and charts.
 *
 * Planned features:
 * - Total portfolio value with daily change
 * - Asset allocation pie chart (by asset type and sector)
 * - Performance line chart (1D, 1W, 1M, 3M, 1Y, ALL)
 * - Top gainers/losers widgets
 * - Recent transactions feed
 * - WebSocket-driven real-time price ticker
 */
const Dashboard: React.FC = () => {
  const user = useAppSelector((state) => state.auth.user);

  return (
    <div>
      <h1>Dashboard</h1>
      <p>Welcome back, {user?.firstName}!</p>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem', marginTop: '1.5rem' }}>
        {/* Summary Cards */}
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ color: '#888', fontSize: '0.85rem', margin: 0 }}>Total Portfolio Value</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.5rem 0' }}>$--,---</p>
          <span style={{ color: '#4caf50', fontSize: '0.85rem' }}>+0.00% today</span>
        </div>
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ color: '#888', fontSize: '0.85rem', margin: 0 }}>Total Gain/Loss</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.5rem 0' }}>$--,---</p>
          <span style={{ color: '#888', fontSize: '0.85rem' }}>All time</span>
        </div>
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ color: '#888', fontSize: '0.85rem', margin: 0 }}>Holdings</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.5rem 0' }}>--</p>
          <span style={{ color: '#888', fontSize: '0.85rem' }}>Across all portfolios</span>
        </div>
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ color: '#888', fontSize: '0.85rem', margin: 0 }}>Dividend Income (YTD)</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.5rem 0' }}>$--,---</p>
          <span style={{ color: '#888', fontSize: '0.85rem' }}>Year to date</span>
        </div>
      </div>

      {/* Placeholder chart area */}
      <div style={{ marginTop: '2rem', background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)', minHeight: 300 }}>
        <h3>Portfolio Performance</h3>
        <p style={{ color: '#888' }}>Performance chart will be rendered here using Recharts.</p>
      </div>
    </div>
  );
};

export default Dashboard;
