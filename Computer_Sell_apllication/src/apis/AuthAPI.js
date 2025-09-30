import axios from "axios";

import { API_URL } from "../utilities/constants";

export const login = async (username, password) => {
  const response = await axios.post(`${API_URL}/auth/access-token`, {
    username,
    password,
    platform: "web",
    version: "1.0.0",
    deviceToken: "vite-react",
  });
  return response.data;
};

export const refreshToken = async (username, password) => {
  const response = await axios.post(`${API_URL}/auth/refresh-token`, {
    username,
    password,
    platform: "web",
    version: "1.0.0",
    deviceToken: "vite-react",
  });
  return response.data;
};


export const removeToken = async (token) => {
  const response = await axios.post(
    `${API_URL}/auth/remove-token`,
    {},
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    }
  );
  return response.data;
};

// API Đăng ký
export const register = async (userData) => {
  const response = await axios.post(`${API_URL}/user/register`, userData);
  return response.data;
};