package com.example.demo;

import lombok.Data;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class TextStripper extends PDFTextStripper {

    private List<Map<String, Object>> fontData;

    public TextStripper() throws IOException {
        super();
        this.fontData = new ArrayList<>();
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        Map<String, Object> fontInfo = new HashMap<>();

        fontInfo.put("page", this.getCurrentPageNo());

        fontInfo.put("width", text.getWidth());
        fontInfo.put("height", text.getHeight());

        fontInfo.put("font", text.getFont());

        fontInfo.put("text", text.getUnicode());

        fontInfo.put("size", text.getFontSizeInPt());

        fontInfo.put("x", text.getX());
        fontInfo.put("y", text.getY());

        fontData.add(fontInfo);
    }

}
