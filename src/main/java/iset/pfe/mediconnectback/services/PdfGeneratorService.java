package iset.pfe.mediconnectback.services;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.*;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import iset.pfe.mediconnectback.entities.FichierMedicalForm;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfGeneratorService {

    public byte[] generateMedicalFormPdf(FichierMedicalForm form) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Fonts
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont textFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Title
            Paragraph title = new Paragraph("Patient Medical Form")
                    .setFont(titleFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 4}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setBorder(new SolidBorder(ColorConstants.GRAY, 1));

            // Helper to add row
            addRow(table, "Patient", form.getPatient().getFullName(), textFont);
            addRow(table, "Blood Type", form.getBloodType(), textFont);
            addRow(table, "Height (cm)", String.valueOf(form.getHeight()), textFont);
            addRow(table, "Weight (kg)", String.valueOf(form.getWeight()), textFont);
            addRow(table, "Allergies", form.getAllergies(), textFont);
            addRow(table, "Chronic Diseases", form.getChronicDiseases(), textFont);
            addRow(table, "Current Medications", form.getCurrentMedications(), textFont);
            addRow(table, "Surgical History", form.getSurgicalHistory(), textFont);
            addRow(table, "Family Medical History", form.getFamilyMedicalHistory(), textFont);
            addRow(table, "Smoker", form.getSmoker() != null ? (form.getSmoker() ? "Yes" : "No") : "N/A", textFont);
            addRow(table, "Alcohol Use", form.getAlcoholUse() != null ? (form.getAlcoholUse() ? "Yes" : "No") : "N/A", textFont);
            addRow(table, "Activity Level", form.getActivityLevel(), textFont);
            addRow(table, "Dietary Preferences", form.getDietaryPreferences(), textFont);

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating medical form PDF", e);
        }
    }

    private void addRow(Table table, String label, String value, PdfFont font) {
        Cell labelCell = new Cell().add(new Paragraph(label).setFont(font).setBold())
                                   .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                   .setPadding(5)
                                   .setBorder(Border.NO_BORDER);
        Cell valueCell = new Cell().add(new Paragraph(value != null ? value : "N/A").setFont(font))
                                   .setPadding(5)
                                   .setBorder(Border.NO_BORDER);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
