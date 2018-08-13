package karsai.laszlo.bringcloser.model;

import com.itextpdf.text.Image;

public class PdfDetail {

    private Image imageExtra;
    private String timestamp;
    private String message;
    private String type;
    private String fromName;
    private String fromUid;
    private Image fromImage;
    private String whyId;
    private String eventPlace;

    public PdfDetail(
            Image imageExtra,
            String timestamp,
            String message,
            String type,
            String fromName,
            String fromUid,
            Image fromImage,
            String whyId,
            String eventPlace) {
        this.imageExtra = imageExtra;
        this.timestamp = timestamp;
        this.message = message;
        this.type = type;
        this.fromName = fromName;
        this.fromUid = fromUid;
        this.fromImage = fromImage;
        this.whyId = whyId;
        this.eventPlace = eventPlace;
    }

    public PdfDetail() {}

    public String getWhyId() {
        return whyId;
    }

    public void setWhyId(String whyId) {
        this.whyId = whyId;
    }

    public String getEventPlace() {
        return eventPlace;
    }

    public void setEventPlace(String eventPlace) {
        this.eventPlace = eventPlace;
    }

    public Image getImageExtra() {
        return imageExtra;
    }

    public void setImageExtra(Image imageExtra) {
        this.imageExtra = imageExtra;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public Image getFromImage() {
        return fromImage;
    }

    public void setFromImage(Image fromImage) {
        this.fromImage = fromImage;
    }
}
