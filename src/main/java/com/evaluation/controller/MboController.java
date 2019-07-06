package com.evaluation.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.evaluation.domain.Mbo;
import com.evaluation.domain.RelationMbo;
import com.evaluation.domain.Reply;
import com.evaluation.domain.Staff;
import com.evaluation.domain.embeddable.RatioValue;
import com.evaluation.service.BookService;
import com.evaluation.service.CompanyService;
import com.evaluation.service.DepartmentService;
import com.evaluation.service.MboService;
import com.evaluation.service.RelationMboService;
import com.evaluation.service.ReplyService;
import com.evaluation.service.StaffService;
import com.evaluation.service.TurnService;
import com.google.gson.Gson;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/mbo/*")
@Slf4j
@AllArgsConstructor
@Transactional
public class MboController {

    BookService bookService;

    CompanyService companyService;

    DepartmentService departmentService;

    MboService mboService;

    RelationMboService relationMboService;

    ReplyService replyService;

    TurnService turnService;

    StaffService staffService;

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
    public String userLogin(String company, long tno, Staff staff, RedirectAttributes rttr,
            HttpServletRequest request) {
        log.info("user login" + tno + staff);
        rttr.addAttribute("company", company);

        if (tno == 0) {
            rttr.addFlashAttribute("error", "tno");
            return "redirect:/mbo/";
        }

        if (!relationMboService.findInEvaluator(tno, staff.getEmail()).isPresent()) {
            rttr.addFlashAttribute("error", "email");
            return "redirect:/mbo/";
        } else if (!relationMboService.findInEvaluator(tno, staff.getEmail()).get().getPassword()
                .equals(staff.getPassword())) {
            rttr.addFlashAttribute("error", "password");
            return "redirect:/mbo/";
        }

        // Staff evaluator = relation360Service.findInEvaluator(tno, staff.getEmail());
        relationMboService.findInEvaluator(tno, staff.getEmail()).ifPresent(evaluator -> {
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

        // mbo turn에 따른 navbar 구분을 위해
        turnService.get(tno).ifPresent(turn -> {
            model.addAttribute("turn", turn);
        });
    }

    @GetMapping("/main")
    public String main(String company, Long tno, Model model, HttpServletRequest request, RedirectAttributes rttr) {
        log.info("====>turn main by company" + company);

        HttpSession session = request.getSession();

        if (session.getAttribute("evaluator") == null) {
            rttr.addAttribute("company", company);
            return "redirect:/mbo/";
        }

        model.addAttribute("company", company);
        model.addAttribute("tno", tno);

        Staff staff = (Staff) session.getAttribute("evaluator");
        long sno = staff.getSno();

        departmentService.findByTnoSno(tno, sno).ifPresent(list -> {
            model.addAttribute("department", list);
        });

        companyService.readByCompanyId(company).ifPresent(origin -> {
            model.addAttribute("companyInfo", origin);
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

        relationMboService.findByEvaluator(evaluator.getSno(), tno).ifPresent(relation -> {
            // 관계정보 전달
            model.addAttribute("evaluatedList", relation);

            // 관계 종류를 전달하기 위한 Set
            Set<String> relationList = new HashSet<>();
            relation.forEach(origin -> {
                relationList.add(origin.getRelation());
            });
            model.addAttribute("relationList", relationList);

            // 피평가자의 특정 정보를 얻기 위한 list와 for문
            List<List<String>> ratioList = new ArrayList<>();
            List<RelationMbo> relationMeList = new ArrayList<>();

            relation.forEach(origin -> {
                // 피평가자의 서베이 진행률을 얻기 위한
                mboService.ratioByTnoSno(tno, origin.getEvaluated().getSno()).ifPresent(ratio -> {
                    ratioList.addAll(ratio);
                });

                // 피평가자의 본인 평가 완료 여부를 얻기 위한
                relationMboService.findMeRelationByTnoSno(tno, origin.getEvaluated().getSno()).ifPresent(tmpRel -> {
                    relationMeList.add(tmpRel);
                });
            });
            model.addAttribute("ratioList", ratioList);
            model.addAttribute("relationMeList", relationMeList);
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

    // 목표와 목표댓글을 불러오기 위한
    // @GetMapping("/object")
    @PostMapping("/object")
    public void object(Long rno, long tno, String company, Model model) {
        log.info("" + rno);

        model.addAttribute("company", company);
        model.addAttribute("tno", tno);

        companyService.readByCompanyId(company).ifPresent(origin -> {
            model.addAttribute("companyInfo", origin);
        });

        turnService.get(tno).ifPresent(turn -> {
            model.addAttribute("turn", turn);
        });

        turnService.get(tno).ifPresent(turn -> {
            // 평가 단계에서 회답지 추가
            if (turn.getInfoMbo().getStatus().equals("see") || turn.getInfoMbo().getStatus().equals("count")) {
                // 회답지 추가
                bookService.read(turn.getInfoMbo().getReplyCode()).ifPresent(book -> {
                    model.addAttribute("replyCodeList", book.getContents());
                });
                // 가중치 추가
                bookService.read(turn.getInfoMbo().getWeightCode()).ifPresent(book -> {
                    model.addAttribute("weightCodeList", book.getContents());
                });
            }
        });

        relationMboService.read(rno).ifPresent(relation -> {
            // 관계에 대한 정보 추가
            model.addAttribute("relation", relation);

            // 본인평가 정보 전달
            relationMboService.findMeRelationByTnoSno(tno, relation.getEvaluated().getSno()).ifPresent(relationMe -> {
                model.addAttribute("relationMe", relationMe);
            });

            // 피평가자의 팀 목표 전달
            departmentService.findByDepartment(tno, relation.getEvaluated().getDepartment1(),
                    relation.getEvaluated().getDepartment2()).ifPresent(origin -> {
                        model.addAttribute("department", origin);
                    });

            // 피평가자의 목표 가져오기, 피평가자 sno와 tno로
            mboService.listByTnoSno(tno, relation.getEvaluated().getSno()).ifPresent(list -> {
                // finish Y인 목록과 N인 목록 구분 지음.
                List<ObjectWithReplyNum> objectList = new LinkedList<ObjectWithReplyNum>();
                List<ObjectWithReplyNum> removedList = new LinkedList<ObjectWithReplyNum>();

                for (int i = 0; i < list.size(); i++) {

                    // 댓글 수와 목표를 묶은 객체를 만들어서 list에 추가함.
                    ObjectWithReplyNum object = new ObjectWithReplyNum();
                    // 목표를 추가하고
                    object.mbo = list.get(i);
                    // 목표에 해당하는 댓글수를 가져와서 추가하고
                    replyService.listByMno(list.get(i).getMno()).ifPresent(origin -> {
                        object.replyNum = origin.size();
                    });

                    // 삭제,수정 된 목표와 아닌 것을 구분하고
                    if (list.get(i).getFinish().equals("Y")) {
                        objectList.add(object);
                    } else {
                        removedList.add(object);
                    }
                }

                model.addAttribute("objectList", objectList);
                model.addAttribute("removedList", removedList);

                // 댓글 리스트 목록으로 만들어 전달하기 현재 목표리스트의 mno와 일치하는 댓글만
                List<Reply> replyList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    replyService.listByMno(list.get(i).getMno()).ifPresent(origin -> {
                        replyList.addAll(origin);
                    });
                }
                model.addAttribute("replyList", replyList);
            });
        });
    }

    /**
     * 댓글수를 같이 가져가기 위한 내부 클래스
     */
    public class ObjectWithReplyNum {
        public Mbo mbo;
        public int replyNum;
    }

    // 면담 내용을 쓰기 위한 REST!!!
    @PostMapping("/note/{rno}/{step}")
    public ResponseEntity<HttpStatus> noteCreate(@PathVariable("rno") long rno, @PathVariable("step") String step,
            String note) {
        log.info("" + note);

        relationMboService.read(rno).ifPresent(origin -> {
            origin.getComments().put(step, note);
            relationMboService.modify(origin);
        });

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 면담 내용을 읽기 위한 REST!!!
    @GetMapping("/note/{rno}/{step}")
    @ResponseBody
    public ResponseEntity<String> noteRead(@PathVariable("rno") long rno, @PathVariable("step") String step) {
        log.info("read" + rno + step);

        Optional<String> object = Optional.ofNullable(relationMboService.read(rno).get().getComments().get(step));
        String note = object.get();

        return new ResponseEntity<String>(note, HttpStatus.OK);
    }

    // 평가 제출하기 위한 REST
    @PutMapping("/submit")
    @ResponseBody
    public ResponseEntity<HttpStatus> submit(@RequestBody Map<String, Object> answer, RedirectAttributes rttr) {
        log.info("" + answer);
        long tmpRno = Long.parseLong(answer.get("rno").toString());
        String tmpFinish = String.valueOf(answer.get("finish"));
        // key-value를 전달하는 경우랑 finish만 전달하는 경우를 나눠서 처리함
        if (answer.containsKey("key")) {
            // parse variable csrf 때문에 string으로 안 받아짐. 때문에 object로 받아서 변환
            String tmpKey = String.valueOf(answer.get("key"));
            Gson gson = new Gson();
            RatioValue tmpValue = gson.fromJson(answer.get("value").toString(), RatioValue.class);

            relationMboService.read(tmpRno).ifPresent(origin -> {
                origin.setFinish(tmpFinish);
                origin.getAnswers().put(tmpKey, tmpValue);
                relationMboService.modify(origin);
            });
        } else {
            relationMboService.read(tmpRno).ifPresent(origin -> {
                origin.setFinish(tmpFinish);
                relationMboService.modify(origin);
            });
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/profile")
    public void profile(String company, long tno, Model model) {

        model.addAttribute("company", company);
        model.addAttribute("tno", tno);
        // mbo turn에 따른 navbar 구분을 위해
        turnService.get(tno).ifPresent(turn -> {
            model.addAttribute("turn", turn);
        });
        companyService.readByCompanyId(company).ifPresent(origin -> {
            model.addAttribute("companyInfo", origin);
        });

    }

    @GetMapping("/modify")
    public void modify(String company, long tno, Model model) {

        model.addAttribute("company", company);
        model.addAttribute("tno", tno);
        // mbo turn에 따른 navbar 구분을 위해
        turnService.get(tno).ifPresent(turn -> {
            model.addAttribute("turn", turn);
        });
        companyService.readByCompanyId(company).ifPresent(origin -> {
            model.addAttribute("companyInfo", origin);
        });

    }

    @PostMapping("/modify")
    public String modify(Staff staff, String company, long tno, RedirectAttributes rttr, HttpServletRequest request) {
        staffService.readByEmail(staff.getEmail()).ifPresent(origin -> {
            long sno = origin.getSno();
            log.info("===>" + sno);
            staff.setSno(sno);
            staffService.modify(staff);

            HttpSession session = request.getSession();
            session.setAttribute("evaluator", staff);
        });

        rttr.addAttribute("company", company);
        rttr.addAttribute("tno", tno);
        companyService.readByCompanyId(company).ifPresent(origin -> {
            rttr.addFlashAttribute("companyInfo", origin);
        });

        return "redirect:/mbo/profile";
    }
}