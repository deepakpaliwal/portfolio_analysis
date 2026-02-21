import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAppDispatch } from '../store/store';
import { loginSuccess } from '../store/slices/authSlice';
import apiClient from '../api/client';
import CommonHeader from '../components/CommonHeader';
import CommonFooter from '../components/CommonFooter';
import logo from '../assets/logo.svg';

const Register: React.FC = () => {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (password.length < 12) {
      errors.password = 'Password must be at least 12 characters';
    } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(password)) {
      errors.password = 'Must contain uppercase, lowercase, digit, and special character (@$!%*?&)';
    }

    if (password !== confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});

    if (!validateForm()) return;

    setLoading(true);
    try {
      const response = await apiClient.post('/v1/auth/register', {
        email,
        password,
        firstName,
        lastName,
      });
      dispatch(loginSuccess({ token: response.data.token, user: response.data.user }));
      navigate('/dashboard');
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { error?: string; fieldErrors?: Record<string, string> } } };
      if (axiosErr.response?.data?.fieldErrors) {
        setFieldErrors(axiosErr.response.data.fieldErrors);
      } else if (axiosErr.response?.data?.error) {
        setError(axiosErr.response.data.error);
      } else {
        setError('Registration failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const inputStyle = {
    width: '100%',
    padding: '0.58rem',
    border: '1px solid #cbd5e1',
    borderRadius: 8,
    boxSizing: 'border-box' as const,
  };

  const errorInputStyle = {
    ...inputStyle,
    border: '1px solid #dc2626',
  };

  return (
    <div style={{ minHeight: '100vh', background: 'linear-gradient(180deg, #F8FBFF 0%, #EEF5FF 100%)', display: 'flex', flexDirection: 'column' }}>
      <CommonHeader />
      <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '1.5rem' }}>
      <div style={{ background: '#fff', padding: '1.8rem', borderRadius: 14, width: 440, boxShadow: '0 15px 35px rgba(15,23,42,0.2)' }}>
        <div style={{ textAlign: 'center' }}>
          <img src={logo} alt="Portfolio Analysis logo" style={{ height: 48, marginBottom: 8 }} />
          <h1 style={{ textAlign: 'center', marginBottom: '0.4rem', color: '#0F172A', fontSize: '1.45rem' }}>Create account</h1>
          <p style={{ textAlign: 'center', marginBottom: '1.2rem', color: '#64748B' }}>Start your educational portfolio journey</p>
        </div>
        <form onSubmit={handleSubmit}>
          {error && (
            <div style={{ background: '#fee2e2', color: '#b91c1c', padding: '0.75rem', borderRadius: 8, marginBottom: '1rem' }}>
              {error}
            </div>
          )}

          <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem' }}>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600, color: '#1E293B' }}>First Name</label>
              <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} required
                style={fieldErrors.firstName ? errorInputStyle : inputStyle} />
              {fieldErrors.firstName && <span style={{ color: '#b91c1c', fontSize: '0.8rem' }}>{fieldErrors.firstName}</span>}
            </div>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600, color: '#1E293B' }}>Last Name</label>
              <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} required
                style={fieldErrors.lastName ? errorInputStyle : inputStyle} />
              {fieldErrors.lastName && <span style={{ color: '#b91c1c', fontSize: '0.8rem' }}>{fieldErrors.lastName}</span>}
            </div>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: 4, fontWeight: 600, color: '#1E293B' }}>Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required
              style={fieldErrors.email ? errorInputStyle : inputStyle} />
            {fieldErrors.email && <span style={{ color: '#b91c1c', fontSize: '0.8rem' }}>{fieldErrors.email}</span>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: 4, fontWeight: 600, color: '#1E293B' }}>Password</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required
              style={fieldErrors.password ? errorInputStyle : inputStyle} />
            {fieldErrors.password && <span style={{ color: '#b91c1c', fontSize: '0.8rem' }}>{fieldErrors.password}</span>}
            <span style={{ color: '#94A3B8', fontSize: '0.75rem', display: 'block', marginTop: 2 }}>
              Min 12 chars, uppercase, lowercase, digit, and special character
            </span>
          </div>

          <div style={{ marginBottom: '1.2rem' }}>
            <label style={{ display: 'block', marginBottom: 4, fontWeight: 600, color: '#1E293B' }}>Confirm Password</label>
            <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required
              style={fieldErrors.confirmPassword ? errorInputStyle : inputStyle} />
            {fieldErrors.confirmPassword && <span style={{ color: '#b91c1c', fontSize: '0.8rem' }}>{fieldErrors.confirmPassword}</span>}
          </div>

          <button type="submit" disabled={loading}
            style={{ width: '100%', padding: '0.74rem', background: '#1D4ED8', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 700, fontSize: '1rem' }}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>

          <p style={{ textAlign: 'center', marginTop: '1rem', color: '#64748B' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: '#1D4ED8', fontWeight: 700 }}>Sign in</Link>
          </p>

          <p style={{ marginTop: '0.8rem', color: '#94A3B8', fontSize: '0.77rem', lineHeight: 1.5 }}>
            Educational use only. No financial advice is provided and we accept no liability for decisions based on this platform.
          </p>
        </form>
      </div>
      </div>
      <CommonFooter />
    </div>
  );
};

export default Register;
