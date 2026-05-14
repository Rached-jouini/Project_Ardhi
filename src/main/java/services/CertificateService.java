package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

public class CertificateService {

    public String generateCertificate(String userName, String eventName) {
        String fileName = "Certificat_" + userName.replace(" ", "_") + ".pdf";
        Document document = new Document(PageSize.A4.rotate()); // Paysage pour faire plus "diplôme"

        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Style de bordure
            Rectangle rect = new Rectangle(document.getPageSize());
            rect.setBorder(Rectangle.BOX);
            rect.setBorderWidth(5);
            rect.setBorderColor(new BaseColor(45, 106, 79)); // Vert Ardhi
            document.add(rect);

            // Titre
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 40, new BaseColor(45, 106, 79));
            Paragraph title = new Paragraph("CERTIFICAT DE PRESENCE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(50);
            document.add(title);

            // Sous-titre
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 20);
            Paragraph subTitle = new Paragraph("\nCe certificat est décerné à", subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            // Nom de l'utilisateur
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, BaseColor.BLACK);
            Paragraph name = new Paragraph(userName, nameFont);
            name.setAlignment(Element.ALIGN_CENTER);
            name.setSpacingBefore(20);
            document.add(name);

            // Détails
            Paragraph details = new Paragraph("\nPour sa participation active à l'événement :\n", subTitleFont);
            details.setAlignment(Element.ALIGN_CENTER);
            document.add(details);

            Font eventFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 25, new BaseColor(27, 67, 50));
            Paragraph event = new Paragraph(eventName, eventFont);
            event.setAlignment(Element.ALIGN_CENTER);
            document.add(event);

            // Pied de page
            Paragraph footer = new Paragraph("\n\nFait le " + java.time.LocalDate.now() + "\nL'équipe Ardhi", subTitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
