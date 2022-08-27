package com.ace.ai.admin.controller;

import com.ace.ai.admin.datamodel.Batch;
import com.ace.ai.admin.datamodel.Course;
import com.ace.ai.admin.datamodel.Student;
import com.ace.ai.admin.datamodel.Teacher;
import com.ace.ai.admin.dtomodel.BatchDTO;
import com.ace.ai.admin.dtomodel.StudentDTO;
import com.ace.ai.admin.dtomodel.TeacherDTO;
import com.ace.ai.admin.service.AttendanceService;
import com.ace.ai.admin.service.BatchService;
import com.ace.ai.admin.service.ChapterViewService;
import com.ace.ai.admin.service.ExamScheduleService;

import com.sun.security.jgss.InquireSecContextPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BatchController {
    @Autowired
    ChapterViewService chapterViewService;
    @Autowired
    BatchService batchService;
    @Autowired
    ExamScheduleService examScheduleService;
    @Autowired
    AttendanceService attendanceService;

    @GetMapping({"/goToBatch"})
    public String gotoBatch(Model model){
        List<Batch> batchList= batchService.findAll();
        model.addAttribute("batchList",batchList);
        return "A003";
    }
    @GetMapping({"/BatchDetail1"})
    @ResponseBody
    public String gotoBatchFromBatchLock(Model model){
        List<Batch> batchList= batchService.findAll();
        model.addAttribute("batchList",batchList);
        return "A003";
    }

    @GetMapping({"/batchSeeMore"})
    public ModelAndView batchSeeMore(@RequestParam("id") Integer id,Model model) throws ParseException {
        model.addAttribute("chapterDTOList",chapterViewService.findAllChapterInChapterBatchByBatchId(id));
        model.addAttribute("teacherList",batchService.findALlTeacherByBatchId(id));
        model.addAttribute("teacherList1",batchService.findALlTeacherForAllBatchExcept(id));
        model.addAttribute("batch_id", id);
        model.addAttribute("examScheduleList", examScheduleService.showExamScheduleTable(id));
        model.addAttribute("attendanceList",  attendanceService.showAttendanceTable(id));
       // model.addAttribute("studentList",batchService.findAllStudentByBatchId(id));
        return new ModelAndView("A003-03","TeacherDTO",new TeacherDTO());
    }

    @GetMapping(path="/SendData")
    @ResponseBody
    public ResponseEntity SendData(@RequestParam("chpName") String chpName, @RequestParam("startDate")String startDate, @RequestParam("endDate")String endDate, @RequestParam("batchId")Integer batchId){
        chapterViewService.saveDatesForChapter(chpName,startDate,endDate,batchId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping({"/goToAddBatch"})
    public ModelAndView gotoAddBatch(Model model){

        List<Course> courseList=batchService.findAllCourse();
        List<Teacher> teacherList=  batchService.findALlTeacherByDeleteStatus(false);
        model.addAttribute("teacherList",teacherList);
        model.addAttribute("courseList",courseList);
        return new ModelAndView("A003-01","batchDTO",new BatchDTO());
    }
    @GetMapping({"/goToAddBatchSuccess"})
    public ModelAndView gotoAddBatchSuccess(Model model){

        List<Course> courseList=batchService.findAllCourse();
        List<Teacher> teacherList=  batchService.findALlTeacherByDeleteStatus(false);
        model.addAttribute("teacherList",teacherList);
        model.addAttribute("courseList",courseList);
        model.addAttribute("msg","Add Teacher Success");
        return new ModelAndView("A003-01","batchDTO",new BatchDTO());
    }
    @GetMapping({"/CheckBatchName"})
    @ResponseBody
    public ResponseEntity checkBatchName(@RequestParam("batchName") String batchName){
        Batch batch=batchService.findBatchByName(batchName);
        if(batch!=null){
            return ResponseEntity.ok(HttpStatus.OK);
        }
        else{

            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping({"/addBatch"})
    public String saveBatch(@ModelAttribute("batchDTO") BatchDTO batchDTO)
    {
        Batch batch=new Batch();
        batch.setDeleteStatus(false);
        LocalDate localDate=LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String localDateString = localDate.format(dateTimeFormatter);
        batch.setCreatedDate(localDateString);
        batch.setName(batchDTO.getName());
        Course course=batchService.findCourseById(batchDTO.getCourseId());
        batch.setCourse(course);
        batchService.saveBatch(batch);
        batch=batchService.findLastBatch();
        batchService.saveTeacherBatch(batchDTO.getTeacherId(), batch.getId());
        return "redirect:/goToAddBatchSuccess";
    }

    @GetMapping({"/BatchClose"})
    @ResponseBody
    public ResponseEntity batchClose(Model model,@RequestParam("batchId")Integer batchId){
        Batch batch=batchService.findBatchById(batchId);
        batch.setDeleteStatus(true);
        System.out.println(batch.getName());
        batchService.saveBatch(batch);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping({"/BatchReopen"})
    @ResponseBody
    public ResponseEntity batchReopen(Model model,@RequestParam("batchId")Integer batchId){
        Batch batch=batchService.findBatchById(batchId);
        batch.setDeleteStatus(false);
        System.out.println(batch.getName());
        batchService.saveBatch(batch);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    @GetMapping({"/addStudent{batch_id}"})
    public String addStudent(@PathVariable("batch_id")Integer batchId,Model model){
       model.addAttribute("batchId",batchId);

        return "A003-04";
    }

    @PostMapping("/saveStudent")
    @ResponseBody
    public void saveStudent(@RequestBody ArrayList<StudentDTO> studentList){
        batchService.saveStudent(studentList);
    }

    @PostMapping("/addTeacherToExistingBatch")
    public String addTeacherToBatch(@RequestParam String code,@RequestParam Integer batchId){
          batchService.addTeacherByCodeAndBatchId(code,batchId);
          return String.format("redirect:/batchSeeMore?id=%d",batchId);
    }
}
