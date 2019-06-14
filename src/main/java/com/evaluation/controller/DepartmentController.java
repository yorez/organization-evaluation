package com.evaluation.controller;

import java.util.ArrayList;
import java.util.List;

import com.evaluation.domain.Department;
import com.evaluation.domain.Leader;
import com.evaluation.domain.Staff;
import com.evaluation.service.DepartmentService;
import com.evaluation.service.StaffService;
import com.evaluation.service.TurnService;
import com.evaluation.vo.PageMaker;
import com.evaluation.vo.PageVO;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/department/*")
@Slf4j
@AllArgsConstructor
public class DepartmentController {

    DepartmentService departmentService;

    TurnService turnService;

    StaffService staffService;

    @PostMapping("/register")
    public String register(long tno, Department department, RedirectAttributes rttr) {
        log.info("department register by " + tno + department);

        rttr.addFlashAttribute("msg", "register");
        rttr.addAttribute("tno", tno);

        long cno = turnService.get(tno).get().getCno();
        department.setCno(cno);

        departmentService.register(department);

        return "redirect:/department/list";
    }

    @PostMapping("/modify")
    public String modify(Department department, long tno, PageVO vo, RedirectAttributes rttr) {
        log.info("modify " + department);

        departmentService.modify(department);
        rttr.addFlashAttribute("msg", "modify");

        rttr.addAttribute("tno", tno);
        rttr.addAttribute("page", vo.getPage());
        rttr.addAttribute("size", vo.getSize());
        rttr.addAttribute("type", vo.getType());
        rttr.addAttribute("keyword", vo.getKeyword());

        return "redirect:/department/list";
    }

    @PostMapping("/remove")
    public String remove(long dno, long tno, PageVO vo, RedirectAttributes rttr) {
        log.info("remove " + dno);

        departmentService.remove(dno);

        rttr.addFlashAttribute("msg", "remove");
        rttr.addAttribute("tno", tno);
        rttr.addAttribute("page", vo.getPage());
        rttr.addAttribute("size", vo.getSize());
        rttr.addAttribute("type", vo.getType());
        rttr.addAttribute("keyword", vo.getKeyword());

        return "redirect:/department/list";
    }

    @GetMapping("/list")
    public void readList(long tno, PageVO vo, Model model) {
        log.info("department list by " + tno);

        model.addAttribute("tno", tno);

        long cno = turnService.get(tno).get().getCno();
        Page<Department> result = departmentService.getListWithPaging(cno, vo);
        model.addAttribute("result", new PageMaker<>(result));

        List<Staff> leader = new ArrayList<Staff>();
        result.getContent().forEach(department -> {
            if (department.getLeader() != null) {
                staffService.read(department.getLeader().getSno()).ifPresent(staff -> {
                    leader.add(staff);
                });
            }
        });
        model.addAttribute("leaderList", leader);

    }

    @GetMapping("/leader")
    public void readLeader(long dno, long tno, PageVO vo, Model model) {
        departmentService.read(dno).ifPresent(department -> {
            model.addAttribute("team", department.getLeader());

            if (department.getLeader() != null) {
                staffService.read(department.getLeader().getSno()).ifPresent(staff -> {
                    model.addAttribute("leader", staff);
                });
            }
        });

        long cno = turnService.get(tno).get().getCno();
        staffService.readBycno(cno).ifPresent(origin -> {
            model.addAttribute("staffList", origin);
        });

        model.addAttribute("dno", dno);
        model.addAttribute("tno", tno);
        model.addAttribute("page", vo.getPage());
        model.addAttribute("size", vo.getSize());
        model.addAttribute("type", vo.getType());
        model.addAttribute("keyword", vo.getKeyword());
    }

    @PostMapping("/leader/register")
    public String registerLeader(Leader leader, long dno, long tno, PageVO vo, RedirectAttributes rttr) {

        departmentService.read(dno).ifPresent(origin -> {
            origin.setLeader(leader);
            departmentService.modify(origin);
        });

        rttr.addAttribute("tno", tno);
        rttr.addAttribute("page", vo.getPage());
        rttr.addAttribute("size", vo.getSize());
        rttr.addAttribute("type", vo.getType());
        rttr.addAttribute("keyword", vo.getKeyword());

        return "redirect:/department/list";
    }
}
