package com.articurated.service;

import com.articurated.model.Order;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfInvoiceService {

    public byte[] generateInvoice(Order order) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();
        
        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Invoice", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Add order details
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("Order Number: " + order.getOrderNumber(), valueFont));
        document.add(new Paragraph("Order Date: " + order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE), valueFont));
        document.add(new Paragraph("Status: " + order.getStatus(), valueFont));
        document.add(new Paragraph("\nShipping Address:", labelFont));
        document.add(new Paragraph(order.getShippingAddress(), valueFont));
        document.add(new Paragraph("\nItems:", labelFont));

        // Add order items
        for (com.articurated.model.OrderItem item : order.getOrderItems()) {
            Paragraph itemPara = new Paragraph(
                String.format("%s x%d - $%.2f", item.getProductName(), item.getQuantity(), item.getTotalPrice()),
                valueFont
            );
            document.add(itemPara);
        }

        // Add total
        document.add(new Paragraph("\nTotal Amount: $" + order.getTotalAmount(), labelFont));
        document.add(new Paragraph("Payment Method: " + order.getPaymentMethod(), valueFont));

        document.close();

        return baos.toByteArray();
    }

    public void sendInvoiceEmail(Order order) {
        // Simulate email sending - in real implementation, use email service
        System.out.println("Sending invoice email for order: " + order.getOrderNumber());
        System.out.println("Email would be sent to: " + order.getCustomer().getEmail());
    }
}



