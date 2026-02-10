import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface Holding {
  id: number;
  assetType: string;
  ticker: string;
  name: string;
  quantity: number;
  purchasePrice: number;
  currency: string;
  sector: string;
}

interface Portfolio {
  id: number;
  name: string;
  description: string;
  baseCurrency: string;
  holdingCount: number;
  holdings: Holding[];
}

interface PortfolioState {
  portfolios: Portfolio[];
  selectedPortfolioId: number | null;
  loading: boolean;
  error: string | null;
}

const initialState: PortfolioState = {
  portfolios: [],
  selectedPortfolioId: null,
  loading: false,
  error: null,
};

const portfolioSlice = createSlice({
  name: 'portfolio',
  initialState,
  reducers: {
    setPortfolios(state, action: PayloadAction<Portfolio[]>) {
      state.portfolios = action.payload;
      state.loading = false;
      state.error = null;
    },
    addPortfolio(state, action: PayloadAction<Portfolio>) {
      state.portfolios.push(action.payload);
    },
    removePortfolio(state, action: PayloadAction<number>) {
      state.portfolios = state.portfolios.filter(p => p.id !== action.payload);
      if (state.selectedPortfolioId === action.payload) {
        state.selectedPortfolioId = null;
      }
    },
    selectPortfolio(state, action: PayloadAction<number | null>) {
      state.selectedPortfolioId = action.payload;
    },
    setLoading(state, action: PayloadAction<boolean>) {
      state.loading = action.payload;
    },
    setError(state, action: PayloadAction<string>) {
      state.error = action.payload;
      state.loading = false;
    },
  },
});

export const { setPortfolios, addPortfolio, removePortfolio, selectPortfolio, setLoading, setError } = portfolioSlice.actions;
export type { Portfolio };
export default portfolioSlice.reducer;
