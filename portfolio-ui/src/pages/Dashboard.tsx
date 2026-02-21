import React, { useEffect, useMemo, useState } from 'react';
import apiClient from '../api/client';
import { useAppSelector } from '../store/store';

interface PortfolioSummary {
  id: number;
  holdingCount: number;
}

interface ValuationData {
  totalMarketValue: number;
  totalGainLoss: number;
}

interface TransactionData {
  transactionType: string;
  quantity: number;
  price: number;
}

const cardStyle: React.CSSProperties = {
  background: '#ffffff',
  padding: '1.25rem',
  borderRadius: 14,
  boxShadow: '0 10px 24px rgba(15,23,42,0.08)',
  border: '1px solid #E2E8F0',
};

const formatCurrency = (value: number): string =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 2 }).format(value);

const Dashboard: React.FC = () => {
  const user = useAppSelector((state) => state.auth.user);

  const [totalPortfolioValue, setTotalPortfolioValue] = useState(0);
  const [totalGainLoss, setTotalGainLoss] = useState(0);
  const [totalHoldings, setTotalHoldings] = useState(0);
  const [dividendIncome, setDividendIncome] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      setLoading(true);
      try {
        const portfolioRes = await apiClient.get<PortfolioSummary[]>('/v1/portfolios');
        const portfolios = portfolioRes.data;

        if (!portfolios.length) {
          setTotalPortfolioValue(0);
          setTotalGainLoss(0);
          setTotalHoldings(0);
          setDividendIncome(0);
          return;
        }

        const valuationResponses = await Promise.all(
          portfolios.map((portfolio) =>
            apiClient
              .get<ValuationData>(`/v1/portfolios/${portfolio.id}/valuation`)
              .then((res) => res.data)
              .catch(() => ({ totalMarketValue: 0, totalGainLoss: 0 }))
          )
        );

        const transactionResponses = await Promise.all(
          portfolios.map((portfolio) =>
            apiClient
              .get<TransactionData[]>(`/v1/portfolios/${portfolio.id}/transactions`)
              .then((res) => res.data)
              .catch(() => [] as TransactionData[])
          )
        );

        const totalValue = valuationResponses.reduce((sum, item) => sum + (item.totalMarketValue || 0), 0);
        const totalGain = valuationResponses.reduce((sum, item) => sum + (item.totalGainLoss || 0), 0);
        const holdings = portfolios.reduce((sum, portfolio) => sum + (portfolio.holdingCount || 0), 0);
        const dividends = transactionResponses
          .flat()
          .filter((transaction) => transaction.transactionType === 'DIVIDEND')
          .reduce((sum, transaction) => sum + (transaction.quantity || 0) * (transaction.price || 0), 0);

        setTotalPortfolioValue(totalValue);
        setTotalGainLoss(totalGain);
        setTotalHoldings(holdings);
        setDividendIncome(dividends);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const gainLossColor = useMemo(() => (totalGainLoss >= 0 ? '#16A34A' : '#DC2626'), [totalGainLoss]);

  return (
    <div>
      <h1 style={{ marginBottom: 6, color: '#0F172A' }}>Dashboard</h1>
      <p style={{ color: '#475569', marginTop: 0 }}>
        Welcome back, {user?.firstName}! Here’s your portfolio snapshot.
      </p>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          gap: '1rem',
          marginTop: '1.25rem',
        }}
      >
        <div style={cardStyle}>
          <h3 style={{ color: '#64748B', fontSize: '0.85rem', margin: 0 }}>Total Portfolio Value</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.55rem 0' }}>
            {loading ? 'Loading...' : formatCurrency(totalPortfolioValue)}
          </p>
          <span style={{ color: '#64748B', fontSize: '0.85rem' }}>Across all portfolios</span>
        </div>
        <div style={cardStyle}>
          <h3 style={{ color: '#64748B', fontSize: '0.85rem', margin: 0 }}>Total Gain/Loss</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.55rem 0', color: gainLossColor }}>
            {loading ? 'Loading...' : formatCurrency(totalGainLoss)}
          </p>
          <span style={{ color: '#64748B', fontSize: '0.85rem' }}>All time</span>
        </div>
        <div style={cardStyle}>
          <h3 style={{ color: '#64748B', fontSize: '0.85rem', margin: 0 }}>Holdings</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.55rem 0' }}>{loading ? 'Loading...' : totalHoldings}</p>
          <span style={{ color: '#64748B', fontSize: '0.85rem' }}>Across all portfolios</span>
        </div>
        <div style={cardStyle}>
          <h3 style={{ color: '#64748B', fontSize: '0.85rem', margin: 0 }}>Dividend Income (YTD)</h3>
          <p style={{ fontSize: '1.75rem', fontWeight: 700, margin: '0.55rem 0' }}>
            {loading ? 'Loading...' : formatCurrency(dividendIncome)}
          </p>
          <span style={{ color: '#64748B', fontSize: '0.85rem' }}>From dividend transactions</span>
        </div>
      </div>

      <div style={{ marginTop: '1.5rem', ...cardStyle, minHeight: 300 }}>
        <h3 style={{ marginBottom: 10 }}>Portfolio Performance</h3>
        <p style={{ color: '#64748B' }}>
          This section will show trend charts in a future enhancement.
        </p>
      </div>
    </div>
  );
};

export default Dashboard;
