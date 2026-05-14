package services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import models.TransactionFinanciere;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfService {

    public String generateFinancialReport(List<TransactionFinanciere> transactions, String fileName) throws Exception {
        String home = System.getProperty("user.home");
        File desktop = new File(home, "Desktop");
        if (!desktop.exists()) {
            desktop = new File(home, "Bureau"); // Fallback pour Windows en français
        }
        if (!desktop.exists()) {
            desktop = new File(home); // Fallback ultime
        }

        String path = desktop.getAbsolutePath() + File.separator + fileName + ".pdf";
        
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Titre
        Paragraph title = new Paragraph("ARDHI AGRITECH - BILAN FINANCIER")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY);
        document.add(title);

        Paragraph subTitle = new Paragraph("Généré le : " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        document.add(subTitle);

        // Tableau
        Table table = new Table(UnitValue.createPercentArray(new float[]{20, 40, 20, 20}))
                .useAllAvailableWidth();

        // En-têtes
        table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Mode").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant (DT)").setBold()));

        double totalRevenue = 0;
        double totalExpense = 0;

        for (TransactionFinanciere tx : transactions) {
            String dateStr = tx.getDate_operation() != null ? tx.getDate_operation().toString() : "N/A";
            String modeStr = tx.getModePaiement() != null ? tx.getModePaiement() : "N/A";
            String descStr = tx.getDescription() != null ? tx.getDescription() : (tx.getType() != null ? tx.getType() : "N/A");

            table.addCell(new Cell().add(new Paragraph(dateStr)));
            table.addCell(new Cell().add(new Paragraph(descStr)));
            table.addCell(new Cell().add(new Paragraph(modeStr)));
            
            boolean isRevenu = tx.getType() != null && tx.getType().equalsIgnoreCase("Revenu");
            Paragraph amountPara = new Paragraph(String.format("%.2f", tx.getMontant()));
            if (isRevenu) {
                amountPara.setFontColor(ColorConstants.GREEN);
                totalRevenue += tx.getMontant();
            } else {
                amountPara.setFontColor(ColorConstants.RED);
                totalExpense += tx.getMontant();
            }
            table.addCell(new Cell().add(amountPara));
        }

        document.add(table);

        // Résumé
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("RÉSUMÉ FINAL").setBold().setUnderline());
        document.add(new Paragraph("Total Revenus : " + String.format("%.2f", totalRevenue) + " DT").setFontColor(ColorConstants.GREEN));
        document.add(new Paragraph("Total Dépenses : " + String.format("%.2f", totalExpense) + " DT").setFontColor(ColorConstants.RED));
        document.add(new Paragraph("Bénéfice Net : " + String.format("%.2f", totalRevenue - totalExpense) + " DT")
                .setBold()
                .setFontSize(14));

        document.close();
        return path;
    }
}
