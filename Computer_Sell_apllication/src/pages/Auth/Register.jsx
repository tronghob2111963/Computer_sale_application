import React, { useState, useEffect } from "react";
import {
  Container,
  Grid,
  Box,
  TextField,
  Button,
  Typography,
  Alert,
  Paper,
  Divider,
  IconButton,
} from "@mui/material";
import GoogleIcon from "@mui/icons-material/Google";
import DeleteIcon from "@mui/icons-material/Delete";
import { useDispatch, useSelector } from "react-redux";
import { registerUser, resetRegisterState } from "../../redux/registerSlice";
import { useNavigate } from "react-router-dom";
import mascot from "../../assets/mascot.png";

const Register = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, success } = useSelector((state) => state.register);

  const [formData, setFormData] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    dateOfBirth: "",
    password: "",
    confirmPassword: "",
    addresses: [
      { apartmentNumber: "", streetNumber: "", ward: "", city: "", addressType: "HOME" },
    ],
  });

  useEffect(() => {
    if (success) {
      setTimeout(() => {
        dispatch(resetRegisterState());
        navigate("/login");
      }, 2000);
    }
  }, [success, navigate, dispatch]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleAddressChange = (index, e) => {
    const updated = [...formData.addresses];
    updated[index][e.target.name] = e.target.value;
    setFormData({ ...formData, addresses: updated });
  };

  const addAddress = () => {
    setFormData({
      ...formData,
      addresses: [...formData.addresses, { apartmentNumber: "", streetNumber: "", ward: "", city: "", addressType: "HOME" }],
    });
  };

  const removeAddress = (index) => {
    const updated = formData.addresses.filter((_, i) => i !== index);
    setFormData({ ...formData, addresses: updated });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      alert("Mật khẩu nhập lại không khớp!");
      return;
    }
    dispatch(registerUser(formData));
  };

  return (
    <Container maxWidth="md" sx={{ mt: 5 }}>
      <Paper sx={{ p: 5, borderRadius: 3, boxShadow: 3 }}>
        {/* Tiêu đề */}
        <Typography
          variant="h4"
          align="center"
          sx={{ mb: 2, fontWeight: "bold", color: "#d70018" }}
        >
          Đăng ký trở thành SMEMBER
        </Typography>

        {/* Mascot */}
        <Box display="flex" justifyContent="center" mb={2}>
          <img src={mascot} alt="Mascot" style={{ width: 80, height: 80 }} />
        </Box>

        {/* Đăng ký bằng MXH */}
        <Typography align="center" variant="body2" sx={{ mb: 2 }}>
          Đăng ký bằng tài khoản mạng xã hội
        </Typography>
        <Box display="flex" justifyContent="center" gap={2} mb={3}>
          <Button variant="outlined" startIcon={<GoogleIcon />} sx={{ textTransform: "none", px: 3 }}>
            Google
          </Button>
          <Button variant="outlined" sx={{ textTransform: "none", px: 3 }}>
            Zalo
          </Button>
        </Box>

        <Divider sx={{ my: 3 }}>Hoặc điền thông tin sau</Divider>

        {/* Thông báo */}
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2 }}>Đăng ký thành công! Vui lòng đăng nhập.</Alert>}

        {/* Form */}
        <Box component="form" onSubmit={handleSubmit}>
          {/* Username */}
          <Grid container spacing={2} sx={{ mb: 2 }}>
            <Grid item xs={12}>
              <TextField
                label="Tên đăng nhập"
                name="username"
                fullWidth
                required
                value={formData.username}
                onChange={handleChange}
              />
            </Grid>
          </Grid>

          {/* Thông tin cá nhân */}
          <Typography variant="h6" sx={{ mb: 1 }}>Thông tin cá nhân</Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <TextField label="Họ" name="firstName" fullWidth required value={formData.firstName} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Tên" name="lastName" fullWidth required value={formData.lastName} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Ngày sinh" name="dateOfBirth" type="date" fullWidth InputLabelProps={{ shrink: true }} value={formData.dateOfBirth} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Email (Không bắt buộc)" name="email" type="email" fullWidth value={formData.email} onChange={handleChange} />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Số điện thoại" name="phoneNumber" fullWidth required value={formData.phoneNumber} onChange={handleChange} />
            </Grid>
          </Grid>

          <Divider sx={{ my: 3 }} />

          {/* Địa chỉ */}
          <Typography variant="h6" sx={{ mb: 1 }}>Địa chỉ</Typography>
          {formData.addresses.map((addr, i) => (
            <Grid container spacing={2} key={i} sx={{ mb: 2, p: 2, borderRadius: 2, backgroundColor: "#fafafa" }}>
              <Grid item xs={12} sm={6}>
                <TextField label="Số nhà / Căn hộ" name="apartmentNumber" fullWidth value={addr.apartmentNumber} onChange={(e) => handleAddressChange(i, e)} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Đường" name="streetNumber" fullWidth value={addr.streetNumber} onChange={(e) => handleAddressChange(i, e)} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Phường / Xã" name="ward" fullWidth value={addr.ward} onChange={(e) => handleAddressChange(i, e)} />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField label="Thành phố" name="city" fullWidth value={addr.city} onChange={(e) => handleAddressChange(i, e)} />
              </Grid>
              <Grid item xs={12} textAlign="right">
                <IconButton color="error" onClick={() => removeAddress(i)}>
                  <DeleteIcon />
                </IconButton>
              </Grid>
            </Grid>
          ))}
          <Button variant="outlined" onClick={addAddress} sx={{ mb: 3 }}>+ Thêm địa chỉ</Button>

          <Divider sx={{ my: 3 }} />

          {/* Tạo mật khẩu */}
          <Typography variant="h6" sx={{ mb: 1 }}>Tạo mật khẩu</Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <TextField label="Mật khẩu" name="password" type="password" fullWidth required value={formData.password} onChange={handleChange} helperText="Tối thiểu 6 ký tự, gồm chữ & số" />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField label="Nhập lại mật khẩu" name="confirmPassword" type="password" fullWidth required value={formData.confirmPassword} onChange={handleChange} />
            </Grid>
          </Grid>

          {/* Nút hành động */}
          <Box mt={4} display="flex" justifyContent="space-between">
            <Button variant="outlined" onClick={() => navigate("/login")}>Quay lại đăng nhập</Button>
            <Button type="submit" variant="contained" sx={{ backgroundColor: "#d70018", "&:hover": { backgroundColor: "#a50012" } }} disabled={loading}>
              {loading ? "Đang xử lý..." : "Hoàn tất đăng ký"}
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default Register;

