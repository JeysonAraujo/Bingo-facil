package com.bingoai.backend.ticket;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.util.List;

@Component
public class PdfGenerator {

    private final QrCodeGenerator qrCodeGenerator;

    @Value("${app.activation-base-url:http://localhost:3000}")
    private String activationBaseUrl;

    public PdfGenerator(QrCodeGenerator qrCodeGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
    }

    /**
     * Generates a printable A4 PDF for the given tickets.
     *
     * @param tickets    list of tickets to include in the PDF.
     * @param eventTitle the event title to use in the header (avoids lazy-loading the Event association).
     */
    public byte[] generateTicketsPdf(List<Ticket> tickets, String eventTitle) {
        return generateTicketsPdf(tickets, eventTitle, null, null, false, null);
    }

    public byte[] generateTicketsPdf(List<Ticket> tickets, String eventTitle, String prizes, String customFooter, boolean hasCutStrip, String headerImageUrl) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Set margins: 30 left/right
            // Top margin: 55mm (header) + 5 points = 160.9f points
            // Bottom margin: 50mm (stub) + 5 points = 146.73f points if hasCutStrip, 20f points otherwise
            float topMargin = 55f / 25.4f * 72f + 5f;
            float bottomMargin = hasCutStrip ? (50f / 25.4f * 72f + 5f) : 20f;
            Document document = new Document(PageSize.A4, 30f, 30f, topMargin, bottomMargin);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            boolean first = true;
            for (Ticket ticket : tickets) {
                // Skip newPage() on the first ticket — document.open() already initialises page 1.
                if (first) {
                    first = false;
                } else {
                    document.newPage();
                }

                PdfContentByte cb = writer.getDirectContent();

                // Header (Image) - Drawn absolutely at the top (210x55mm)
                if (headerImageUrl != null && !headerImageUrl.trim().isEmpty()) {
                    try {
                        Image img = Image.getInstance(headerImageUrl);
                        float imgWidth = PageSize.A4.getWidth();
                        float imgHeight = 55f / 25.4f * 72f;
                        img.setAbsolutePosition(0, PageSize.A4.getHeight() - imgHeight);
                        img.scaleAbsolute(imgWidth, imgHeight);
                        cb.addImage(img);
                    } catch (Exception e) {
                        System.err.println("[PdfGenerator] Failed to load header image: " + e.getMessage());
                    }
                }

                // QR Code, Serial and Prizes layout in a 3-column table
                String activationUrl = activationBaseUrl + "/activate?serial=" + ticket.getSerial();
                int qrSize = hasCutStrip ? 75 : 90;
                byte[] qrCodeImageBytes = qrCodeGenerator.generateQrCode(activationUrl, qrSize, qrSize);
                Image qrCodeImage = Image.getInstance(qrCodeImageBytes);
                qrCodeImage.setAlignment(Element.ALIGN_LEFT);

                PdfPTable topRowTable = new PdfPTable(3);
                topRowTable.setWidthPercentage(100);
                topRowTable.setWidths(new float[]{1.5f, 2.5f, 4.0f});
                topRowTable.setSpacingAfter(3f);

                PdfPCell qrCell = new PdfPCell();
                qrCell.setBorder(Rectangle.NO_BORDER);
                qrCell.addElement(qrCodeImage);
                topRowTable.addCell(qrCell);

                PdfPCell serialCell = new PdfPCell();
                serialCell.setBorder(Rectangle.NO_BORDER);
                serialCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                Paragraph serialPara = new Paragraph("CARTELA:\n" + ticket.getSerial(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                serialCell.addElement(serialPara);
                topRowTable.addCell(serialCell);

                PdfPCell prizesCell = new PdfPCell();
                prizesCell.setBorder(Rectangle.NO_BORDER);
                prizesCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                prizesCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                
                Paragraph prizesPara = new Paragraph();
                prizesPara.setAlignment(Element.ALIGN_RIGHT);
                prizesPara.add(new Chunk("PRÊMIOS:\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                if (prizes != null && !prizes.trim().isEmpty()) {
                    prizesPara.add(new Chunk(prizes, FontFactory.getFont(FontFactory.HELVETICA, 9)));
                } else {
                    prizesPara.add(new Chunk("Consulte a organização", FontFactory.getFont(FontFactory.HELVETICA, 9)));
                }
                prizesCell.addElement(prizesPara);
                topRowTable.addCell(prizesCell);

                document.add(topRowTable);

                // Cards Table
                PdfPTable mainTable = new PdfPTable(2);
                mainTable.setWidthPercentage(100);
                mainTable.setSpacingBefore(5f);

                List<CardMatrix> cards = ticket.getMatrix().getCards();
                for (int i = 0; i < cards.size(); i++) {
                    PdfPCell cell = new PdfPCell();
                    cell.addElement(createCardTable(cards.get(i), i + 1));
                    cell.setPadding(4);
                    cell.setBorder(Rectangle.NO_BORDER);
                    mainTable.addCell(cell);
                }
                document.add(mainTable);

                // Footer Text
                if (customFooter != null && !customFooter.trim().isEmpty()) {
                    Paragraph footerPara = new Paragraph(customFooter, FontFactory.getFont(FontFactory.HELVETICA, 9));
                    footerPara.setAlignment(Element.ALIGN_CENTER);
                    footerPara.setSpacingBefore(5f);
                    document.add(footerPara);
                }

                // Dotted line & Stub for Admin at absolute position
                if (hasCutStrip) {
                    float stubHeight = 50f / 25.4f * 72f; // 141.73 pt
                    
                    // Draw a real dotted line across the sheet
                    cb.saveState();
                    cb.setLineWidth(1f);
                    cb.setLineDash(3f, 3f);
                    cb.moveTo(0, stubHeight);
                    cb.lineTo(PageSize.A4.getWidth(), stubHeight);
                    cb.stroke();
                    cb.restoreState();

                    PdfPTable stubTable = new PdfPTable(2);
                    stubTable.setTotalWidth(PageSize.A4.getWidth() - 60f); // 30pt margins
                    stubTable.setLockedWidth(true);

                    Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                    Font valFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

                    PdfPCell leftCell = new PdfPCell();
                    leftCell.setBorder(Rectangle.NO_BORDER);
                    leftCell.addElement(new Paragraph("CANHOTO DE CONTROLE - ADMINISTRADOR", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                    
                    Paragraph fields = new Paragraph();
                    fields.add(new Chunk("Nome: __________________________________\n", labelFont));
                    fields.add(new Chunk("Telefone: _______________________________\n", labelFont));
                    fields.add(new Chunk("Vendedor: _______________________________\n", labelFont));
                    leftCell.addElement(fields);

                    PdfPCell rightCell = new PdfPCell();
                    rightCell.setBorder(Rectangle.NO_BORDER);
                    
                    Paragraph rightPara = new Paragraph();
                    rightPara.setAlignment(Element.ALIGN_RIGHT);
                    rightPara.add(new Chunk("EVENTO: " + eventTitle + "\n", valFont));
                    rightPara.add(new Chunk("SERIAL: " + ticket.getSerial() + "\n", valFont));
                    rightCell.addElement(rightPara);

                    stubTable.addCell(leftCell);
                    stubTable.addCell(rightCell);
                    
                    // Draw the table just below the cut line
                    stubTable.writeSelectedRows(0, -1, 30f, stubHeight - 10f, cb);
                }
            }

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private PdfPTable createCardTable(CardMatrix card, int cardNumber) {
        // Table should be exactly 88x63mm
        // 88mm -> 249.45 points
        // 63mm -> 178.58 points
        PdfPTable table = new PdfPTable(5);
        table.setTotalWidth(249.45f);
        table.setLockedWidth(true);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);

        float cellHeight = 178.58f / 6f; // 29.76f points per cell (6 rows: header + 5 numbers)

        String[] headers = {"B", "I", "N", "G", "O"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setFixedHeight(cellHeight);
            table.addCell(cell);
        }

        for (int row = 0; row < 5; row++) {
            table.addCell(createNumberCell(card.getB().get(row), cellHeight));
            table.addCell(createNumberCell(card.getI().get(row), cellHeight));

            int nVal = card.getN().get(row);
            if (row == 2 && nVal == 0) {
                PdfPCell freeCell = new PdfPCell(new Phrase("FREE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                freeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                freeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                freeCell.setFixedHeight(cellHeight);
                freeCell.setBackgroundColor(Color.YELLOW);
                table.addCell(freeCell);
            } else {
                table.addCell(createNumberCell(nVal, cellHeight));
            }

            table.addCell(createNumberCell(card.getG().get(row), cellHeight));
            table.addCell(createNumberCell(card.getO().get(row), cellHeight));
        }

        return table;
    }

    private PdfPCell createNumberCell(int number, float height) {
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(number), FontFactory.getFont(FontFactory.HELVETICA, 14)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setFixedHeight(height);
        return cell;
    }
}
