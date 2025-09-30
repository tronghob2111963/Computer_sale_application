// Redux store management toolkitimport { configureStore } from "@reduxjs/toolkit";
import authReducer from "./authSlice";
import { configureStore } from "@reduxjs/toolkit";
import registerReducer from "./registerSlice";

export const store = configureStore({
  reducer: {
    auth: authReducer,
    register: registerReducer,
  },
});

export default store;
