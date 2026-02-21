import React from 'react';

const cardStyle: React.CSSProperties = {
  background: '#ffffff',
  padding: '1.25rem',
  borderRadius: 14,
  boxShadow: '0 10px 24px rgba(15,23,42,0.08)',
  border: '1px solid #E2E8F0',
};

const Trading: React.FC = () => {
  return (
    <div>
      <h1 style={{ marginBottom: 6 }}>Trading</h1>
      <p style={{ marginTop: 0, color: '#475569' }}>Place orders and monitor execution activity in a unified workspace.</p>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1rem' }}>
        <div style={cardStyle}>
          <h3>New Order</h3>
          <div style={{ marginBottom: '0.9rem' }}>
            <label style={{ fontWeight: 600, display: 'block', marginBottom: 4 }}>Ticker</label>
            <input type="text" placeholder="e.g., AAPL" style={{ width: '100%', padding: '0.58rem', borderRadius: 8, border: '1px solid #cbd5e1', boxSizing: 'border-box' }} />
          </div>
          <div style={{ marginBottom: '0.9rem' }}>
            <label style={{ fontWeight: 600, display: 'block', marginBottom: 4 }}>Action</label>
            <select style={{ width: '100%', padding: '0.58rem', borderRadius: 8, border: '1px solid #cbd5e1' }}>
              <option>BUY</option>
              <option>SELL</option>
            </select>
          </div>
          <div style={{ marginBottom: '0.9rem' }}>
            <label style={{ fontWeight: 600, display: 'block', marginBottom: 4 }}>Order Type</label>
            <select style={{ width: '100%', padding: '0.58rem', borderRadius: 8, border: '1px solid #cbd5e1' }}>
              <option>MARKET</option>
              <option>LIMIT</option>
              <option>STOP_LOSS</option>
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ fontWeight: 600, display: 'block', marginBottom: 4 }}>Quantity</label>
            <input type="number" placeholder="0" style={{ width: '100%', padding: '0.58rem', borderRadius: 8, border: '1px solid #cbd5e1', boxSizing: 'border-box' }} />
          </div>
          <button style={{ width: '100%', padding: '0.72rem', background: '#1D4ED8', color: '#fff', border: 'none', borderRadius: 8, cursor: 'pointer', fontWeight: 700 }}>
            Submit Order
          </button>
        </div>

        <div style={cardStyle}>
          <h3>Open Orders</h3>
          <p style={{ color: '#64748B', textAlign: 'center', padding: '2rem' }}>No open orders.</p>
        </div>
      </div>
    </div>
  );
};

export default Trading;
