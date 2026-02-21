import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAppSelector } from '../store/store';
import CommonHeader from '../components/CommonHeader';
import CommonFooter from '../components/CommonFooter';
import heroChart from '../assets/hero-chart.svg';
import featureRisk from '../assets/feature-risk.svg';
import featureTrading from '../assets/feature-trading.svg';

const cardStyle: React.CSSProperties = {
  background: '#ffffff',
  borderRadius: 16,
  padding: '1.2rem',
  boxShadow: '0 10px 30px rgba(15, 23, 42, 0.08)',
};

const imageFallbackStyle: React.CSSProperties = {
  width: '100%',
  height: 180,
  borderRadius: 12,
  marginBottom: 12,
  background: 'linear-gradient(135deg, #DBEAFE 0%, #E0F2FE 100%)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: 42,
};

const Home: React.FC = () => {
  const { isAuthenticated, user } = useAppSelector((state) => state.auth);
  const isLoggedIn = isAuthenticated && !!user;
  const [riskImgFailed, setRiskImgFailed] = useState(false);
  const [tradingImgFailed, setTradingImgFailed] = useState(false);

  return (
    <div style={{ background: 'linear-gradient(180deg, #F8FBFF 0%, #EEF5FF 60%, #FFFFFF 100%)', minHeight: '100vh' }}>
      <CommonHeader />



      <section style={{ maxWidth: 1160, margin: '0 auto', padding: '2rem 1.25rem 1.75rem', display: 'grid', gridTemplateColumns: '1.05fr 1fr', gap: '2rem', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '2.6rem', lineHeight: 1.15, margin: '0 0 1rem', color: '#0F172A' }}>
            Analyze, Track, and Learn from your Multi-Asset Portfolio
          </h1>
          <p style={{ fontSize: '1.05rem', color: '#334155', lineHeight: 1.6, marginBottom: '1.4rem' }}>
            Portfolio Analysis helps you manage stocks, crypto, options, bonds, and more in one place. Explore screening,
            valuation, risk analytics, and correlation tools through a clean and intuitive dashboard.
          </p>
          <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
            {!isLoggedIn ? (
              <>
                <Link to="/register" style={{ textDecoration: 'none', background: '#1D4ED8', color: '#fff', padding: '0.78rem 1.2rem', borderRadius: 10, fontWeight: 700 }}>Create Free Account</Link>
                <Link to="/login" style={{ textDecoration: 'none', background: '#E2E8F0', color: '#0F172A', padding: '0.78rem 1.2rem', borderRadius: 10, fontWeight: 700 }}>Open Dashboard</Link>
              </>
            ) : (
              <Link to="/dashboard" style={{ textDecoration: 'none', background: '#1D4ED8', color: '#fff', padding: '0.78rem 1.2rem', borderRadius: 10, fontWeight: 700 }}>Continue to Dashboard</Link>
            )}
          </div>
        </div>
        <img src={heroChart} alt="Illustration of portfolio value and trend chart" style={{ width: '100%', borderRadius: 18, boxShadow: '0 14px 30px rgba(15,23,42,0.15)' }} />
      </section>

      <section style={{ maxWidth: 1160, margin: '0 auto', padding: '0 1.25rem 2rem' }}>
        <h2 style={{ color: '#0F172A', marginBottom: '1rem' }}>Why users love it</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: '1rem' }}>
          <div style={cardStyle}>
            {!riskImgFailed ? (
              <img
                src={featureRisk}
                alt="Risk analytics illustration"
                style={{ width: '100%', borderRadius: 12, marginBottom: 12 }}
                onError={() => setRiskImgFailed(true)}
              />
            ) : (
              <div style={imageFallbackStyle}>📉</div>
            )}
            <h3 style={{ margin: '0 0 0.5rem', color: '#0F172A' }}>Risk-first insights</h3>
            <p style={{ margin: 0, color: '#475569', lineHeight: 1.55 }}>Track VaR, volatility, and diversification so you can understand downside exposure before acting.</p>
          </div>
          <div style={cardStyle}>
            {!tradingImgFailed ? (
              <img
                src={featureTrading}
                alt="Trading analytics illustration"
                style={{ width: '100%', borderRadius: 12, marginBottom: 12 }}
                onError={() => setTradingImgFailed(true)}
              />
            ) : (
              <div style={imageFallbackStyle}>📊</div>
            )}
            <h3 style={{ margin: '0 0 0.5rem', color: '#0F172A' }}>Smart portfolio workflows</h3>
            <p style={{ margin: 0, color: '#475569', lineHeight: 1.55 }}>Build portfolios, import holdings, analyze allocation, and monitor valuation with a consistent workflow.</p>
          </div>
          <div style={cardStyle}>
            <div style={{ height: 180, borderRadius: 12, marginBottom: 12, background: 'linear-gradient(135deg, #DBEAFE 0%, #E0F2FE 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 64 }}>📚</div>
            <h3 style={{ margin: '0 0 0.5rem', color: '#0F172A' }}>Research ready</h3>
            <p style={{ margin: 0, color: '#475569', lineHeight: 1.55 }}>Use ticker and sector screeners, saved reports, and performance snapshots to support your learning journey.</p>
          </div>
        </div>
      </section>

      <CommonFooter />

    </div>
  );
};

export default Home;
