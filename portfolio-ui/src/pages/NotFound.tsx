import React from 'react';
import { Link } from 'react-router-dom';
import CommonHeader from '../components/CommonHeader';
import CommonFooter from '../components/CommonFooter';

const NotFound: React.FC = () => {
  return (
    <div style={{ minHeight: '100vh', background: 'linear-gradient(180deg, #F8FBFF 0%, #EEF5FF 100%)', display: 'flex', flexDirection: 'column' }}>
      <CommonHeader />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
        <h1 style={{ fontSize: '4rem', margin: 0, color: '#0F172A' }}>404</h1>
        <p style={{ fontSize: '1.1rem', color: '#64748B' }}>Page not found.</p>
        <Link to="/" style={{ color: '#1D4ED8', textDecoration: 'none', fontWeight: 700 }}>Back to Home</Link>
      </div>
      <CommonFooter />
    </div>
  );
};

export default NotFound;
