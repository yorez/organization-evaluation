package com.evaluation.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evaluation.domain.Company;
import com.evaluation.function.AboutExcel;
import com.evaluation.service.CompanyService;
import com.evaluation.service.QuestionService;
import com.evaluation.service.Relation360Service;
import com.evaluation.service.RelationMBOService;
import com.evaluation.service.TurnService;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/progress/*")
@AllArgsConstructor
@Slf4j
public class ProgressController {

    Relation360Service relation360Service;

    RelationMBOService relationMBOService;

    TurnService turnService;

    CompanyService companyService;

    QuestionService questionService;

    @GetMapping(value = { "/survey", "/mbo" })
    public void survey(long tno, Model model, HttpServletRequest request) {
        String whatYouCall = request.getServletPath();

        if (whatYouCall.equals("/progress/survey")) {
            relation360Service.progressOfSurevey(tno).ifPresent(origin -> {
                model.addAttribute("progress", origin);
                // 총 개수 구하기
                int completeCount = 0;
                int totalCount = 0;
                for (int i = 0; i < origin.size(); i++) {
                    completeCount += Integer.parseInt(origin.get(i).get(5));
                    totalCount += Integer.parseInt(origin.get(i).get(6));
                }

                model.addAttribute("completeCount", completeCount);
                model.addAttribute("totalCount", totalCount);
            });
        } else if (whatYouCall.equals("/progress/mbo")) {
            relationMBOService.progressOfSurevey(tno).ifPresent(origin -> {
                model.addAttribute("progress", origin);

                // 총 개수 구하기
                int completeCount = 0;
                int totalCount = 0;
                for (int i = 0; i < origin.size(); i++) {
                    completeCount += Integer.parseInt(origin.get(i).get(5));
                    totalCount += Integer.parseInt(origin.get(i).get(6));
                }

                model.addAttribute("completeCount", completeCount);
                model.addAttribute("totalCount", totalCount);
            });
        }

        model.addAttribute("tno", tno);
    }

    // 평가자의 피평가자 리스트 얻어오는 컨트롤러 중복 제거 위해, 합침. 다형성 이용해서 중복 더 줄이고 싶으나, 복잡성 증가해서 스탑.
    @GetMapping(value = { "/survey/evaluatedList", "/mbo/evaluatedList" })
    public void mboEvaluated(long tno, long sno, Model model, HttpServletRequest request) {
        String whatYouCall = request.getServletPath();

        if (whatYouCall.equals("/progress/survey/evaluatedList")) {
            relation360Service.findByEvaluator(sno, tno).ifPresent(origin -> {
                model.addAttribute("evaluatedList", origin);
            });
        } else if (whatYouCall.equals("/progress/mbo/evaluatedList")) {
            relationMBOService.findByEvaluator(sno, tno).ifPresent(origin -> {
                model.addAttribute("evaluatedList", origin);
            });
        }
        model.addAttribute("tno", tno);
    }

    // 평가자의 피평가자 서베이 저장 상태 바꿀 수 있도록하는 REST
    @PutMapping(value = { "/survey/evaluatedList", "/mbo/evaluatedList" })
    public ResponseEntity<HttpStatus> mboEvaluatedFinishChange(long rno, String finish, HttpServletRequest request) {
        String whatYouCall = request.getServletPath();

        if (whatYouCall.equals("/progress/survey/evaluatedList")) {
            relation360Service.read(rno).ifPresent(origin -> {
                origin.setFinish(finish);
                relation360Service.modify(origin);
            });
        } else if (whatYouCall.equals("/progress/mbo/evaluatedList")) {
            relationMBOService.read(rno).ifPresent(origin -> {
                origin.setFinish(finish);
                relationMBOService.modify(origin);
            });
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/survey")
    @ResponseBody
    public void surveyXlDownload(long tno, HttpServletResponse response) {

        turnService.get(tno).ifPresent(origin -> {
            long cno = origin.getCno();
            String company = companyService.get(cno).map(Company::getName).orElse("etc");

            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd//HHmmss");
            String format_time = format.format(System.currentTimeMillis());

            String fileName = "default";
            try {
                fileName = URLEncoder.encode(company + "_surveyProgress _" + format_time, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
            relation360Service.progressOfSurevey(tno).ifPresent(list -> {
                List<List<String>> xlList = new ArrayList<List<String>>();
                List<String> header = new ArrayList<String>();

                header.add("이름");
                header.add("이메일");
                header.add("직책");
                header.add("부문");
                header.add("부서");
                header.add("완료");
                header.add("총");
                header.add("비율");

                xlList.add(header);

                int completeCount = 0;
                int totalCount = 0;
                for (int i = 0; i < list.size(); i++) {
                    List<String> tmpList = new ArrayList<String>();
                    tmpList.add(list.get(i).get(0));
                    tmpList.add(list.get(i).get(1));
                    tmpList.add(list.get(i).get(2));
                    tmpList.add(list.get(i).get(3));
                    tmpList.add(list.get(i).get(4));
                    tmpList.add(list.get(i).get(5));
                    tmpList.add(list.get(i).get(6));
                    tmpList.add(list.get(i).get(7));
                    xlList.add(tmpList);

                    completeCount += Integer.parseInt(list.get(i).get(5));
                    totalCount += Integer.parseInt(list.get(i).get(6));
                }

                // 엑셀 바닥글 설정
                List<String> footer = new ArrayList<String>();
                String[] space = { "-", "-", "-", "-" };
                footer.addAll(Arrays.asList(space));
                footer.add("Total");
                footer.add(Integer.toString(completeCount));
                footer.add(Integer.toString(totalCount));
                footer.add(Integer.toString((completeCount / totalCount) * 100));
                xlList.add(footer);

                try {
                    XSSFWorkbook workbook = AboutExcel.writeExcel(xlList);
                    workbook.write(response.getOutputStream());
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });
    }

    @PostMapping("/survey/result")
    @ResponseBody
    public void surveyResultDownload(long tno, HttpServletResponse response) {

        turnService.get(tno).ifPresent(origin -> {
            long cno = origin.getCno();
            String company = companyService.get(cno).map(Company::getName).orElse("etc");

            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd//HHmmss");
            String format_time = format.format(System.currentTimeMillis());

            String fileName = "default";
            try {
                fileName = URLEncoder.encode(company + "_suveyResult_" + format_time, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
            relation360Service.findAllbyTno(tno).ifPresent(list -> {
                List<List<String>> xlList = new ArrayList<List<String>>();
                List<String> header = new ArrayList<String>();

                header.add("이름");
                header.add("이메일");
                header.add("직책");
                header.add("부문");
                header.add("부서");
                header.add("직군");
                header.add("계층");
                header.add("관계");
                header.add("평가자");
                header.add("임시저장");
                header.add("입력일");

                // question중 문항 idx 중복 제거 최대값 구해서 해당 값들을 열 머릿글로 구성한다. 순서대로 입력하려고 linkedHashSet사용
                Set<String> answerKeySet = new LinkedHashSet<String>();
                questionService.findAllByTno(tno).ifPresent(qlist -> {
                    for (int i = 0; i < qlist.size(); i++) {
                        answerKeySet.add('q' + Integer.toString(qlist.get(i).getIdx()));
                    }
                });
                header.addAll(answerKeySet);

                // comment는 size로 키 추정.
                Set<String> commentKeySet = new LinkedHashSet<String>();
                turnService.get(tno).ifPresent(turn -> {
                    for (int i = 0; i < turn.getComments().size(); i++) {
                        commentKeySet.add("c" + (i + 1));
                    }
                });
                header.addAll(commentKeySet);

                xlList.add(header);

                for (int i = 0; i < list.size(); i++) {
                    List<String> tmpList = new ArrayList<String>();
                    tmpList.add(list.get(i).getEvaluated().getName());
                    tmpList.add(list.get(i).getEvaluated().getEmail());
                    tmpList.add(list.get(i).getEvaluated().getLevel());
                    tmpList.add(list.get(i).getEvaluated().getDepartment1());
                    tmpList.add(list.get(i).getEvaluated().getDepartment2());
                    tmpList.add(list.get(i).getEvaluated().getDivision1());
                    tmpList.add(list.get(i).getEvaluated().getDivision2());
                    tmpList.add(list.get(i).getRelation());
                    tmpList.add(list.get(i).getEvaluator().getEmail());
                    tmpList.add(list.get(i).getFinish());
                    // 입력시간은 N가 아닌 것들만 입력 해준다!
                    if (list.get(i).getFinish().equals("N")) {
                        tmpList.add(null);
                    } else {
                        tmpList.add("" + list.get(i).getUpdateDate());
                    }
                    // answer를 위에서 만든 key로 for문 돌린다.
                    for (String key : answerKeySet) {
                        if (list.get(i).getAnswers().get(key) == null) {
                            tmpList.add(null);
                        } else {
                            tmpList.add("" + list.get(i).getAnswers().get(key));
                        }
                    }
                    // comment를 위에서 만든 key로 for문 돌린다.
                    for (String key : commentKeySet) {
                        if (list.get(i).getComments().get(key) == null) {
                            tmpList.add(null);
                        } else {
                            tmpList.add("" + list.get(i).getComments().get(key));
                        }
                    }
                    xlList.add(tmpList);
                }

                try {
                    XSSFWorkbook workbook = AboutExcel.writeExcel(xlList);
                    workbook.write(response.getOutputStream());
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });
    }

    // mbo의 평가 progress 다운받기
    @PostMapping("/mbo")
    @ResponseBody
    public void seeXlDownload(long tno, HttpServletResponse response) {

        turnService.get(tno).ifPresent(origin -> {
            long cno = origin.getCno();
            String company = companyService.get(cno).map(Company::getName).orElse("etc");

            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd//HHmmss");
            String format_time = format.format(System.currentTimeMillis());

            String fileName = "default";
            try {
                fileName = URLEncoder.encode(company + "_mboSeeProgress_" + format_time, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
            relationMBOService.progressOfSurevey(tno).ifPresent(list -> {
                List<List<String>> xlList = new ArrayList<List<String>>();
                List<String> header = new ArrayList<String>();

                header.add("이름");
                header.add("이메일");
                header.add("직책");
                header.add("부문");
                header.add("부서");
                header.add("완료");
                header.add("총");
                header.add("비율");

                xlList.add(header);

                int completeCount = 0;
                int totalCount = 0;
                for (int i = 0; i < list.size(); i++) {
                    List<String> tmpList = new ArrayList<String>();
                    tmpList.add(list.get(i).get(0));
                    tmpList.add(list.get(i).get(1));
                    tmpList.add(list.get(i).get(2));
                    tmpList.add(list.get(i).get(3));
                    tmpList.add(list.get(i).get(4));
                    tmpList.add(list.get(i).get(5));
                    tmpList.add(list.get(i).get(6));
                    tmpList.add(list.get(i).get(7));
                    xlList.add(tmpList);

                    completeCount += Integer.parseInt(list.get(i).get(5));
                    totalCount += Integer.parseInt(list.get(i).get(6));
                }

                // 엑셀 바닥글 설정
                List<String> footer = new ArrayList<String>();
                String[] space = { "-", "-", "-", "-" };
                footer.addAll(Arrays.asList(space));
                footer.add("Total");
                footer.add(Integer.toString(completeCount));
                footer.add(Integer.toString(totalCount));
                footer.add(Integer.toString((completeCount / totalCount) * 100));
                xlList.add(footer);

                try {
                    XSSFWorkbook workbook = AboutExcel.writeExcel(xlList);
                    workbook.write(response.getOutputStream());
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });
    }

}