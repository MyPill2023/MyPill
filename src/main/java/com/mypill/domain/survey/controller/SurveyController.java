package com.mypill.domain.survey.controller;

import com.mypill.domain.category.entity.Category;
import com.mypill.domain.category.service.CategoryService;
import com.mypill.domain.member.entity.Member;
import com.mypill.domain.member.service.MemberService;
import com.mypill.domain.nutrient.service.NutrientService;
import com.mypill.domain.nutrient.entity.Nutrient;
import com.mypill.domain.question.entity.NutrientQuestion;
import com.mypill.domain.question.entity.Question;
import com.mypill.domain.question.service.NutrientQuestionService;
import com.mypill.domain.question.service.QuestionService;
import com.mypill.domain.survey.service.SurveyService;
import com.mypill.global.rq.Rq;
import com.mypill.global.rsdata.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/survey")
@RequiredArgsConstructor
@Tag(name = "SurveyController", description = "설문조사")
public class SurveyController {
    private final CategoryService categoryService;
    private final QuestionService questionService;
    private final NutrientQuestionService nutrientQuestionService;
    private final NutrientService nutrientService;
    private final MemberService memberService;
    private final Rq rq;
    private final SurveyService surveyService;

    @PreAuthorize("hasAuthority('BUYER') or isAnonymous()")
    @GetMapping("/guide")
    @Operation(summary = "설문 가이드 페이지")
    public String guide(Model model) {
        if(rq.isLogin()){
            return "redirect:/survey/start";
        }
        return "usr/survey/guide";
    }

    @PreAuthorize("hasAuthority('BUYER') or isAnonymous()")
    @GetMapping("/start")
    @Operation(summary = "설문 시작 페이지")
    public String start(Model model) {
        if (rq.isLogin()) {
            Member member = rq.getMember();
            if (!member.getSurveyNutrients().isEmpty()){
                RsData<Member> memberRsData = memberService.surveyDelete(member);
                return rq.redirectWithMsg("/survey/start", memberRsData);
            }
        }
        List<Category> categoryItems = categoryService.findAll();
        model.addAttribute("categoryItems", categoryItems);

        return "usr/survey/start";
    }

    @PreAuthorize("hasAuthority('BUYER') or isAnonymous()")
    @GetMapping("/step")
    @Operation(summary = "설문 질문 페이지")
    public String step(Model model, @RequestParam Map<String, String> param, @RequestParam(defaultValue = "1") Long stepNo) {


        StepParam stepParam = new StepParam(param, stepNo);
        Long categoryItemId = stepParam.getCategoryItemId();

        RsData<String> startSurveyRsData = surveyService.validStartSurvey(stepParam.getCategoryItemIds());

        if (startSurveyRsData.isFail()){
            return "redirect:/survey/start";
        }

        List<Question> questions = questionService.findByCategoryId(categoryItemId);
        Optional<Category> category = categoryService.findById(categoryItemId);
        model.addAttribute("questions", questions);
        model.addAttribute("category",category);
        model.addAttribute("stepParam", stepParam);

        return "usr/survey/step";
    }

    @PreAuthorize("hasAuthority('BUYER') or isAnonymous()")
    @Transactional
    @GetMapping("/complete")
    @Operation(summary = "설문 결과 페이지")
    public String complete(Model model, @RequestParam Map<String, String> param) {
        StepParam stepParam = new StepParam(param, 1L);

        Long[] questionIds = stepParam.getQuestionIds();

        RsData<String> completeSurveyRsData = surveyService.validCompleteSurvey(questionIds);

        if (completeSurveyRsData.isFail()){
            return "redirect:/survey/step";
        }

        Set<Long> answers = new HashSet<>();
        for (Long id : questionIds) {
            List<NutrientQuestion> nutrientQuestions = nutrientQuestionService.findByQuestionId(id);

            for (NutrientQuestion nutrientQuestion : nutrientQuestions) {
                answers.add(nutrientQuestion.getNutrient().getId());
            }
        }

        List<Nutrient> nutrientAnswers = new ArrayList<>();
        for (Long id : answers) {
            Optional<Nutrient> nutrient = nutrientService.findById(id);

            nutrient.ifPresent(nutrientAnswers::add);
        }

        if (rq.isLogin()) {
            Member member = rq.getMember();
            member.getSurveyNutrients().addAll(nutrientAnswers);
        }

        model.addAttribute("nutrientAnswers",nutrientAnswers);

        return "usr/survey/complete";
    }
}

@Getter
@ToString
class StepParam {
    private final Map<String, String> param;
    private final Long stepNo;
    private final Long[] categoryItemIds;
    private final Long[] questionIds;
    private final boolean isFirst;
    private final boolean isLast;

    public StepParam (Map<String, String> param, Long stepNo) {
        this.param = param;
        this.stepNo = stepNo;
        categoryItemIds = param
                .keySet()
                .stream()
                .filter(key -> key.startsWith("category_"))
                .map(key -> Long.parseLong(key.replace("category_", "")))
                .sorted()
                .toArray(Long[]::new);

        questionIds = param
                .keySet()
                .stream()
                .filter(key -> key.startsWith("question_"))
                .map(key -> Long.parseLong(key.replace("question_", "")))
                .sorted()
                .toArray(Long[]::new);

        isFirst = stepNo == 1L;
        isLast = stepNo == categoryItemIds.length;
    }

    public Long getCategoryItemId() {
        return categoryItemIds[stepNo.intValue() - 1];
    }

    public Long getNextStepNo() {
        return stepNo + 1L;
    }

    public Long getPrevStepNo() {
        return stepNo - 1L;
    }

    public boolean isChecked(Long questionId) {
        return param.containsKey("question_" + questionId);
    }

}
