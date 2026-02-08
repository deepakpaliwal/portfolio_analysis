import React from 'react';

/**
 * Admin Panel page — user and system management for ADMIN role.
 *
 * Planned features:
 * - User management: list, activate, deactivate, change roles
 * - Feature toggle management
 * - System health dashboard
 * - Audit log viewer
 * - Batch job monitoring and manual trigger
 * - Subscription and billing overview
 */
const AdminPanel: React.FC = () => {
  return (
    <div>
      <h1>Admin Panel</h1>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ margin: '0 0 0.5rem' }}>Users</h3>
          <p style={{ color: '#888', margin: 0 }}>Manage user accounts, roles, and access.</p>
        </div>
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ margin: '0 0 0.5rem' }}>Feature Toggles</h3>
          <p style={{ color: '#888', margin: 0 }}>Enable or disable features per subscription tier.</p>
        </div>
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ margin: '0 0 0.5rem' }}>Batch Jobs</h3>
          <p style={{ color: '#888', margin: 0 }}>Monitor and trigger batch processing jobs.</p>
        </div>
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3 style={{ margin: '0 0 0.5rem' }}>Audit Log</h3>
          <p style={{ color: '#888', margin: 0 }}>View administrative action history.</p>
        </div>
      </div>
    </div>
  );
};

export default AdminPanel;
