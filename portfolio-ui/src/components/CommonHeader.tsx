import React from 'react';
import { Link } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../store/store';
import { logout } from '../store/slices/authSlice';
import logo from '../assets/logo.svg';

const CommonHeader: React.FC = () => {
  const dispatch = useAppDispatch();
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const isLoggedIn = isAuthenticated && !!user;

  return (
    <header style={{ width: '100%', borderBottom: '1px solid #E2E8F0', background: 'rgba(255,255,255,0.85)', backdropFilter: 'blur(6px)' }}>
      <div style={{ maxWidth: 1160, margin: '0 auto', padding: '0.9rem 1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Link to="/" style={{ display: 'inline-flex', alignItems: 'center' }}>
          <img src={logo} alt="Portfolio Analysis logo" style={{ height: 42 }} />
        </Link>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.7rem' }}>
          <Link to="/" style={{ textDecoration: 'none', color: '#0F172A', fontWeight: 600 }}>Home</Link>
          {isLoggedIn ? (
            <>
              <span style={{ color: '#334155', fontWeight: 600 }}>Hi, {user?.firstName}</span>
              <Link to="/dashboard" style={{ textDecoration: 'none', background: '#1D4ED8', color: '#fff', padding: '0.5rem 0.8rem', borderRadius: 8, fontWeight: 600 }}>Dashboard</Link>
              <button
                onClick={() => dispatch(logout())}
                style={{ border: '1px solid #94A3B8', background: '#fff', color: '#1E293B', padding: '0.5rem 0.8rem', borderRadius: 8, fontWeight: 600, cursor: 'pointer' }}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" style={{ textDecoration: 'none', color: '#0F172A', fontWeight: 600 }}>Sign In</Link>
              <Link to="/register" style={{ textDecoration: 'none', background: '#2563EB', color: '#fff', padding: '0.5rem 0.8rem', borderRadius: 8, fontWeight: 600 }}>Get Started</Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
};

export default CommonHeader;
