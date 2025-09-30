import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { loginUser } from "../../redux/authSlice";
import {
  Container,
  Grid,
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  Paper,
} from "@mui/material";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import { useNavigate } from "react-router-dom";
import mascot from "../../assets/mascot.png";


const Login = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, token } = useSelector((state) => state.auth);

  const [formData, setFormData] = useState({ username: "", password: "" });
  const [success, setSuccess] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    dispatch(loginUser(formData));
  };

  useEffect(() => {
    if (token) {
      setSuccess(true);
      setTimeout(() => {
        navigate("/");
      }, 1500);
    }
  }, [token, navigate]);

  return (
    <Container maxWidth="lg">
      <Grid container spacing={2} sx={{ mt: 8 }}>
        {/* Cột trái */}
        <Grid item xs={12} md={6}>
          <Box
            sx={{
              p: 3,
              borderRadius: 2,
              backgroundColor: "#fff5f5",
              boxShadow: 2,
              height: "100%",
            }}
          >
            <Typography variant="h5" sx={{ fontWeight: "bold", color: "#d70018", mb: 2 }}>
              Nhập hội khách hàng thành viên <span style={{ color: "red" }}>TMEMBER</span>
            </Typography>
            <Typography variant="subtitle1" sx={{ mb: 2 }}>
              Để không bỏ lỡ các ưu đãi hấp dẫn từ THComputer
            </Typography>
            <ul>
              <li>🎁 Chiết khấu đến 5% khi mua sản phẩm</li>
              <li>🚚 Miễn phí giao hàng cho đơn hàng từ 300.000đ</li>
              <li>🎂 Tặng voucher sinh nhật đến 500.000đ</li>
              <li>💰 Trợ giá thu cũ lên đổi mới đến 1 triệu</li>
              <li>🎟️ Thăng hạng nhận voucher đến 300.000đ</li>
            </ul>
            <Box mt={2}>
              <img
                src={mascot}
                alt="Mascot"
                style={{
                  width: "250px",
                  maxWidth: "100%",
                  borderRadius: "12px",
                  display: "block",
                  margin: "0 auto"
                }}
              />
            </Box>
          </Box>
        </Grid>

        {/* Cột phải */}
        <Grid item xs={12} md={6}>
          <Paper elevation={6} sx={{ p: 4, borderRadius: 3 }}>
            <Box
              display="flex"
              flexDirection="column"
              alignItems="center"
              justifyContent="center"
              gap={2}
            >
              <LockOutlinedIcon fontSize="large" color="primary" />
              <Typography component="h1" variant="h5" sx={{ fontWeight: "bold" }}>
                Đăng nhập
              </Typography>

              {error && <Alert severity="error">{error}</Alert>}
              {success && <Alert severity="success">Đăng nhập thành công!</Alert>}

              <Box component="form" onSubmit={handleSubmit} width="100%">
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  label="Tên đăng nhập"
                  value={formData.username}
                  onChange={(e) =>
                    setFormData({ ...formData, username: e.target.value })
                  }
                />
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  label="Mật khẩu"
                  type="password"
                  value={formData.password}
                  onChange={(e) =>
                    setFormData({ ...formData, password: e.target.value })
                  }
                />

                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  sx={{ mt: 2, mb: 2, backgroundColor: "#d70018" }}
                  disabled={loading}
                >
                  {loading ? (
                    <CircularProgress size={24} color="inherit" />
                  ) : (
                    "Đăng nhập"
                  )}
                </Button>

                <Typography align="center" variant="body2">
                  Quên mật khẩu?
                </Typography>

                <Typography align="center" variant="body2" sx={{ mt: 2 }}>
                  Bạn chưa có tài khoản?{" "}
                  <span style={{ color: "#d70018", cursor: "pointer" }} onClick={() => navigate("/register")}>
                    Đăng ký ngay
                  </span>
                </Typography>
              </Box>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Login;