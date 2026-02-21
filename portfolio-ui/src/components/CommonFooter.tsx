import React from 'react';

const CommonFooter: React.FC = () => {
  return (
    <footer style={{ width: '100%', borderTop: '1px solid #E2E8F0', background: '#ffffff' }}>
      <div style={{ maxWidth: 1160, margin: '0 auto', padding: '0.9rem 1.25rem' }}>
        <p style={{ margin: 0, fontSize: '0.8rem', color: '#64748B', lineHeight: 1.5 }}>
          Educational use only. This platform is not financial, legal, or tax advice. We accept no liability for any decisions made using this website.
        </p>
      </div>
    </footer>
  );
};

export default CommonFooter;
