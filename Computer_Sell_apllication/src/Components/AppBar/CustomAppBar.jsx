import React from "react";
import {
  AppBar,
  Toolbar,
  Typography,
  Box,
  Button,
  IconButton,
  TextField,
  InputAdornment,
  Badge,
  Menu,
  MenuItem,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import Brightness4Icon from "@mui/icons-material/Brightness4";
import AccountCircle from "@mui/icons-material/AccountCircle";
import { useSelector, useDispatch } from "react-redux";
import { logout } from "../../redux/authSlice";
import { useNavigate } from "react-router-dom";

export default function CustomAppBar() {
  const { token, user } = useSelector((state) => state.auth);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [anchorEl, setAnchorEl] = React.useState(null);

  const handleMenu = (event) => setAnchorEl(event.currentTarget);
  const handleClose = () => setAnchorEl(null);

  const handleLogout = () => {
    dispatch(logout());
    handleClose();
    navigate("/login");
  };

  return (
    <AppBar
      position="static"
      sx={{
        backgroundColor: "#d70018",
        borderRadius: "8px",
        margin: "8px",
        boxShadow: 3,
      }}
    >
      <Toolbar sx={{ display: "flex", justifyContent: "space-between", gap: 3 }}>
        {/* Logo */}
        <Typography
          variant="h6"
          sx={{
            fontWeight: "bold",
            cursor: "pointer",
            whiteSpace: "nowrap",
          }}
          onClick={() => navigate("/")}
        >
          THComputer
        </Typography>

        {/* Search */}
        <Box sx={{ flexGrow: 1, maxWidth: 700 }}>
          <TextField
            fullWidth
            size="small"
            placeholder="Bạn cần tìm gì?"
            variant="outlined"
            sx={{
              backgroundColor: "white",
              borderRadius: "50px",
              "& .MuiOutlinedInput-root": {
                borderRadius: "50px",
              },
            }}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton>
                    <SearchIcon />
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />
        </Box>

        {/* Action area (giỏ hàng, user, dark mode) */}
        <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
          {/* Giỏ hàng */}
          <IconButton
            sx={{
              color: "white",
              transition: "0.3s",
              "&:hover": {
                backgroundColor: "rgba(255,255,255,0.2)",
                transform: "scale(1.1)",
              },
            }}
            onClick={() => navigate("/cart")}
          >
            <Badge badgeContent={3} color="error">
              <ShoppingCartIcon />
            </Badge>
          </IconButton>

          {/* Auth section */}
          {!token ? (
            <Button
              variant="contained"
              sx={{
                backgroundColor: "white",
                color: "#d70018",
                fontWeight: "bold",
                borderRadius: "20px",
                px: 2,
                transition: "0.3s",
                "&:hover": {
                  backgroundColor: "#fff0f0",
                  transform: "translateY(-2px)",
                  boxShadow: "0 3px 6px rgba(0,0,0,0.2)",
                },
              }}
              onClick={() => navigate("/login")}
            >
              Đăng nhập
            </Button>
          ) : (
            <>
              <Box
                sx={{
                  display: "flex",
                  alignItems: "center",
                  gap: 1.5,
                  cursor: "pointer",
                  "&:hover": { opacity: 0.9 },
                }}
                onClick={handleMenu}
              >
                <Typography
                  sx={{
                    color: "white",
                    fontWeight: 500,
                    whiteSpace: "nowrap",
                  }}
                >
                  Xin chào, <b>{user?.username}</b>
                </Typography>
                <Badge badgeContent={2} color="error" overlap="circular">
                  <AccountCircle fontSize="large" sx={{ color: "white" }} />
                </Badge>
              </Box>

              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleClose}
              >
                <MenuItem onClick={() => navigate("/profile")}>
                  Thông tin tài khoản
                </MenuItem>
                <MenuItem onClick={() => navigate("/orders")}>
                  Đơn hàng của tôi
                </MenuItem>
                <MenuItem onClick={handleLogout}>Đăng xuất</MenuItem>
              </Menu>
            </>
          )}

          {/* Dark/Light mode */}
          <IconButton
            sx={{
              color: "white",
              transition: "0.3s",
              "&:hover": {
                backgroundColor: "rgba(255,255,255,0.2)",
                transform: "rotate(20deg) scale(1.1)",
              },
            }}
          >
            <Brightness4Icon />
          </IconButton>
        </Box>
      </Toolbar>
    </AppBar>
  );
}