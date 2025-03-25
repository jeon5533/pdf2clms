package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.bouncycastle.pqc.legacy.crypto.ntru.IndexGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PdfController {

    private final Util util;

    @GetMapping("/form")
    public String pdfForm() {
        return "form";
    }

    @GetMapping("/test")
    public String test() {
        return "sp";
    }

    // text , img 추출해서 html로 뿌리기
    @PostMapping("/pdfToHtml")
    @ResponseBody
    public String uploadPdf(@RequestParam("pdf") MultipartFile pdf , @RequestParam("pdfName") String pdfName) throws Exception {
        
        long startTime = System.currentTimeMillis();
        
        // pdf 문서
        PDDocument document = Loader.loadPDF(Util.convertMultipartFileToFile(pdf));

        // PDF 텍스트 추출 (폰트 정보 포함)
        TextStripper fontExtractor = new TextStripper();
        fontExtractor.getText(document);
        List<Map<String, Object>> fontData = fontExtractor.getFontData();

        // 페이지별 html
        Map<Integer, StringBuilder> pageDivs = new HashMap<>();

        // 이미지 추출
        util.exportImg(document , pdfName);

        // 테두리 정보
        List<Map<String, Double>> border = util.getBolderLoc();

        for (Map<String, Object> font : fontData) {
            
            // 현재 루프 텍스트의 페이지
            int page = (int) font.get("page");
            // text 메타정보
            float x = (float) font.get("x");
            float y = (float) font.get("y");
            float fontSize = (float) font.get("size");
            String text = (String) font.get("text");

            // System.out.println( ((PDFont)font.get("font")).getName() );

            // 페이지 생성
            pageDivs.putIfAbsent(page, new StringBuilder());

            // 각 페이지 div에 텍스트 추가
            pageDivs.get(page)
                    .append("<div style=\"")
                    .append("left: ").append(x).append("px; ")
                    .append("top: ").append(y).append("px; ")
                    .append("position: absolute; ")
                    .append("font-size: ").append(fontSize).append("px;\">")
                    .append(text)
                    .append("</div>");
        }


        // 페이지 border 생성
        StringBuilder finalHtml = new StringBuilder();
        for (int page : pageDivs.keySet()) {
            PDPage pdfPage = document.getPage(page - 1);
            PDRectangle mediaBox = pdfPage.getMediaBox();
            float pdfWidth = mediaBox.getWidth();
            float pdfHeight = mediaBox.getHeight();

            // 각 페이지를 감싸는 div 추가
            finalHtml.append("<div style=\"")
                    .append("width: ").append(pdfWidth).append("px; ")
                    .append("height: ").append(pdfHeight).append("px; ")
                    .append("position: relative; ")
                    .append("border: 1px solid #ccc; ")
                    .append("margin-bottom: 20px;\">") // 페이지 구분
                    .append(pageDivs.get(page)) // 페이지 내 텍스트 추가
                    .append("</div>");
        }

        document.close();




        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) + " millSec");

        return finalHtml.toString();
    }

    @PostMapping("/getText")
    @ResponseBody
    public Map<String , String> getText(@RequestParam(name = "left") String left,
                                        @RequestParam(name = "top") String top,
                                        @RequestParam(name = "width") String width,
                                        @RequestParam(name = "height") String height,
                                        @RequestParam(name = "type") String type,
                                        @RequestParam(name = "pdf") MultipartFile pdf ,
                                        @RequestParam(name = "pdfHeight" , required = false) String pdfHeight){


        try {
            // PDF 문서 로드
            PDDocument document = Loader.loadPDF(Util.convertMultipartFileToFile(pdf));

            // ---------------------------------           1페이지만 하고있음. 다른 페이지들 처리 필요.
            // 첫 번째 페이지 가져오기
            PDPage page = document.getPage(0);

            // 추출할 영역을 정의
            Rectangle rect = new Rectangle(Integer.parseInt(left), Integer.parseInt(top), Integer.parseInt(width), Integer.parseInt(height));

            // PDFTextStripperByArea 객체 생성
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.addRegion("region1", rect);

            // 페이지에서 텍스트 추출
            stripper.extractRegions(page);

            // 특정 영역의 텍스트 출력
            String text = stripper.getTextForRegion("region1");
            System.out.println(text);
            System.out.println("=========================");

            // 문서 닫기
            document.close();

        } catch (IOException e) {
            e.printStackTrace();
        }



        return null;
    }


}
