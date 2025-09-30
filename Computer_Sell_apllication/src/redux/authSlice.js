import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

export const loginUser = createAsyncThunk(
  "auth/loginUser",
  async ({ username, password }, { rejectWithValue }) => {
    try {
      const response = await axios.post("http://localhost:8080/auth/access-token", {
        username,
        password,
        platform: "web",
        version: "1.0.0",
        deviceToken: "vite-react",
      });
      return response.data; // { username, accessToken, refreshToken }
    } catch (err) {
      return rejectWithValue(err.response?.data || "Đăng nhập thất bại");
    }
  }
);

const authSlice = createSlice({
  name: "auth",
  initialState: {
    user: localStorage.getItem("username")
      ? { username: localStorage.getItem("username") }
      : null,
    token: localStorage.getItem("token") || null,
    loading: false,
    error: null,
    loginSuccess: false,
  },
  reducers: {
    logout: (state) => {
      const token = localStorage.getItem("token");
      axios.post("http://localhost:8080/auth/remove-token", {}, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      state.user = null;
      state.token = null;
      state.loginSuccess = false;
      localStorage.removeItem("token");
      localStorage.removeItem("username");
    },
    resetLoginState: (state) => {
      state.loginSuccess = false;
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loading = false;
        state.token = action.payload.accessToken;
        state.user = { username: action.payload.username };
        state.loginSuccess = true;

        localStorage.setItem("token", action.payload.accessToken);
        localStorage.setItem("username", action.payload.username);
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { logout, resetLoginState } = authSlice.actions;
export default authSlice.reducer;
