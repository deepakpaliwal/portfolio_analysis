import React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../store/store';
import { logout } from '../store/slices/authSlice';

const navItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/portfolio', label: 'Portfolio' },
  { to: '/screener', label: 'Screener' },
  { to: '/risk', label: 'Risk Analytics' },
  { to: '/trading', label: 'Trading' },
  { to: '/batch', label: 'Batch Prices' },
  { to: '/correlation', label: 'Correlation' },
];

const Layout: React.FC = () => {
  const dispatch = useAppDispatch();
  const user = useAppSelector((state) => state.auth.user);

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar Navigation */}
      <aside style={{ width: 240, background: '#1a1a2e', color: '#fff', padding: '1rem' }}>
        <h2 style={{ margin: '0 0 2rem', fontSize: '1.2rem' }}>Portfolio Analysis</h2>
        <nav>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              style={({ isActive }) => ({
                display: 'block',
                padding: '0.75rem 1rem',
                color: isActive ? '#4fc3f7' : '#ccc',
                textDecoration: 'none',
                borderRadius: 4,
                marginBottom: 4,
                background: isActive ? 'rgba(79,195,247,0.1)' : 'transparent',
              })}
            >
              {item.label}
            </NavLink>
          ))}

          {user?.role === 'ADMIN' && (
            <NavLink
              to="/admin"
              style={({ isActive }) => ({
                display: 'block',
                padding: '0.75rem 1rem',
                color: isActive ? '#4fc3f7' : '#ccc',
                textDecoration: 'none',
                borderRadius: 4,
                marginBottom: 4,
                background: isActive ? 'rgba(79,195,247,0.1)' : 'transparent',
              })}
            >
              Admin Panel
            </NavLink>
          )}
        </nav>
        <div style={{ marginTop: 'auto', paddingTop: '2rem' }}>
          <div style={{ fontSize: '0.85rem', color: '#aaa', marginBottom: 8 }}>
            {user?.firstName} {user?.lastName}
          </div>
          <button
            onClick={() => dispatch(logout())}
            style={{
              background: 'none',
              border: '1px solid #666',
              color: '#ccc',
              padding: '0.5rem 1rem',
              borderRadius: 4,
              cursor: 'pointer',
              width: '100%',
            }}
          >
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main style={{ flex: 1, padding: '2rem', background: '#f5f5f5' }}>
        <Outlet />
      </main>
    </div>
  );
};

export default Layout;
