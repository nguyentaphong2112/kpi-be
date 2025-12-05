package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.kpi.constants.Constant;
import vn.kpi.exceptions.ErrorImportException;
import vn.kpi.models.AttributeConfigDto;
import vn.kpi.models.AttributeRequestDto;
import vn.kpi.models.dto.CategoryDto;
import vn.kpi.models.dto.WardDto;
import vn.kpi.models.response.ContractTypesResponse;
import vn.kpi.models.response.EmpTypesResponse;
import vn.kpi.models.response.EmployeesResponse;
import vn.kpi.models.response.JobsResponse;
import vn.kpi.models.response.OrganizationsResponse;
import vn.kpi.models.response.PositionsResponse;
import vn.kpi.repositories.entity.BankAccountsEntity;
import vn.kpi.repositories.entity.CategoryEntity;
import vn.kpi.repositories.entity.ContactAddressesEntity;
import vn.kpi.repositories.entity.ContractProcessEntity;
import vn.kpi.repositories.entity.EducationDegreesEntity;
import vn.kpi.repositories.entity.EmployeesEntity;
import vn.kpi.repositories.entity.PersonalIdentitiesEntity;
import vn.kpi.repositories.impl.BankAccountsRepository;
import vn.kpi.repositories.impl.ContractProcessRepository;
import vn.kpi.repositories.impl.ContractTypesRepository;
import vn.kpi.repositories.impl.EducationDegreesRepository;
import vn.kpi.repositories.impl.EmpTypesRepository;
import vn.kpi.repositories.impl.EmployeesRepository;
import vn.kpi.repositories.impl.JobsRepository;
import vn.kpi.repositories.impl.OrganizationsRepository;
import vn.kpi.repositories.impl.PersonalIdentitiesRepository;
import vn.kpi.repositories.impl.PositionsRepository;
import vn.kpi.repositories.jpa.EmployeesRepositoryJPA;
import vn.kpi.services.EmployeesService;
import vn.kpi.services.ImportEmployeeService;
import vn.kpi.services.ObjectAttributesService;
import vn.kpi.services.UtilsService;
import vn.kpi.utils.AsyncUtils;
import vn.kpi.utils.ExportExcel;
import vn.kpi.utils.I18n;
import vn.kpi.utils.ImportExcel;
import vn.kpi.utils.ResponseUtils;
import vn.kpi.utils.Utils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportEmployeeServiceImpl implements ImportEmployeeService {
    private final ObjectAttributesService objectAttributesService;
    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;
    private final EmployeesRepository employeesRepository;
    private final OrganizationsRepository organizationsRepository;
    private final JobsRepository jobsRepository;
    private final EmpTypesRepository empTypesRepository;
    private final ContractTypesRepository contractTypesRepository;
    private final PositionsRepository positionsRepository;
    private final ContractProcessRepository contractProcessRepository;
    private final PersonalIdentitiesRepository personalIdentitiesRepository;
    private final BankAccountsRepository bankAccountsRepository;
    private final EducationDegreesRepository educationDegreesRepository;
    private final UtilsService utilsService;
    private final EmployeesRepositoryJPA employeesRepositoryJPA;
    private final EmployeesService employeesService;

    @Override
    public ResponseEntity<Object> downloadImportTemplate(boolean isForceUpdate) throws Exception {
        if (isForceUpdate) {
            return getTemplateUpdateData();
        }

        String pathTemplate = "template/import/BM_Import_danh_sach_nhan_vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<CategoryDto> genderList = new ArrayList<>();
        List<OrganizationsResponse.DetailBean> orgList = new ArrayList<>();
        List<OrganizationsResponse.DetailBean> orgList2 = new ArrayList<>();
        List<JobsResponse.DetailBean> jobList = new ArrayList<>();
        List<EmpTypesResponse.DetailBean> empTypeList = new ArrayList<>();
        List<ContractTypesResponse.DetailBean> contractTypeList = new ArrayList<>();
        List<CategoryDto> bankList = new ArrayList<>();
        List<CategoryDto> majorList = new ArrayList<>();
        List<CategoryDto> trainingMethodList = new ArrayList<>();
        List<PositionsResponse.DetailBean> positionList = new ArrayList<>();
        List<CategoryDto> placeList = new ArrayList<>();

        AsyncUtils.runParallel(taskExecutor.getThreadPoolExecutor(),
                () -> genderList.addAll(employeesRepository.getListCategories(Constant.CATEGORY_CODES.GIOI_TINH)),
                () -> orgList.addAll(organizationsRepository.getListOrg()),
                () -> orgList2.addAll(organizationsRepository.getListOrg2()),
                () -> jobList.addAll(jobsRepository.getListJobs(List.of(Constant.JOB_TYPE.CHUC_VU), null)),
                () -> empTypeList.addAll(empTypesRepository.getListEmpType()),
                () -> contractTypeList.addAll(contractTypesRepository.getListContractType(Constant.CLASSIFY_CONTRACT.HOP_DONG, null)),
                () -> bankList.addAll(employeesRepository.getListCategories(Constant.CATEGORY_CODES.NGAN_HANG)),
                () -> majorList.addAll(employeesRepository.getListCategories(Constant.CATEGORY_CODES.TRINH_DO_DAO_TAO)),
                () -> trainingMethodList.addAll(employeesRepository.getListCategories(Constant.CATEGORY_CODES.HINH_THUC_DAO_TAO)),
                () -> positionList.addAll(positionsRepository.getListPosition()),
                () -> placeList.addAll(employeesRepository.getListCategories(Constant.CATEGORY_CODES.NOI_CAP_CCCD))
        );

        //Export danh mục giới tính
        exportCategoryToTemplate(genderList, dynamicExport, 1);

        //Danh mục đơn vị cấp 1
        exportCategoryToTemplate(orgList, dynamicExport, 2);
        exportCategoryToTemplate(orgList2, dynamicExport, 3);
        exportCategoryToTemplate(jobList, dynamicExport, 4);
        exportCategoryToTemplate(empTypeList, dynamicExport, 5);
        exportCategoryToTemplate(contractTypeList, dynamicExport, 6);
        //Danh mục ngân hàng
        exportCategoryToTemplate(bankList, dynamicExport, 7);
        //Danh mục chuyên ngành
        exportCategoryToTemplate(majorList, dynamicExport, 8);
        //Danh mục hình thức đào tạo
        exportCategoryToTemplate(trainingMethodList, dynamicExport, 9);
        exportCategoryToTemplate(positionList, dynamicExport, 10);

        exportCategoryToTemplate(placeList, dynamicExport, 11);

        dynamicExport.setActiveSheet(0);
        return ResponseUtils.ok(dynamicExport, "BM_Import_danh_sach_nhan_vien.xlsx", false);
    }

    private static void exportCategoryToTemplate(List bankList,
                                                 ExportExcel dynamicExport,
                                                 int activeSheet) throws Exception {
        dynamicExport.setActiveSheet(activeSheet);
        int row = 1;
        for (Object entry : bankList) {
            dynamicExport.setText(String.valueOf(row), 0, row);
            if (entry instanceof CategoryDto) {
                dynamicExport.setText(((CategoryDto) entry).getName(), 1, row++);
            } else if (entry instanceof OrganizationsResponse.DetailBean) {
                dynamicExport.setText(((OrganizationsResponse.DetailBean) entry).getName(), 1, row++);
            } else if (entry instanceof PositionsResponse.DetailBean) {
                dynamicExport.setText(((PositionsResponse.DetailBean) entry).getName(), 1, row++);
            } else if (entry instanceof EmpTypesResponse.DetailBean) {
                dynamicExport.setText(((EmpTypesResponse.DetailBean) entry).getName(), 1, row++);
            } else if (entry instanceof ContractTypesResponse.DetailBean) {
                dynamicExport.setText(((ContractTypesResponse.DetailBean) entry).getName(), 1, row++);
            } else if (entry instanceof JobsResponse.DetailBean) {
                dynamicExport.setText(((JobsResponse.DetailBean) entry).getName(), 1, row++);
            }
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> processImportUpdate(MultipartFile file) throws IOException {
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        List<Object[]> dataList = new ArrayList<>();
        String fileConfigName = "BM_Import-update-thong-tin-nhan-vien.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        List<AttributeConfigDto> attributeConfigDtos = objectAttributesService.getAttributes("hr_employees", Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.THONG_TIN_CO_BAN);

        Map<String, Map<String, CategoryDto>> mapCategories = new HashMap<>();


        attributeConfigDtos.forEach(dto -> {
            ImportExcel.ImportConfigBean importConfigBean = new ImportExcel.ImportConfigBean();
            importConfigBean.setExcelColumn(dto.getName());
            importConfigBean.setCheckDuplicate(false);
            importConfigBean.setLength(500);
            importConfigBean.setIgnore(false);
            if (dto.isRequired()) {
                importConfigBean.setNullable(false);
            }
            if (dto.getDataType().equalsIgnoreCase("double")) {
                importConfigBean.setType(ImportExcel.DOUBLE);
            } else if (dto.getDataType().equalsIgnoreCase("int")) {
                importConfigBean.setType(ImportExcel.LONG);
            } else if (dto.getDataType().equalsIgnoreCase("date")) {
                importConfigBean.setType(ImportExcel.DATE);
            } else {
                importConfigBean.setType(ImportExcel.STRING);
                if (dto.getDataType().equalsIgnoreCase("list")) {
                    List<CategoryDto> categoryDtos = utilsService.getListEntitiesFromUrl(dto.getUrlApi(), CategoryDto.class);
                    mapCategories.put(dto.getCode(), new HashMap<>());
                    categoryDtos.forEach(categoryDto -> {
                        mapCategories.get(dto.getCode()).put(categoryDto.getName().toLowerCase(), categoryDto);
                        mapCategories.get(dto.getCode()).put(categoryDto.getValue().toLowerCase(), categoryDto);
                        if (categoryDto.getCode() != null) {
                            mapCategories.get(dto.getCode()).put(categoryDto.getCode().toLowerCase(), categoryDto);
                        }
                    });
                }
            }
            importExcel.addColumnConfig(importConfigBean);
        });
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            Map<String, String> mapGioiTinh = new ConcurrentHashMap<>();
            Map<String, String> mapTinhTrangHonNhan = new ConcurrentHashMap<>();
            Map<String, String> mapDanToc = new ConcurrentHashMap<>();
            Map<String, String> mapTonGiao = new ConcurrentHashMap<>();
            Map<String, String> mapTinh = new ConcurrentHashMap<>();
            Map<String, String> mapXa = new ConcurrentHashMap<>();
            Map<String, EmployeesResponse.BasicInfo> mapEmp = new ConcurrentHashMap<>();
            Map<String, ContactAddressesEntity> mapContactAddress = new ConcurrentHashMap<>();
            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = ((String) obj[1]).toUpperCase();
                if (!empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }
            AsyncUtils.runParallel(taskExecutor.getThreadPoolExecutor(),
                    () -> mapGioiTinh.putAll(getMapCategories(Constant.CATEGORY_CODES.GIOI_TINH)),
                    () -> mapTinhTrangHonNhan.putAll(getMapCategories(Constant.CATEGORY_CODES.TINH_TRANG_HON_NHAN)),
                    () -> mapDanToc.putAll(getMapCategories(Constant.CATEGORY_CODES.DAN_TOC)),
                    () -> mapTonGiao.putAll(getMapCategories(Constant.CATEGORY_CODES.TON_GIAO)),
                    () -> mapTinh.putAll(getMapCategories(Constant.CATEGORY_CODES.TINH)),
                    () -> {
                        List<WardDto> wardDtos = employeesRepository.getListWards();
                        wardDtos.forEach(dto -> {
                            mapXa.put(dto.getProvinceId() + "-" + dto.getName().toLowerCase(), dto.getValue());
                            if (dto.getCode() != null) {
                                mapXa.put(dto.getProvinceId() + "-" + dto.getCode().toLowerCase(), dto.getValue());
                            }
                        });
                    },
                    () -> mapEmp.putAll(employeesRepository.getMapEmpByCode(empCodeList)),
                    () -> mapContactAddress.putAll(employeesRepository.getMapContactAddress(empCodeList))
            );


            int row = 0;
            List<EmployeesEntity> employeesEntities = new ArrayList<>();
            List<ContactAddressesEntity> listContactAddresses = new ArrayList<>();
            List<ContactAddressesEntity> listContactAddressesUpdate = new ArrayList<>();
            for (Object[] obj : dataList) {
                int col = 1;
                String employeeCode = (String) obj[col];
                Long employeeId = 0L;
                EmployeesEntity employeesEntity = new EmployeesEntity();
                if (mapEmp.get(employeeCode.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Mã nhân viên không tồn tại", employeeCode);
                } else if (!mapEmp.get(employeeCode.toLowerCase()).getFullName().equalsIgnoreCase((String) obj[2])) {
                    importExcel.addError(row, col, MessageFormat.format("Họ tên nhân viên không khớp với {0} - {1}", employeeCode, mapEmp.get(employeeCode.toLowerCase()).getFullName()), (String) obj[2]);
                } else {
                    employeeId = mapEmp.get(employeeCode.toLowerCase()).getEmployeeId();
                    employeesEntity = employeesRepositoryJPA.getById(employeeId);
                    employeesEntities.add(employeesEntity);
                }
                //gioi tinh
                col = 3;
                String gioiTinh = (String) obj[col];
                if (mapGioiTinh.get(gioiTinh.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Giới tính không hợp lệ!", gioiTinh);
                } else {
                    employeesEntity.setGenderId(mapGioiTinh.get(gioiTinh.toLowerCase()));
                }
                //ngày sinh
                employeesEntity.setDateOfBirth((Date) obj[4]);
                employeesEntity.setMobileNumber((String) obj[5]);
                employeesEntity.setEmail((String) obj[6]);
                //tinh trang hon nhan
                col = 7;
                String tinhTrangHonNhan = (String) obj[col];
                if (mapTinhTrangHonNhan.get(tinhTrangHonNhan.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Tình trạng hôn nhân không hợp lệ!", tinhTrangHonNhan);
                } else {
                    employeesEntity.setMaritalStatusId(mapTinhTrangHonNhan.get(tinhTrangHonNhan.toLowerCase()));
                }
                //Dân tộc
                col = 8;
                String danToc = (String) obj[col];
                if (mapDanToc.get(danToc.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Dân tộc không hợp lệ!", danToc);
                } else {
                    employeesEntity.setEthnicId(mapDanToc.get(danToc.toLowerCase()));
                }
                //Tôn giáo
                col = 9;
                String tonGiao = (String) obj[col];
                if (mapTonGiao.get(tonGiao.toLowerCase()) == null) {
                    importExcel.addError(row, col, "Tôn giáo không hợp lệ!", tonGiao);
                } else {
                    employeesEntity.setReligionId(mapTonGiao.get(tonGiao.toLowerCase()));
                }
                employeesEntity.setInsuranceNo((String) obj[10]);
                employeesEntity.setTaxNo((String) obj[11]);
                employeesEntity.setPlaceOfBirth((String) obj[12]);
                employeesEntity.setOriginalAddress((String) obj[13]);
                //Địa chỉ thường trú
                col = 14;
                ContactAddressesEntity contactAddressesEntity = mapContactAddress.get(
                        employeeId + "-" + Constant.ADDRESS_TYPE.THUONG_TRU
                );
                if (contactAddressesEntity == null) {
                    contactAddressesEntity = new ContactAddressesEntity();
                    contactAddressesEntity.setEmployeeId(employeeId);
                    contactAddressesEntity.setAddressType(Constant.ADDRESS_TYPE.THUONG_TRU);
                    listContactAddresses.add(contactAddressesEntity);
                } else {
                    listContactAddressesUpdate.add(contactAddressesEntity);
                }
                String tinh = (String) obj[col];
                if (!Utils.isNullOrEmpty(tinh)) {
                    if (mapTinh.get(tinh.toLowerCase()) == null) {
                        importExcel.addError(row, col, "Tỉnh không hợp lệ!", tinh);
                    } else {
                        contactAddressesEntity.setProvinceId(mapTinh.get(tinh.toLowerCase()));
                        col = 15;
                        String xa = (String) obj[col];
                        if (!Utils.isNullOrEmpty(xa)) {
                            if (mapXa.get(contactAddressesEntity.getProvinceId() + "-" + xa.toLowerCase()) == null) {
                                importExcel.addError(row, col, "Xã/phường không hợp lệ!", xa);
                            } else {
                                contactAddressesEntity.setWardId(mapXa.get(contactAddressesEntity.getProvinceId() + "-" + xa.toLowerCase()));
                            }
                        }
                    }
                    contactAddressesEntity.setVillageAddress((String) obj[16]);
                    employeesEntity.setPermanentAddress(getFullAddress(contactAddressesEntity));
                }

                //Địa chỉ thường trú
                col = 17;
                contactAddressesEntity = mapContactAddress.get(
                        employeeId + "-" + Constant.ADDRESS_TYPE.HIEN_TAI
                );
                if (contactAddressesEntity == null) {
                    contactAddressesEntity = new ContactAddressesEntity();
                    contactAddressesEntity.setEmployeeId(employeeId);
                    contactAddressesEntity.setAddressType(Constant.ADDRESS_TYPE.HIEN_TAI);
                    listContactAddresses.add(contactAddressesEntity);
                } else {
                    listContactAddressesUpdate.add(contactAddressesEntity);
                }
                tinh = (String) obj[col];
                if (!Utils.isNullOrEmpty(tinh)) {
                    if (mapTinh.get(tinh.toLowerCase()) == null) {
                        importExcel.addError(row, col, "Tỉnh không hợp lệ!", tinh);
                    } else {
                        contactAddressesEntity.setProvinceId(mapTinh.get(tinh.toLowerCase()));
                        col = 18;
                        String xa = (String) obj[col];
                        if (!Utils.isNullOrEmpty(xa)) {
                            if (mapXa.get(contactAddressesEntity.getProvinceId() + "-" + xa.toLowerCase()) == null) {
                                importExcel.addError(row, col, "Xã/phường không hợp lệ!", xa);
                            } else {
                                contactAddressesEntity.setWardId(mapXa.get(contactAddressesEntity.getProvinceId() + "-" + xa.toLowerCase()));
                            }
                        }
                    }
                    contactAddressesEntity.setVillageAddress((String) obj[19]);
                    employeesEntity.setCurrentAddress(getFullAddress(contactAddressesEntity));
                }
                col = 20;
                List<AttributeRequestDto> listAttributes = new ArrayList<>();
                for (AttributeConfigDto dto : attributeConfigDtos) {
                    AttributeRequestDto attributeRequestDto = new AttributeRequestDto();
                    attributeRequestDto.setAttributeCode(dto.getCode());
                    attributeRequestDto.setAttributeName(dto.getName());
                    attributeRequestDto.setDataType(dto.getDataType());
                    if (obj[col] != null) {
                        if (dto.getDataType().equalsIgnoreCase("list")) {
                            String content = (String) obj[col];
                            if (mapCategories.get(dto.getCode()) == null
                                || mapCategories.get(dto.getCode()).get(content.toLowerCase()) == null
                            ) {
                                importExcel.addError(row, col, "Dữ liệu không theo danh mục!", content);
                            } else {
                                attributeRequestDto.setAttributeValue(mapCategories.get(dto.getCode()).get(content.toLowerCase()).getValue());
                            }
                        } else if (dto.getDataType().equalsIgnoreCase("date")) {
                            attributeRequestDto.setAttributeValue(Utils.formatDate((Date) obj[col]));
                        } else {
                            attributeRequestDto.setAttributeValue(obj[col].toString());
                        }
                    }
                    listAttributes.add(attributeRequestDto);
                    col++;
                }
                objectAttributesService.saveObjectAttributes(employeeId, listAttributes, EmployeesEntity.class, Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.THONG_TIN_CO_BAN);
                row++;
            }
            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                employeesRepository.updateBatch(EmployeesEntity.class, employeesEntities, true);
                employeesRepository.updateBatch(ContactAddressesEntity.class, listContactAddressesUpdate, true);
                employeesRepository.insertBatch(ContactAddressesEntity.class, listContactAddresses, userName);
                return ResponseUtils.ok();
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> processImport(MultipartFile file) throws Exception {
        String userName = Utils.getUserNameLogin();
        Date currentDate = new Date();
        List<Object[]> dataList = new ArrayList<>();
        String fileConfigName = "BM_Import_danh_sach_nhan_vien.xml";
        ImportExcel importExcel = new ImportExcel("template/import/" + fileConfigName);
        Map<String, String> genderMap = new ConcurrentHashMap<>();
        Map<String, Long> orgMap = new ConcurrentHashMap<>();
        Map<String, OrganizationsResponse.DetailBean> orgMap2 = new ConcurrentHashMap<>();
        Map<String, Long> jobMap = new ConcurrentHashMap<>();
        Map<String, Long> empTypeMap = new ConcurrentHashMap<>();
        Map<String, List<ContractTypesResponse.DetailBean>> contractTypeMap = new ConcurrentHashMap<>();
        Map<String, String> bankMap = new ConcurrentHashMap<>();
        Map<String, String> majorMap = new ConcurrentHashMap<>();
        Map<String, String> trainingMethodMap = new ConcurrentHashMap<>();
        Map<String, List<PositionsResponse.DetailBean>> positionMap = new ConcurrentHashMap<>();
        Map<String, EmployeesEntity> mapEmp = new ConcurrentHashMap<>();
        Map<String, String> placeMap = new ConcurrentHashMap<>();
        Map<String, List<PersonalIdentitiesEntity>> mapIdentityAllData = new ConcurrentHashMap<>();
        Map<String, List<BankAccountsEntity>> mapBankAllData = new ConcurrentHashMap<>();

        if (importExcel.validateCommon(file.getInputStream(), dataList)) {
            List<String> empCodeList = new ArrayList<>();
            for (Object[] obj : dataList) {
                String empCode = obj[1] != null ? ((String) obj[1]).toUpperCase() : null;
                if (empCode != null && !empCodeList.contains(empCode)) {
                    empCodeList.add(empCode);
                }
            }

            AsyncUtils.runParallel(taskExecutor.getThreadPoolExecutor(),
                    () -> genderMap.putAll(getMapCategories(Constant.CATEGORY_CODES.GIOI_TINH)),
                    () -> {
                        List<OrganizationsResponse.DetailBean> list = organizationsRepository.getListOrg();
                        orgMap.putAll(list.stream().collect(Collectors.toMap(dto -> dto.getName().toLowerCase(), OrganizationsResponse.DetailBean::getOrganizationId, (existing, replacement) -> replacement)));
                    },
                    () -> {
                        List<OrganizationsResponse.DetailBean> list = organizationsRepository.getListOrg2();
                        orgMap2.putAll(list.stream().collect(Collectors.toMap(dto -> dto.getName().toLowerCase(), Function.identity(), (existing, replacement) -> replacement)));
                    },
                    () -> {
                        List<JobsResponse.DetailBean> list = jobsRepository.getListJobs(List.of(Constant.JOB_TYPE.CHUC_VU), null);
                        jobMap.putAll(list.stream().collect(Collectors.toMap(dto -> dto.getName().toLowerCase(), JobsResponse.DetailBean::getJobId, (existing, replacement) -> replacement)));
                    },
                    () -> {
                        List<EmpTypesResponse.DetailBean> list = empTypesRepository.getListEmpType();
                        empTypeMap.putAll(list.stream().collect(Collectors.toMap(dto -> dto.getName().toLowerCase(), EmpTypesResponse.DetailBean::getEmpTypeId, (existing, replacement) -> replacement)));
                    },
                    () -> contractTypeMap.putAll(contractTypesRepository.getMapContractType(Constant.CLASSIFY_CONTRACT.HOP_DONG, null)),
                    () -> bankMap.putAll(getMapCategories(Constant.CATEGORY_CODES.NGAN_HANG)),
                    () -> majorMap.putAll(getMapCategories(Constant.CATEGORY_CODES.TRINH_DO_DAO_TAO)),
                    () -> trainingMethodMap.putAll(getMapCategories(Constant.CATEGORY_CODES.HINH_THUC_DAO_TAO)),
                    () -> positionMap.putAll(positionsRepository.getMapPosition()),
                    () -> mapEmp.putAll(employeesRepository.getMapEmpEntityByCode(empCodeList)),
                    () -> placeMap.putAll(getMapCategories(Constant.CATEGORY_CODES.NOI_CAP_CCCD)),
                    () -> mapIdentityAllData.putAll(personalIdentitiesRepository.getMapAllData()),
                    () -> mapBankAllData.putAll(bankAccountsRepository.getMapAllData())
            );
            int row = 0;
            int col;
            List<EmployeesEntity> listSaveEmployeeAdd = new ArrayList<>();
            List<ContractProcessEntity> listSaveContractAdd = new ArrayList<>();
            List<PersonalIdentitiesEntity> listSaveIdentityAdd = new ArrayList<>();
            List<BankAccountsEntity> listSaveBankAdd = new ArrayList<>();
            List<EducationDegreesEntity> listSaveEducationAdd = new ArrayList<>();
            for (Object[] obj : dataList) {
                col = 1;
                String employeeCode = (String) obj[col++];
                String employeeName = (String) obj[col++];
                String genderName = (String) obj[col++];
                String genderId = genderMap.get(genderName.trim().toLowerCase());
                if (genderId == null) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.category.invalid"), genderName);

                }
                Date dateOfBirth = (Date) obj[col++];
                String phoneNumber = obj[col] != null ? (String) obj[col] : null;
                if (!Utils.isValidPhoneNumber(phoneNumber)) {
                    importExcel.addError(row, col, I18n.getMessage("Số điện thoại không hợp lệ"), phoneNumber);
                }
                col++;
                String email = obj[col] != null ? (String) obj[col] : null;
                col++;
                String orgName = (String) obj[col++];
                Long orgId = orgMap.get(orgName.trim().toLowerCase());
                if (orgId == null) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.category.invalid"), orgName);

                }

                String org2Name = (String) obj[col++];
                if (org2Name != null) {
                    if (orgMap2.get(org2Name.trim().toLowerCase()) == null) {
                        importExcel.addError(row, col - 1, I18n.getMessage("error.category.invalid"), org2Name);

                    } else {
                        if (orgMap2.get(org2Name.trim().toLowerCase()).getParentId() != orgId) {
                            importExcel.addError(row, col - 1, "Tổ nhóm " + org2Name + " không tồn tại ở Khoa phòng " + orgName, org2Name);

                        } else {
                            orgId = orgMap2.get(org2Name.trim().toLowerCase()).getOrganizationId();
                        }
                    }
                }

                String jobName = (String) obj[col++];
                Long jobId = null;
                if (jobName != null) {
                    if (jobMap.get(jobName.trim().toLowerCase()) == null) {
                        importExcel.addError(row, col - 1, I18n.getMessage("error.category.invalid"), jobName);

                    } else {
                        jobId = jobMap.get(jobName.trim().toLowerCase());
                    }
                }

                String positionName = (String) obj[col++];
                Long positionId = null;
                if (positionMap.get(positionName.trim().toLowerCase()) == null) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.category.invalid"), positionName);

                } else {
                    boolean isInvalid = true;
                    PositionsResponse.DetailBean dataValid = new PositionsResponse.DetailBean();
                    for (PositionsResponse.DetailBean it : positionMap.get(positionName.trim().toLowerCase())) {
                        if (it.getOrganizationId().equals(orgId)) {
                            isInvalid = false;
                            dataValid = it;
                            break;
                        }
                    }
                    if (isInvalid) {
                        importExcel.addError(row, col - 1, "Chức danh không khớp với đơn vị", positionName);

                    } else {
                        positionId = dataValid.getPositionId();
                    }
                }

                String empTypeName = (String) obj[col++];
                Long empTypeId = empTypeMap.get(empTypeName.trim().toLowerCase());
                if (empTypeId == null) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.category.invalid"), empTypeName);

                }

                String contractTypeName = (String) obj[col++];
                Long contractTypeId = null;
                if (contractTypeMap.get(contractTypeName.trim().toLowerCase()) == null) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.category.invalid"), contractTypeName);
                } else {
                    boolean isInvalid = true;
                    ContractTypesResponse.DetailBean dataValid = new ContractTypesResponse.DetailBean();
                    for (ContractTypesResponse.DetailBean it : contractTypeMap.get(contractTypeName.trim().toLowerCase())) {
                        if (it.getEmpTypeId().equals(empTypeId)) {
                            isInvalid = false;
                            dataValid = it;
                            break;
                        }
                    }
                    if (isInvalid) {
                        importExcel.addError(row, col - 1, I18n.getMessage("Loại hợp đồng không khớp với đối tượng"), contractTypeName);

                    } else {
                        contractTypeId = dataValid.getContractTypeId();
                    }
                }

                Date startDate = obj[col] != null ? (Date) obj[col] : null;
                col++;
                Date endDate = obj[col] != null ? (Date) obj[col] : null;
                col++;
                String documentNo = obj[col] != null ? (String) obj[col] : null;
                col++;
                String identityNo = ((String) obj[col++]).trim();
                if (!identityNo.matches("\\d{12}")) {
                    importExcel.addError(row, col - 1, I18n.getMessage("Số CCCD phải có độ dài là 12 và phải là số"), identityNo);

                } else if (!Utils.isNullOrEmpty(mapIdentityAllData.get(identityNo.toLowerCase())) && ((!Utils.isNullOrEmpty(employeeCode.toLowerCase())
                                                                                                       && mapEmp.get(employeeCode.toLowerCase()) != null
                                                                                                       && !mapIdentityAllData.get(identityNo.toLowerCase()).get(0).getEmployeeId().equals(mapEmp.get(employeeCode.toLowerCase()).getEmployeeId()))
                                                                                                      || Utils.isNullOrEmpty(employeeCode.toLowerCase()) || mapEmp.get(employeeCode.toLowerCase()) == null)
                ) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.personalIdentity.identityNo.duplicateWithEmp"), identityNo);

                } else if (!Utils.isNullOrEmpty(mapIdentityAllData.get(identityNo.toLowerCase())) && !mapIdentityAllData.get(identityNo.toLowerCase()).get(0).getIdentityTypeId().equals("1")) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.personalIdentity.identityNo.duplicateWithEmp"), identityNo);

                }
                Date identityIssueDate = (Date) obj[col++];
                String identityIssuePlaceName = (String) obj[col++];
                String identityIssuePlace = placeMap.get(identityIssuePlaceName.trim().toLowerCase());
                if (Utils.isNullOrEmpty(identityIssuePlace)) {
                    importExcel.addError(row, col - 1, I18n.getMessage("error.category.invalid"), identityIssuePlaceName);

                }

                String insuranceNo = obj[col] != null ? (String) obj[col] : null;
                col++;
                String taxNo = obj[col] != null ? (String) obj[col] : null;
                col++;
                String accountNo = obj[col] != null ? (String) obj[col] : null;
                if (!Utils.isNullOrEmpty(accountNo)) {
                    if (!Utils.isNullOrEmpty(mapBankAllData.get(accountNo.toLowerCase()))
                        && ((!Utils.isNullOrEmpty(employeeCode.toLowerCase())
                             && mapEmp.get(employeeCode.toLowerCase()) != null
                             && !mapBankAllData.get(accountNo.toLowerCase()).get(0).getEmployeeId().equals(mapEmp.get(employeeCode.toLowerCase()).getEmployeeId()))
                            || Utils.isNullOrEmpty(employeeCode.toLowerCase()) || mapEmp.get(employeeCode.toLowerCase()) == null)) {
                        importExcel.addError(row, col - 1, I18n.getMessage("error.bankAccount.accountNo.duplicateWithEmp"), accountNo.trim());
                    }
                }
                col++;
                String bankName = obj[col] != null ? (String) obj[col] : null;
                String bankId = null;
                if (!Utils.isNullOrEmpty(bankName)) {
                    bankId = bankMap.get(bankName.trim().toLowerCase());
                    if (Utils.isNullOrEmpty(bankId)) {
                        importExcel.addError(row, col, I18n.getMessage("error.category.invalid"), bankName);
                    }
                }
                col++;
                String bankBranch = obj[col] != null ? (String) obj[col] : null;
                col++;
                String majorLevelName = obj[col] != null ? (String) obj[col] : null;
                String majorLevelId = null;
                if (!Utils.isNullOrEmpty(majorLevelName)) {
                    majorLevelId = majorMap.get(majorLevelName.trim().toLowerCase());
                    if (Utils.isNullOrEmpty(majorLevelId)) {
                        importExcel.addError(row, col, I18n.getMessage("error.category.invalid"), majorLevelName);
                    }
                }
                col++;
                String majorName = obj[col] != null ? (String) obj[col] : null;
                col++;
                String trainingSchoolName = obj[col] != null ? (String) obj[col] : null;
                col++;
                String trainingMethodName = obj[col] != null ? (String) obj[col] : null;
                String trainingMethodId = null;
                if (!Utils.isNullOrEmpty(trainingMethodName)) {
                    trainingMethodId = trainingMethodMap.get(trainingMethodName.trim().toLowerCase());
                    if (Utils.isNullOrEmpty(trainingMethodId)) {
                        importExcel.addError(row, col, I18n.getMessage("error.category.invalid"), trainingMethodName);
                    }
                }
                col++;
                String graduatedYear = obj[col] != null ? ((Long) obj[col]).toString() : null;


                EmployeesEntity entity = null;
                if (Utils.isNullOrEmpty(employeeCode) || mapEmp.get(employeeCode.toLowerCase()) == null) {
                    entity = new EmployeesEntity();
                    entity.setEmployeeId(employeesRepository.getNextId(EmployeesEntity.class));
                    entity.setEmployeeCode(Utils.isNullOrEmpty(employeeCode) ? employeesService.getNextEmployeeCode() : employeeCode);
                    entity.setFullName(employeeName);
                    entity.setGenderId(genderId);
                    entity.setDateOfBirth(dateOfBirth);
                    entity.setMobileNumber(phoneNumber != null ? phoneNumber.toString() : "");
                    entity.setEmail(email);
                    entity.setOrganizationId(orgId);
                    entity.setJobId(jobId);
                    entity.setPositionId(positionId);
                    entity.setTaxNo(taxNo);
                    entity.setInsuranceNo(insuranceNo);
                } else if (mapEmp.get(employeeCode.toLowerCase()) != null) {
                    importExcel.addError(row, col, "Mã nhân viên đã tồn tại", employeeCode.toLowerCase());
                }
                if (entity != null) {
                    BankAccountsEntity bankAccountsEntity = null;
                    PersonalIdentitiesEntity personalIdentitiesEntity = null;
                    EducationDegreesEntity educationDegreesEntity = null;
                    ContractProcessEntity contractProcessEntity = null;

                    if (startDate != null && Utils.daysBetween(new Date(), startDate) <= 0 && Utils.daysBetween(new Date(), Utils.NVL(endDate)) >= 0) {
                        entity.setEmpTypeId(empTypeId);
                    }
                    contractProcessEntity = new ContractProcessEntity();
                    contractProcessEntity.setEmployeeId(entity.getEmployeeId());
                    contractProcessEntity.setEmpTypeId(empTypeId);
                    contractProcessEntity.setContractTypeId(contractTypeId);
                    contractProcessEntity.setClassifyCode(Constant.CLASSIFY_CONTRACT.HOP_DONG);
                    contractProcessEntity.setStartDate(startDate);
                    contractProcessEntity.setEndDate(endDate);
                    contractProcessEntity.setDocumentNo(documentNo);
                    listSaveContractAdd.add(contractProcessEntity);
                    listSaveEmployeeAdd.add(entity);


                    personalIdentitiesEntity = new PersonalIdentitiesEntity();
                    personalIdentitiesEntity.setEmployeeId(entity.getEmployeeId());
                    personalIdentitiesEntity.setIdentityTypeId("1");
                    personalIdentitiesEntity.setIdentityNo(identityNo);
                    personalIdentitiesEntity.setIdentityIssueDate(identityIssueDate);
                    personalIdentitiesEntity.setIdentityIssuePlace(identityIssuePlace);
                    listSaveIdentityAdd.add(personalIdentitiesEntity);

                    if (accountNo != null) {
                        bankAccountsEntity = new BankAccountsEntity();
                        bankAccountsEntity.setEmployeeId(entity.getEmployeeId());
                        bankAccountsEntity.setAccountNo(accountNo);
                        bankAccountsEntity.setBankId(bankId);
                        bankAccountsEntity.setBankBranch(bankBranch);
                        listSaveBankAdd.add(bankAccountsEntity);
                    }


                    if (majorLevelId != null && trainingSchoolName != null && majorName != null) {
                        educationDegreesEntity = new EducationDegreesEntity();
                        educationDegreesEntity.setEmployeeId(entity.getEmployeeId());
                        educationDegreesEntity.setMajorLevelId(majorLevelId);
                        educationDegreesEntity.setMajorName(majorName);
                        educationDegreesEntity.setTrainingSchoolName(trainingSchoolName);
                        educationDegreesEntity.setTrainingMethodId(trainingMethodId);
                        educationDegreesEntity.setGraduatedYear(graduatedYear != null ? Integer.parseInt(graduatedYear) : null);
                        listSaveEducationAdd.add(educationDegreesEntity);
                    }

                }
                row++;
            }


            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                employeesRepository.insertBatch(EmployeesEntity.class, listSaveEmployeeAdd, userName);

                contractProcessRepository.insertBatch(ContractProcessEntity.class, listSaveContractAdd, userName);

                personalIdentitiesRepository.insertBatch(PersonalIdentitiesEntity.class, listSaveIdentityAdd, userName);

                bankAccountsRepository.insertBatch(BankAccountsEntity.class, listSaveBankAdd, userName);

                educationDegreesRepository.insertBatch(EducationDegreesEntity.class, listSaveEducationAdd, userName);

            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }


        return ResponseUtils.ok();
    }

    private ResponseEntity<Object> getTemplateUpdateData() throws Exception {
        String pathTemplate = "template/import/BM-import-update-thong-tin-nhan-vien.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);

        List<CategoryDto> genders = new ArrayList<>();
        List<CategoryDto> maritalStatus = new ArrayList<>();
        List<CategoryDto> ethnics = new ArrayList<>();
        List<CategoryDto> religions = new ArrayList<>();
        List<WardDto> wardDtos = new ArrayList<>();
        AsyncUtils.runParallel(taskExecutor.getThreadPoolExecutor(),
                () -> genders.addAll(employeesRepository.getListCategories(Constant.CATEGORY_CODES.GIOI_TINH)),
                () -> maritalStatus.addAll(employeesRepository.getListCategories(Constant.CATEGORY_CODES.TINH_TRANG_HON_NHAN)),
                () -> ethnics.addAll(employeesRepository.getListCategories(Constant.CATEGORY_CODES.DAN_TOC)),
                () -> religions.addAll(employeesRepository.getListCategories(Constant.CATEGORY_CODES.TON_GIAO)),
                () -> wardDtos.addAll(employeesRepository.getListWards())
        );

        List<AttributeConfigDto> attributeConfigDtos = objectAttributesService.getAttributes("hr_employees", Constant.OBJECT_ATTRIBUTES.FUNCTION_CODES.THONG_TIN_CO_BAN);
        int col = 20;
        for (AttributeConfigDto dto : attributeConfigDtos) {
            dynamicExport.setText(dto.getName(), col++, 2);
        }
        dynamicExport.setCellFormat(1, 0, 2, col, ExportExcel.BORDER_FORMAT);

        exportCategoryToTemplate(genders, dynamicExport, 1);
        exportCategoryToTemplate(maritalStatus, dynamicExport, 2);
        exportCategoryToTemplate(ethnics, dynamicExport, 3);
        exportCategoryToTemplate(religions, dynamicExport, 4);

        dynamicExport.setActiveSheet(5);
        int row = 1;
        for (WardDto dto : wardDtos) {
            col = 0;
            dynamicExport.setEntry(String.valueOf(row), col++, row);
            dynamicExport.setText(dto.getProvinceName(), col++, row);
            dynamicExport.setText(dto.getName(), col, row);
            row++;
        }

        dynamicExport.setActiveSheet(0);

        return ResponseUtils.ok(dynamicExport, "BM-import-update-thong-tin-nhan-vien.xlsx", false);
    }

    private Map<String, String> getMapCategories(String categoryType) {
        List<CategoryEntity> categoryEntities = employeesRepository.findByProperties(CategoryEntity.class, "categoryType", categoryType);
        Map<String, String> mapCategories = new HashMap<>();
        for (CategoryEntity categoryEntity : categoryEntities) {
            if (categoryEntity.getCode() != null) {
                mapCategories.put(categoryEntity.getCode().toLowerCase(), categoryEntity.getValue());
            }
            mapCategories.put(categoryEntity.getName().toLowerCase(), categoryEntity.getValue());
        }
        return mapCategories;
    }

    private String getFullAddress(ContactAddressesEntity contactAddresses) {
        StringBuilder strAddress = new StringBuilder();
        if (!Utils.isNullOrEmpty(contactAddresses.getVillageAddress())) {
            strAddress.append(contactAddresses.getVillageAddress()).append(", ");
        }
        String xa = null;
        if (!Utils.isNullOrEmpty(contactAddresses.getWardId())) {
            CategoryEntity categoryWard = employeesRepository.get(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.XA, "value", contactAddresses.getWardId());
            xa = categoryWard == null ? null : categoryWard.getName();
        }
        String huyen = null;
        if (!Utils.isNullOrEmpty(contactAddresses.getDistrictId())) {
            CategoryEntity categoryDistrict = employeesRepository.get(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.HUYEN, "value", contactAddresses.getDistrictId());
            huyen = categoryDistrict == null ? null : categoryDistrict.getName();
        }
        String tinh = null;

        if (!Utils.isNullOrEmpty(contactAddresses.getProvinceId())) {
            CategoryEntity categoryProvince = employeesRepository.get(CategoryEntity.class, "categoryType", Constant.CATEGORY_CODES.TINH, "value", contactAddresses.getProvinceId());
            tinh = categoryProvince == null ? null : categoryProvince.getName();
        }
        return Utils.join(", ", contactAddresses.getVillageAddress(), xa, huyen, tinh);
    }
}
