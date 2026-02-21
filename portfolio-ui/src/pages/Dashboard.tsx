import React from 'react';
import { useAppSelector } from '../store/store';

const cardStyle: React.CSSProperties = {
  background: '#ffffff',
  padding: '1.25rem',
  borderRadius: 14,
  boxShadow: '0 10px 24px rgba(15,23,42,0.08)',
  border: '1px solid #E2E8F0',
};

const Dashboard: React.FC = () => {
  const user = useAppSelector((state) => state.auth.user);

  return (
    <div>
      <h1 style={{ marginBottom: 6, color: '#0F172A' }}>Dashboard</h1>
      <p style={{ color: '#475569', marginTop: 0 }}>Welcome back, {user?.firstName}! Here’s your portfolio snapshot.</p>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem', marginTop: '1.25rem' }}>
        <div style={cardStyle}>
          <h3 style={{ color: '#64748B', fontSize: '0.85rem', margin: 0 }}>Total Portfolio Value</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.55rem 0' }}>$--,---</p>
          <span style={{ color: '#16A34A', fontSize: '0.85rem' }}>+0.00% today</span>
        </div>
        <div style={cardStyle}>
          <h3 style={{ color: '#64748B', fontSize: '0.85rem', margin: 0 }}>Total Gain/Loss</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.55rem 0' }}>$--,---</p>
          <span style={{ color: '#64748B', fontSize: '0.85rem' }}>All time</span>
        </div>
        <div style={cardStyle}>
          <h3 style={{ color: '#64748B', fontSize: '0.85rem', margin: 0 }}>Holdings</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.55rem 0' }}>--</p>
          <span style={{ color: '#64748B', fontSize: '0.85rem' }}>Across all portfolios</span>
        </div>
        <div style={cardStyle}>
          <h3 style={{ color: '#64748B', fontSize: '0.85rem', margin: 0 }}>Dividend Income (YTD)</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.55rem 0' }}>$--,---</p>
          <span style={{ color: '#64748B', fontSize: '0.85rem' }}>Year to date</span>
        </div>
      </div>

      <div style={{ marginTop: '1.5rem', ...cardStyle, minHeight: 300 }}>
        <h3 style={{ marginBottom: 10 }}>Portfolio Performance</h3>
        <p style={{ color: '#64748B' }}>Performance chart will be rendered here using Recharts.</p>
      </div>
    </div>
  );
};

export default Dashboard;
