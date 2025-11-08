package com.trong.Computer_sell.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.trong.Computer_sell.DTO.response.Shipping.ShippingOrderResponse;
import com.trong.Computer_sell.model.OrderDetailEntity;
import com.trong.Computer_sell.model.ShippingOrderEntity;
import com.trong.Computer_sell.repository.ShippingOrderRepository;
import com.trong.Computer_sell.service.ShippingOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingOrderServiceImpl implements ShippingOrderService {

    private final ShippingOrderRepository shippingOrderRepository;

    /**
     * 🔹 Lấy tất cả phiếu vận chuyển
     */
    @Override
    public List<ShippingOrderResponse> getAllShippingOrders() {
        List<ShippingOrderEntity> entities = shippingOrderRepository.findAll();
        if (entities.isEmpty()) {
            throw new RuntimeException("Hiện chưa có phiếu vận chuyển nào được tạo");
        }
        return entities.stream().map(ShippingOrderResponse::fromEntity).toList();
    }

    /**
     * 🔹 Lấy chi tiết 1 phiếu vận chuyển
     */
    @Override
    public ShippingOrderResponse getShippingOrderById(UUID id) {
        ShippingOrderEntity entity = shippingOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu vận chuyển có ID: " + id));

        if (entity.getOrder() != null &&
                entity.getOrder().getStatus().name().equalsIgnoreCase("CANCELED")) {
            throw new RuntimeException("Phiếu vận chuyển này không khả dụng vì đơn hàng đã bị hủy");
        }

        return ShippingOrderResponse.fromEntity(entity);
    }

    /**
     * 🔹 Xuất phiếu vận chuyển ra PDF có hỗ trợ tiếng Việt
     */
    @Override
    public ByteArrayInputStream exportShippingOrderToPdf(UUID id) {
        ShippingOrderEntity order = shippingOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu vận chuyển!"));

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 🧩 Load font Unicode có hỗ trợ tiếng Việt
            String fontPath = "src/main/resources/fonts/arial.ttf";
            BaseFont unicodeFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font titleFont = new Font(unicodeFont, 20, Font.BOLD, BaseColor.BLACK);
            Font normalFont = new Font(unicodeFont, 12, Font.NORMAL, BaseColor.BLACK);
            Font boldFont = new Font(unicodeFont, 12, Font.BOLD, BaseColor.BLACK);
            Font footerFont = new Font(unicodeFont, 11, Font.NORMAL, BaseColor.DARK_GRAY);

            // 🧾 Format tiền tệ kiểu Việt Nam
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            String totalFormatted = currencyFormat.format(order.getTotalAmount());

            // ==================== HEADER ====================
            Paragraph title = new Paragraph("PHIẾU VẬN CHUYỂN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // ==================== THÔNG TIN NGƯỜI NHẬN ====================
            document.add(new Paragraph("Tên người nhận: " + order.getRecipientName(), normalFont));
            document.add(new Paragraph("Số điện thoại: " + order.getRecipientPhone(), normalFont));
            document.add(new Paragraph("Địa chỉ: " + order.getShippingAddress(), normalFont));
            document.add(new Paragraph("Ngày tạo phiếu: " + order.getCreatedAt(), normalFont));
            document.add(Chunk.NEWLINE);

            // ==================== THÔNG TIN THANH TOÁN ====================
            document.add(new Paragraph("Tổng tiền: " + totalFormatted + " VND", boldFont));
            document.add(new Paragraph("Thanh toán: " +
                    (order.isPaymentCompleted() ? "✅ Đã thanh toán" : "❌ Chưa thanh toán"), normalFont));

            // 💬 Thêm dòng “Bằng chữ”
            String totalInWords = convertNumberToWords(order.getTotalAmount().longValue());
            document.add(new Paragraph("Bằng chữ: " + totalInWords + ".", normalFont));
            document.add(Chunk.NEWLINE);

            // ==================== BẢNG SẢN PHẨM ====================
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{4, 1, 2});

            PdfPCell h1 = new PdfPCell(new Phrase("Tên sản phẩm", boldFont));
            PdfPCell h2 = new PdfPCell(new Phrase("Số lượng", boldFont));
            PdfPCell h3 = new PdfPCell(new Phrase("Thành tiền (VND)", boldFont));
            h1.setHorizontalAlignment(Element.ALIGN_CENTER);
            h2.setHorizontalAlignment(Element.ALIGN_CENTER);
            h3.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(h1);
            table.addCell(h2);
            table.addCell(h3);

            for (OrderDetailEntity detail : order.getOrder().getOrderDetails()) {
                String formattedSubtotal = currencyFormat.format(detail.getSubtotal());
                PdfPCell c1 = new PdfPCell(new Phrase(detail.getProduct().getName(), normalFont));
                PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(detail.getQuantity()), normalFont));
                PdfPCell c3 = new PdfPCell(new Phrase(formattedSubtotal + " VND", normalFont));
                c1.setPaddingLeft(5);
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(c1);
                table.addCell(c2);
                table.addCell(c3);
            }

            document.add(table);
            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // ==================== FOOTER (CHỮ KÝ) ====================
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{1, 1});

            PdfPCell sender = new PdfPCell(new Phrase("Người giao hàng\n\n\n__________________", footerFont));
            sender.setBorder(Rectangle.NO_BORDER);
            sender.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell receiver = new PdfPCell(new Phrase("Người nhận hàng\n\n\n__________________", footerFont));
            receiver.setBorder(Rectangle.NO_BORDER);
            receiver.setHorizontalAlignment(Element.ALIGN_CENTER);

            footerTable.addCell(sender);
            footerTable.addCell(receiver);
            document.add(footerTable);

            document.close();
            log.info("📦 Xuất file PDF phiếu vận chuyển thành công cho Order ID {}", order.getOrder().getId());

        } catch (Exception e) {
            log.error("❌ Lỗi khi tạo file PDF phiếu vận chuyển: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo file PDF phiếu vận chuyển: " + e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // ============================================================
    // 🔠 HÀM HỖ TRỢ CHUYỂN SỐ THÀNH CHỮ (BẰNG TIẾNG VIỆT)
    // ============================================================
    private String convertNumberToWords(long number) {
        if (number == 0) return "Không đồng";

        final String[] units = {"", "nghìn", "triệu", "tỷ"};
        final String[] numNames = {"", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};

        StringBuilder words = new StringBuilder();
        int unitIndex = 0;

        while (number > 0) {
            int group = (int) (number % 1000);
            if (group > 0) {
                String groupWords = readThreeDigits(group, numNames);
                words.insert(0, groupWords + " " + units[unitIndex] + " ");
            }
            number /= 1000;
            unitIndex++;
        }

        String result = words.toString().trim();
        result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        return result + " đồng chẵn";
    }

    private String readThreeDigits(int number, String[] numNames) {
        int hundred = number / 100;
        int ten = (number % 100) / 10;
        int one = number % 10;

        StringBuilder result = new StringBuilder();

        if (hundred > 0) {
            result.append(numNames[hundred]).append(" trăm ");
        }

        if (ten > 1) {
            result.append(numNames[ten]).append(" mươi ");
            if (one == 1) result.append("mốt");
            else if (one == 5) result.append("lăm");
            else if (one > 0) result.append(numNames[one]);
        } else if (ten == 1) {
            result.append("mười ");
            if (one > 0) result.append(numNames[one]);
        } else if (ten == 0 && one > 0) {
            result.append(numNames[one]);
        }

        return result.toString().trim();
    }
}
