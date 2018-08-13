package karsai.laszlo.bringcloser;

import android.content.Context;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEvent;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import java.util.Locale;

import timber.log.Timber;

public class PdfPageEventHandler implements PdfPageEvent {

    /*
        Implementation of the event is based on code from:
        https://developers.itextpdf.com/examples/itext-action-second-edition/chapter-5#225-moviecountries1.java
    */

    private PdfTemplate mPdfTemplate;
    private Context mContext;
    private Font.FontFamily mFontFamily;

    public PdfPageEventHandler(Context context, Font.FontFamily fontFamily) {
        this.mContext = context;
        this.mFontFamily = fontFamily;
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
            mPdfTemplate = writer.getDirectContent().createTemplate(30, 16);
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {

    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable table = new PdfPTable(3);
            try {
                table.setWidths(new int[]{24, 24, 2});
                table.setTotalWidth(527);
                table.setLockedWidth(true);
                table.getDefaultCell().setFixedHeight(20);
                table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                table.addCell(
                        new Paragraph(
                                mContext.getResources().getString(R.string.footer_note),
                                new Font(mFontFamily, 8, Font.ITALIC)
                        )
                );
                table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(
                        new Paragraph(String.format(
                                Locale.getDefault(),
                                mContext.getResources().getString(R.string.page) + "%d /",
                                writer.getPageNumber()
                        ), new Font(mFontFamily, 10, Font.ITALIC))

                );
                PdfPCell cell = new PdfPCell(Image.getInstance(mPdfTemplate));
                cell.setBorder(Rectangle.BOTTOM);
                table.addCell(cell);
                table.writeSelectedRows(0, -1, 34, 40, writer.getDirectContent());
            }
            catch(DocumentException de) {
                Timber.wtf("onEndPage" + de.getMessage());
            }
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(
                    mPdfTemplate,
                    Element.ALIGN_LEFT,
                    new Paragraph(
                            String.valueOf(writer.getPageNumber()),
                            new Font(mFontFamily, 12, Font.ITALIC)
                    ),
                     2,
                    2,
                    0);
    }

    @Override
    public void onParagraph(PdfWriter writer, Document document, float paragraphPosition) {

    }

    @Override
    public void onParagraphEnd(PdfWriter writer, Document document, float paragraphPosition) {

    }

    @Override
    public void onChapter(PdfWriter writer, Document document, float paragraphPosition, Paragraph title) {

    }

    @Override
    public void onChapterEnd(PdfWriter writer, Document document, float paragraphPosition) {

    }

    @Override
    public void onSection(PdfWriter writer, Document document, float paragraphPosition, int depth, Paragraph title) {

    }

    @Override
    public void onSectionEnd(PdfWriter writer, Document document, float paragraphPosition) {

    }

    @Override
    public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {

    }
}
