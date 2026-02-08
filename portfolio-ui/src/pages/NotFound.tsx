import React from 'react';
import { Link } from 'react-router-dom';

const NotFound: React.FC = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
      <h1 style={{ fontSize: '4rem', margin: 0, color: '#1a1a2e' }}>404</h1>
      <p style={{ fontSize: '1.2rem', color: '#888' }}>Page not found.</p>
      <Link to="/dashboard" style={{ color: '#4fc3f7', textDecoration: 'none' }}>Back to Dashboard</Link>
    </div>
  );
};

export default NotFound;
