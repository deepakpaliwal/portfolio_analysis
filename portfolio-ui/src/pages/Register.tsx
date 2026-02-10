import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAppDispatch } from '../store/store';
import { loginSuccess } from '../store/slices/authSlice';
import apiClient from '../api/client';

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
      const axiosErr = err as { response?: { data?: { error?: string; fieldErrors?: Record<string, string> }; status?: number } };
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
    padding: '0.5rem',
    border: '1px solid #ccc',
    borderRadius: 4,
    boxSizing: 'border-box' as const,
  };

  const errorInputStyle = {
    ...inputStyle,
    border: '1px solid #c00',
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#1a1a2e' }}>
      <div style={{ background: '#fff', padding: '2rem', borderRadius: 8, width: 420, boxShadow: '0 4px 20px rgba(0,0,0,0.3)' }}>
        <h1 style={{ textAlign: 'center', marginBottom: '0.5rem', color: '#1a1a2e' }}>Portfolio Analysis</h1>
        <p style={{ textAlign: 'center', marginBottom: '1.5rem', color: '#666' }}>Create your account</p>
        <form onSubmit={handleSubmit}>
          {error && (
            <div style={{ background: '#fee', color: '#c00', padding: '0.75rem', borderRadius: 4, marginBottom: '1rem' }}>
              {error}
            </div>
          )}

          <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem' }}>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>First Name</label>
              <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} required
                style={fieldErrors.firstName ? errorInputStyle : inputStyle} />
              {fieldErrors.firstName && <span style={{ color: '#c00', fontSize: '0.8rem' }}>{fieldErrors.firstName}</span>}
            </div>
            <div style={{ flex: 1 }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Last Name</label>
              <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} required
                style={fieldErrors.lastName ? errorInputStyle : inputStyle} />
              {fieldErrors.lastName && <span style={{ color: '#c00', fontSize: '0.8rem' }}>{fieldErrors.lastName}</span>}
            </div>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required
              style={fieldErrors.email ? errorInputStyle : inputStyle} />
            {fieldErrors.email && <span style={{ color: '#c00', fontSize: '0.8rem' }}>{fieldErrors.email}</span>}
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Password</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required
              style={fieldErrors.password ? errorInputStyle : inputStyle} />
            {fieldErrors.password && <span style={{ color: '#c00', fontSize: '0.8rem' }}>{fieldErrors.password}</span>}
            <span style={{ color: '#888', fontSize: '0.75rem', display: 'block', marginTop: 2 }}>
              Min 12 chars, uppercase, lowercase, digit, and special character
            </span>
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Confirm Password</label>
            <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required
              style={fieldErrors.confirmPassword ? errorInputStyle : inputStyle} />
            {fieldErrors.confirmPassword && <span style={{ color: '#c00', fontSize: '0.8rem' }}>{fieldErrors.confirmPassword}</span>}
          </div>

          <button type="submit" disabled={loading}
            style={{ width: '100%', padding: '0.75rem', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 600, fontSize: '1rem' }}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>

          <p style={{ textAlign: 'center', marginTop: '1rem', color: '#666' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: '#1a1a2e', fontWeight: 600 }}>Sign in</Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default Register;
