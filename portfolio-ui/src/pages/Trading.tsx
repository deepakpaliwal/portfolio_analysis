import React from 'react';

/**
 * Trading page — order entry and execution management.
 *
 * Planned features:
 * - Order entry form (market, limit, stop-loss)
 * - Paper trading toggle for testing strategies
 * - Open orders list with cancel/modify actions
 * - Executed orders history
 * - Strategy-driven automated order suggestions
 * - Broker account connection management
 * - Real-time price feed via WebSocket
 */
const Trading: React.FC = () => {
  return (
    <div>
      <h1>Trading</h1>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem' }}>
        {/* Order Entry Panel */}
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3>New Order</h3>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ fontWeight: 600, display: 'block', marginBottom: 4 }}>Ticker</label>
            <input type="text" placeholder="e.g., AAPL" style={{ width: '100%', padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc', boxSizing: 'border-box' }} />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ fontWeight: 600, display: 'block', marginBottom: 4 }}>Action</label>
            <select style={{ width: '100%', padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}>
              <option>BUY</option>
              <option>SELL</option>
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ fontWeight: 600, display: 'block', marginBottom: 4 }}>Order Type</label>
            <select style={{ width: '100%', padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc' }}>
              <option>MARKET</option>
              <option>LIMIT</option>
              <option>STOP_LOSS</option>
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ fontWeight: 600, display: 'block', marginBottom: 4 }}>Quantity</label>
            <input type="number" placeholder="0" style={{ width: '100%', padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc', boxSizing: 'border-box' }} />
          </div>
          <button style={{ width: '100%', padding: '0.75rem', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer', fontWeight: 600 }}>
            Submit Order
          </button>
        </div>

        {/* Open Orders */}
        <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
          <h3>Open Orders</h3>
          <p style={{ color: '#888', textAlign: 'center', padding: '2rem' }}>No open orders.</p>
        </div>
      </div>
    </div>
  );
};

export default Trading;
