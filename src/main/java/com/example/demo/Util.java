package com.example.demo;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class Util {



    public static File convertMultipartFileToFile(MultipartFile file) throws IOException {

        File convFile = File.createTempFile("uploaded-", ".pdf"); // 임시 파일 생성
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();

        return convFile;
    }

    public void replaceInStringBuilder(StringBuilder sb, String target, String replacement) {
        int index;
        while ((index = sb.indexOf(target)) != -1) {
            sb.replace(index, index + target.length(), replacement);
        }
    }

    public void exportImg(PDDocument doc , String pdfName){

        try {

            // 추출 이미지 저장 폴더 생성
            String id = createCode();
            String outputFolder = "C:\\Users\\jeon\\Desktop\\test_sample\\mvp\\export_img\\"+id+"_"+pdfName;
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }


            int imgIndex = 1; // 이미지 번호 (중복 방지)
            int pageIndex = 1;
            for (PDPage page : doc.getPages()) {
                for (COSName cosName : page.getResources().getXObjectNames()) {
                    if (page.getResources().isImageXObject(cosName)) {
                        // PDF에서 이미지 추출
                        PDImageXObject pdImg = (PDImageXObject) page.getResources().getXObject(cosName);
                        BufferedImage bufImg = pdImg.getImage();

                        String imagePath = outputFolder + "/" +pageIndex + "_" + imgIndex + ".png";
                        File imageFile = new File(imagePath);

                        // 이미지 저장
                        ImageIO.write(bufImg, "png", imageFile);
                        System.out.println("이미지 저장 완료: " + imagePath);

                        imgIndex++; // 이미지 파일명 중복 방지
                    }
                }
                pageIndex++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private static void extractImagesFromFormXObject(PDFormXObject formXObject, String outputFolder) throws IOException {
        for (COSName cosName : formXObject.getResources().getXObjectNames()) {
            PDXObject xObject = formXObject.getResources().getXObject(cosName);

            if (xObject instanceof PDImageXObject) {
                PDImageXObject image = (PDImageXObject) xObject;
                BufferedImage bufferedImage = image.getImage();
                String imagePath = outputFolder + "/embedded_x_y.png";

                File outputFile = new File(imagePath);
                ImageIO.write(bufferedImage, "png", outputFile);
                System.out.println("Extracted: " + imagePath);
            }
        }
    }

    public String createCode(){

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSSSSS");
        Random random = new Random();
        char r1 = (char) (random.nextBoolean() ? random.nextInt(26) + 'A' : random.nextInt(26) + 'a');
        char r2 = (char) (random.nextBoolean() ? random.nextInt(26) + 'A' : random.nextInt(26) + 'a');
        char r3 = (char) (random.nextBoolean() ? random.nextInt(26) + 'A' : random.nextInt(26) + 'a');

        int i1 = random.nextInt(10);
        int i2 = random.nextInt(10);
        int i3 = random.nextInt(10);

        //return String.valueOf(r1) + i1 + String.valueOf(r2) + i2 + String.valueOf(r3) + i3 +"_" + now.format(formatter);
        return now.format(formatter);
    }


    public List<Map<String, Double>> getBolderLoc() throws Exception{

        // JSON 파일을 읽어오기
        FileReader reader = new FileReader("C:\\Users\\jeon\\Desktop\\test_sample\\mvp\\export_img\\res_json.json");

        // JSON 데이터를 파싱
        StringBuilder jsonContent = new StringBuilder();
        int i;
        while ((i = reader.read()) != -1) {
            jsonContent.append((char) i);
        }
        String jsonString = jsonContent.toString();

        // JSONArray로 파싱
        JSONArray jsonArray = new JSONArray(jsonString);

        // List<Map>으로 변환
        List<Map<String, Double>> result = new ArrayList<>();

        for (int j = 0; j < jsonArray.length(); j++) {
            JSONObject jsonObject = jsonArray.getJSONObject(j);
            Map<String, Double> map = new HashMap<>();

            // JSON 객체의 데이터를 Map에 추가
            map.put("page", Double.valueOf(jsonObject.getInt("page")));
            map.put("x", jsonObject.getDouble("x"));
            map.put("y", jsonObject.getDouble("y"));
            map.put("width", jsonObject.getDouble("width"));
            map.put("height", jsonObject.getDouble("height"));

            result.add(map);
        }

        return result;

    }



}
