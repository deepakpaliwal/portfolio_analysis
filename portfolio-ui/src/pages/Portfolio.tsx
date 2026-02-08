import React from 'react';

/**
 * Portfolio page — detailed view of portfolio holdings, allocation, and transactions.
 *
 * Planned features:
 * - Portfolio selector dropdown
 * - Holdings table with ticker, quantity, price, current value, gain/loss
 * - Asset allocation pie chart (by type and sector)
 * - Add/edit/delete holdings forms
 * - CSV import for holdings
 * - Transaction history tab
 */
const Portfolio: React.FC = () => {
  return (
    <div>
      <h1>Portfolio</h1>

      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem' }}>
        <select style={{ padding: '0.5rem', borderRadius: 4, border: '1px solid #ccc', minWidth: 200 }}>
          <option>Select Portfolio...</option>
        </select>
        <button style={{ padding: '0.5rem 1rem', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
          + New Portfolio
        </button>
      </div>

      {/* Holdings Table */}
      <div style={{ background: '#fff', borderRadius: 8, boxShadow: '0 1px 3px rgba(0,0,0,0.1)', overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: '#f9f9f9', textAlign: 'left' }}>
              <th style={{ padding: '0.75rem 1rem' }}>Ticker</th>
              <th style={{ padding: '0.75rem 1rem' }}>Name</th>
              <th style={{ padding: '0.75rem 1rem' }}>Type</th>
              <th style={{ padding: '0.75rem 1rem' }}>Quantity</th>
              <th style={{ padding: '0.75rem 1rem' }}>Avg Cost</th>
              <th style={{ padding: '0.75rem 1rem' }}>Current Price</th>
              <th style={{ padding: '0.75rem 1rem' }}>Gain/Loss</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td colSpan={7} style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>
                Select a portfolio to view holdings.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Portfolio;
