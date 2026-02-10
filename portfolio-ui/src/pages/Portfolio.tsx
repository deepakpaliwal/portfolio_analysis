import React, { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '../store/store';
import {
  setPortfolios,
  addPortfolio,
  removePortfolio,
  selectPortfolio,
  setLoading,
  setError,
} from '../store/slices/portfolioSlice';
import apiClient from '../api/client';

const Portfolio: React.FC = () => {
  const dispatch = useAppDispatch();
  const { portfolios, selectedPortfolioId, loading, error } = useAppSelector(
    (state) => state.portfolio
  );

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newName, setNewName] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [newCurrency, setNewCurrency] = useState('USD');
  const [formError, setFormError] = useState('');
  const [creating, setCreating] = useState(false);

  const selectedPortfolio = portfolios.find((p) => p.id === selectedPortfolioId) || null;

  useEffect(() => {
    fetchPortfolios();
  }, []);

  const fetchPortfolios = async () => {
    dispatch(setLoading(true));
    try {
      const response = await apiClient.get('/v1/portfolios');
      dispatch(setPortfolios(response.data));
    } catch {
      dispatch(setError('Failed to load portfolios'));
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');

    if (!newName.trim()) {
      setFormError('Portfolio name is required');
      return;
    }

    setCreating(true);
    try {
      const response = await apiClient.post('/v1/portfolios', {
        name: newName.trim(),
        description: newDescription.trim() || null,
        baseCurrency: newCurrency,
      });
      dispatch(addPortfolio(response.data));
      dispatch(selectPortfolio(response.data.id));
      setNewName('');
      setNewDescription('');
      setNewCurrency('USD');
      setShowCreateForm(false);
    } catch (err: unknown) {
      const axiosErr = err as { response?: { data?: { message?: string } } };
      setFormError(axiosErr.response?.data?.message || 'Failed to create portfolio');
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this portfolio?')) return;
    try {
      await apiClient.delete(`/v1/portfolios/${id}`);
      dispatch(removePortfolio(id));
    } catch {
      dispatch(setError('Failed to delete portfolio'));
    }
  };

  const inputStyle = {
    width: '100%',
    padding: '0.5rem',
    border: '1px solid #ccc',
    borderRadius: 4,
    boxSizing: 'border-box' as const,
  };

  return (
    <div>
      <h1>Portfolio</h1>

      {error && (
        <div style={{ background: '#fee', color: '#c00', padding: '0.75rem', borderRadius: 4, marginBottom: '1rem' }}>
          {error}
        </div>
      )}

      {/* Portfolio selector + New button */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', alignItems: 'center' }}>
        <select
          value={selectedPortfolioId ?? ''}
          onChange={(e) => {
            const val = e.target.value;
            dispatch(selectPortfolio(val ? Number(val) : null));
          }}
          style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc', minWidth: 200 }}
        >
          <option value="">Select Portfolio...</option>
          {portfolios.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name} ({p.holdingCount} holdings)
            </option>
          ))}
        </select>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          style={{ padding: '0.5rem 1rem', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}
        >
          {showCreateForm ? 'Cancel' : '+ New Portfolio'}
        </button>
        {loading && <span style={{ color: '#888' }}>Loading...</span>}
      </div>

      {/* Create Portfolio Form */}
      {showCreateForm && (
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)', marginBottom: '1rem', maxWidth: 500 }}>
          <h3 style={{ marginTop: 0, marginBottom: '1rem' }}>Create New Portfolio</h3>
          <form onSubmit={handleCreate}>
            {formError && (
              <div style={{ background: '#fee', color: '#c00', padding: '0.5rem', borderRadius: 4, marginBottom: '0.75rem', fontSize: '0.9rem' }}>
                {formError}
              </div>
            )}
            <div style={{ marginBottom: '0.75rem' }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Name *</label>
              <input
                type="text"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                placeholder="e.g. Retirement Fund"
                maxLength={200}
                required
                style={inputStyle}
              />
            </div>
            <div style={{ marginBottom: '0.75rem' }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Description</label>
              <input
                type="text"
                value={newDescription}
                onChange={(e) => setNewDescription(e.target.value)}
                placeholder="Optional description"
                maxLength={500}
                style={inputStyle}
              />
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'block', marginBottom: 4, fontWeight: 600 }}>Base Currency</label>
              <select
                value={newCurrency}
                onChange={(e) => setNewCurrency(e.target.value)}
                style={{ ...inputStyle, width: 'auto', minWidth: 100 }}
              >
                <option value="USD">USD</option>
                <option value="EUR">EUR</option>
                <option value="GBP">GBP</option>
                <option value="JPY">JPY</option>
                <option value="CAD">CAD</option>
                <option value="AUD">AUD</option>
                <option value="CHF">CHF</option>
                <option value="INR">INR</option>
              </select>
            </div>
            <button
              type="submit"
              disabled={creating}
              style={{ padding: '0.5rem 1.5rem', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 600 }}
            >
              {creating ? 'Creating...' : 'Create Portfolio'}
            </button>
          </form>
        </div>
      )}

      {/* Selected Portfolio Details */}
      {selectedPortfolio ? (
        <div style={{ background: '#fff', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)', overflow: 'hidden' }}>
          <div style={{ padding: '1rem', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h2 style={{ margin: 0 }}>{selectedPortfolio.name}</h2>
              {selectedPortfolio.description && (
                <p style={{ margin: '0.25rem 0 0', color: '#666' }}>{selectedPortfolio.description}</p>
              )}
              <span style={{ fontSize: '0.85rem', color: '#888' }}>
                Currency: {selectedPortfolio.baseCurrency} &middot; {selectedPortfolio.holdingCount} holding(s)
              </span>
            </div>
            <button
              onClick={() => handleDelete(selectedPortfolio.id)}
              style={{ padding: '0.4rem 0.75rem', background: '#c00', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer', fontSize: '0.85rem' }}
            >
              Delete
            </button>
          </div>

          {/* Holdings Table */}
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
                <th style={{ padding: '0.75rem 1rem' }}>Ticker</th>
                <th style={{ padding: '0.75rem 1rem' }}>Name</th>
                <th style={{ padding: '0.75rem 1rem' }}>Type</th>
                <th style={{ padding: '0.75rem 1rem' }}>Quantity</th>
                <th style={{ padding: '0.75rem 1rem' }}>Avg Cost</th>
              </tr>
            </thead>
            <tbody>
              {selectedPortfolio.holdings && selectedPortfolio.holdings.length > 0 ? (
                selectedPortfolio.holdings.map((h) => (
                  <tr key={h.id} style={{ borderTop: '1px solid #eee' }}>
                    <td style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>{h.ticker}</td>
                    <td style={{ padding: '0.75rem 1rem' }}>{h.name}</td>
                    <td style={{ padding: '0.75rem 1rem' }}>{h.assetType}</td>
                    <td style={{ padding: '0.75rem 1rem' }}>{h.quantity}</td>
                    <td style={{ padding: '0.75rem 1rem' }}>${h.purchasePrice.toFixed(2)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>
                    No holdings yet. Add holdings to this portfolio.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      ) : (
        !showCreateForm && (
          <div style={{ background: '#fff', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)', padding: '3rem', textAlign: 'center', color: '#888' }}>
            {portfolios.length === 0
              ? 'No portfolios yet. Click "+ New Portfolio" to create one.'
              : 'Select a portfolio to view its details.'}
          </div>
        )
      )}
    </div>
  );
};

export default Portfolio;
