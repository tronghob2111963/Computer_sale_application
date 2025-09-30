import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { register } from "../apis/AuthAPI";

// Thunk register
export const registerUser = createAsyncThunk(
  "register/registerUser",
  async (userData, { rejectWithValue }) => {
    try {
      const response = await register(userData);
      return response;
    } catch (err) {
      return rejectWithValue(err.response?.data || "Đăng ký thất bại");
    }
  }
);

const registerSlice = createSlice({
  name: "register",
  initialState: {
    loading: false,
    error: null,
    success: false,
  },
  reducers: {
    resetRegisterState: (state) => {
      state.loading = false;
      state.error = null;
      state.success = false;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(registerUser.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.success = false;
      })
      .addCase(registerUser.fulfilled, (state) => {
        state.loading = false;
        state.success = true;
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { resetRegisterState } = registerSlice.actions;
export default registerSlice.reducer;
