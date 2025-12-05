package vn.hbtplus.services.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.configs.FontConfig;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.dto.PagePdfDto;
import vn.hbtplus.models.request.ShudRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.repositories.entity.CustomersEntity;
import vn.hbtplus.repositories.entity.EmployeesEntity;
import vn.hbtplus.repositories.entity.ShudArithmeticTypeEntity;
import vn.hbtplus.repositories.entity.ShudPdfPageEntity;
import vn.hbtplus.repositories.impl.ObjectAttributesRepository;
import vn.hbtplus.repositories.impl.ShudPdfRepositoryImpl;
import vn.hbtplus.repositories.jpa.CustomersRepositoryJPA;
import vn.hbtplus.repositories.jpa.EmployeesRepositoryJPA;
import vn.hbtplus.services.ShudService;
import vn.hbtplus.utils.PdfboxUtils;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShudServiceImpl implements ShudService {

    private final FontConfig fontConfig;
    @Value("${folder.template.root}")
    private String folderTemplateRoot;

    @Value("${folder.template.export.crm:../}")
    private String folderExport;

    private final ShudPdfRepositoryImpl shudPdfRepository;

    private final EmployeesRepositoryJPA employeesRepositoryJPA;
    private final CustomersRepositoryJPA customersRepositoryJPA;

    private final ObjectAttributesRepository objectAttributesRepository;

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<String> exportData(ShudRequest.ExportForm dto) {

        if (StringUtils.equalsIgnoreCase(dto.getPersonType(), "NGUOI_LON")) {
            return exportFileAdult(dto);
        } else {
            return exportFileChild(dto);
        }
    }

    private BaseResponseEntity<String> exportFileAdult(ShudRequest.ExportForm dto) {
        try {
            PDDocument document = new PDDocument();
            PDDocumentOutline outline = new PDDocumentOutline();
            document.getDocumentCatalog().setDocumentOutline(outline);
            PDType0Font fontTime = PDType0Font.load(document, new File(fontConfig.getTimes()));
            PDType0Font fontTimeItalic = PDType0Font.load(document, new File(fontConfig.getTimeItalic()));
            // add bia
            PDPage page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_bia.jpg", page, document);
            // but ky
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/but ky.jpg", page, document);
            // add user info
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_user-info.jpg", page, document);
            PdfboxUtils.fillTextToPage(230f, 404f, fontTime, page, document, dto.getFullName(), 16, null);

            PdfboxUtils.fillTextToPage(230f, 350f, fontTime, page, document, dto.getBirthday(), 16, null);

            PdfboxUtils.fillTextToPage(230f, 299f, fontTime, page, document, dto.getMobile(), 16, null);

            PdfboxUtils.fillTextToPage(230f, 244f, fontTime, page, document, dto.getEmail(), 16, null);

            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            String space = "\t\t\t\t\t\t";
            PdfboxUtils.writeTextWithWrapping(contentStream, fontTime, space + StringUtils.trimToEmpty(dto.getAddress()), 135f, 190f, 350f, 16);
            contentStream.close();

            PDRectangle mediaBox = page.getMediaBox();
            float xPage = mediaBox.getWidth() - 25;
            float yPage = 15f;
            int pageIndex = 4;
            // mo dau
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_loi-mo-dau.jpg", page, document);
            PdfboxUtils.addBookmark("Lời mở đầu", outline, page, null, null, true);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // giới thiệu pitago
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_doi-net-pytago.jpg", page, document);
            PdfboxUtils.addBookmark("Đôi nét về số học pytago", outline, page, null, null);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // --- XU LY BIEU DO NGAY SINH ---
            String strChartBirthday = Utils.join("", dto.getChartBirthday());
            List<String> tainang = getTaiNang(strChartBirthday);
            List<String> khuyetThieu = getKhuyetThieu(strChartBirthday);

            dto.setMuiTenTaiNang(tainang);
            dto.setMuiTenKhuyetThieu(khuyetThieu);

            List<String> listFlagChapter1 = List.of("HK_thai_do", "HK_tuong_tac", "HK_so_lap");
            List<String> listFlagChapter2 = List.of("HK_noi_tam", "HK_noi_cam", "HK_trai_nghiem", "HK_truc_giac", "HK_cam_xuc", "HK_logic");
            List<String> listFlagChapter3 = List.of("HK_su_menh", "HK_duong_doi", "HK_ket_noi_duong_doi_va_su_menh", "HK_truong_thanh");
            List<String> listFlagChapter4 = List.of("HK_ngay_sinh", "HK_mui_ten_tai_nang", "HK_mui_ten_khuyet_thieu", "HK_bo_sung");
            List<String> listFlagChapter5 = List.of("HK_4_dinh_cao_cuoc_doi", "HK_thach_thuc", "HK_nam_ca_nhan", "HK_can_bang", "HK_ket_noi_noi_tam_va_tuong_tac");

            boolean isChapter1 = false;
            PDOutlineItem parentChapter1 = new PDOutlineItem();
            boolean isChapter2 = false;
            PDOutlineItem parentChapter2 = new PDOutlineItem();
            boolean isChapter3 = false;
            PDOutlineItem parentChapter3 = new PDOutlineItem();
            boolean isChapter4 = false;
            PDOutlineItem parentChapter4 = new PDOutlineItem();
            boolean isChapter5 = false;
            PDOutlineItem parentChapter5 = new PDOutlineItem();

            Map<String, Object> mapJsonProperty = convertDataToObject(dto);
            List<Pair<String, Object>> pairList = new ArrayList<>();
            pairList.add(new MutablePair<>("person_type", "NGUOI_LON"));
            List<ShudArithmeticTypeEntity> arithmeticTypeList = shudPdfRepository.findByProperties(ShudArithmeticTypeEntity.class, pairList, " weight asc");

            String preNameSlug = "";
            Color color = new Color(105, 189, 68);
            for (ShudArithmeticTypeEntity arithmeticTypeEntity : arithmeticTypeList) {
                String nameSlug = arithmeticTypeEntity.getNameSlug();
                LinkedList<PagePdfDto> pageDataList = new LinkedList<>();
                if (mapJsonProperty.get(nameSlug) != null) {
                    List<Integer> inputNumberList = convertObjectToList(mapJsonProperty.get(nameSlug));

                    List<String> chapterList = List.of("HK_so_lap", "HK_noi_cam", "HK_mui_ten_tai_nang", "HK_mui_ten_khuyet_thieu", "HK_bo_sung", "HK_4_dinh_cao_cuoc_doi", "HK_thach_thuc");
                    if (chapterList.contains(nameSlug)) {
                        pairList.clear();
                        pairList.add(new MutablePair<>("number", inputNumberList));
                        pairList.add(new MutablePair<>("person_type", "NGUOI_LON"));
                        pairList.add(new MutablePair<>("name_slug", nameSlug));
                        List<ShudPdfPageEntity> pdfPageList = shudPdfRepository.findByProperties(ShudPdfPageEntity.class, pairList, null);
                        if (!Utils.isNullOrEmpty(pdfPageList)) {
                            ShudPdfPageEntity pageEntity = pdfPageList.get(0);
                            String name = pageEntity.getName();
                            int index = 0;
                            for (Integer number : inputNumberList) {
                                PagePdfDto pagePdfDto = PagePdfDto.builder().name(name).nameSlug(nameSlug).build();
                                if (index == 0) {
                                    pagePdfDto.setImage(nameSlug + "_" + number + ".jpg");
                                } else {
                                    pagePdfDto.setImage(nameSlug + "_" + number + "_child.jpg");
                                }
                                pageDataList.add(pagePdfDto);
                            }
                        }
                    } else if (!Utils.isNullOrEmpty(inputNumberList)){
                        pairList.clear();
                        pairList.add(new MutablePair<>("number", inputNumberList));
                        pairList.add(new MutablePair<>("person_type", "NGUOI_LON"));
                        pairList.add(new MutablePair<>("name_slug", nameSlug));
                        List<ShudPdfPageEntity> pdfPageList = shudPdfRepository.findByProperties(ShudPdfPageEntity.class, pairList, "number asc");
                        pageDataList.addAll(pdfPageList.stream().map(item -> {
                            PagePdfDto pagePdfDto = new PagePdfDto();
                            Utils.copyProperties(item, pagePdfDto);

                            return pagePdfDto;
                        }).collect(Collectors.toList()));
                    }


                    for (PagePdfDto pagePdfDto : pageDataList) {
                        if (listFlagChapter1.contains(nameSlug) && !isChapter1) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_chuong-1.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 1: THẾ GIỚI BÊN NGOÀI", outline, page, parentChapter1, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter1 = true;
                        } else if (listFlagChapter2.contains(nameSlug) && !isChapter2) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_chuong-2.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 2: THẾ GIỚI BÊN TRONG", outline, page, parentChapter2, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter2 = true;
                        } else if (listFlagChapter3.contains(nameSlug) && !isChapter3) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_chuong-3.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 3: ĐỊNH HƯỚNG CUỘC ĐỜI", outline, page, parentChapter3, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter3 = true;
                        } else if (listFlagChapter4.contains(nameSlug) && !isChapter4) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_chuong-4.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 4: ĐIỂM MẠNH, ĐIỂM YẾU", outline, page, parentChapter4, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter4 = true;
                        } else if (listFlagChapter5.contains(nameSlug) && !isChapter5) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_chuong-5.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 5: CƠ HỘI - THÁCH THỨC & CÂN BẰNG", outline, page, parentChapter5, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter5 = true;
                        }

                        page = new PDPage();
                        document.addPage(page);
                        if (StringUtils.isNotBlank(nameSlug) && !StringUtils.equals(nameSlug, preNameSlug)) {
                            if (listFlagChapter1.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter1, null);
                            } else if (listFlagChapter2.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter2, null);
                            } else if (listFlagChapter3.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter3, null);
                            } else if (listFlagChapter4.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter4, null);
                            } else if (listFlagChapter5.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter5, null);
                            }
                        }
                        String pathImage = folderTemplateRoot + "pdf-page-sample/" + nameSlug + "/" + pagePdfDto.getImage();
                        log.info("Path Image {}", pathImage);
                        PdfboxUtils.addImageFullPage(pathImage, page, document);
                        PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                        preNameSlug = nameSlug;
                    }
                }
            }

            List<String> chartNameList = dto.getChartFullName();
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("Tổng quan bài phân tích số học", outline, page, null, color);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/tong-quan/tong-quan.jpg", page, document);
            PdfboxUtils.fillTextToPage(395f, 770f, fontTime, page, document, dto.getFullName(), 13, Color.WHITE);
            PdfboxUtils.fillTextToPage(395f, 753f, fontTime, page, document, dto.getBirthday(), 13, Color.WHITE);
            String ageOfBirth = dto.getAge() + "t (" + Utils.formatDate(new Date(), "YYYY") + ")";
            PdfboxUtils.fillTextToPage(495f, 753f, fontTime, page, document, ageOfBirth, 13, Color.WHITE);
            PdfboxUtils.fillTextToPage(395f, 735f, fontTime, page, document, dto.getEmail(), 13, Color.WHITE);
            PdfboxUtils.fillTextToPage(395f, 718f, fontTime, page, document, dto.getMobile(), 13, Color.WHITE);

            if (!Utils.isNullOrEmpty(chartNameList.get(1)) && !Utils.isNullOrEmpty(chartNameList.get(2)) && !Utils.isNullOrEmpty(chartNameList.get(3))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/ho-ten-1.png", page, document, 32, 472, 170, 233);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(4)) && !Utils.isNullOrEmpty(chartNameList.get(5)) && !Utils.isNullOrEmpty(chartNameList.get(6))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/ho-ten-2.png", page, document, 16, 472, 270, 233);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(7)) && !Utils.isNullOrEmpty(chartNameList.get(8)) && !Utils.isNullOrEmpty(chartNameList.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/ho-ten-3.png", page, document, 76  , 472, 170, 233);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(1)) && !Utils.isNullOrEmpty(chartNameList.get(5)) && !Utils.isNullOrEmpty(chartNameList.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/ho-ten-7.png", page, document, 12, 470, 280, 240);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(3)) && !Utils.isNullOrEmpty(chartNameList.get(6)) && !Utils.isNullOrEmpty(chartNameList.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/ho-ten-4.png", page, document, 12, 476, 282, 228);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(2)) && !Utils.isNullOrEmpty(chartNameList.get(5)) && !Utils.isNullOrEmpty(chartNameList.get(8))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/ho-ten-5.png", page, document, 12, 478, 282, 228);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(1)) && !Utils.isNullOrEmpty(chartNameList.get(4)) && !Utils.isNullOrEmpty(chartNameList.get(7))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/ho-ten-6.png", page, document,12, 475, 282, 236);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(3)) && !Utils.isNullOrEmpty(chartNameList.get(5)) && !Utils.isNullOrEmpty(chartNameList.get(7))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/ho-ten-8.png", page, document, 12, 475, 282, 233);
            }

            // BIEU DO HO TEN
            PdfboxUtils.fillTextToPage(56f, 524f, fontTime, page, document, chartNameList.get(1), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(56f, 574f, fontTime, page, document, chartNameList.get(2), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(56f, 625f, fontTime, page, document, chartNameList.get(3), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(111f, 524f, fontTime, page, document, chartNameList.get(4), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(111f, 574f, fontTime, page, document, chartNameList.get(5), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(111f, 625f, fontTime, page, document, chartNameList.get(6), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(168f, 524f, fontTime, page, document, chartNameList.get(7), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(168f, 574f, fontTime, page, document, chartNameList.get(8), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(168f, 625f, fontTime, page, document, chartNameList.get(9), 13, Color.WHITE, "C");

            // BIEU DO NGAY SINH
            List<String> chartDate = dto.getChartBirthday();
            if (!Utils.isNullOrEmpty(chartDate.get(1)) && !Utils.isNullOrEmpty(chartDate.get(2)) && !Utils.isNullOrEmpty(chartDate.get(3))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/date-chart/ke-hoach-to-chuc.png", page, document, 20f, 215, 273, 241);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(4)) && !Utils.isNullOrEmpty(chartDate.get(5)) && !Utils.isNullOrEmpty(chartDate.get(6))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/date-chart/y-chi-khat-vong.png", page, document, 20f, 215, 273, 241);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(7)) && !Utils.isNullOrEmpty(chartDate.get(8)) && !Utils.isNullOrEmpty(chartDate.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/date-chart/hanh-dong-chu-dong.png", page, document, 20f  , 215, 273, 241);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(1)) && !Utils.isNullOrEmpty(chartDate.get(5)) && !Utils.isNullOrEmpty(chartDate.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/date-chart/quyet-tam.png", page, document, 16, 215, 273, 240);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(3)) && !Utils.isNullOrEmpty(chartDate.get(6)) && !Utils.isNullOrEmpty(chartDate.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/date-chart/tri-tue-tam-nhin.png", page, document, 15, 205, 280, 251);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(2)) && !Utils.isNullOrEmpty(chartDate.get(5)) && !Utils.isNullOrEmpty(chartDate.get(8))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/date-chart/tam-hon-cam-xuc.png", page, document,15, 213, 280, 251);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(1)) && !Utils.isNullOrEmpty(chartDate.get(4)) && !Utils.isNullOrEmpty(chartDate.get(7))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/date-chart/the-chat-thuc-te.png", page, document, 12, 217, 285, 251);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(3)) && !Utils.isNullOrEmpty(chartDate.get(5)) && !Utils.isNullOrEmpty(chartDate.get(7))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample/tong-quan/date-chart/tam-linh.png", page, document, 15, 216, 283, 240);
            }

            PdfboxUtils.fillTextToPage(57f, 271f, fontTime, page, document, chartDate.get(1), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(57f, 321f, fontTime, page, document, chartDate.get(2), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(57f, 371f, fontTime, page, document, chartDate.get(3), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(112f, 271f, fontTime, page, document, chartDate.get(4), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(112f, 321f, fontTime, page, document, chartDate.get(5), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(112f, 371f, fontTime, page, document, chartDate.get(6), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(167f, 271f, fontTime, page, document, chartDate.get(7), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(167f, 321f, fontTime, page, document, chartDate.get(8), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(167f, 371f, fontTime, page, document, chartDate.get(9), 13, Color.WHITE, "C");

            // DINH CAO CUOC DOI
            PdfboxUtils.fillTextToPage(64f, 81f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(0).toString(), 9, Color.WHITE, "C");
            int yearBirth = Integer.parseInt(dto.getBirthday().split("/")[2]);
            PdfboxUtils.fillTextToPage(73f, 69f, fontTime, page, document, dto.getTuoiDinh1() + "t", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(73f, 61f, fontTime, page, document, "(" + (yearBirth + Integer.parseInt(dto.getTuoiDinh1())) + ")", 9, Color.BLACK, "C");

            PdfboxUtils.fillTextToPage(123f, 110f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(1).toString(), 9, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(133f, 95f, fontTime, page, document, dto.getTuoiDinh2() + "t", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(133f, 87f, fontTime, page, document, "(" + (yearBirth + Integer.parseInt(dto.getTuoiDinh2())) + ")", 9, Color.BLACK, "C");

            PdfboxUtils.fillTextToPage(179f, 137f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(2).toString(), 9, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(188f, 123f, fontTime, page, document, dto.getTuoiDinh3() + "t", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(188f, 115f, fontTime, page, document, "(" + (yearBirth + Integer.parseInt(dto.getTuoiDinh3())) + ")", 9, Color.BLACK, "C");

            PdfboxUtils.fillTextToPage(236f, 165f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(3).toString(), 9, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(245f, 152f, fontTime, page, document, dto.getTuoiDinh4() + "t", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(245f, 145f, fontTime, page, document, "(" + (yearBirth + Integer.parseInt(dto.getTuoiDinh4())) + ")", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // THAI DO
            String hexCode = "#1b3562";
            Color colorHexCode = Color.decode(hexCode);
            PdfboxUtils.fillTextToPage(530f, 640f, fontTime, page, document, String.valueOf(dto.getThaiDo()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 620f, fontTime, page, document, String.valueOf(dto.getTuongTac()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 600f, fontTime, page, document, StringUtils.join(dto.getSoLap(), ","), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(530f, 557f, fontTime, page, document, String.valueOf(dto.getNoiTam()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 537f, fontTime, page, document, StringUtils.join(dto.getNoiCam(), ","), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 517f, fontTime, page, document, String.valueOf(dto.getTraiNghiem()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 497f, fontTime, page, document, String.valueOf(dto.getTrucGiac()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 477f, fontTime, page, document, String.valueOf(dto.getCamXuc()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 457f, fontTime, page, document, String.valueOf(dto.getLogic()), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(530f, 412f, fontTime, page, document, String.valueOf(dto.getSuMenh()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 392f, fontTime, page, document, String.valueOf(dto.getDuongDoi()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 372f, fontTime, page, document, String.valueOf(dto.getKetNoiDuongDoiVaSuMenh()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 352f, fontTime, page, document, String.valueOf(dto.getTruongThanh()), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(530f, 310f, fontTime, page, document, String.valueOf(dto.getNgaySinh()), 13, colorHexCode);
            String txtTaiNang = "";
            if (!StringUtils.join(dto.getMuiTenTaiNang()).contains("0")) {
                if (dto.getMuiTenTaiNang().size() == 1) {
                    txtTaiNang = addCharacterToString(dto.getMuiTenTaiNang().get(0));
                } else {
                    txtTaiNang = "(" + dto.getMuiTenTaiNang().size() + ")";
                }
            } else {
                txtTaiNang = String.join("", dto.getMuiTenTaiNang());
            }
            PdfboxUtils.fillTextToPage(530f, 290f, fontTime, page, document, txtTaiNang, 13, colorHexCode);

            String txtKhuyetThieu = "";
            if (!StringUtils.join(dto.getMuiTenKhuyetThieu()).contains("0")) {
                if (dto.getMuiTenKhuyetThieu().size() == 1) {
                    txtKhuyetThieu = addCharacterToString(dto.getMuiTenKhuyetThieu().get(0));
                } else {
                    txtKhuyetThieu = "(" + dto.getMuiTenKhuyetThieu().size() + ")";
                }
            } else {
                txtKhuyetThieu = String.join("", dto.getMuiTenKhuyetThieu());
            }
            PdfboxUtils.fillTextToPage(530f, 270f, fontTime, page, document, txtKhuyetThieu, 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 250f, fontTime, page, document, StringUtils.join(dto.getBoSung(), ", "), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(322f, 184f, fontTime, page, document, (yearBirth + Integer.parseInt(dto.getTuoiDinh1())) + "(" + dto.getTuoiDinh1() + "t)", 13, colorHexCode);
            PdfboxUtils.fillTextToPage(435f, 184f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(0).toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 184f, fontTime, page, document, dto.getThachThuc().get(0).toString(), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(322f, 164f, fontTime, page, document, (yearBirth + Integer.parseInt(dto.getTuoiDinh2())) + "(" + dto.getTuoiDinh2() + "t)", 13, colorHexCode);
            PdfboxUtils.fillTextToPage(435f, 164f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(1).toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 164f, fontTime, page, document, dto.getThachThuc().get(1).toString(), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(322f, 144f, fontTime, page, document, (yearBirth + Integer.parseInt(dto.getTuoiDinh3())) + "(" + dto.getTuoiDinh3() + "t)", 13, colorHexCode);
            PdfboxUtils.fillTextToPage(435f, 144f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(2).toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 144f, fontTime, page, document, dto.getThachThuc().get(2).toString(), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(322f, 124f, fontTime, page, document, (yearBirth + Integer.parseInt(dto.getTuoiDinh4())) + "(" + dto.getTuoiDinh4() + "t)", 13, colorHexCode);
            PdfboxUtils.fillTextToPage(435f, 124f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(3).toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 124f, fontTime, page, document, dto.getThachThuc().get(3).toString(), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(530f, 104f, fontTime, page, document, dto.getNamCaNhan().split(" ")[0], 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 84f, fontTime, page, document, dto.getCanBang().toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 64f, fontTime, page, document, dto.getKetNoiNoiTamVaTuongTac().toString(), 13, colorHexCode);
            // LA BAN CUOC DOI
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("Bộ tứ huyệt đạo - tấm la bàn cuộc đời", outline, page, null, color);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_bo-tu-huyet-dao.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // --- TRANG DINH HUONG ---
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("CHƯƠNG 6: ĐỊNH HƯỚNG NGHỀ NGHIỆP", outline, page, null, color);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_chuong-6-a.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_chuong-6-b.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

//            page = new PDPage();
//            document.addPage(page);
//            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_chuong-6-c.jpg", page, document);
//            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            pairList.clear();
            pairList.add(new MutablePair<>("name_slug", "HK_goi_y_nghe_nghiep"));
            pairList.add(new MutablePair<>("person_type", "NGUOI_LON"));

            List<ShudPdfPageEntity> chapter6PageList = shudPdfRepository.findByProperties(ShudPdfPageEntity.class, pairList, null);
            if (!Utils.isNullOrEmpty(chapter6PageList)) {
                page = new PDPage();
                document.addPage(page);
                PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_goi_y_nghe_nghiep/HK_goi_y_nghe_nghiep_0.jpg", page, document);
                PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

                for (ShudPdfPageEntity entity : chapter6PageList) {
                    String nameSlug = entity.getNameSlug();
                    if (StringUtils.equalsIgnoreCase("HK_goi_y_nghe_nghiep_" + dto.getNoiTam() + ".jpg", entity.getImage())) {
                        page = new PDPage();
                        document.addPage(page);
                        PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/" + nameSlug + "/" + entity.getImage(), page, document);
                        PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

//                        page = new PDPage();
//                        document.addPage(page);
//                        PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/" + nameSlug + "/HK_goi_y_nghe_nghiep_child.jpg", page, document);
//                        PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                    }
                }
            }

            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_chuong-6-d.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // --- GIOI THIEU TAC GIA ---
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("Giới thiệu về tác giả", outline, page, null, null);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_tac-gia-1.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_tac-gia-2.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // --- PAGE TAM NHIN SU MENH ---
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("Tầm nhìn sứ mệnh & giá trị cốt lõi", outline, page, null, null);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_tam-nhin-su-menh.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // --- PAGE BIA CUOI ---
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_bia-cuoi.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex), 11, null);

            // --- PAGE MUC LUC ---
            page = new PDPage();
            PDPageTree pageTree = document.getPages();
            pageTree.insertBefore(page, document.getPage(document.getNumberOfPages() - 1));
            PdfboxUtils.addBookmark("Mục lục", outline, page, null, null);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample/HK_muc-luc.jpg", page, document);
            Map<Integer, Integer> mapPositionIndex = new HashMap<>();
            writeTableOfContent(document, fontTime, 12, mapPositionIndex);
            setLinkTableOfContents(mapPositionIndex, document, page);

            String fileName = getFileName(dto.getFullName());
            document.save(folderExport + fileName);
            document.close();

            updateNumberPrint();
            return ResponseUtils.ok(fileName);
        } catch (Exception e) {
            log.error("[exportFileAdult] error", e);
            throw new BaseAppException("Có lỗi xảy ra");
        }
    }

    private void setLinkTableOfContents(Map<Integer, Integer> mapPositionIndex, PDDocument document, PDPage pageToc) throws Exception {
        List<PDAnnotationLink> links = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : mapPositionIndex.entrySet()) {
            PDAnnotationLink link = new PDAnnotationLink();
            PDRectangle position = new PDRectangle(100, entry.getValue(), 200, 20);  // Vị trí liên kết trên trang
            link.setRectangle(position);

            // Tạo đích đến cho liên kết (điều hướng đến trang i)
            PDPage targetPage = document.getPage(entry.getKey());  // Trang đích
            PDPageXYZDestination destination = new PDPageXYZDestination();
            destination.setPage(targetPage);
            destination.setTop((int) targetPage.getMediaBox().getHeight());

            // Tạo hành động điều hướng đến trang đích
            PDActionGoTo action = new PDActionGoTo();
            action.setDestination(destination);
            link.setAction(action);

            // Thêm đường viền cho liên kết (nếu cần)
            PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
            borderStyle.setWidth(0);  // Không có đường viền
            link.setBorderStyle(borderStyle);

            // Thêm liên kết vào danh sách
            links.add(link);
        }

        pageToc.getAnnotations().addAll(links);
    }
    private BaseResponseEntity<String> exportFileChild(ShudRequest.ExportForm dto) {
        try {
            PDDocument document = new PDDocument();
            PDDocumentOutline outline = new PDDocumentOutline();
            document.getDocumentCatalog().setDocumentOutline(outline);
            PDType0Font fontTime = PDType0Font.load(document, new File(fontConfig.getTimes()));
            PDType0Font fontTimeItalic = PDType0Font.load(document, new File(fontConfig.getTimeItalic()));
            // add bia
            PDPage page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_bia.jpg", page, document);

            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_user-info.jpg", page, document);
            PdfboxUtils.fillTextToPage(240f, 542f, fontTime, page, document, dto.getFullName(), 18, null);

            PdfboxUtils.fillTextToPage(240f, 483f, fontTime, page, document, dto.getBirthday(), 18, null);

            PdfboxUtils.fillTextToPage(240f, 425f, fontTime, page, document, dto.getMobile(), 18, null);

            PdfboxUtils.fillTextToPage(240f, 365f, fontTime, page, document, dto.getEmail(), 18, null);

            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            PdfboxUtils.writeTextWithWrapping(contentStream, fontTime, dto.getAddress(), 240f, 310f, 300f, 18);
            contentStream.close();

            PDRectangle mediaBox = page.getMediaBox();
            float xPage = mediaBox.getWidth() - 25;
            float yPage = 15f;
            int pageIndex = 4;
            // loi cam on
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/note-blank.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
            // mo dau
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/thu-gui-con.jpg", page, document);
            PdfboxUtils.addBookmark("Thư gửi con", outline, page, null, null, true);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // giới thiệu pitago
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_doi-net-pytago.jpg", page, document);
            PdfboxUtils.addBookmark("Đôi nét về số học pytago", outline, page, null, null);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // --- XU LY BIEU DO NGAY SINH ---
            String strChartBirthday = Utils.join("", dto.getChartBirthday());
            List<String> tainang = getTaiNang(strChartBirthday);
            List<String> khuyetThieu = getKhuyetThieu(strChartBirthday);

            dto.setMuiTenTaiNang(tainang);
            dto.setMuiTenKhuyetThieu(khuyetThieu);

            List<String> listFlagChapter1 = List.of("HK_thai_do", "HK_tuong_tac", "HK_so_lap");
            List<String> listFlagChapter2 = List.of("HK_noi_tam", "HK_noi_cam", "HK_trai_nghiem", "HK_truc_giac", "HK_cam_xuc", "HK_logic");
            List<String> listFlagChapter3 = List.of("HK_su_menh", "HK_duong_doi", "HK_ket_noi_duong_doi_va_su_menh", "HK_truong_thanh");
            List<String> listFlagChapter4 = List.of("HK_ngay_sinh", "HK_mui_ten_tai_nang", "HK_mui_ten_khuyet_thieu", "HK_bo_sung");
            List<String> listFlagChapter5 = List.of("HK_4_dinh_cao_cuoc_doi", "HK_thach_thuc", "HK_can_bang", "HK_ket_noi_noi_tam_va_tuong_tac");

            boolean isChapter1 = false;
            PDOutlineItem parentChapter1 = new PDOutlineItem();
            boolean isChapter2 = false;
            PDOutlineItem parentChapter2 = new PDOutlineItem();
            boolean isChapter3 = false;
            PDOutlineItem parentChapter3 = new PDOutlineItem();
            boolean isChapter4 = false;
            PDOutlineItem parentChapter4 = new PDOutlineItem();
            boolean isChapter5 = false;
            PDOutlineItem parentChapter5 = new PDOutlineItem();

            Map<String, Object> mapJsonProperty = convertDataToObject(dto);
            List<Pair<String, Object>> pairList = new ArrayList<>();
            pairList.add(new MutablePair<>("person_type", "NGUOI_LON"));
            List<ShudArithmeticTypeEntity> arithmeticTypeList = shudPdfRepository.findByProperties(ShudArithmeticTypeEntity.class, pairList, " weight asc");

            String preNameSlug = "";
            Color color = new Color(105, 189, 68);
            for (ShudArithmeticTypeEntity arithmeticTypeEntity : arithmeticTypeList) {
                String nameSlug = arithmeticTypeEntity.getNameSlug();
                LinkedList<PagePdfDto> pageDataList = new LinkedList<>();
                if (mapJsonProperty.get(nameSlug) != null) {
                    List<Integer> inputNumberList = convertObjectToList(mapJsonProperty.get(nameSlug));

                    List<String> chapterList = List.of("HK_so_lap", "HK_noi_cam", "HK_mui_ten_tai_nang", "HK_mui_ten_khuyet_thieu", "HK_bo_sung", "HK_4_dinh_cao_cuoc_doi", "HK_thach_thuc");
                    if (chapterList.contains(nameSlug)) {
                        pairList.clear();
                        pairList.add(new MutablePair<>("number", inputNumberList));
                        pairList.add(new MutablePair<>("person_type", "TRE_EM"));
                        pairList.add(new MutablePair<>("name_slug", nameSlug));
                        List<ShudPdfPageEntity> pdfPageList = shudPdfRepository.findByProperties(ShudPdfPageEntity.class, pairList, null);
                        if (!Utils.isNullOrEmpty(pdfPageList)) {
                            ShudPdfPageEntity pageEntity = pdfPageList.get(0);
                            String name = pageEntity.getName();
                            int index = 0;
                            for (Integer number : inputNumberList) {
                                PagePdfDto pagePdfDto = PagePdfDto.builder().name(name).nameSlug(nameSlug).build();
                                if (index == 0) {
                                    pagePdfDto.setImage(nameSlug + "_" + number + ".jpg");
                                } else {
                                    pagePdfDto.setImage(nameSlug + "_" + number + "_child.jpg");
                                }
                                pageDataList.add(pagePdfDto);
                                }
                        }
                    } else if (!Utils.isNullOrEmpty(inputNumberList)){
                        pairList.clear();
                        pairList.add(new MutablePair<>("number", inputNumberList));
                        pairList.add(new MutablePair<>("person_type", "TRE_EM"));
                        pairList.add(new MutablePair<>("name_slug", nameSlug));
                        List<ShudPdfPageEntity> pdfPageList = shudPdfRepository.findByProperties(ShudPdfPageEntity.class, pairList, "number asc");
                        pageDataList.addAll(pdfPageList.stream().map(item -> {
                            PagePdfDto pagePdfDto = new PagePdfDto();
                            Utils.copyProperties(item, pagePdfDto);

                            return pagePdfDto;
                        }).collect(Collectors.toList()));
                    }


                    for (PagePdfDto pagePdfDto : pageDataList) {
                        if (listFlagChapter1.contains(nameSlug) && !isChapter1) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_chuong-1.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 1: THẾ GIỚI BÊN NGOÀI", outline, page, parentChapter1, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter1 = true;
                        } else if (listFlagChapter2.contains(nameSlug) && !isChapter2) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_chuong-2.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 2: THẾ GIỚI BÊN TRONG", outline, page, parentChapter2, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter2 = true;
                        } else if (listFlagChapter3.contains(nameSlug) && !isChapter3) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_chuong-3.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 3: ĐỊNH HƯỚNG CUỘC ĐỜI", outline, page, parentChapter3, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter3 = true;
                        } else if (listFlagChapter4.contains(nameSlug) && !isChapter4) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_chuong-4.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 4: ĐIỂM MẠNH, ĐIỂM YẾU", outline, page, parentChapter4, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter4 = true;
                        } else if (listFlagChapter5.contains(nameSlug) && !isChapter5) {
                            page = new PDPage();
                            document.addPage(page);
                            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_chuong-5.jpg", page, document);
                            PdfboxUtils.addBookmarkParent("CHƯƠNG 5: CƠ HỘI - THÁCH THỨC & CÂN BẰNG", outline, page, parentChapter5, color);
                            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                            isChapter5 = true;
                        }

                        page = new PDPage();
                        document.addPage(page);
                        if (StringUtils.isNotBlank(nameSlug) && !StringUtils.equals(nameSlug, preNameSlug)) {
                            if (listFlagChapter1.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter1, null);
                            } else if (listFlagChapter2.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter2, null);
                            } else if (listFlagChapter3.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter3, null);
                            } else if (listFlagChapter4.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter4, null);
                            } else if (listFlagChapter5.contains(nameSlug)) {
                                PdfboxUtils.addBookmark(pagePdfDto.getName(), outline, page, parentChapter5, null);
                            }
                        }
                        PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/" + nameSlug + "/" + pagePdfDto.getImage(), page, document);
                        PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                        preNameSlug = nameSlug;
                    }
                }
            }

            List<String> chartNameList = dto.getChartFullName();
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("Tổng quan bài phân tích số học", outline, page, null, color);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/tong-quan/tong-quan.jpg", page, document);
            PdfboxUtils.fillTextToPage(395f, 770f, fontTime, page, document, dto.getFullName(), 13, Color.WHITE);
            PdfboxUtils.fillTextToPage(395f, 753f, fontTime, page, document, dto.getBirthday(), 13, Color.WHITE);
            String ageOfBirth = dto.getAge() + "t (" + Utils.formatDate(new Date(), "YYYY") + ")";
            PdfboxUtils.fillTextToPage(495f, 753f, fontTime, page, document, ageOfBirth, 13, Color.WHITE);
            PdfboxUtils.fillTextToPage(395f, 735f, fontTime, page, document, dto.getEmail(), 13, Color.WHITE);
            PdfboxUtils.fillTextToPage(395f, 718f, fontTime, page, document, dto.getMobile(), 13, Color.WHITE);

            if (!Utils.isNullOrEmpty(chartNameList.get(1)) && !Utils.isNullOrEmpty(chartNameList.get(2)) && !Utils.isNullOrEmpty(chartNameList.get(3))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/ho-ten-1.png", page, document, 42, 478, 170, 186);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(4)) && !Utils.isNullOrEmpty(chartNameList.get(5)) && !Utils.isNullOrEmpty(chartNameList.get(6))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/ho-ten-2.png", page, document, 26, 478, 170, 186);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(7)) && !Utils.isNullOrEmpty(chartNameList.get(8)) && !Utils.isNullOrEmpty(chartNameList.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/ho-ten-3.png", page, document, 13  , 476, 170, 186);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(1)) && !Utils.isNullOrEmpty(chartNameList.get(5)) && !Utils.isNullOrEmpty(chartNameList.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/ho-ten-4.png", page, document, 28, 480, 175, 180);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(3)) && !Utils.isNullOrEmpty(chartNameList.get(6)) && !Utils.isNullOrEmpty(chartNameList.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/ho-ten-5.png", page, document, 28, 482, 179, 150);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(2)) && !Utils.isNullOrEmpty(chartNameList.get(5)) && !Utils.isNullOrEmpty(chartNameList.get(8))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/ho-ten-6.png", page, document,28, 462, 179, 120);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(1)) && !Utils.isNullOrEmpty(chartNameList.get(4)) && !Utils.isNullOrEmpty(chartNameList.get(7))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/ho-ten-7.png", page, document, 28, 482, 179, 50);
            }
            if (!Utils.isNullOrEmpty(chartNameList.get(3)) && !Utils.isNullOrEmpty(chartNameList.get(5)) && !Utils.isNullOrEmpty(chartNameList.get(7))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/ho-ten-8.png", page, document, 28, 480, 175, 175);
            }

            // BIEU DO HO TEN
            PdfboxUtils.fillTextToPage(56f, 524f, fontTime, page, document, chartNameList.get(1), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(56f, 574f, fontTime, page, document, chartNameList.get(2), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(56f, 625f, fontTime, page, document, chartNameList.get(3), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(111f, 524f, fontTime, page, document, chartNameList.get(4), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(111f, 574f, fontTime, page, document, chartNameList.get(5), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(111f, 625f, fontTime, page, document, chartNameList.get(6), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(168f, 524f, fontTime, page, document, chartNameList.get(7), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(168f, 574f, fontTime, page, document, chartNameList.get(8), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(168f, 625f, fontTime, page, document, chartNameList.get(9), 13, Color.WHITE, "C");

            // BIEU DO NGAY SINH
            List<String> chartDate = dto.getChartBirthday();
            if (!Utils.isNullOrEmpty(chartDate.get(1)) && !Utils.isNullOrEmpty(chartDate.get(2)) && !Utils.isNullOrEmpty(chartDate.get(3))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/date-chart/ke-hoach-to-chuc.png", page, document, 37f, 250, 40, 193);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(4)) && !Utils.isNullOrEmpty(chartDate.get(5)) && !Utils.isNullOrEmpty(chartDate.get(6))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/date-chart/y-chi-khat-vong.png", page, document, 93f, 250, 40, 193);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(7)) && !Utils.isNullOrEmpty(chartDate.get(8)) && !Utils.isNullOrEmpty(chartDate.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/date-chart/hanh-dong-chu-dong.png", page, document, 153f  , 250, 40, 190);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(1)) && !Utils.isNullOrEmpty(chartDate.get(5)) && !Utils.isNullOrEmpty(chartDate.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/date-chart/quyet-tam.png", page, document, 30, 250, 235, 172);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(3)) && !Utils.isNullOrEmpty(chartDate.get(6)) && !Utils.isNullOrEmpty(chartDate.get(9))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/date-chart/tri-tue.png", page, document, 28, 340, 260, 40);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(2)) && !Utils.isNullOrEmpty(chartDate.get(5)) && !Utils.isNullOrEmpty(chartDate.get(8))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/date-chart/tam-hon-cam-xuc.png", page, document,28, 310, 230, 20);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(1)) && !Utils.isNullOrEmpty(chartDate.get(4)) && !Utils.isNullOrEmpty(chartDate.get(7))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/date-chart/the-chat-thuc-te.png", page, document, 28, 260, 230, 20);
            }
            if (!Utils.isNullOrEmpty(chartDate.get(3)) && !Utils.isNullOrEmpty(chartDate.get(5)) && !Utils.isNullOrEmpty(chartDate.get(7))) {
                PdfboxUtils.addImageToPage( folderTemplateRoot + "/pdf-page-sample-children/tong-quan/date-chart/tam-linh.png", page, document, 28, 230, 232, 172);
            }

            PdfboxUtils.fillTextToPage(57f, 271f, fontTime, page, document, chartDate.get(1), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(57f, 321f, fontTime, page, document, chartDate.get(2), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(57f, 371f, fontTime, page, document, chartDate.get(3), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(112f, 271f, fontTime, page, document, chartDate.get(4), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(112f, 321f, fontTime, page, document, chartDate.get(5), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(112f, 371f, fontTime, page, document, chartDate.get(6), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(167f, 271f, fontTime, page, document, chartDate.get(7), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(167f, 321f, fontTime, page, document, chartDate.get(8), 13, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(167f, 371f, fontTime, page, document, chartDate.get(9), 13, Color.WHITE, "C");

            // DINH CAO CUOC DOI
            PdfboxUtils.fillTextToPage(64f, 81f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(0).toString(), 9, Color.WHITE, "C");
            int yearBirth = Integer.parseInt(dto.getBirthday().split("/")[2]);
            PdfboxUtils.fillTextToPage(73f, 69f, fontTime, page, document, dto.getTuoiDinh1() + "t", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(73f, 61f, fontTime, page, document, "(" + (yearBirth + Integer.parseInt(dto.getTuoiDinh1())) + ")", 9, Color.BLACK, "C");

            PdfboxUtils.fillTextToPage(123f, 110f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(1).toString(), 9, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(133f, 95f, fontTime, page, document, dto.getTuoiDinh2() + "t", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(133f, 87f, fontTime, page, document, "(" + (yearBirth + Integer.parseInt(dto.getTuoiDinh2())) + ")", 9, Color.BLACK, "C");

            PdfboxUtils.fillTextToPage(179f, 137f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(2).toString(), 9, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(188f, 123f, fontTime, page, document, dto.getTuoiDinh3() + "t", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(188f, 115f, fontTime, page, document, "(" + (yearBirth + Integer.parseInt(dto.getTuoiDinh3())) + ")", 9, Color.BLACK, "C");

            PdfboxUtils.fillTextToPage(236f, 165f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(3).toString(), 9, Color.WHITE, "C");
            PdfboxUtils.fillTextToPage(245f, 152f, fontTime, page, document, dto.getTuoiDinh4() + "t", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(245f, 145f, fontTime, page, document, "(" + (yearBirth + Integer.parseInt(dto.getTuoiDinh4())) + ")", 9, Color.BLACK, "C");
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // THAI DO
            String hexCode = "#1b3562";
            Color colorHexCode = Color.decode(hexCode);
            PdfboxUtils.fillTextToPage(530f, 640f, fontTime, page, document, String.valueOf(dto.getThaiDo()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 620f, fontTime, page, document, String.valueOf(dto.getTuongTac()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 600f, fontTime, page, document, StringUtils.join(dto.getSoLap(), ","), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(530f, 557f, fontTime, page, document, String.valueOf(dto.getNoiTam()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 537f, fontTime, page, document, StringUtils.join(dto.getNoiCam(), ","), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 517f, fontTime, page, document, String.valueOf(dto.getTraiNghiem()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 497f, fontTime, page, document, String.valueOf(dto.getTrucGiac()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 477f, fontTime, page, document, String.valueOf(dto.getCamXuc()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 457f, fontTime, page, document, String.valueOf(dto.getLogic()), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(530f, 412f, fontTime, page, document, String.valueOf(dto.getSuMenh()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 392f, fontTime, page, document, String.valueOf(dto.getDuongDoi()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 372f, fontTime, page, document, String.valueOf(dto.getKetNoiDuongDoiVaSuMenh()), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 352f, fontTime, page, document, String.valueOf(dto.getTruongThanh()), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(530f, 310f, fontTime, page, document, String.valueOf(dto.getNgaySinh()), 13, colorHexCode);
            String txtTaiNang = "";
            if (!StringUtils.join(dto.getMuiTenTaiNang()).contains("0")) {
                if (dto.getMuiTenTaiNang().size() == 1) {
                    txtTaiNang = addCharacterToString(dto.getMuiTenTaiNang().get(0));
                } else {
                    txtTaiNang = "(" + dto.getMuiTenTaiNang().size() + ")";
                }
            } else {
                txtTaiNang = String.join("", dto.getMuiTenTaiNang());
            }
            PdfboxUtils.fillTextToPage(530f, 290f, fontTime, page, document, txtTaiNang, 13, colorHexCode);

            String txtKhuyetThieu = "";
            if (!StringUtils.join(dto.getMuiTenKhuyetThieu()).contains("0")) {
                if (dto.getMuiTenKhuyetThieu().size() == 1) {
                    txtKhuyetThieu = addCharacterToString(dto.getMuiTenKhuyetThieu().get(0));
                } else {
                    txtKhuyetThieu = "(" + dto.getMuiTenKhuyetThieu().size() + ")";
                }
            } else {
                txtKhuyetThieu = String.join("", dto.getMuiTenKhuyetThieu());
            }
            PdfboxUtils.fillTextToPage(530f, 270f, fontTime, page, document, txtKhuyetThieu, 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 250f, fontTime, page, document, StringUtils.join(dto.getBoSung(), ", "), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(322f, 184f, fontTime, page, document, (yearBirth + Integer.parseInt(dto.getTuoiDinh1())) + "(" + dto.getTuoiDinh1() + "t)", 13, colorHexCode);
            PdfboxUtils.fillTextToPage(435f, 184f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(0).toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 184f, fontTime, page, document, dto.getThachThuc().get(0).toString(), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(322f, 164f, fontTime, page, document, (yearBirth + Integer.parseInt(dto.getTuoiDinh2())) + "(" + dto.getTuoiDinh2() + "t)", 13, colorHexCode);
            PdfboxUtils.fillTextToPage(435f, 164f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(1).toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 164f, fontTime, page, document, dto.getThachThuc().get(1).toString(), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(322f, 144f, fontTime, page, document, (yearBirth + Integer.parseInt(dto.getTuoiDinh3())) + "(" + dto.getTuoiDinh3() + "t)", 13, colorHexCode);
            PdfboxUtils.fillTextToPage(435f, 144f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(2).toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 144f, fontTime, page, document, dto.getThachThuc().get(2).toString(), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(322f, 124f, fontTime, page, document, (yearBirth + Integer.parseInt(dto.getTuoiDinh4())) + "(" + dto.getTuoiDinh4() + "t)", 13, colorHexCode);
            PdfboxUtils.fillTextToPage(435f, 124f, fontTime, page, document, dto.getDinhCaoCuocDoi().get(3).toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 124f, fontTime, page, document, dto.getThachThuc().get(3).toString(), 13, colorHexCode);

            PdfboxUtils.fillTextToPage(530f, 104f, fontTime, page, document, dto.getNamCaNhan().split(" ")[0], 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 84f, fontTime, page, document, dto.getCanBang().toString(), 13, colorHexCode);
            PdfboxUtils.fillTextToPage(530f, 64f, fontTime, page, document, dto.getKetNoiNoiTamVaTuongTac().toString(), 13, colorHexCode);

            // --- TRANG DINH HUONG ---
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("CHƯƠNG 6: ĐỊNH HƯỚNG NGHỀ NGHIỆP", outline, page, null, color);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_chuong-6.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            pairList.clear();
            pairList.add(new MutablePair<>("name_slug", "HK_goi_y_nghe_nghiep"));
            pairList.add(new MutablePair<>("person_type", "TRE_EM"));

            List<ShudPdfPageEntity> chapter6PageList = shudPdfRepository.findByProperties(ShudPdfPageEntity.class, pairList, null);
            if (!Utils.isNullOrEmpty(chapter6PageList)) {
                for (ShudPdfPageEntity entity : chapter6PageList) {
                    String nameSlug = entity.getNameSlug();
                    if (StringUtils.equalsIgnoreCase("HK_goi_y_nghe_nghiep_" + dto.getNoiTam() + ".jpg", entity.getImage())) {
                        page = new PDPage();
                        document.addPage(page);
                        PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/" + nameSlug + "/" + entity.getImage(), page, document);
                        PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);
                    }
                }
            }
            // --- BO TU HUYET DAO ---
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("Bộ tứ huyệt đạo - tấm la bàn cuộc đời", outline, page, null, color);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "/pdf-page-sample-children/la-ban.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            PdfboxUtils.fillTextToPage(200f, 580f, fontTime, page, document, String.valueOf(dto.getSuMenh()), 18, colorHexCode, "C");
            PdfboxUtils.fillTextToPage(297f, 393f, fontTime, page, document, String.valueOf(dto.getDuongDoi()), 18, colorHexCode, "C");
            PdfboxUtils.fillTextToPage(300f, 316f, fontTime, page, document, String.valueOf(dto.getNoiTam()), 18, colorHexCode,  "C");

            // --- GIOI THIEU TAC GIA ---
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("Giới thiệu về tác giả", outline, page, null, null);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_tac-gia-1.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_tac-gia-2.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // --- PAGE TAM NHIN SU MENH ---
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addBookmark("Tầm nhìn sứ mệnh & giá trị cốt lõi", outline, page, null, null);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_tam-nhin-su-menh.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex++), 11, null);

            // --- PAGE BIA CUOI ---
            page = new PDPage();
            document.addPage(page);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/HK_bia-cuoi.jpg", page, document);
            PdfboxUtils.fillTextToPage(xPage, yPage, fontTimeItalic, page, document, String.valueOf(pageIndex), 11, null);

            // --- PAGE MUC LUC ---
            page = new PDPage();
            PDPageTree pageTree = document.getPages();
            pageTree.insertBefore(page, document.getPage(document.getNumberOfPages() - 1));
            PdfboxUtils.addBookmark("Mục lục", outline, page, null, null, false);
            PdfboxUtils.addImageFullPage(folderTemplateRoot + "pdf-page-sample-children/muc-luc.jpg", page, document);
            Map<Integer, Integer> mapPositionIndex = new HashMap<>();
            writeTableOfContent(document, fontTime, 12, mapPositionIndex);
            setLinkTableOfContents(mapPositionIndex, document, page);

            String fileName = getFileName(dto.getFullName());
            document.save(folderExport + fileName);
            document.close();

            updateNumberPrint();
            return ResponseUtils.ok(fileName);
        } catch (Exception e) {
            log.error("[exportFileAdult] error", e);
            throw new BaseAppException("Có lỗi xảy ra");
        }
    }

    private String addCharacterToString(String input) {
        switch (input) {
            case "123":
                return "1 - 2 - 3";
            case "147":
                return "1 - 4 - 7";
            case "159":
                return "1 - 5 - 9";
            case "258":
                return "2 - 5 - 8";
            case "357":
                return "3 - 5 - 7";
            case "369":
                return "3 - 6 - 9";
            case "456":
                return "4 - 5 - 6";
            case "789":
                return "7 - 8 - 9";
            default:
                return "";
        }
    }

    private String getFileName(String fileName) {
        fileName = Utils.removeSign(fileName);
        fileName = StringUtils.replace(fileName, " ", "_");
        fileName += "_" + Utils.formatDate(new Date(), BaseConstants.COMMON_EXPORT_DATE_TIME_FORMAT) + ".pdf";

        return fileName;
    }

    private List<String> getTaiNang(String strChartBirthday) {
        List<String> tainang = new ArrayList<>();
        if (strChartBirthday.contains("1") && strChartBirthday.contains("2") && strChartBirthday.contains("3")) {
            tainang.add("123");
        }

        if (strChartBirthday.contains("1") && strChartBirthday.contains("4") && strChartBirthday.contains("7")) {
            tainang.add("147");
        }

        if (strChartBirthday.contains("1") && strChartBirthday.contains("5") && strChartBirthday.contains("9")) {
            tainang.add("159");
        }

        if (strChartBirthday.contains("2") && strChartBirthday.contains("5") && strChartBirthday.contains("8")) {
            tainang.add("258");
        }

        if (strChartBirthday.contains("3") && strChartBirthday.contains("5") && strChartBirthday.contains("7")) {
            tainang.add("357");
        }

        if (strChartBirthday.contains("3") && strChartBirthday.contains("6") && strChartBirthday.contains("9")) {
            tainang.add("369");
        }

        if (strChartBirthday.contains("4") && strChartBirthday.contains("5") && strChartBirthday.contains("6")) {
            tainang.add("456");
        }
        if (strChartBirthday.contains("7") && strChartBirthday.contains("8") && strChartBirthday.contains("9")) {
            tainang.add("789");
        }
        if (tainang.isEmpty()) {
            tainang.add("0");
        }

        return tainang;
    }

    private List<String> getKhuyetThieu(String strChartBirthday) {
        List<String> khuyetThieu = new ArrayList<>();
        if (!strChartBirthday.contains("1") && !strChartBirthday.contains("2") && !strChartBirthday.contains("3")) {
            khuyetThieu.add("123");
        }

        if (!strChartBirthday.contains("1") && !strChartBirthday.contains("4") && !strChartBirthday.contains("7")) {
            khuyetThieu.add("147");
        }

        if (!strChartBirthday.contains("1") && !strChartBirthday.contains("5") && !strChartBirthday.contains("9")) {
            khuyetThieu.add("159");
        }

        if (!strChartBirthday.contains("2") && !strChartBirthday.contains("5") && !strChartBirthday.contains("8")) {
            khuyetThieu.add("258");
        }

        if (!strChartBirthday.contains("3") && !strChartBirthday.contains("5") && !strChartBirthday.contains("7")) {
            khuyetThieu.add("357");
        }

        if (!strChartBirthday.contains("3") && !strChartBirthday.contains("6") && !strChartBirthday.contains("9")) {
            khuyetThieu.add("369");
        }

        if (!strChartBirthday.contains("4") && !strChartBirthday.contains("5") && !strChartBirthday.contains("6")) {
            khuyetThieu.add("456");
        }
        if (!strChartBirthday.contains("7") && !strChartBirthday.contains("8") && !strChartBirthday.contains("9")) {
            khuyetThieu.add("789");
        }
        if (khuyetThieu.isEmpty()) {
            khuyetThieu.add("0");
        }

        return khuyetThieu;
    }

    private Map<String, Object> convertDataToObject(ShudRequest.ExportForm dto) throws Exception {
        Map<String, Object> mapJsonProperty = new HashMap<>();
        Class className = ShudRequest.ExportForm.class;
        Field[] fields = className.getDeclaredFields();
        for (Field field : fields) {
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            field.setAccessible(true);
            mapJsonProperty.put(jsonProperty.value(), field.get(dto));
        }
        return mapJsonProperty;
    }

    private List<Integer> convertObjectToList(Object obj) {
        List<Integer> resultList = new ArrayList<>();

        if (obj == null) {
            return resultList;
        }
        if (obj instanceof Integer) {
            resultList.add((Integer) obj);
        } else if (obj instanceof List<?>) {
            List<Object> list = (List<Object>) obj;
            if (!Utils.isNullOrEmpty(list)) {
                Object first = list.get(0);
                if (first instanceof Integer) {
                    resultList.addAll(list.stream().map(item -> (Integer) item).collect(Collectors.toList()));
                } else {
                    list.forEach(item -> {
                        String data = (String) item;
                        if (data == null) {
                            data = StringUtils.EMPTY;
                        }
                        String[] splitData = data.split(" ");
                        addIntegerToList(splitData[0], resultList);
                    });
                }
            }
        } else if (obj instanceof String) {
            String[] splitData = ((String) obj).split(" ");
            addIntegerToList(splitData[0], resultList);
        }

        return resultList;
    }

    private void addIntegerToList(String number, List<Integer> integerList) {
        try {
            integerList.add(Integer.parseInt(number));
        } catch (Exception e) {
            log.error("Not cast string to integer: {}", number);
        }
    }

    private void writeTableOfContent(PDDocument document, PDFont font, int fontSize, Map<Integer, Integer> mapPositionIndex) throws Exception {
        PDPage tocPage = document.getPage(document.getNumberOfPages() - 2);
        PDRectangle mediaBox = tocPage.getMediaBox();
        PDPageContentStream contentStream = new PDPageContentStream(document, tocPage, PDPageContentStream.AppendMode.APPEND, true, true);
        contentStream.setFont(font, fontSize);
        int yOffset = 680;
        PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();

        if (outline != null) {
            processOutlineItems(outline, document, contentStream, yOffset, 0, font, fontSize, mediaBox.getWidth() - 100, mapPositionIndex);
        }
        contentStream.close();
    }
    private int processOutlineItems(PDOutlineNode outlineNode, PDDocument document, PDPageContentStream contentStream,
                                    int yOffset, int indentLevel, PDFont font, int fontSize, float tocLineWidth,
                                    Map<Integer, Integer> mapPositionIndex) throws Exception {
        PDOutlineItem current = outlineNode.getFirstChild();
        while (current != null) {
            String title = current.getTitle();
            if (!StringUtils.equalsIgnoreCase(title, "Mục lục")) {
                int pageNumber = document.getPages().indexOf(current.findDestinationPage(document)) + 1;
                String pageNumberTxt = fillSpace(String.valueOf(pageNumber), 3);
                float titleWidth = font.getStringWidth(title) / 1000 * fontSize;
                float pageNumberWidth = font.getStringWidth(pageNumberTxt) / 1000 * fontSize;
                float availableSpace = tocLineWidth - titleWidth - pageNumberWidth - indentLevel * fontSize;
                contentStream.beginText();
                contentStream.newLineAtOffset(50 + (indentLevel * fontSize), yOffset);
                contentStream.setNonStrokingColor(current.getTextColor());
                contentStream.showText(title + fillDots(availableSpace, font, fontSize) + pageNumberTxt);
                contentStream.endText();
                mapPositionIndex.put(pageNumber - 1, yOffset );
                yOffset -= fontSize + 4;

                if (current.hasChildren()) {
                    yOffset = processOutlineItems(current, document, contentStream, yOffset, indentLevel + 1, font, fontSize, tocLineWidth, mapPositionIndex);
                }
            }
            current = current.getNextSibling();
        }
        return yOffset;
    }
    private String fillSpace(String input, int size) {
        return StringUtils.leftPad(input, size);
    }

    private String fillDots(float availableSpace, PDFont font, int fontSize) throws Exception {
        float dotWidth = font.getStringWidth(".") / 1000 * fontSize;
        int numberOfDots = (int)(availableSpace / dotWidth);
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < numberOfDots; i++) {
            dots.append(".");
        }

        return dots.toString();
    }

    private void updateNumberPrint() {
        String userNameLogin = Utils.getUserNameLogin();
        EmployeesEntity employeesEntity = employeesRepositoryJPA.getEmployeeByLoginName(userNameLogin);
        List<Pair<String, Object>> pairList = new ArrayList<>();
        if (employeesEntity != null) {
            pairList.add(new MutablePair<>("attribute_code", "SO_LAN_IN"));
            pairList.add(new MutablePair<>("object_id", employeesEntity.getEmployeeId()));
            pairList.add(new MutablePair<>("table_name", Constant.TableObjectAttribute.CRM_EMPLOYEES));
        } else {
            CustomersEntity customersEntity = customersRepositoryJPA.getCustomerByLoginName(userNameLogin);
            if (customersEntity != null) {
                pairList.add(new MutablePair<>("attribute_code", "SO_LAN_IN"));
                pairList.add(new MutablePair<>("object_id", customersEntity.getCustomerId()));
                pairList.add(new MutablePair<>("table_name", Constant.TableObjectAttribute.CRM_CUSTOMERS));
            }
        }

        objectAttributesRepository.updateNumberPrint(pairList);
    }
}
