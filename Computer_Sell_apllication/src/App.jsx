import React from "react";
import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import Login from "./pages/Auth/Login";
import Home from "./pages/Home/Home";
import CustomAppBar from "../src/Components/AppBar/CustomAppBar";
import Register from "./pages/Auth/Register";

function AppLayout() {
  const location = useLocation();
  
  const hideAppBar = location.pathname === "/login" || location.pathname === "/register";

  return (
    <>
      {!hideAppBar && <CustomAppBar />}
      <Routes>
        <Route path="/" element={<Home />} />
         <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
      </Routes>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppLayout />
    </BrowserRouter>
  );
}


