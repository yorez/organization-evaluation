package com.evaluation.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.evaluation.domain.Reply;
import com.evaluation.domain.Staff;
import com.evaluation.service.CompanyService;
import com.evaluation.service.DepartmentService;
import com.evaluation.service.MBOService;
import com.evaluation.service.RelationMBOService;
import com.evaluation.service.ReplyService;
import com.evaluation.service.TurnService;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/mbo/**")
@Slf4j
@AllArgsConstructor
@Transactional
public class MBOController {

    MBOService mboService;

    ReplyService replyService;

    CompanyService companyService;

    TurnService turnService;

    RelationMBOService relationMBOService;

    DepartmentService departmentService;

    @GetMapping("/")
    public String survey(String company, Model model) {
        log.info("====>survey by company" + company);

        // 회사에 관한 정보 찾고
        companyService.readByCompanyId(company).ifPresent(origin -> {
            long cno = origin.getCno();
            model.addAttribute("company", origin);
            // 회사 cno로 turn정보를 찾는다.
            turnService.getListInMBO(cno).ifPresent(turn -> {
                model.addAttribute("turns", turn);
            });
        });

        return "/mbo/index";
    }

    @PostMapping("/login")
    public String userLogin(long tno, Staff staff, RedirectAttributes rttr, HttpServletRequest request) {
        log.info("user login" + tno + staff);

        turnService.get(tno).ifPresent(turn -> {
            long cno = turn.getCno();
            companyService.get(cno).ifPresent(origin -> {
                String company = origin.getId();
                rttr.addAttribute("company", company);
            });
        });

        // Staff evaluator = relation360Service.findInEvaluator(tno, staff.getEmail());
        relationMBOService.findInEvaluator(tno, staff.getEmail()).ifPresent(evaluator -> {
            if (evaluator.getPassword().equals(staff.getPassword())) {
                HttpSession session = request.getSession();
                session.setAttribute("evaluator", evaluator);
            }
        });

        rttr.addAttribute("tno", tno);
        // 로그인 실패하면 메인으로 가도 세션없어서 로그인 폼으로 감! 패스워드 일치여부에 관계없이 메인으로 리다이렉트.
        return "redirect:/mbo/main";
    }

    @PostMapping("/logout")
    public String userLogOut(String company, HttpServletRequest request, RedirectAttributes rttr) {
        log.info("log out!");

        HttpSession session = request.getSession();
        session.invalidate();

        rttr.addAttribute("company", company);

        return "redirect:/mbo/";
    }

    @GetMapping("/contact")
    public void contact(String company, Long tno, Model model) {

        model.addAttribute("company", company);
        model.addAttribute("tno", tno);
        companyService.readByCompanyId(company).ifPresent(origin -> {
            model.addAttribute("companyInfo", origin);
        });

    }

    @GetMapping("/main")
    public String main(String company, Long tno, Model model, HttpServletRequest request, RedirectAttributes rttr) {
        log.info("====>turn main by company" + company);

        HttpSession session = request.getSession();
        long sno;
        if (session.getAttribute("evaluator") == null) {
            rttr.addAttribute("company", company);
            return "redirect:/mbo/";
        } else {
            Staff staff = (Staff) session.getAttribute("evaluator");
            sno = staff.getSno();
        }

        model.addAttribute("company", company);
        model.addAttribute("tno", tno);

        companyService.readByCompanyId(company).ifPresent(origin -> {
            model.addAttribute("companyInfo", origin);
            long cno = origin.getCno();
            departmentService.findByCnoSno(cno, sno).ifPresent(list -> {
                model.addAttribute("department", list);
            });
        });

        turnService.get(tno).ifPresent(turn -> {
            model.addAttribute("turn", turn);
        });

        return "/mbo/main";
    }

    @GetMapping("/list")
    public String list(String company, long tno, HttpServletRequest request, Model model, RedirectAttributes rttr) {
        log.info("====>turn list by company" + company);
        HttpSession session = request.getSession();
        Staff evaluator = (Staff) session.getAttribute("evaluator");

        if (evaluator == null) {
            rttr.addAttribute("company", company);
            return "redirect:/mbo/";
        }

        companyService.readByCompanyId(company).ifPresent(origin -> {
            model.addAttribute("companyInfo", origin);
        });

        model.addAttribute("tno", tno);
        model.addAttribute("company", company);

        relationMBOService.findByEvaluator(evaluator.getSno(), tno).ifPresent(relation -> {
            Set<String> relationList = new HashSet<>();
            relation.forEach(origin -> {
                relationList.add(origin.getRelation());
            });
            model.addAttribute("relationList", relationList);
            model.addAttribute("evaluatedList", relation);

            List<List<String>> ratioList = new ArrayList<>();
            relation.forEach(origin -> {
                mboService.ratioByTnoSno(tno, origin.getEvaluated().getSno()).ifPresent(ratio -> {
                    ratioList.addAll(ratio);
                });
            });
            model.addAttribute("ratioList", ratioList);
        });

        turnService.get(tno).ifPresent(turn -> {
            model.addAttribute("turn", turn);
        });

        return "/mbo/list";
    }

    // 새로 고침시 리스트로 복귀하기 위한 매핑
    @GetMapping("/object")
    public String object(String company, long tno, HttpServletRequest request, RedirectAttributes rttr) {
        log.info("" + tno);

        HttpSession session = request.getSession();
        if (session.getAttribute("evaluator") == null) {
            rttr.addAttribute("company", company);
            return "redirect:/mbo/";
        }

        companyService.readByCompanyId(company).ifPresent(origin -> {
            rttr.addFlashAttribute("companyInfo", origin);
        });

        rttr.addAttribute("company", company);
        rttr.addAttribute("tno", tno);
        return "redirect:/mbo/list";
    }

    // @GetMapping("/object")
    @PostMapping("/object")
    public void object(Long rno, long tno, String company, Model model) {
        log.info("" + rno);

        model.addAttribute("company", company);
        model.addAttribute("tno", tno);

        companyService.readByCompanyId(company).ifPresent(origin -> {
            model.addAttribute("companyInfo", origin);
        });

        relationMBOService.read(rno).ifPresent(relation -> {
            // 관계에 대한 정보 추가
            model.addAttribute("relation", relation);

            // 피평가자의 목표 가져오기, 피평가자 sno와 tno로
            mboService.listByTnoSno(tno, relation.getEvaluated().getSno()).ifPresent(list -> {
                model.addAttribute("objectList", list);

                // 댓글 리스트 목록으로 만들어 전달하기 현재 목표리스트의 mno와 일치하는 댓글만
                List<Reply> replyList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    replyService.listByMbo(list.get(i).getMno()).ifPresent(origin -> {
                        replyList.addAll(origin);
                    });
                }
                model.addAttribute("replyList", replyList);
            });
        });

    }
}