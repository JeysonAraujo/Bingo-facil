package com.bingoai.backend.ticket;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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

    public byte[] generateTicketsPdf(List<Ticket> tickets, String eventTitle, String customHeader, String customFooter, boolean hasCutStrip, String headerImageUrl) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Set margins: 30 left/right, 20 top/bottom
            Document document = new Document(PageSize.A4, 30f, 30f, 20f, 20f);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            boolean first = true;
            for (Ticket ticket : tickets) {
                // Skip newPage() on the first ticket — document.open() already initialises page 1.
                if (first) {
                    first = false;
                } else {
                    document.newPage();
                }

                // Header (Text or Image)
                if (headerImageUrl != null && !headerImageUrl.trim().isEmpty()) {
                    try {
                        Image img = Image.getInstance(headerImageUrl);
                        img.scaleToFit(535f, 100f);
                        img.setAlignment(Element.ALIGN_CENTER);
                        img.setSpacingAfter(10f);
                        document.add(img);
                    } catch (Exception e) {
                        System.err.println("[PdfGenerator] Failed to load header image: " + e.getMessage());
                        String headerText = (customHeader != null && !customHeader.trim().isEmpty()) ? customHeader : "BINGO EVENT: " + eventTitle;
                        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                        Paragraph header = new Paragraph(headerText, headerFont);
                        header.setAlignment(Element.ALIGN_CENTER);
                        header.setSpacingAfter(5f);
                        document.add(header);
                    }
                } else {
                    String headerText = (customHeader != null && !customHeader.trim().isEmpty()) ? customHeader : "BINGO EVENT: " + eventTitle;
                    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                    Paragraph header = new Paragraph(headerText, headerFont);
                    header.setAlignment(Element.ALIGN_CENTER);
                    header.setSpacingAfter(5f);
                    document.add(header);
                }

                // QR Code, Serial and Custom Text layout in a 3-column table
                String activationUrl = activationBaseUrl + "/activate?serial=" + ticket.getSerial();
                int qrSize = hasCutStrip ? 75 : 90;
                byte[] qrCodeImageBytes = qrCodeGenerator.generateQrCode(activationUrl, qrSize, qrSize);
                Image qrCodeImage = Image.getInstance(qrCodeImageBytes);
                qrCodeImage.setAlignment(Element.ALIGN_LEFT);

                PdfPTable topRowTable = new PdfPTable(3);
                topRowTable.setWidthPercentage(100);
                topRowTable.setWidths(new float[]{1.5f, 2.5f, 3.5f});
                topRowTable.setSpacingAfter(5f);

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

                PdfPCell noteCell = new PdfPCell();
                noteCell.setBorder(Rectangle.NO_BORDER);
                noteCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                String noteText = (customFooter != null && !customFooter.trim().isEmpty()) ? customFooter : "Escaneie o QR Code para ativar sua cartela e jogar em tempo real!";
                Paragraph notePara = new Paragraph(noteText, FontFactory.getFont(FontFactory.HELVETICA, 10));
                noteCell.addElement(notePara);
                topRowTable.addCell(noteCell);

                document.add(topRowTable);

                // Cards Table
                PdfPTable mainTable = new PdfPTable(2);
                mainTable.setWidthPercentage(100);
                mainTable.setSpacingBefore(hasCutStrip ? 10f : 20f);

                List<CardMatrix> cards = ticket.getMatrix().getCards();
                for (int i = 0; i < cards.size(); i++) {
                    PdfPCell cell = new PdfPCell();
                    cell.addElement(createCardTable(cards.get(i), i + 1));
                    cell.setPadding(8);
                    cell.setBorder(Rectangle.NO_BORDER);
                    mainTable.addCell(cell);
                }
                document.add(mainTable);

                // Footer Text (only if not using cut strip, as vertical space is tight)
                if (!hasCutStrip && customFooter != null && !customFooter.trim().isEmpty()) {
                    Paragraph footerPara = new Paragraph(customFooter, FontFactory.getFont(FontFactory.HELVETICA, 10));
                    footerPara.setAlignment(Element.ALIGN_CENTER);
                    footerPara.setSpacingBefore(10f);
                    document.add(footerPara);
                }

                // Dotted line & Stub for Admin
                if (hasCutStrip) {
                    Paragraph dashedLine = new Paragraph("- - - - - - - - - - - - - - - - - - - - - - CORTAR AQUI - - - - - - - - - - - - - - - - - - - - - -", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8));
                    dashedLine.setAlignment(Element.ALIGN_CENTER);
                    dashedLine.setSpacingBefore(10f);
                    dashedLine.setSpacingAfter(5f);
                    document.add(dashedLine);

                    PdfPTable stubTable = new PdfPTable(2);
                    stubTable.setWidthPercentage(100);

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
                    document.add(stubTable);
                }
            }

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private PdfPTable createCardTable(CardMatrix card, int cardNumber) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        String[] headers = {"B", "I", "N", "G", "O"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }

        for (int row = 0; row < 5; row++) {
            table.addCell(createNumberCell(card.getB().get(row)));
            table.addCell(createNumberCell(card.getI().get(row)));

            int nVal = card.getN().get(row);
            if (row == 2 && nVal == 0) {
                PdfPCell freeCell = new PdfPCell(new Phrase("FREE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                freeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                freeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                freeCell.setMinimumHeight(30);
                freeCell.setBackgroundColor(Color.YELLOW);
                table.addCell(freeCell);
            } else {
                table.addCell(createNumberCell(nVal));
            }

            table.addCell(createNumberCell(card.getG().get(row)));
            table.addCell(createNumberCell(card.getO().get(row)));
        }

        return table;
    }

    private PdfPCell createNumberCell(int number) {
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(number), FontFactory.getFont(FontFactory.HELVETICA, 12)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setMinimumHeight(30);
        return cell;
    }
}
