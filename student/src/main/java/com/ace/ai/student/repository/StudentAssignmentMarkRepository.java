package com.ace.ai.student.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ace.ai.student.datamodel.StudentAssignmentMark;

@Repository
public interface StudentAssignmentMarkRepository extends JpaRepository<StudentAssignmentMark , Integer>{
    StudentAssignmentMark findByAssignmentIdAndStudentId(int assignmentId,int studentId);
}
