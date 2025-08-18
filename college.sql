use college;

CREATE TABLE transfer_certificates (
    id INT PRIMARY KEY AUTO_INCREMENT,
    admission_no VARCHAR(50),
    register_no VARCHAR(50),
    student_name VARCHAR(100),
    father_name VARCHAR(100),
    nationality VARCHAR(50),
    community VARCHAR(50),
    gender VARCHAR(10),
    date_of_birth DATE,
    admission_date DATE,
    course_of_study VARCHAR(100),
    academic_year VARCHAR(100),
    fees_paid_status VARCHAR(10), -- e.g., YES/NO
    date_of_leaving DATE,
    conduct_character VARCHAR(100),
    tc_application_date DATE,
    tc_issue_date DATE,
    completed_course_status VARCHAR(10),
    principal_signature VARCHAR(100),
    date_issued DATE
);
