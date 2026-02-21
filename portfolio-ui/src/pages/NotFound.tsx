import React from 'react';
import { Link } from 'react-router-dom';

const NotFound: React.FC = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: 'linear-gradient(180deg, #F8FBFF 0%, #EEF5FF 100%)' }}>
      <h1 style={{ fontSize: '4rem', margin: 0, color: '#0F172A' }}>404</h1>
      <p style={{ fontSize: '1.1rem', color: '#64748B' }}>Page not found.</p>
      <Link to="/" style={{ color: '#1D4ED8', textDecoration: 'none', fontWeight: 700 }}>Back to Home</Link>
    </div>
  );
};

export default NotFound;
