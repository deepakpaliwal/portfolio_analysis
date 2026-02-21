import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface AuthState {
  isAuthenticated: boolean;
  token: string | null;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
  } | null;
}

const storedToken = localStorage.getItem('token');
const storedUserRaw = localStorage.getItem('user');

let storedUser: AuthState['user'] = null;
if (storedUserRaw) {
  try {
    storedUser = JSON.parse(storedUserRaw);
  } catch {
    localStorage.removeItem('user');
  }
}

const initialState: AuthState = {
  isAuthenticated: !!storedToken && !!storedUser,
  token: storedToken,
  user: storedUser,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    loginSuccess(state, action: PayloadAction<{ token: string; user: AuthState['user'] }>) {
      state.isAuthenticated = true;
      state.token = action.payload.token;
      state.user = action.payload.user;
      localStorage.setItem('token', action.payload.token);
      localStorage.setItem('user', JSON.stringify(action.payload.user));
    },
    logout(state) {
      state.isAuthenticated = false;
      state.token = null;
      state.user = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },
  },
});

export const { loginSuccess, logout } = authSlice.actions;
export default authSlice.reducer;
