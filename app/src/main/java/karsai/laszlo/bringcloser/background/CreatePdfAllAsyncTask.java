package karsai.laszlo.bringcloser.background;

import android.content.Context;
import android.os.AsyncTask;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import karsai.laszlo.bringcloser.CustomChunk;
import karsai.laszlo.bringcloser.PdfPageEventHandler;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.EventDetail;
import karsai.laszlo.bringcloser.model.PdfDetail;
import karsai.laszlo.bringcloser.model.ReceivedDetail;
import karsai.laszlo.bringcloser.model.ThoughtDetail;
import karsai.laszlo.bringcloser.model.WishDetail;
import karsai.laszlo.bringcloser.utils.ApplicationUtils;
import karsai.laszlo.bringcloser.utils.NotificationUtils;
import karsai.laszlo.bringcloser.utils.PdfUtils;
import timber.log.Timber;

public class CreatePdfAllAsyncTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> mContext;
    private String mCurrentUserUid;
    private List<ReceivedDetail> mDisplayedReceivedDetailList;
    private String mCurrentName;
    private String mCurrentPhotoUrl;
    private String mCurrentRegistrationTime;

    private File mDestinationFile;
    private BaseColor mPrimaryColor;
    private BaseColor mPrimaryDarkColor;
    private BaseColor mAccentColor;
    private LineSeparator mLineSeparatorGeneral;
    private LineSeparator mLineSeparatorThick;
    private List<PdfDetail> mPdfDetailList;
    private Image mCurrentImage;
    private int mWishNum;
    private int mEventNum;
    private int mThoughtNum;
    private Font mPrimaryFont;
    private Font mPrimaryDarkFont;
    private Font mAccentFont;
    private Font mSectionHeaderFont;
    private Font mMemoryHeaderFont;

    private final static float GENERAL_FONT_SIZE = 12.0F;
    private final static float BIG_FONT_SIZE = 16.0F;
    private final static float EXTRA_FONT_SIZE = 20.0F;
    private final static float SEPARATOR_WIDTH_GENERAL = 1.0F;
    private final static float SEPARATOR_WIDTH_THICK = 3.0F;
    private final static Font.FontFamily FONT_FAMILY = Font.FontFamily.TIMES_ROMAN;

    public CreatePdfAllAsyncTask(
            WeakReference<Context> context,
            String currentUserUid,
            List<ReceivedDetail> receivedDetailList,
            String currentName,
            String currentPhotoUrl,
            String currentRegistrationTime) {
        this.mContext = context;
        this.mCurrentUserUid = currentUserUid;
        this.mDisplayedReceivedDetailList = receivedDetailList;
        this.mCurrentName = currentName;
        this.mCurrentPhotoUrl = currentPhotoUrl;
        this.mCurrentRegistrationTime = currentRegistrationTime;

        this.mPrimaryColor = new BaseColor(152, 66, 0, 255);
        this.mPrimaryDarkColor = new BaseColor(106, 46, 0, 255);
        this.mAccentColor = new BaseColor(255, 147, 64, 255);
        this.mLineSeparatorGeneral = new LineSeparator();
        this.mLineSeparatorGeneral.setLineColor(this.mAccentColor);
        this.mLineSeparatorGeneral.setLineWidth(SEPARATOR_WIDTH_GENERAL);
        this.mLineSeparatorThick = new LineSeparator();
        this.mLineSeparatorThick.setLineColor(this.mAccentColor);
        this.mLineSeparatorThick.setLineWidth(SEPARATOR_WIDTH_THICK);
        this.mPrimaryFont = new Font(FONT_FAMILY, GENERAL_FONT_SIZE, Font.NORMAL, mPrimaryColor);
        this.mPrimaryDarkFont = new Font(FONT_FAMILY, GENERAL_FONT_SIZE, Font.NORMAL, mPrimaryDarkColor);
        this.mAccentFont = new Font(FONT_FAMILY, GENERAL_FONT_SIZE, Font.NORMAL, mAccentColor);
        this.mSectionHeaderFont = new Font(FONT_FAMILY, BIG_FONT_SIZE, Font.BOLDITALIC, mPrimaryDarkColor);
        this.mMemoryHeaderFont = new Font(FONT_FAMILY, GENERAL_FONT_SIZE, Font.BOLDITALIC, mPrimaryDarkColor);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            NotificationUtils.addPdfNotification(mContext.get(), mDestinationFile, false, 1);
            if (mCurrentPhotoUrl == null) {
                mCurrentImage = PdfUtils.getDefaultImage(mContext.get());
            } else {
                mCurrentImage = PdfUtils.getImage(mCurrentPhotoUrl);
            }
            mPdfDetailList = getPdfDetailList();
            NotificationUtils.addPdfNotification(mContext.get(), mDestinationFile, false, 2);
            createDocumentAndHeader();
        } catch (Exception e) {
            Timber.wtf(e.getMessage() + " - all - " + mCurrentUserUid);
            PdfUtils.showError(mContext.get(), e.getMessage());
        }
        return null;
    }

    private List<PdfDetail> getPdfDetailList() throws IOException, BadElementException {
        List<PdfDetail> pdfDetailList = new ArrayList<>();
        for (ReceivedDetail receivedDetail : mDisplayedReceivedDetailList) {
            WishDetail wishDetail = receivedDetail.getWishDetail();
            EventDetail eventDetail = receivedDetail.getEventDetail();
            ThoughtDetail thoughtDetail = receivedDetail.getThoughtDetail();
            if (wishDetail != null) {
                mWishNum++;
                pdfDetailList.add(
                        PdfUtils.addPdfDetail(
                                mContext.get(),
                                receivedDetail.getType(),
                                wishDetail.getFromPhotoUrl(),
                                wishDetail.getFromName(),
                                wishDetail.getFromUid(),
                                wishDetail.getExtraPhotoUrl(),
                                wishDetail.getText(),
                                wishDetail.getWhenToArrive(),
                                wishDetail.getOccasion(),
                                null
                        )
                );
            } else if (eventDetail != null) {
                mEventNum++;
                pdfDetailList.add(
                        PdfUtils.addPdfDetail(
                                mContext.get(),
                                receivedDetail.getType(),
                                eventDetail.getFromPhotoUrl(),
                                eventDetail.getFromName(),
                                eventDetail.getFromUid(),
                                eventDetail.getExtraPhotoUrl(),
                                eventDetail.getText(),
                                eventDetail.getWhenToArrive(),
                                eventDetail.getTitle(),
                                eventDetail.getPlace()
                        )
                );
            } else if (thoughtDetail != null) {
                mThoughtNum++;
                pdfDetailList.add(
                        PdfUtils.addPdfDetail(
                                mContext.get(),
                                receivedDetail.getType(),
                                thoughtDetail.getFromPhotoUrl(),
                                thoughtDetail.getFromName(),
                                thoughtDetail.getFromUid(),
                                thoughtDetail.getExtraPhotoUrl(),
                                thoughtDetail.getText(),
                                thoughtDetail.getTimestamp(),
                                null,
                                null
                        )
                );
            }
        }
        Collections.sort(pdfDetailList, new Comparator<PdfDetail>() {
            @Override
            public int compare(PdfDetail pdfDetailOne, PdfDetail pdfDetailTwo) {
                return pdfDetailOne.getType().compareToIgnoreCase(pdfDetailTwo.getType());
            }
        });
        return pdfDetailList;
    }

    private void createDocumentAndHeader() {
        String pdfName = mCurrentName.replaceAll(" ", "_")
                + mContext.get().getResources().getString(R.string.pdf_creation_all_file_title);
        String pdfExtension = ".pdf";

        mDestinationFile = new File(android.os.Environment.getExternalStorageDirectory()
                + File.separator
                + mContext.get().getResources().getString(R.string.app_name),
                pdfName + pdfExtension);
        try {
            if (!mDestinationFile.getParentFile().exists()) {
                mDestinationFile.getParentFile().mkdirs();
            }
            if (!mDestinationFile.exists()) {
                mDestinationFile.createNewFile();
            }

            Document document = new Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(mDestinationFile));
            pdfWriter.setPageEvent(new PdfPageEventHandler(mContext.get(), FONT_FAMILY));
            document.open();
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor(mContext.get().getResources().getString(R.string.app_name));
            document.addCreator("iText");

            Map<String, Integer> timeWentByInfo;
            String currentLocalTime = ApplicationUtils.getLocalDateAndTime(
                    mContext.get(),
                    ApplicationUtils.getCurrentUTCDateAndTime()
            );
            String currentUserRegistrationTimeLocal = ApplicationUtils.getLocalDateAndTime(
                    mContext.get(),
                    mCurrentRegistrationTime
            );
            timeWentByInfo = PdfUtils.getTimeWentByInfo(
                    currentLocalTime,
                    currentUserRegistrationTimeLocal
            );
            Font welcomeFont = new Font(FONT_FAMILY, EXTRA_FONT_SIZE, Font.BOLD, mPrimaryDarkColor);
            Chunk nameChunk = new CustomChunk(
                    mContext.get(),
                    mCurrentName
                            + mContext.get()
                            .getResources().getString(R.string.pdf_creation_all_doc_title_part),
                    welcomeFont
            );
            Font headerNumberFont = new Font(FONT_FAMILY, EXTRA_FONT_SIZE, Font.BOLDITALIC, mAccentColor);
            Font headerNumberHelperFont = new Font(FONT_FAMILY, GENERAL_FONT_SIZE, Font.ITALIC, mAccentColor);
            Font headerGeneralFont = new Font(FONT_FAMILY, GENERAL_FONT_SIZE, Font.NORMAL, mPrimaryColor);

            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidths(new int[]{1,2});
            headerTable.addCell(PdfUtils.addImageCell(mCurrentImage));
            Paragraph headerContentParagraph = new Paragraph();
            headerContentParagraph.add(nameChunk);
            headerContentParagraph.add(Chunk.NEWLINE);
            headerContentParagraph.add(Chunk.NEWLINE);

            NotificationUtils.addPdfNotification(mContext.get(), mDestinationFile, false, 3);

            if (timeWentByInfo != null) {
                headerContentParagraph.add(
                        new CustomChunk(
                                mContext.get(),
                                mContext.get()
                                        .getResources()
                                        .getString(R.string.pdf_creation_all_doc_since),
                                headerGeneralFont
                        )
                );
                headerContentParagraph.add(Chunk.NEWLINE);
                Integer dayNum = timeWentByInfo.get(PdfUtils.PDF_DAY_ID);
                Integer hourNum = timeWentByInfo.get(PdfUtils.PDF_HOUR_ID);
                Integer minuteNum = timeWentByInfo.get(PdfUtils.PDF_MINUTE_ID);
                Integer secNum = timeWentByInfo.get(PdfUtils.PDF_SEC_ID);
                PdfUtils.importantNumberAndHelperTextHandler(
                        dayNum,
                        headerContentParagraph,
                        headerNumberFont,
                        headerNumberHelperFont,
                        mContext.get().getResources().getString(R.string.day),
                        mContext.get().getResources().getString(R.string.days),
                        mContext.get()
                );
                PdfUtils.importantNumberAndHelperTextHandler(
                        hourNum,
                        headerContentParagraph,
                        headerNumberFont,
                        headerNumberHelperFont,
                        mContext.get().getResources().getString(R.string.hour),
                        mContext.get().getResources().getString(R.string.hours),
                        mContext.get()
                );
                PdfUtils.importantNumberAndHelperTextHandler(
                        minuteNum,
                        headerContentParagraph,
                        headerNumberFont,
                        headerNumberHelperFont,
                        mContext.get().getResources().getString(R.string.minute),
                        mContext.get().getResources().getString(R.string.minutes),
                        mContext.get()
                );
                PdfUtils.importantNumberAndHelperTextHandler(
                        secNum,
                        headerContentParagraph,
                        headerNumberFont,
                        headerNumberHelperFont,
                        mContext.get().getResources().getString(R.string.second),
                        mContext.get().getResources().getString(R.string.seconds),
                        mContext.get()
                );
                headerContentParagraph.add(Chunk.NEWLINE);
                headerContentParagraph.add(Chunk.NEWLINE);
            }
            int displayedMemoriesNum = mDisplayedReceivedDetailList.size();
            headerContentParagraph.add(
                    new CustomChunk(
                            mContext.get(),
                            new StringBuilder()
                            .append(mContext.get().getResources().getString(R.string.pdf_creation_all_doc_displayed_1))
                            .append(displayedMemoriesNum)
                            .append(displayedMemoriesNum == 1 ?
                                    mContext.get().getResources().getString(R.string.pdf_creation_all_doc_displayed_2_single) :
                                    mContext.get().getResources().getString(R.string.pdf_creation_all_doc_displayed_2_plural)
                            ).toString(),
                            headerGeneralFont
                    )
            );
            headerContentParagraph.add(Chunk.NEWLINE);
            PdfUtils.importantNumberAndHelperTextHandler(
                    mEventNum,
                    headerContentParagraph,
                    headerNumberFont,
                    headerNumberHelperFont,
                    mContext.get().getResources().getString(R.string.pdf_event),
                    mContext.get().getResources().getString(R.string.pdf_events),
                    mContext.get()
            );
            PdfUtils.importantNumberAndHelperTextHandler(
                    mThoughtNum,
                    headerContentParagraph,
                    headerNumberFont,
                    headerNumberHelperFont,
                    mContext.get().getResources().getString(R.string.pdf_thought),
                    mContext.get().getResources().getString(R.string.pdf_thoughts),
                    mContext.get()
            );
            PdfUtils.importantNumberAndHelperTextHandler(
                    mWishNum,
                    headerContentParagraph,
                    headerNumberFont,
                    headerNumberHelperFont,
                    mContext.get().getResources().getString(R.string.pdf_wish),
                    mContext.get().getResources().getString(R.string.pdf_wishes),
                    mContext.get()
            );
            headerTable.addCell(PdfUtils.addParagraphCell(headerContentParagraph));
            document.add(headerTable);

            NotificationUtils.addPdfNotification(mContext.get(), mDestinationFile, false, 4);
            document.add(new Paragraph(" "));
            PdfUtils.createContent(
                    mPdfDetailList,
                    mContext.get(),
                    mDestinationFile,
                    mLineSeparatorGeneral,
                    mLineSeparatorThick,
                    mMemoryHeaderFont,
                    mPrimaryDarkFont,
                    mPrimaryFont,
                    mAccentFont,
                    mSectionHeaderFont,
                    document,
                    pdfWriter,
                    true);
            NotificationUtils.addPdfNotification(mContext.get(), mDestinationFile, false, 6);

            document.close();

            NotificationUtils.addPdfNotification(mContext.get(), mDestinationFile, true, 0);
        } catch (IOException e) {
            Timber.wtf("pdf - " + e.getMessage());
            PdfUtils.showError(mContext.get(), e.getMessage());
        } catch (DocumentException e) {
            Timber.wtf("pdf document - " + e.getMessage());
            PdfUtils.showError(mContext.get(), e.getMessage());
        }
    }
}
