package karsai.laszlo.bringcloser.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.intellij.lang.annotations.RegExp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import karsai.laszlo.bringcloser.CustomChunk;
import karsai.laszlo.bringcloser.R;
import karsai.laszlo.bringcloser.model.Connection;
import karsai.laszlo.bringcloser.model.PdfDetail;
import timber.log.Timber;

public class PdfUtils {

    public static final String PDF_DAY_ID = "pdf_day";
    public static final String PDF_HOUR_ID = "pdf_hour";
    public static final String PDF_MINUTE_ID = "pdf_minute";
    public static final String PDF_SEC_ID = "pdf_sec";
    private static final float NEW_PAGE_THRESHOLD = 200.0F;

    public static void createContent(
            List<PdfDetail> pdfDetailList,
            Context context,
            File destinationFile,
            LineSeparator lineSeparatorGeneral,
            LineSeparator lineSeparatorThick,
            Font memoryHeaderFont,
            Font primaryDarkFont,
            Font primaryFont,
            Font accentFont,
            Font sectionHeaderFont,
            Document document,
            PdfWriter pdfWriter,
            boolean isAll) throws DocumentException, IOException {
        String type = "";
        boolean shouldAvoidDivider = false;
        int size = pdfDetailList.size();
        int sectionItemCounter = 0;
        for (int i = 0; i < size; i++) {
            float currentVerticalPos = pdfWriter.getVerticalPosition(false);
            if (currentVerticalPos <= NEW_PAGE_THRESHOLD) {
                document.newPage();
            }
            if (i >= size / 2) {
                NotificationUtils.addPdfNotification(context, destinationFile, false, 5);
            }
            PdfDetail pdfDetail = pdfDetailList.get(i);
            String currentType = pdfDetail.getType();
            if (type.isEmpty() || !currentType.equals(type)) {
                type = currentType;
                boolean isSectionAdded = addSectionHeader(
                        type,
                        context,
                        sectionHeaderFont,
                        document,
                        lineSeparatorThick);
                if (!isSectionAdded) {
                    continue;
                }
                shouldAvoidDivider = true;
                sectionItemCounter = 0;
            }
            if (!shouldAvoidDivider) {
                document.add(lineSeparatorGeneral);
            }

            sectionItemCounter++;
            addMemory(
                    pdfDetail,
                    sectionItemCounter,
                    context,
                    memoryHeaderFont,
                    primaryDarkFont,
                    primaryFont,
                    accentFont,
                    document,
                    isAll);
            if (!isAll) {
                document.add(Chunk.NEWLINE);
            }
            shouldAvoidDivider = false;
        }
    }

    private static String getMemoryHeader(int sectionItemCounter, String type, Context context) {
        StringBuilder stringBuilder = new StringBuilder()
                .append(sectionItemCounter)
                .append(". ");
        if (type.equals(ApplicationUtils.TYPE_WISH_IDENTIFIER)) {
            stringBuilder.append(context.getResources().getString(R.string.received_detail_wish));
        } else if (type.equals(ApplicationUtils.TYPE_EVENT_IDENTIFIER)) {
            stringBuilder.append(context.getResources().getString(R.string.received_detail_event));
        } else {
            stringBuilder.append(context.getResources().getString(R.string.received_detail_thought));
        }
        return stringBuilder.toString();
    }

    private static void addMemory(
            PdfDetail pdfDetail,
            int sectionItemCounter,
            Context context,
            Font memoryHeaderFont,
            Font primaryDarkFont,
            Font primaryFont,
            Font accentFont,
            Document document,
            boolean shouldIncludeFromCell)
            throws DocumentException, IOException {
        PdfPTable pdfPTable;
        Image image = pdfDetail.getImageExtra();
        if (image == null) {
            if (shouldIncludeFromCell) {
                pdfPTable = new PdfPTable(2);
                pdfPTable.setWidths(new int[]{2,1});
                pdfPTable.addCell(
                        addMemoryHeaderParagraphCell(
                                new Paragraph(
                                        new CustomChunk(
                                                context,
                                                getMemoryHeader(
                                                        sectionItemCounter,
                                                        pdfDetail.getType(),
                                                        context
                                                ),memoryHeaderFont
                                        )
                                ), 2
                        )
                );
                pdfPTable.addCell(addTableCell(addContentTable(pdfDetail, context, accentFont, primaryFont)));
                pdfPTable.addCell(addTableCell(addFromTable(pdfDetail, context, primaryDarkFont)));
            } else {
                pdfPTable = new PdfPTable(1);
                pdfPTable.addCell(
                        addMemoryHeaderParagraphCell(
                                new Paragraph(
                                        new CustomChunk(
                                                context,
                                                getMemoryHeader(
                                                        sectionItemCounter,
                                                        pdfDetail.getType(),
                                                        context
                                                ),memoryHeaderFont
                                        )
                                ), 1
                        )
                );
                pdfPTable.addCell(addTableCell(addContentTable(pdfDetail, context, accentFont, primaryFont)));
            }
        } else {
            if (shouldIncludeFromCell) {
                pdfPTable = new PdfPTable(3);
                pdfPTable.setWidths(new int[]{1,2,1});
                pdfPTable.addCell(
                        addMemoryHeaderParagraphCell(
                                new Paragraph(
                                        new CustomChunk(
                                                context,
                                                getMemoryHeader(
                                                        sectionItemCounter,
                                                        pdfDetail.getType(),
                                                        context
                                                ),memoryHeaderFont
                                        )
                                ), 3
                        )
                );
                pdfPTable.addCell(addImageCell(image));
                pdfPTable.addCell(addTableCell(addContentTable(pdfDetail, context, accentFont, primaryFont)));
                pdfPTable.addCell(addTableCell(addFromTable(pdfDetail, context, primaryDarkFont)));
            } else {
                pdfPTable = new PdfPTable(2);
                pdfPTable.setWidths(new int[]{1,2});
                pdfPTable.addCell(
                        addMemoryHeaderParagraphCell(
                                new Paragraph(
                                        new CustomChunk(
                                                context,
                                                getMemoryHeader(
                                                        sectionItemCounter,
                                                        pdfDetail.getType(),
                                                        context
                                                ),memoryHeaderFont
                                        )
                                ), 2
                        )
                );
                pdfPTable.addCell(addImageCell(image));
                pdfPTable.addCell(addTableCell(addContentTable(pdfDetail, context, accentFont, primaryFont)));
            }
        }
        pdfPTable.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        document.add(new Paragraph(" "));
        document.add(pdfPTable);
    }

    private static PdfPTable addFromTable(PdfDetail pdfDetail, Context context, Font primaryDarkFont) {
        PdfPTable table = new PdfPTable(1);
        table.addCell(
                addParagraphCell(
                        new Paragraph(
                                new CustomChunk(
                                        context,
                                        pdfDetail.getFromName(),
                                        primaryDarkFont
                                )
                        )
                )
        );
        table.addCell(
                addImageCell(pdfDetail.getFromImage(), 10)
        );
        return table;
    }

    private static PdfPTable addContentTable(
            PdfDetail pdfDetail,
            Context context,
            Font accentFont,
            Font primaryFont) throws DocumentException, IOException {
        Paragraph contentDetailParagraph = new Paragraph();
        String whyId = pdfDetail.getWhyId();
        String eventPlace = pdfDetail.getEventPlace();
        String timestamp = ApplicationUtils.getLocalDateAndTimeToDisplay(
                context,
                pdfDetail.getTimestamp()
        );
        boolean hasDetail = false;
        if (whyId != null) {
            contentDetailParagraph.add(
                    new CustomChunk(
                            context,
                            pdfDetail.getType().equals(ApplicationUtils.TYPE_WISH_IDENTIFIER) ?
                                    ApplicationUtils.getTranslatedWishOccasion(context, whyId) :
                                    whyId,
                            accentFont
                    )
            );
            contentDetailParagraph.add(Chunk.NEWLINE);
            contentDetailParagraph.add(Chunk.NEWLINE);
            hasDetail = true;
        }
        if (eventPlace != null) {
            contentDetailParagraph.add(
                    new CustomChunk(
                            context,
                            eventPlace,
                            accentFont
                    )
            );
            contentDetailParagraph.add(Chunk.NEWLINE);
            contentDetailParagraph.add(Chunk.NEWLINE);
            hasDetail = true;
        }
        if (!timestamp.equals(context.getResources().getString(R.string.data_not_available))) {
            contentDetailParagraph.add(
                    new CustomChunk(
                            context,
                            timestamp,
                            accentFont
                    )
            );
            hasDetail = true;
        }
        PdfPTable contentTable;
        if (hasDetail) {
            contentTable = new PdfPTable(2);
            contentTable.setWidths(new float[]{1.5F,1.0F});
        } else {
            contentTable = new PdfPTable(1);
        }
        contentTable.addCell(
                addParagraphCell(
                        addEmojiHandlerParagraph(
                                context,
                                primaryFont,
                                pdfDetail.getMessage()
                        ), 5
                )
        );
        if (hasDetail) {
            contentTable.addCell(addParagraphCell(contentDetailParagraph, 5));
        }
        return contentTable;
    }

    private static Paragraph addEmojiHandlerParagraph(Context context, Font primaryFont, String message) throws IOException, BadElementException {
        /*
        regex is from:
        https://stackoverflow.com/questions/28366172/check-if-letter-is-emoji
         */
        String emojiRegex = "([\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";
        Paragraph emojiHandledParagraph = new Paragraph();
        String textWithoutEmoji = "";
        boolean isEmojiFound = false;
        for (int i = 0; i < message.length(); i++) {
            if (i == message.length() - 1) {
                if (!isEmojiFound) {
                    textWithoutEmoji += message.charAt(i);
                    emojiHandledParagraph.add(
                            new CustomChunk(
                                    context,
                                    textWithoutEmoji,
                                    primaryFont
                            )
                    );
                }
                continue;
            }
            String messagePart = message.substring(i, i + 2);
            isEmojiFound = false;
            Matcher matcher = Pattern.compile(emojiRegex).matcher(messagePart);
            if (matcher.find()) {
                emojiHandledParagraph.add(
                        new CustomChunk(
                                context,
                                textWithoutEmoji,
                                primaryFont
                        )
                );
                emojiHandledParagraph.add(
                        new Chunk(
                                getEmojiImage(context, matcher.group()), 0, 0
                        )
                );
                textWithoutEmoji = "";
                isEmojiFound = true;
            }
            if (!isEmojiFound) {
                textWithoutEmoji += message.charAt(i);
            }
        }
        return emojiHandledParagraph;
    }

    private static boolean addSectionHeader(
            String type,
            Context context,
            Font sectionHeaderFont,
            Document document,
            LineSeparator lineSeparatorThick) throws DocumentException {
        Paragraph sectionHeaderParagraph;
        if (type.equals(ApplicationUtils.TYPE_WISH_IDENTIFIER)) {
            sectionHeaderParagraph = new Paragraph(
                    context.getResources().getString(R.string.pdf_collection_wishes),
                    sectionHeaderFont
            );
        } else if (type.equals(ApplicationUtils.TYPE_EVENT_IDENTIFIER)) {
            sectionHeaderParagraph = new Paragraph(
                    context.getResources().getString(R.string.pdf_collection_events),
                    sectionHeaderFont
            );
        } else if (type.equals(ApplicationUtils.TYPE_THOUGHT_IDENTIFIER)) {
            sectionHeaderParagraph = new Paragraph(
                    context.getResources().getString(R.string.pdf_collection_thoughts),
                    sectionHeaderFont
            );
        } else {
            return false;
        }
        document.add(lineSeparatorThick);
        sectionHeaderParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(sectionHeaderParagraph);
        document.add(new Paragraph(" "));
        document.add(lineSeparatorThick);
        return true;
    }

    public static void importantNumberAndHelperTextHandler(
            Integer num,
            Paragraph paragraph,
            Font numberFormat,
            Font numberHelperFont,
            String helperSingle,
            String helperPlural,
            Context context) {
        if (num != 0) {
            paragraph.add(
                    new CustomChunk(
                            context,
                            String.valueOf(num),
                            numberFormat
                    )
            );
            String helperText;
            if (num == 1) {
                helperText = helperSingle;
            } else {
                helperText = helperPlural;
            }
            paragraph.add(
                    new CustomChunk(
                            context,
                            helperText,
                            numberHelperFont
                    )
            );
            paragraph.add(new Paragraph(" "));
        }
    }

    private static PdfPCell addMemoryHeaderParagraphCell(
            Paragraph paragraph,
            int colNum) {
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setColspan(colNum);
        cell.setPadding(0);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public static PdfPCell addParagraphCell(Paragraph paragraph) {
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setPadding(0);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public static PdfPCell addParagraphCell(Paragraph paragraph, int padding) {
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setPadding(padding);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private static PdfPCell addTableCell(PdfPTable table) {
        PdfPCell cell = new PdfPCell(table);
        cell.setPadding(0);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public static PdfPCell addImageCell(Image image) {
        PdfPCell cell = new PdfPCell(image);
        cell.setPadding(0);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private static PdfPCell addImageCell(Image image, int padding) {
        PdfPCell cell = new PdfPCell(image);
        cell.setPadding(padding);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    public static Map<String, Integer> getTimeWentByInfo(String currentTime, String timeInPast) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                ApplicationUtils.FULL_DATE_PATTERN,
                Locale.getDefault()
        );
        try {
            Date currentDate = sdf.parse(currentTime);
            Date dateInPast = sdf.parse(timeInPast);
            long diffInMilliseconds = Math.abs(currentDate.getTime() - dateInPast.getTime());
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;
            long elapsedDays = diffInMilliseconds / daysInMilli;
            diffInMilliseconds = diffInMilliseconds % daysInMilli;
            long elapsedHours = diffInMilliseconds / hoursInMilli;
            diffInMilliseconds = diffInMilliseconds % hoursInMilli;
            long elapsedMinutes = diffInMilliseconds / minutesInMilli;
            diffInMilliseconds = diffInMilliseconds % minutesInMilli;
            long elapsedSeconds = diffInMilliseconds / secondsInMilli;
            Map<String, Integer> resultMap = new HashMap<>();
            resultMap.put(PDF_DAY_ID, (int)elapsedDays);
            resultMap.put(PDF_HOUR_ID, (int)elapsedHours);
            resultMap.put(PDF_MINUTE_ID, (int)elapsedMinutes);
            resultMap.put(PDF_SEC_ID, (int)elapsedSeconds);
            return resultMap;
        } catch (ParseException e) {
            return null;
        }
    }

    public static PdfDetail addPdfDetail(
            Context context,
            String type,
            String fromPhotoUrl,
            String fromName,
            String fromUid,
            String extraPhotoUrl,
            String text,
            String timestamp,
            String whyId,
            String eventPlace) throws IOException, BadElementException {
        PdfDetail pdfDetail = new PdfDetail();
        pdfDetail.setType(type);
        if (fromPhotoUrl == null) {
            pdfDetail.setFromImage(getDefaultImage(context));
        } else {
            pdfDetail.setFromImage(getImage(fromPhotoUrl));
        }
        pdfDetail.setFromName(fromName);
        pdfDetail.setFromUid(fromUid);
        if (extraPhotoUrl != null) {
            pdfDetail.setImageExtra(getImage(extraPhotoUrl));
        }
        pdfDetail.setMessage(text);
        pdfDetail.setTimestamp(timestamp);
        pdfDetail.setWhyId(whyId);
        pdfDetail.setEventPlace(eventPlace);
        return pdfDetail;
    }

    public static Image getImage(String photoUrl) throws IOException, BadElementException {
        URL url = new URL(photoUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        int rotDegree = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            rotDegree = ApplicationUtils.getExifRotation(connection.getInputStream());
        }
        connection.disconnect();
        Image image = Image.getInstance(new URL(photoUrl));
        image.scaleAbsolute(100F, 100F);
        switch (rotDegree) {
            case 90:
                image.setRotationDegrees(-rotDegree);
                break;
            case 180:
                image.setRotationDegrees(rotDegree);
                break;
            case 270:
                image.setRotationDegrees(rotDegree - 180);
                break;
            default:
                break;
        }
        return image;
    }

    public static Image getDefaultImage(Context context) throws IOException, BadElementException {
        Bitmap bmp = decodeSampledBitmapFromResource(
                context.getResources(),
                R.drawable.baseline_face_black_48
        );
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image image = Image.getInstance(stream.toByteArray());
        image.scaleAbsolute(100F, 100F);
        return image;
    }

    private static Bitmap decodeSampledBitmapFromResource(
            Resources res,
            int resId) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = ApplicationUtils.calculateInSampleSize(options, 100, 100);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static void showError(Context context, String message) {
        Activity activity = (Activity) context;
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                context.getResources().getString(R.string.problem) + " " + message,
                Snackbar.LENGTH_LONG
        ).show();
    }

    private static Image getEmojiImage(Context context, String emoji) throws IOException, BadElementException {
        TextView tv = new TextView(context);
        int width = 47;
        int height = 53;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        tv.setLayoutParams(layoutParams);
        tv.setText(emoji);
        tv.setTextColor(Color.BLACK);
        tv.setBackgroundColor(Color.TRANSPARENT);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        tv.layout(0, 0, width, height);
        tv.draw(c);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image image = Image.getInstance(stream.toByteArray());
        image.scalePercent((float) 12 / ((width + height) / 2) * 100);
        return image;
    }
}
