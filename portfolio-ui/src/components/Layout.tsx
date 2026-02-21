import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../store/store';
import { logout } from '../store/slices/authSlice';
import logo from '../assets/logo.svg';

const navItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/portfolio', label: 'Portfolio' },
  { to: '/screener', label: 'Screener' },
  { to: '/risk', label: 'Risk Analytics' },
  { to: '/trading', label: 'Trading' },
  { to: '/batch', label: 'Batch Prices' },
  { to: '/correlation', label: 'Correlation' },
];

const linkStyle = (isActive: boolean): React.CSSProperties => ({
  display: 'block',
  padding: '0.75rem 0.9rem',
  color: isActive ? '#E0F2FE' : '#BFDBFE',
  textDecoration: 'none',
  borderRadius: 10,
  marginBottom: 6,
  background: isActive ? 'linear-gradient(90deg, rgba(59,130,246,0.42), rgba(14,165,233,0.32))' : 'transparent',
  fontWeight: isActive ? 700 : 500,
  border: isActive ? '1px solid rgba(186,230,253,0.45)' : '1px solid transparent',
});

const Layout: React.FC = () => {
  const dispatch = useAppDispatch();
  const user = useAppSelector((state) => state.auth.user);

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#F1F5F9' }}>
      <aside style={{ width: 268, background: 'linear-gradient(180deg, #0F172A, #0B3A72)', color: '#fff', padding: '1rem', display: 'flex', flexDirection: 'column' }}>
        <div style={{ marginBottom: '1.25rem', padding: '0.25rem' }}>
          <img src={logo} alt="Portfolio Analysis logo" style={{ width: '100%', maxWidth: 196, filter: 'brightness(1.15)' }} />
        </div>
        <nav>
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} style={({ isActive }) => linkStyle(isActive)}>
              {item.label}
            </NavLink>
          ))}

          {user?.role === 'ADMIN' && (
            <NavLink to="/admin" style={({ isActive }) => linkStyle(isActive)}>
              Admin Panel
            </NavLink>
          )}
        </nav>

        <div style={{ marginTop: 'auto', paddingTop: '1rem', borderTop: '1px solid rgba(148,163,184,0.25)' }}>
          <div style={{ fontSize: '0.92rem', color: '#E2E8F0', marginBottom: 8, fontWeight: 600 }}>
            {user?.firstName} {user?.lastName}
          </div>
          <button
            onClick={() => dispatch(logout())}
            style={{
              border: '1px solid rgba(191,219,254,0.55)',
              color: '#DBEAFE',
              background: 'rgba(59,130,246,0.14)',
              padding: '0.62rem 1rem',
              borderRadius: 10,
              cursor: 'pointer',
              width: '100%',
              fontWeight: 600,
            }}
          >
            Sign Out
          </button>
        </div>
      </aside>

      <main style={{ flex: 1, padding: '1.6rem 1.8rem', background: 'linear-gradient(180deg, #F8FAFC 0%, #EEF2FF 100%)' }}>
        <Outlet />
        <div style={{ marginTop: '1.6rem', fontSize: '0.8rem', color: '#64748B', background: '#fff', borderRadius: 10, padding: '0.7rem 0.9rem', border: '1px solid #E2E8F0' }}>
          Educational use only. This platform is not financial advice and carries no liability for any investment decisions.
        </div>
      </main>
    </div>
  );
};

export default Layout;
