CREATE TABLE exm_question_groups (
     question_group_id INT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID nhóm câu hỏi (audio/passage)',
     title VARCHAR(255) NULL COMMENT 'Tên nhóm hoặc tiêu đề đoạn',
     description TEXT NULL COMMENT 'Nội dung mô tả đoạn văn hoặc hội thoại',
     media_path VARCHAR(255) NULL COMMENT 'Đường dẫn file audio hoặc hình ảnh minh họa',
     section_code VARCHAR(20) NULL COMMENT 'Part 1–7 hoặc phần thi tương ứng',
     skill_type VARCHAR(20) NULL COMMENT 'LISTENING / READING',
     is_deleted           enum ('N','Y')  NOT NULL DEFAULT 'N' COMMENT 'Trạng thái xóa (Y : đã xóa, N : chưa xóa)',
     created_by           varchar(50)     NULL     DEFAULT NULL COMMENT 'Người tạo',
     created_time         timestamp(0)    NULL     DEFAULT NULL COMMENT 'Ngày tạo',
     modified_by          varchar(50)     NULL     DEFAULT NULL COMMENT 'Người sửa',
     modified_time        timestamp(0)    NULL     DEFAULT NULL COMMENT 'Ngày sửa'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Nhóm câu hỏi dùng chung audio hoặc passage (TOEIC Part 3–7)';


CREATE TABLE exm_questions (
       question_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT 'ID câu hỏi',
       code VARCHAR(50) NULL UNIQUE COMMENT 'Mã câu hỏi (tùy chọn, có thể tự sinh)',
       subject_code VARCHAR(20) NOT NULL COMMENT 'Mã môn học. Liên kết tới bảng sys_categories.type=EXAM_SUBJECT',
       topic_code VARCHAR(20) NULL COMMENT 'Topic câu hỏi. Dùng để phân loại chủ đề nhỏ hơn trong một môn. \nVí dụ: trong môn “Toán” có các topic như Hàm số, Đạo hàm, Tích phân, Liên kết tới bảng sys_categories.type=EXAM_TOPIC',
       type_code VARCHAR(20) NOT NULL COMMENT 'Loại câu hỏi. Liên kết tới bảng sys_categories.type=EXAM_QUESTION_TYPE',
       section_code VARCHAR(20) NOT NULL COMMENT 'Câu hỏi thuộc phần số mấy của bài thi. \nVí dụ bài thi toeic sẽ có part1, part 2. \nLiên kết tới bảng sys_categories.type=EXAM_SECTION',
       level_code VARCHAR(20) COMMENT 'Mức độ khó. Liên kết tới bảng sys_categories.type=EXAM_QUESTION_LEVEL',
       skill_type VARCHAR(20) NOT NULL COMMENT 'Loại Kỹ năng. Liên kết tới bảng sys_categories.type=EXAM_SKILL_TYPE',
       question_group_id int(11) comment 'Nhóm câu hỏi cùng đoạn hoặc audio',
       order_number INT(3) NOT NULL DEFAULT 1 COMMENT 'Thứ tự câu hỏi',

       default_score DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT 'Điểm mặc định của câu hỏi',
       default_weight DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT 'Trọng số',
       time_suggested_seconds INT(5) NULL COMMENT 'Thời gian gợi ý (giây)',
       content LONGTEXT NOT NULL COMMENT 'Nội dung câu hỏi (rich text / HTML)',
       explanation LONGTEXT NULL COMMENT 'Giải thích đáp án (nội bộ)',
       solution LONGTEXT NULL COMMENT 'Lời giải hiển thị cho thí sinh',
       status_code VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT 'Trạng thái câu hỏi. Liên kết tới bảng sys_categories.type=EXAM_QUESTION_STATUS',
       is_deleted           enum ('N','Y')  NOT NULL DEFAULT 'N' COMMENT 'Trạng thái xóa (Y : đã xóa, N : chưa xóa)',
       created_by           varchar(50)     NULL     DEFAULT NULL COMMENT 'Người tạo',
       created_time         timestamp(0)    NULL     DEFAULT NULL COMMENT 'Ngày tạo',
       modified_by          varchar(50)     NULL     DEFAULT NULL COMMENT 'Người sửa',
       modified_time        timestamp(0)    NULL     DEFAULT NULL COMMENT 'Ngày sửa'

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Ngân hàng câu hỏi';

-- Form tìm kiếm: Nội dung câu hỏi(Mặc định hiển thị). Các trường sau hiển thị khi mở rông: Trạng thái, Môn học,Chủ đề (Topic) (topic_code load ra nếu chọn Môn học theo parent_id = mã môn học), Loại câu hỏi, Mức độ khó, Loại kỹ năng
-- Lưới hiển thị:
        -- Mặc định hiển thị: STT, Mã, Nội dung câu hỏi, Trạng thái, Môn học, Chủ đề (Topic) , Loại câu hỏi, Phần thi, Mức độ khó, Loại kỹ năng
        -- Mặc định ẩn: Toàn bộ các trường còn lại


CREATE TABLE exm_question_options (
       question_option_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT 'ID đáp án',
       question_id INT(11) NOT NULL COMMENT 'ID câu hỏi. Liên kết tới exam_questions.question_id',
       option_code VARCHAR(20) NULL COMMENT 'Mã đáp án (A, B, C, D, v.v.)',
       content LONGTEXT NOT NULL COMMENT 'Nội dung đáp án (text hoặc HTML)',
       media_path VARCHAR(255) NULL COMMENT 'Đường dẫn file (ảnh, âm thanh, video) nếu có',
       is_correct ENUM('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Đáp án đúng (Y: đúng, N: sai)',

       explanation LONGTEXT NULL COMMENT 'Giải thích riêng cho đáp án (nếu cần)',
       order_number INT(3) NOT NULL DEFAULT 1 COMMENT 'Thứ tự hiển thị của đáp án',

       is_deleted ENUM('N','Y') NOT NULL DEFAULT 'N' COMMENT 'Trạng thái xóa (Y: đã xóa, N: còn hiệu lực)',
       created_by VARCHAR(50) NULL DEFAULT NULL COMMENT 'Người tạo',
       created_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
       modified_by VARCHAR(50) NULL DEFAULT NULL COMMENT 'Người sửa',
       modified_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày sửa gần nhất',
       INDEX idx_qos_question_active_order(question_id, is_deleted, order_number)
)
    ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Danh sách đáp án cho câu hỏi trong ngân hàng câu hỏi';


CREATE TABLE exm_exam_papers (
     exam_paper_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT 'ID đề thi',
     code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã đề thi (ví dụ: TOEIC-2025-P1, MATH-GR10-01)',
     name VARCHAR(255) NOT NULL COMMENT 'Tên đề thi (ví dụ: Đề thi Toán lớp 10 - Học kỳ I)',

     subject_code VARCHAR(20) NOT NULL COMMENT 'Mã môn học. Liên kết sys_categories.category_type = EXAM_SUBJECT',
     topic_code VARCHAR(20) NULL COMMENT 'Mã chủ đề chính của đề thi. Liên kết sys_categories.type=EXAM_TOPIC.\n Nếu 1 đề thi có nhiều chủ đề thì để trống. Hệ thống hiển thị theo câu hỏi',
     description TEXT NULL COMMENT 'Mô tả chung về đề thi (ghi chú, phạm vi, nguồn...)',

     total_questions INT(5) NULL COMMENT 'Tổng số câu hỏi trong đề thi',
     total_score DECIMAL(6,2) NULL COMMENT 'Tổng điểm của đề thi',
     duration_minutes INT(5) NOT NULL COMMENT 'Thời gian làm bài (phút)',

     difficulty_distribution VARCHAR(255) NULL COMMENT 'Phân bố độ khó (vd: 20% dễ, 60% trung bình, 20% khó). {"EASY":30,"MEDIUM":50,"HARD":20}',
     skill_distribution VARCHAR(255) NULL COMMENT 'Phân bố kỹ năng (vd: Reading=50%, Listening=50%). {"READING":50,"LISTENING":50}',
     random_order ENUM('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Có trộn ngẫu nhiên câu hỏi hay không',
     random_option_order ENUM('Y','N') NOT NULL DEFAULT 'Y' COMMENT 'Có trộn ngẫu nhiên thứ tự đáp án hay không',

    -- 🔹 Cấu hình sinh đề tự động
     generation_mode ENUM('MANUAL','AUTO') NOT NULL DEFAULT 'MANUAL' COMMENT 'Chế độ tạo đề (MANUAL: chọn câu thủ công, AUTO: sinh tự động)',
     generation_strategy VARCHAR(50) NULL COMMENT 'Chiến lược sinh đề (vd: BY_TOPIC, BY_LEVEL, MIXED)',

     status_code ENUM('DRAFT', 'APPROVED', 'INACTIVE') NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái phê duyệt đề thi',
     is_deleted ENUM('N','Y') NOT NULL DEFAULT 'N' COMMENT 'Trạng thái xóa (Y: đã xóa, N: còn hiệu lực)',

     created_by VARCHAR(50) NULL DEFAULT NULL COMMENT 'Người tạo',
     created_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
     modified_by VARCHAR(50) NULL DEFAULT NULL COMMENT 'Người sửa',
     modified_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày sửa gần nhất'

)
    ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng quản lý đề thi';

CREATE TABLE exm_exam_paper_questions (
      exam_paper_question_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT 'ID dòng liên kết',
      exam_paper_id INT(11) NOT NULL COMMENT 'Liên kết tới exam_papers',
      question_id INT(11) NOT NULL COMMENT 'Liên kết tới exam_questions',
      order_number INT(3) NOT NULL DEFAULT 1 COMMENT 'Thứ tự câu hỏi trong đề',
      score DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT 'Điểm của câu hỏi',
      weight DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT 'Trọng số của câu hỏi trong đề thi (dùng để tính điểm quy đổi)',
      is_deleted ENUM('N','Y') NOT NULL DEFAULT 'N' COMMENT 'Trạng thái xóa (Y: đã xóa, N: còn hiệu lực)',
      created_by VARCHAR(50) NULL DEFAULT NULL COMMENT 'Người tạo',
      created_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
      modified_by VARCHAR(50) NULL DEFAULT NULL COMMENT 'Người sửa',
      modified_time TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày sửa gần nhất',

      INDEX idx_epq_paper (exam_paper_id),
      INDEX idx_epq_question (question_id)
)
    ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Danh sách câu hỏi thuộc đề thi';


CREATE TABLE exm_sessions (
   exam_session_id      INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT 'ID kỳ thi',
   code                 VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã kỳ thi (tự sinh hoặc do người quản trị đặt)',
   name                 VARCHAR(255) NOT NULL COMMENT 'Tên kỳ thi',
   description          TEXT NULL COMMENT 'Mô tả chi tiết về kỳ thi',

   exam_paper_id        INT(11) NOT NULL COMMENT 'ID đề thi sử dụng. Liên kết tới exam_papers.exam_paper_id',
   subject_code         VARCHAR(20) NULL COMMENT 'Mã môn học, liên kết sys_categories.type=EXAM_SUBJECT',
   topic_code           VARCHAR(20) NULL COMMENT 'Chủ đề của kỳ thi, liên kết sys_categories.type=EXAM_TOPIC',

   exam_type_code       VARCHAR(20) NULL COMMENT 'Loại kỳ thi (giữa kỳ, cuối kỳ, thử, chính thức...). Liên kết sys_categories.type=EXAM_TYPE',
   mode_code            ENUM('ONLINE','OFFLINE','MIXED') NOT NULL DEFAULT 'ONLINE' COMMENT 'Hình thức thi (trực tuyến, trực tiếp, kết hợp)',

   total_questions      INT(5) NULL COMMENT 'Tổng số câu hỏi trong đề thi',
   total_score          DECIMAL(6,2) NULL DEFAULT 100.00 COMMENT 'Tổng điểm tối đa của kỳ thi',
   duration_minutes     INT(4) NOT NULL COMMENT 'Thời gian làm bài (phút)',

   start_time           DATETIME NOT NULL COMMENT 'Thời gian bắt đầu mở kỳ thi',
   end_time             DATETIME NOT NULL COMMENT 'Thời gian kết thúc kỳ thi',
   allow_retakes        ENUM('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Cho phép thi lại hay không',
   max_attempts         INT(2) NULL DEFAULT 1 COMMENT 'Số lần được phép thi lại (nếu có)',
   allow_late_minutes INT DEFAULT 0 COMMENT 'Cho phép vào muộn (phút)',

   randomize_questions  ENUM('Y','N') NOT NULL DEFAULT 'Y' COMMENT 'Ngẫu nhiên thứ tự câu hỏi',
   randomize_options    ENUM('Y','N') NOT NULL DEFAULT 'Y' COMMENT 'Ngẫu nhiên thứ tự đáp án',

   password             VARCHAR(100) NULL COMMENT 'Mật khẩu vào phòng thi (nếu có)',
   visibility_code      ENUM('PUBLIC','PRIVATE') NOT NULL DEFAULT 'PRIVATE' COMMENT 'Trạng thái hiển thị kỳ thi',
   require_webcam ENUM('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Yêu cầu bật webcam khi thi',
   show_result_after_submit ENUM('Y','N') NOT NULL DEFAULT 'Y' COMMENT 'Hiển thị kết quả ngay sau nộp',
   show_correct_answers  ENUM('Y','N') NOT NULL DEFAULT 'Y' COMMENT 'Hiển thị đáp án đúng khi kết thúc thi',

   status_code          ENUM('DRAFT','READY','ONGOING','COMPLETED','CLOSED')
NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái kỳ thi',
   is_deleted           ENUM('N','Y') NOT NULL DEFAULT 'N' COMMENT 'Đánh dấu xóa',

   created_by           VARCHAR(50) NULL COMMENT 'Người tạo',
   created_time         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
   modified_by          VARCHAR(50) NULL COMMENT 'Người sửa',
   modified_time        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày sửa gần nhất',

   INDEX idx_ess_exam_paper_id (exam_paper_id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng quản lý kỳ thi';

CREATE TABLE exm_session_participants (
       session_participant_id       INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT 'ID bản ghi thí sinh tham dự kỳ thi',

       exam_session_id      INT(11) NOT NULL COMMENT 'ID kỳ thi. Liên kết tới exam_sessions.exam_session_id',
       user_id              INT(11) NOT NULL COMMENT 'ID người dùng (thí sinh). Liên kết tới bảng users hoặc students',
       participant_code     VARCHAR(50) NULL COMMENT 'Mã định danh thí sinh trong kỳ thi (nếu có)',
       latest_attempt_id    BIGINT NULL COMMENT 'ID lượt thi gần nhất (được chọn làm kết quả cuối cùng)',
       status_code          VARCHAR(20) COMMENT 'Trạng thái tham dự kỳ thi',
       attempt_count        INT(2) NOT NULL DEFAULT 0 COMMENT 'Tổng số lượt thi của thí sinh trong kỳ thi này',
       best_score           DECIMAL(6,2) NULL COMMENT 'Điểm cao nhất trong các lượt thi',
       final_score          DECIMAL(6,2) NULL COMMENT 'Điểm được tính là kết quả cuối cùng (có thể = best_score hoặc theo quy tắc riêng)',
       scaled_score         DECIMAL(6,2) NULL COMMENT 'Điểm quy đổi theo thang chuẩn (ví dụ: TOEIC 990, IELTS 9.0)',
       grade                VARCHAR(10) NULL COMMENT 'Xếp loại (A, B, C, Giỏi, Khá, Trung bình,...)',
       pass_status          ENUM('PASS','FAIL','PENDING') NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái đạt/không đạt',
       rank_in_exam         INT(6) NULL COMMENT 'Xếp hạng trong kỳ thi (nếu có)',

       remarks              VARCHAR(255) NULL COMMENT 'Nhận xét hoặc ghi chú của giảng viên / hội đồng thi',
       approved_by          VARCHAR(50) NULL COMMENT 'Người phê duyệt kết quả cuối cùng',
       approved_time        DATETIME NULL COMMENT 'Thời điểm phê duyệt',

       last_attempt_time    DATETIME NULL COMMENT 'Thời gian thi gần nhất',
       is_late_entry        ENUM('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Thí sinh vào muộn so với giờ bắt đầu (Y/N)',
       start_time_actual    DATETIME NULL COMMENT 'Thời điểm thí sinh bắt đầu làm bài thực tế',
       end_time_actual      DATETIME NULL COMMENT 'Thời điểm thí sinh nộp bài thực tế',
       total_time_used_sec  INT(6) NULL COMMENT 'Tổng thời gian làm bài thực tế (giây)',
       is_cheating_detected ENUM('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Phát hiện gian lận trong lượt thi (Y/N)',
       cheating_note        VARCHAR(255) NULL COMMENT 'Ghi chú vi phạm hoặc hành vi bất thường nếu có',
       is_deleted           ENUM('N','Y') NOT NULL DEFAULT 'N' COMMENT 'Đánh dấu xóa',
       created_by           VARCHAR(50) NULL COMMENT 'Người tạo',
       created_time         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
       modified_by          VARCHAR(50) NULL COMMENT 'Người sửa',
       modified_time        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày sửa gần nhất',

       INDEX idx_sps_exam_session_id (exam_session_id),
       INDEX idx_user_id (user_id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Danh sách thí sinh tham dự kỳ thi';



CREATE TABLE exm_attempts (
   attempt_id           INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT 'ID lượt làm bài thi',
   exam_session_id      INT(11) NOT NULL COMMENT 'ID kỳ thi. Liên kết tới exam_sessions.exam_session_id',
   session_participant_id       INT(11) COMMENT 'ID bản ghi thí sinh tham dự kỳ thi',
   participant_id       INT(11) NOT NULL COMMENT 'ID thí sinh tham dự. Liên kết tới exam_session_participants.participant_id',
   exam_paper_id        INT(11) NOT NULL COMMENT 'ID đề thi được sử dụng. Liên kết tới exam_papers.exam_paper_id',

   attempt_number       INT(2) NOT NULL DEFAULT 1 COMMENT 'Lần thi thứ mấy của thí sinh trong kỳ thi này',
   start_time           DATETIME NOT NULL COMMENT 'Thời điểm bắt đầu làm bài',
   end_time             DATETIME NULL COMMENT 'Thời điểm kết thúc/nộp bài',
   duration_used_sec    INT(6) NULL COMMENT 'Thời gian thực tế làm bài (giây)',

   total_questions      INT(4) NULL COMMENT 'Tổng số câu hỏi trong lượt thi',
   correct_count        INT(4) NULL COMMENT 'Số câu đúng',
   incorrect_count      INT(4) NULL COMMENT 'Số câu sai',
   unanswered_count     INT(4) NULL COMMENT 'Số câu bỏ trống',

   score_raw            DECIMAL(6,2) NULL COMMENT 'Điểm thô tính được',
   score_scaled         DECIMAL(6,2) NULL COMMENT 'Điểm quy đổi (nếu có thang điểm riêng, ví dụ TOEIC 990)',
   graded_by            VARCHAR(50) NULL COMMENT 'Người chấm (nếu chấm tay)',
   graded_time          DATETIME NULL COMMENT 'Thời điểm chấm điểm xong',
   grade                VARCHAR(10) NULL COMMENT 'Xếp loại (A, B, C, Giỏi, Khá, Trung bình,...)',
   pass_status          ENUM('PASS','FAIL','PENDING') NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái đạt/không đạt',
   rank_in_exam         INT(6) NULL COMMENT 'Xếp hạng trong kỳ thi (nếu có)',
   remarks              VARCHAR(255) NULL COMMENT 'Nhận xét hoặc ghi chú của giảng viên / hội đồng thi',
   submit_status        ENUM('IN_PROGRESS','SUBMITTED','FORCE_SUBMITTED','TIMEOUT')
NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'Trạng thái nộp bài',
   submit_ip            VARCHAR(45) NULL COMMENT 'Địa chỉ IP khi nộp bài',
   submit_device_info   VARCHAR(255) NULL COMMENT 'Thông tin thiết bị khi nộp bài',

   is_cheating_detected ENUM('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Phát hiện gian lận trong lượt thi (Y/N)',
   cheating_note        VARCHAR(255) NULL COMMENT 'Ghi chú vi phạm hoặc hành vi bất thường nếu có',

   is_deleted           ENUM('N','Y') NOT NULL DEFAULT 'N' COMMENT 'Trạng thái xóa',
   created_by           VARCHAR(50) NULL COMMENT 'Người tạo bản ghi',
   created_time         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
   modified_by          VARCHAR(50) NULL COMMENT 'Người sửa cuối',
   modified_time        TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật gần nhất',

   INDEX idx_eas_exam_session_id (exam_session_id),
   INDEX idx_eas_participant_id (participant_id),
   INDEX idx_eas_exam_paper_id (exam_paper_id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Lượt làm bài thi của thí sinh trong kỳ thi';


CREATE TABLE exm_attempt_answers (
      attempt_answer_id     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID chi tiết câu trả lời',

      attempt_id            BIGINT NOT NULL COMMENT 'ID lượt thi. Liên kết tới exam_attempts.attempt_id',
      question_id           INT(11) NOT NULL COMMENT 'ID câu hỏi. Liên kết tới exam_questions.question_id',
      selected_option_id    INT(11) NULL COMMENT 'ID đáp án mà thí sinh chọn. Liên kết tới exam_question_options.question_option_id',
      selected_text         TEXT NULL COMMENT 'Câu trả lời nhập tự do (nếu là dạng tự luận hoặc điền từ)',

      is_correct            ENUM('Y','N','PENDING') NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái đúng/sai (Y: đúng, N: sai, PENDING: chờ chấm tự luận)',
      score_earned          DECIMAL(5,2) NULL DEFAULT 0.00 COMMENT 'Điểm đạt được cho câu này',
      time_spent_seconds    INT(5) NULL COMMENT 'Thời gian thí sinh dùng cho câu này (giây)',

      question_weight       DECIMAL(5,2) NULL DEFAULT 1.00 COMMENT 'Trọng số của câu hỏi khi tính điểm',
      auto_graded           ENUM('Y','N') NOT NULL DEFAULT 'N' COMMENT 'Đã được chấm tự động hay chưa',
      manual_graded_by      VARCHAR(50) NULL COMMENT 'Người chấm thủ công (nếu có)',
      manual_graded_time    DATETIME NULL COMMENT 'Thời điểm chấm thủ công',
      feedback_text         TEXT NULL COMMENT 'Nhận xét hoặc góp ý cho câu trả lời',

      is_deleted            ENUM('N','Y') NOT NULL DEFAULT 'N' COMMENT 'Trạng thái xóa (Y: xóa, N: còn)',
      created_by            VARCHAR(50) NULL COMMENT 'Người tạo bản ghi',
      created_time          TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
      modified_by           VARCHAR(50) NULL COMMENT 'Người sửa cuối',
      modified_time         TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Ngày cập nhật gần nhất',

      INDEX idx_aas_attempt_id (attempt_id),
      INDEX idx_aas_question_id (question_id),
      INDEX idx_aas_selected_option_id (selected_option_id)
) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='Chi tiết câu trả lời của thí sinh trong từng lượt thi';
