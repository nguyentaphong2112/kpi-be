INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number)
VALUES('toan', null, 'Toán học', 'EXAM_SUBJECT', 'N', 10);

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number)
VALUES('vat-ly', null, 'Vật lý', 'EXAM_SUBJECT', 'N', 20);

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number)
VALUES('toeic', null, 'Tiếng Anh - Toeic', 'EXAM_SUBJECT', 'N', 20);


INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, parent_id)
VALUES('dai-so', null, 'Đại số', 'EXAM_TOPIC', 'N', 10, 'toan');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, parent_id)
VALUES('ham-so', null, 'Hàm số', 'EXAM_TOPIC', 'N', 20, 'toan');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, parent_id)
VALUES('quang-hoc', null, 'Quang học', 'EXAM_TOPIC', 'N', 10, 'vat-ly');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, parent_id)
VALUES('vat-ly-hat-nhan', null, 'Vật lý hạt nhân', 'EXAM_TOPIC', 'N', 10, 'vat-ly');

-- ----------------------------Loai cau hoi---------------------------------------------------------------------------------------------------------
INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('multiple_choice_single', NULL, 'Trắc nghiệm 1 đáp án', 'EXAM_QUESTION_TYPE', 'N', 1, 'Chọn một đáp án đúng duy nhất. Part 5 toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('multiple_choice_multiple', NULL, 'Trắc nghiệm nhiều đáp án', 'EXAM_QUESTION_TYPE', 'N', 2, 'Chọn nhiều đáp án đúng cùng lúc');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('true_false', NULL, 'Đúng / Sai', 'EXAM_QUESTION_TYPE', 'N', 3, 'Chọn một trong hai lựa chọn: Đúng hoặc Sai');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('fill_in_the_blank', NULL, 'Điền vào chỗ trống', 'EXAM_QUESTION_TYPE', 'N', 4, 'Nhập đáp án trực tiếp vào ô trống trong câu hỏi. part 6 toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('matching', NULL, 'Nối cặp', 'EXAM_QUESTION_TYPE', 'N', 5, 'Nối các phần tương ứng đúng với nhau');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('ordering', NULL, 'Sắp xếp thứ tự', 'EXAM_QUESTION_TYPE', 'N', 6, 'Sắp xếp các phần tử theo đúng thứ tự logic hoặc thời gian');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('essay', NULL, 'Tự luận', 'EXAM_QUESTION_TYPE', 'N', 7, 'Học viên viết bài luận hoặc lời giải chi tiết; chấm thủ công');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('coding', NULL, 'Lập trình (Coding)', 'EXAM_QUESTION_TYPE', 'N', 8, 'Học viên nhập code; hệ thống chấm tự động bằng test case');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('audio_response', NULL, 'Trả lời bằng giọng nói', 'EXAM_QUESTION_TYPE', 'N', 9, 'Học viên ghi âm giọng nói để trả lời câu hỏi');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('video_response', NULL, 'Trả lời bằng video', 'EXAM_QUESTION_TYPE', 'N', 10, 'Học viên quay video để trả lời; dùng trong phỏng vấn hoặc speaking test');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('file_upload', NULL, 'Nộp tệp bài làm', 'EXAM_QUESTION_TYPE', 'N', 11, 'Học viên tải lên file bài làm, báo cáo hoặc bài thực hành');


INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('photo_description', NULL, 'Mô tả hình ảnh', 'EXAM_QUESTION_TYPE', 'N', 12, 'Nghe và chọn mô tả phù hợp với hình ảnh (TOEIC Part 1)');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('listening_qna', NULL, 'Hỏi – đáp ngắn', 'EXAM_QUESTION_TYPE', 'N', 13, 'Nghe câu hỏi và chọn câu trả lời đúng (TOEIC Part 2)');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('listening_conversation', NULL, 'Hội thoại', 'EXAM_QUESTION_TYPE', 'N', 14, 'Nghe đoạn hội thoại và trả lời câu hỏi (TOEIC Part 3)');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('listening_short_talk', NULL, 'Bài nói ngắn', 'EXAM_QUESTION_TYPE', 'N', 15, 'Nghe bài nói ngắn và chọn đáp án phù hợp (TOEIC Part 4)');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('reading_comprehension', NULL, 'Đọc hiểu đoạn văn', 'EXAM_QUESTION_TYPE', 'N', 16, 'Đọc đoạn văn và chọn đáp án đúng (TOEIC Part 7)');



-- TOEIC Listening Sections
INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('listening_part1', NULL, 'Part 1 – Photo Description', 'EXAM_SECTION', 'N', 1, 'Nghe mô tả hình ảnh và chọn đáp án phù hợp', 'toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('listening_part2', NULL, 'Part 2 – Question & Response', 'EXAM_SECTION', 'N', 2, 'Nghe câu hỏi và chọn câu trả lời đúng', 'toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('listening_part3', NULL, 'Part 3 – Conversation', 'EXAM_SECTION', 'N', 3, 'Nghe đoạn hội thoại và trả lời câu hỏi', 'toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('listening_part4', NULL, 'Part 4 – Short Talk', 'EXAM_SECTION', 'N', 4, 'Nghe bài nói ngắn và chọn đáp án đúng', 'toeic');

-- TOEIC Reading Sections
INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('reading_part5', NULL, 'Part 5 – Incomplete Sentences', 'EXAM_SECTION', 'N', 5, 'Chọn đáp án hoàn thành câu đúng nhất', 'toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('reading_part6', NULL, 'Part 6 – Text Completion', 'EXAM_SECTION', 'N', 6, 'Điền từ hoặc câu thích hợp vào đoạn văn', 'toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('reading_part7', NULL, 'Part 7 – Reading Comprehension', 'EXAM_SECTION', 'N', 7, 'Đọc đoạn văn và trả lời câu hỏi', 'toeic');

-- Optional: TOEIC Speaking & Writing (nếu có)
INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('speaking', NULL, 'TOEIC Speaking Test', 'EXAM_SECTION', 'N', 8, 'Phần thi nói: mô tả, đọc, phản hồi, nêu ý kiến', 'toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('writing', NULL, 'TOEIC Writing Test', 'EXAM_SECTION', 'N', 9, 'Phần thi viết: viết câu, đoạn văn, email', 'toeic');

-- -------------------Mức độ khó--------------------------------------------------------
INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('easy', NULL, 'Dễ', 'EXAM_QUESTION_LEVEL', 'N', 1, 'Câu hỏi cơ bản, kiểm tra kiến thức nền tảng, độ chính xác cao');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('medium', NULL, 'Trung bình', 'EXAM_QUESTION_LEVEL', 'N', 2, 'Câu hỏi có độ phức tạp vừa phải, yêu cầu hiểu bản chất và áp dụng được kiến thức');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('hard', NULL, 'Khó', 'EXAM_QUESTION_LEVEL', 'N', 3, 'Câu hỏi nâng cao, cần tư duy logic hoặc kỹ năng phân tích để giải quyết');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note)
VALUES ('very_hard', NULL, 'Rất khó', 'EXAM_QUESTION_LEVEL', 'N', 4, 'Câu hỏi thử thách, thường xuất hiện ở phần cuối đề thi hoặc để phân loại thí sinh có năng lực cao');


-- ----------------------Nhóm kỹ năng-----------------------------------------------------------------
-- 🗣️ Ngoại ngữ (English, TOEIC, IELTS)
INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('listening', NULL, 'Nghe hiểu (Listening)', 'EXAM_SKILL_TYPE', 'N', 1, 'Khả năng nghe và hiểu nội dung qua hội thoại hoặc bài nói.', 'toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('reading', NULL, 'Đọc hiểu (Reading)', 'EXAM_SKILL_TYPE', 'N', 2, 'Khả năng đọc, hiểu và phân tích văn bản.', 'toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('speaking', NULL, 'Nói (Speaking)', 'EXAM_SKILL_TYPE', 'N', 3, 'Khả năng diễn đạt ý bằng lời nói.', 'toeic');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('writing', NULL, 'Viết (Writing)', 'EXAM_SKILL_TYPE', 'N', 4, 'Khả năng diễn đạt ý bằng văn bản.', 'toeic');

-- 🧮 Toán học (Mathematics)
INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('calculation', NULL, 'Tính toán (Calculation)', 'EXAM_SKILL_TYPE', 'N', 5, 'Khả năng thực hiện phép tính nhanh và chính xác.', 'toan');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('reasoning', NULL, 'Lý luận logic (Logical Reasoning)', 'EXAM_SKILL_TYPE', 'N', 6, 'Khả năng suy luận, chứng minh và lập luận toán học.', 'toan');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('problem_solving', NULL, 'Giải quyết vấn đề (Problem Solving)', 'EXAM_SKILL_TYPE', 'N', 7, 'Khả năng vận dụng kiến thức để giải quyết bài toán thực tế.', 'toan');

-- ⚛️ Vật lý (Physics)
INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('concept_understanding', NULL, 'Hiểu khái niệm (Concept Understanding)', 'EXAM_SKILL_TYPE', 'N', 8, 'Khả năng nắm bắt và hiểu các khái niệm vật lý cơ bản.', 'vat-ly');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('application', NULL, 'Vận dụng công thức (Application)', 'EXAM_SKILL_TYPE', 'N', 9, 'Khả năng áp dụng công thức, định luật vật lý vào bài toán.', 'vat-ly');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number, note, parent_id)
VALUES ('experiment_analysis', NULL, 'Phân tích thí nghiệm (Experiment Analysis)', 'EXAM_SKILL_TYPE', 'N', 10, 'Khả năng đọc hiểu, phân tích dữ liệu và kết quả thí nghiệm.', 'vat-ly');

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number)
VALUES ('draft', NULL, 'Khởi tạo', 'EXAM_QUESTION_STATUS', 'N', 4);

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number)
VALUES ('approved', NULL, 'Đã phê duyệt', 'EXAM_QUESTION_STATUS', 'N', 5);

INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number)
VALUES ('reject', NULL, 'Từ chối', 'EXAM_QUESTION_STATUS', 'N', 5);


INSERT INTO hbt_admin.sys_categories (value, code, name, category_type, is_deleted, order_number)
VALUES ('inactive', NULL, 'Không hoạt động', 'EXAM_QUESTION_STATUS', 'N', 6);
