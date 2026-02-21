import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAppDispatch } from '../store/store';
import { loginSuccess } from '../store/slices/authSlice';
import apiClient from '../api/client';
import logo from '../assets/logo.svg';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await apiClient.post('/v1/auth/login', { email, password });
      dispatch(loginSuccess({ token: response.data.token, user: response.data.user }));
      navigate('/dashboard');
    } catch {
      setError('Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: 'linear-gradient(140deg, #0F172A, #1D4ED8)' }}>
      <div style={{ background: '#fff', padding: '2rem', borderRadius: 14, width: 420, boxShadow: '0 15px 35px rgba(15,23,42,0.35)' }}>
        <div style={{ textAlign: 'center' }}>
          <img src={logo} alt="Portfolio Analysis logo" style={{ height: 48, marginBottom: 8 }} />
          <h1 style={{ textAlign: 'center', marginBottom: '0.5rem', color: '#0F172A', fontSize: '1.5rem' }}>Welcome back</h1>
          <p style={{ color: '#64748B', marginBottom: '1.3rem' }}>Sign in to continue to your dashboard</p>
        </div>
        <form onSubmit={handleSubmit}>
          {error && <div style={{ background: '#fee2e2', color: '#b91c1c', padding: '0.75rem', borderRadius: 8, marginBottom: '1rem' }}>{error}</div>}
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: 4, fontWeight: 600, color: '#1E293B' }}>Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required
              style={{ width: '100%', padding: '0.65rem', border: '1px solid #cbd5e1', borderRadius: 8, boxSizing: 'border-box' }} />
          </div>
          <div style={{ marginBottom: '1.3rem' }}>
            <label style={{ display: 'block', marginBottom: 4, fontWeight: 600, color: '#1E293B' }}>Password</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required
              style={{ width: '100%', padding: '0.65rem', border: '1px solid #cbd5e1', borderRadius: 8, boxSizing: 'border-box' }} />
          </div>
          <button type="submit" disabled={loading}
            style={{ width: '100%', padding: '0.78rem', background: '#1D4ED8', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 700, fontSize: '1rem' }}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
          <p style={{ textAlign: 'center', marginTop: '1rem', color: '#64748B' }}>
            Don&apos;t have an account?{' '}
            <Link to="/register" style={{ color: '#1D4ED8', fontWeight: 700 }}>Sign up</Link>
          </p>
          <p style={{ marginTop: '0.8rem', color: '#94A3B8', fontSize: '0.77rem', lineHeight: 1.5 }}>
            Educational use only. No financial advice is provided and we accept no liability for decisions based on this platform.
          </p>
        </form>
      </div>
    </div>
  );
};

export default Login;
