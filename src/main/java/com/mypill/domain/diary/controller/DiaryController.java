package com.mypill.domain.diary.controller;

import com.mypill.domain.category.service.CategoryService;
import com.mypill.domain.diary.dto.DiaryRequest;
import com.mypill.domain.diary.entity.Diary;
import com.mypill.domain.diary.service.DiaryService;
import com.mypill.domain.nutrient.Service.NutrientService;
import com.mypill.global.rq.Rq;
import com.mypill.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/usr/diary")
public class DiaryController {

    private final DiaryService diaryService;
    private final Rq rq;


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    @Operation(summary = "영양제 등록 폼")
    public String create() {

        return "usr/diary/create";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    @Operation(summary = "영양제 등록")
    public String create(@Valid DiaryRequest diaryRequest) {

        RsData<Diary> createRsData = diaryService.create(diaryRequest, rq.getMember());
        if (createRsData.isFail()) {
            return rq.historyBack(createRsData.getMsg());
        }
        return rq.redirectWithMsg("/usr/diary/detail/%s".formatted(createRsData.getData().getId()), createRsData);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    @Operation(summary = "영양제 목록")
    public String showList(Model model) {
        List<Diary> diaries = diaryService.getList();
        model.addAttribute("diaries", diaries);
        return "usr/diary/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/detail/{diaryId}")
    @Operation(summary = "영양제 정보 상세")
    public String showDetail(@PathVariable Long diaryId, Model model) {
        Diary diary = diaryService.findById(diaryId).orElse(null);
        if(diary == null) {
            return rq.historyBack("존재하지 않는 영양제 정보입니다.");
        }
        if (diary.getDeleteDate() != null){
            return rq.historyBack("삭제된 영양제 정보 입니다.");
        }
        model.addAttribute("diary",diary);
        return "usr/diary/detail";
    }





}