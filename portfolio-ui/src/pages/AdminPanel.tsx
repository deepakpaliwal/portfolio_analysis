import React from 'react';

const cardStyle: React.CSSProperties = {
  background: '#ffffff',
  padding: '1.25rem',
  borderRadius: 14,
  boxShadow: '0 10px 24px rgba(15,23,42,0.08)',
  border: '1px solid #E2E8F0',
};

const AdminPanel: React.FC = () => {
  return (
    <div>
      <h1 style={{ marginBottom: 6 }}>Admin Panel</h1>
      <p style={{ marginTop: 0, color: '#475569' }}>Manage users, feature toggles, system controls, and operational oversight.</p>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 0.5rem' }}>Users</h3>
          <p style={{ color: '#64748B', margin: 0 }}>Manage user accounts, roles, and access.</p>
        </div>
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 0.5rem' }}>Feature Toggles</h3>
          <p style={{ color: '#64748B', margin: 0 }}>Enable or disable features per subscription tier.</p>
        </div>
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 0.5rem' }}>Batch Jobs</h3>
          <p style={{ color: '#64748B', margin: 0 }}>Monitor and trigger batch processing jobs.</p>
        </div>
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 0.5rem' }}>Audit Log</h3>
          <p style={{ color: '#64748B', margin: 0 }}>View administrative action history.</p>
        </div>
      </div>
    </div>
  );
};

export default AdminPanel;
