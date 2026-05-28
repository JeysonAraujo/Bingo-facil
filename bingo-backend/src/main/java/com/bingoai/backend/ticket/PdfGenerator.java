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
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
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

                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph header = new Paragraph("BINGO EVENT: " + eventTitle, headerFont);
                header.setAlignment(Element.ALIGN_CENTER);
                document.add(header);

                Paragraph serial = new Paragraph("TICKET SERIAL: " + ticket.getSerial(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                serial.setAlignment(Element.ALIGN_CENTER);
                serial.setSpacingAfter(20);
                document.add(serial);

                String activationUrl = activationBaseUrl + "/activate?serial=" + ticket.getSerial();
                byte[] qrCodeImageBytes = qrCodeGenerator.generateQrCode(activationUrl, 150, 150);
                Image qrCodeImage = Image.getInstance(qrCodeImageBytes);
                qrCodeImage.setAlignment(Element.ALIGN_CENTER);
                document.add(qrCodeImage);

                PdfPTable mainTable = new PdfPTable(2);
                mainTable.setWidthPercentage(100);
                mainTable.setSpacingBefore(30);

                List<CardMatrix> cards = ticket.getMatrix().getCards();
                for (int i = 0; i < cards.size(); i++) {
                    PdfPCell cell = new PdfPCell();
                    cell.addElement(createCardTable(cards.get(i), i + 1));
                    cell.setPadding(10);
                    cell.setBorder(Rectangle.NO_BORDER);
                    mainTable.addCell(cell);
                }

                document.add(mainTable);
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
